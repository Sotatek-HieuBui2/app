# SmartCleaner - Technical Spec (Part 2)

## 📱 CHỨC NĂNG 7: Dọn Rác Messaging Apps

### Mô Tả
Dọn dẹp các file media được cache bởi WhatsApp, Messenger, Zalo.

### Messaging App Patterns

#### WhatsApp
```
/WhatsApp/
├── Media/
│   ├── WhatsApp Images/       # Received images
│   ├── WhatsApp Video/        # Received videos
│   ├── WhatsApp Voice Notes/  # Voice messages
│   ├── WhatsApp Audio/        # Audio files
│   ├── WhatsApp Documents/    # PDFs, etc.
│   └── .Statuses/            # Stories (auto-delete after 24h)
├── Backups/                   # Chat backups
└── Databases/                 # Message DB (DO NOT DELETE)
```

#### Messenger
```
/Messenger/
├── Cache/
│   ├── Images/
│   ├── Videos/
│   └── Audio/
```

#### Zalo
```
/Zalo/
├── ZaloData/
│   ├── Images/
│   ├── Videos/
│   └── Files/
└── Cache/
```

### Implementation

```kotlin
enum class MessagingApp(
    val packageName: String,
    val displayName: String,
    val mediaFolders: List<String>
) {
    WHATSAPP(
        packageName = "com.whatsapp",
        displayName = "WhatsApp",
        mediaFolders = listOf(
            "WhatsApp/Media/WhatsApp Images",
            "WhatsApp/Media/WhatsApp Video",
            "WhatsApp/Media/WhatsApp Voice Notes",
            "WhatsApp/Media/.Statuses"
        )
    ),
    MESSENGER(
        packageName = "com.facebook.orca",
        displayName = "Messenger",
        mediaFolders = listOf(
            "Messenger/Cache"
        )
    ),
    ZALO(
        packageName = "com.zing.zalo",
        displayName = "Zalo",
        mediaFolders = listOf(
            "Zalo/ZaloData/Images",
            "Zalo/ZaloData/Videos",
            "Zalo/Cache"
        )
    )
}

data class MessagingAppJunk(
    val app: MessagingApp,
    val isInstalled: Boolean,
    val categories: List<MediaCategory>
)

data class MediaCategory(
    val name: String,
    val path: String,
    val files: List<File>,
    val totalSize: Long,
    val oldestDate: Long,
    val newestDate: Long
)

class MessagingAppCleanerRepository(
    context: Context
) {
    suspend fun scanMessagingApps(): List<MessagingAppJunk> {
        return withContext(Dispatchers.IO) {
            MessagingApp.values().map { app ->
                val isInstalled = isAppInstalled(app.packageName)
                val categories = scanMediaCategories(app)
                
                MessagingAppJunk(
                    app = app,
                    isInstalled = isInstalled,
                    categories = categories
                )
            }.filter { it.categories.isNotEmpty() }
        }
    }
    
    private fun scanMediaCategories(app: MessagingApp): List<MediaCategory> {
        val root = Environment.getExternalStorageDirectory()
        
        return app.mediaFolders.mapNotNull { folderPath ->
            val dir = File(root, folderPath)
            if (!dir.exists()) return@mapNotNull null
            
            val files = dir.walkTopDown()
                .filter { it.isFile }
                .toList()
            
            if (files.isEmpty()) return@mapNotNull null
            
            MediaCategory(
                name = dir.name,
                path = dir.absolutePath,
                files = files,
                totalSize = files.sumOf { it.length() },
                oldestDate = files.minOfOrNull { it.lastModified() } ?: 0L,
                newestDate = files.maxOfOrNull { it.lastModified() } ?: 0L
            )
        }
    }
    
    suspend fun cleanMediaCategory(
        category: MediaCategory,
        olderThanDays: Int? = null
    ): Result<Pair<Int, Long>> {
        return withContext(Dispatchers.IO) {
            try {
                val cutoffTime = if (olderThanDays != null) {
                    System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
                } else {
                    0L
                }
                
                var deletedCount = 0
                var freedSpace = 0L
                
                category.files
                    .filter { it.lastModified() < cutoffTime }
                    .forEach { file ->
                        if (file.delete()) {
                            deletedCount++
                            freedSpace += file.length()
                        }
                    }
                
                Result.success(Pair(deletedCount, freedSpace))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

---

## 📊 CHỨC NĂNG 8: Storage Analyzer với Sunburst Chart

[Xem file TECHNICAL_SPEC.md]

---

## 🔐 CHỨC NĂNG 9: Root Mode

[Xem file TECHNICAL_SPEC.md]

---

## 🎨 UX: Dashboard & One Tap Clean

[Xem file TECHNICAL_SPEC.md]

---

## ⏰ UX: Auto Clean Scheduler với WorkManager

[Xem file TECHNICAL_SPEC.md]

---

## 🔔 UX: Realtime Notification khi Gỡ App

### BroadcastReceiver Implementation

```kotlin
package com.smartcleaner.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageRemovedReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var leftoverRepository: LeftoverRepository
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_PACKAGE_REMOVED) return
        
        val packageName = intent.data?.schemeSpecificPart ?: return
        val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
        
        // Don't scan if app is just being updated
        if (replacing) return
        
        Log.d("PackageRemoved", "App uninstalled: $packageName")
        
        // Scan for leftover files in background
        CoroutineScope(Dispatchers.IO).launch {
            scanAndNotify(context, packageName)
        }
    }
    
    private suspend fun scanAndNotify(context: Context, packageName: String) {
        // Quick scan for this specific package
        val leftovers = leftoverRepository.scanSpecificPackage(packageName)
        
        if (leftovers != null && leftovers.totalSize > 0) {
            notificationHelper.showLeftoverDetectedNotification(
                appName = leftovers.appName,
                size = leftovers.totalSize,
                fileCount = leftovers.fileCount
            )
        }
    }
}

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun showLeftoverDetectedNotification(
        appName: String,
        size: Long,
        fileCount: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "leftover_screen")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_LEFTOVER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Leftover Files Detected")
            .setContentText("$appName left $fileCount files (${formatSize(size)})")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$appName was uninstalled but left behind $fileCount files (${formatSize(size)}). Tap to clean up."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_delete,
                "Clean Now",
                createCleanActionIntent(appName)
            )
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(NOTIFICATION_ID_LEFTOVER, notification)
        }
    }
}
```

---

## ☁️ UX: Cloud Backup

### Google Drive Integration

```kotlin
class GoogleDriveBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var googleSignInAccount: GoogleSignInAccount? = null
    private var driveService: Drive? = null
    
    suspend fun signIn(activity: Activity): Result<Boolean> {
        return try {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
            
            val client = GoogleSignIn.getClient(activity, signInOptions)
            val signInIntent = client.signInIntent
            
            // Launch sign-in activity (result handled in onActivityResult)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun backupFiles(
        files: List<File>,
        folderName: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val drive = driveService ?: throw Exception("Not signed in")
                
                // Create backup folder
                val folderMetadata = com.google.api.services.drive.model.File().apply {
                    name = folderName
                    mimeType = "application/vnd.google-apps.folder"
                }
                
                val folder = drive.files().create(folderMetadata)
                    .setFields("id")
                    .execute()
                
                val folderId = folder.id
                
                // Upload files
                files.forEach { file ->
                    val fileMetadata = com.google.api.services.drive.model.File().apply {
                        name = file.name
                        parents = listOf(folderId)
                    }
                    
                    val mediaContent = FileContent(null, file)
                    
                    drive.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute()
                }
                
                Result.success(folderId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun restoreFromBackup(backupId: String, destinationDir: File): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val drive = driveService ?: throw Exception("Not signed in")
                
                // List files in backup folder
                val query = "'$backupId' in parents"
                val result = drive.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .execute()
                
                var restoredCount = 0
                
                result.files.forEach { file ->
                    val outputFile = File(destinationDir, file.name)
                    
                    drive.files().get(file.id)
                        .executeMediaAndDownloadTo(FileOutputStream(outputFile))
                    
                    restoredCount++
                }
                
                Result.success(restoredCount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

### Firebase Storage Alternative

```kotlin
class FirebaseBackupManager @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    
    suspend fun backupFiles(
        files: List<File>,
        folderName: String
    ): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid 
                    ?: throw Exception("User not signed in")
                
                val uploadedUrls = mutableListOf<String>()
                
                files.forEach { file ->
                    val storageRef = storage.reference
                        .child("backups/$userId/$folderName/${file.name}")
                    
                    val uploadTask = storageRef.putFile(Uri.fromFile(file)).await()
                    val downloadUrl = storageRef.downloadUrl.await()
                    
                    uploadedUrls.add(downloadUrl.toString())
                }
                
                Result.success(uploadedUrls)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

---

## 🎨 Theme & Material You

```kotlin
package com.smartcleaner.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SmartCleanerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80
        )
        else -> lightColorScheme(
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40
        )
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 📚 Tổng Kết Implementation

### Các File Đã Tạo

#### Build Configuration
- ✅ `build.gradle.kts` (root)
- ✅ `settings.gradle.kts`
- ✅ `app/build.gradle.kts` (với tất cả dependencies)

#### Manifest & Application
- ✅ `AndroidManifest.xml` (đầy đủ permissions)
- ✅ `SmartCleanerApplication.kt`

#### Domain Layer
- ✅ `domain/model/LeftoverFile.kt`
- ✅ `domain/model/JunkFile.kt`
- ✅ `domain/repository/LeftoverRepository.kt`
- ✅ `domain/repository/JunkRepository.kt`
- ✅ `domain/usecase/leftover/*`
- ✅ `domain/usecase/junk/*`

#### Data Layer
- ✅ `data/repository/LeftoverRepositoryImpl.kt`
- ✅ `data/repository/JunkRepositoryImpl.kt`

#### Presentation Layer
- ✅ `presentation/MainActivity.kt`
- ✅ `presentation/leftover/LeftoverViewModel.kt`
- ✅ `presentation/leftover/LeftoverScreen.kt`
- ✅ `presentation/theme/*`

#### Dependency Injection
- ✅ `di/RepositoryModule.kt`

#### Documentation
- ✅ `README.md`
- ✅ `docs/TECHNICAL_SPEC.md`
- ✅ `docs/TECHNICAL_SPEC_PART2.md`

### Chức Năng Đã Implement Đầy Đủ

1. ✅ **Leftover Files Scanner** - 100% complete
2. ✅ **System Junk Cleaner** - 100% complete
3. ⚠️ **Empty Folders** - Spec only
4. ⚠️ **Unused Apps Analyzer** - Spec only  
5. ⚠️ **ML Classifier** - Spec only
6. ⚠️ **Duplicate Finder** - Spec only
7. ⚠️ **Messaging Apps Cleaner** - Spec only
8. ⚠️ **Storage Analyzer** - Spec only
9. ⚠️ **Root Mode** - Spec only
10. ⚠️ **Dashboard** - Spec only
11. ⚠️ **Auto Scheduler** - Spec only
12. ⚠️ **Realtime Notification** - Spec only
13. ⚠️ **Cloud Backup** - Spec only

### Cách Sử Dụng

1. Import project vào Android Studio
2. Sync Gradle
3. Build & Run
4. Grant permissions khi yêu cầu
5. Bắt đầu scan leftover files hoặc junk files

Project đã sẵn sàng để develop tiếp các chức năng còn lại!
