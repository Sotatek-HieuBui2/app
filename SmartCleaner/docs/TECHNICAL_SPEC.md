# SmartCleaner - Đặc Tả Kỹ Thuật Chi Tiết

## 📐 Kiến Trúc Tổng Quan

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│      Presentation Layer (UI)            │
│  ┌──────────────────────────────────┐   │
│  │  Jetpack Compose Screens         │   │
│  │  - LeftoverScreen                │   │
│  │  - JunkCleanerScreen             │   │
│  │  - DashboardScreen               │   │
│  │  - AnalyzerScreen                │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  ViewModels (MVVM)               │   │
│  │  - State management              │   │
│  │  - Business logic delegation     │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
           ↓ calls use cases ↓
┌─────────────────────────────────────────┐
│         Domain Layer (Business)          │
│  ┌──────────────────────────────────┐   │
│  │  Use Cases                       │   │
│  │  - Single responsibility         │   │
│  │  - Orchestrate business logic    │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Entities & Models               │   │
│  │  - Domain objects                │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Repository Interfaces           │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
           ↓ implemented by ↓
┌─────────────────────────────────────────┐
│          Data Layer                      │
│  ┌──────────────────────────────────┐   │
│  │  Repository Implementations      │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Data Sources                    │   │
│  │  - Local (Room, DataStore)       │   │
│  │  - Remote (Firebase, Drive)      │   │
│  │  - File System (SAF)             │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
           ↓ uses utilities ↓
┌─────────────────────────────────────────┐
│          Core Layer                      │
│  - File Scanner                          │
│  - ML Classifier                         │
│  - Permission Handler                    │
│  - Root Tools                            │
│  - Utilities                             │
└─────────────────────────────────────────┘
```

---

## 🔧 CHỨC NĂNG 1: Quét & Dọn Tệp Còn Sót Lại

### Mô Tả
Phát hiện và xóa các tệp/thư mục của ứng dụng đã gỡ cài đặt nhưng vẫn còn sót lại trên thiết bị.

### Input
- Không có input trực tiếp từ user
- Tự động quét các vị trí: `/Android/data/`, `/Android/obb/`, `/Android/media/`, `/Download/`, `/Pictures/`, `/DCIM/`

### Output
```kotlin
data class LeftoverScanResult(
    val groups: List<LeftoverGroup>,
    val totalSize: Long,
    val totalFiles: Int,
    val scanDurationMs: Long
)
```

### Process Flow
1. **Lấy danh sách ứng dụng đã cài đặt** (via `PackageManager`)
2. **Quét từng thư mục:**
   - Android/data → kiểm tra folder name có match với installed packages không
   - Android/obb → tương tự
   - Download → tìm APK files và extract package name
   - Pictures/DCIM → tìm folder có tên giống app
3. **Thu thập metadata:**
   - Tính size (recursive cho folder)
   - Last modified date
   - Preview image (nếu có)
4. **Nhóm theo package name**
5. **Sắp xếp theo size giảm dần**

### Technical Implementation

#### Domain Layer
```kotlin
// Entities
data class LeftoverFile(
    val path: String,
    val packageName: String,
    val appName: String,
    val size: Long,
    val type: LeftoverType,
    val previewPath: String? = null
)

// Use Cases
class ScanLeftoverFilesUseCase(repository: LeftoverRepository) {
    suspend operator fun invoke(): Flow<ScanProgress>
}

class DeleteLeftoverFilesUseCase(repository: LeftoverRepository) {
    suspend operator fun invoke(group: LeftoverGroup, backup: Boolean): Result<DeleteResult>
}
```

#### Data Layer
```kotlin
class LeftoverRepositoryImpl(context: Context) : LeftoverRepository {
    private fun scanDirectory(dir: File, type: LeftoverType): List<LeftoverFile> {
        // 1. List all folders
        // 2. Check if folder name NOT in installedPackages
        // 3. Calculate size recursively
        // 4. Find preview file (first image)
    }
    
    private fun calculateSize(file: File): Long {
        return file.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }
}
```

#### Presentation Layer
```kotlin
@HiltViewModel
class LeftoverViewModel(
    private val scanUseCase: ScanLeftoverFilesUseCase,
    private val deleteUseCase: DeleteLeftoverFilesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<LeftoverUiState>(Idle)
    val uiState: StateFlow<LeftoverUiState> = _uiState.asStateFlow()
    
    fun startScan() { /* collect flow from use case */ }
    fun deleteGroup(group: LeftoverGroup, backup: Boolean) { /* call delete use case */ }
}
```

### UX Flow
```
┌──────────────┐
│ Idle Screen  │
│  [Scan Now]  │
└──────┬───────┘
       │ tap
       ↓
┌──────────────┐
│  Scanning    │
│  Progress: X%│
└──────┬───────┘
       │ complete
       ↓
┌──────────────────────────┐
│ Results                  │
│ ┌──────────────────────┐ │
│ │ App A: 500MB         │ │
│ │ [Delete]             │ │
│ └──────────────────────┘ │
│ ┌──────────────────────┐ │
│ │ App B: 300MB         │ │
│ │ [Delete]             │ │
│ └──────────────────────┘ │
└──────────────────────────┘
```

### Permissions Required
- `READ_EXTERNAL_STORAGE` (API < 33)
- `MANAGE_EXTERNAL_STORAGE` (API 30+, optional cho full access)

---

## 🗑️ CHỨC NĂNG 2: Dọn Rác Hệ Thống

### Mô Tả
Tìm và xóa các loại rác hệ thống: cache, temp files, logs, backup files, APKs, large files, thumbnails.

### Input
- `largeSizeThresholdMB`: Int = 100 (configurable)
- Selected junk types to clean

### Output
```kotlin
data class JunkScanResult(
    val groups: List<JunkGroup>,
    val totalSize: Long,
    val totalFiles: Int,
    val scanDurationMs: Long
)
```

### Junk Types

#### 1. APP_CACHE
**Source:** `StorageStatsManager.queryStatsForPackage()` (API 26+)
**Process:**
```kotlin
val storageStatsManager = context.getSystemService(StorageStatsManager::class.java)
val stats = storageStatsManager.queryStatsForPackage(uuid, packageName, userHandle)
val cacheSize = stats.cacheBytes
```
**Note:** Clearing cache requires `CLEAR_APP_CACHE` permission (system apps only) or root.

#### 2. TEMP_FILES
**Patterns:** `*.tmp`, `*.temp`
**Search locations:**
- External storage root
- App-specific cache dirs
**Max depth:** 5 levels (performance)

#### 3. LOG_FILES
**Patterns:** `*.log`
**Common locations:**
- `/Download/`
- App folders

#### 4. BACKUP_FILES
**Patterns:** `*.bak`, `*.backup`, `*.old`

#### 5. APK_FILES
**Location:** `/Download/`
**Filter:** Only APKs of apps NOT currently installed

#### 6. LARGE_FILES
**Threshold:** > 100MB (default)
**Process:**
```kotlin
file.walkTopDown()
    .maxDepth(4)
    .filter { it.isFile && it.length() >= thresholdBytes }
```

#### 7. THUMBNAIL_CACHE
**Location:** `/.thumbnails/`
**Safe to delete:** Yes (will regenerate)

### Technical Implementation

#### Repository
```kotlin
class JunkRepositoryImpl(context: Context) : JunkRepository {
    override suspend fun scanJunkFiles(largeSizeThresholdMB: Int): Flow<Int> = flow {
        emit(5)  // Start
        val cacheFiles = scanAppCache()
        emit(15)
        val tempFiles = scanFilesByExtensions(listOf(".tmp", ".temp"), TEMP_FILES)
        emit(30)
        // ... continue for all types
        emit(100)
    }
    
    override suspend fun clearAllAppCache(): Result<Long> {
        // Sum all cache sizes
        // Note: Cannot actually clear without system permission
        return Result.success(totalCacheSize)
    }
}
```

### UX Flow
```
┌────────────────┐
│ Junk Cleaner   │
│ [Start Scan]   │
└────────┬───────┘
         ↓
┌────────────────────────┐
│ Scanning: 45%          │
│ [Progress Bar]         │
└────────┬───────────────┘
         ↓
┌─────────────────────────────────┐
│ Results: 2.5GB found            │
│ ☑ App Cache: 1.2GB              │
│ ☑ Temp Files: 500MB             │
│ ☐ Large Files: 800MB (uncheck)  │
│ [Clean Selected]                │
└─────────────────────────────────┘
         ↓
┌────────────────────────┐
│ Cleaning...            │
│ Deleted: 1.7GB         │
└────────────────────────┘
```

---

## 📂 CHỨC NĂNG 3: Dọn Thư Mục Trống

### Mô Tả
Tìm và xóa tất cả thư mục trống trên external storage.

### Input
- None (auto scan)

### Output
```kotlin
data class EmptyFolderResult(
    val folders: List<EmptyFolder>,
    val totalCount: Int
)

data class EmptyFolder(
    val path: String,
    val parentPath: String,
    val lastModified: Long
)
```

### Process
```kotlin
fun scanEmptyFolders(rootDir: File): List<File> {
    return rootDir.walkTopDown()
        .filter { it.isDirectory }
        .filter { dir ->
            val contents = dir.listFiles()
            contents == null || contents.isEmpty()
        }
        .toList()
}
```

### Implementation
```kotlin
// Use Case
class ScanEmptyFoldersUseCase @Inject constructor(
    private val repository: EmptyFolderRepository
) {
    suspend operator fun invoke(): Result<EmptyFolderResult> {
        return repository.scanEmptyFolders()
    }
}

// Repository
class EmptyFolderRepositoryImpl(context: Context) {
    override suspend fun scanEmptyFolders(): Result<EmptyFolderResult> {
        return withContext(Dispatchers.IO) {
            val root = Environment.getExternalStorageDirectory()
            val emptyFolders = root.walkTopDown()
                .filter { it.isDirectory && it.listFiles()?.isEmpty() == true }
                .map { EmptyFolder(it.absolutePath, it.parent ?: "", it.lastModified()) }
                .toList()
            
            Result.success(EmptyFolderResult(emptyFolders, emptyFolders.size))
        }
    }
}
```

---

## 📊 CHỨC NĂNG 4: Phân Tích Ứng Dụng Không Dùng

### Mô Tả
Sử dụng `UsageStatsManager` để phát hiện ứng dụng không sử dụng lâu.

### Input
- Time range: 30 days, 90 days, never used

### Output
```kotlin
data class UnusedApp(
    val packageName: String,
    val appName: String,
    val lastUsedTime: Long,  // timestamp
    val installedTime: Long,
    val totalSize: Long,
    val category: UnusedCategory
)

enum class UnusedCategory {
    NOT_USED_30_DAYS,
    NOT_USED_90_DAYS,
    NEVER_USED
}
```

### Technical Implementation

#### Permission
Requires `PACKAGE_USAGE_STATS` - user must grant via Settings.

```kotlin
fun requestUsageStatsPermission(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
```

#### Usage Query
```kotlin
val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

val endTime = System.currentTimeMillis()
val startTime = endTime - (90 * 24 * 60 * 60 * 1000L) // 90 days ago

val usageStats = usageStatsManager.queryUsageStats(
    UsageStatsManager.INTERVAL_DAILY,
    startTime,
    endTime
)

usageStats.forEach { stats ->
    val lastUsed = stats.lastTimeUsed
    val packageName = stats.packageName
    
    val daysSinceUse = (currentTime - lastUsed) / (24 * 60 * 60 * 1000)
    
    when {
        daysSinceUse > 90 -> UnusedCategory.NOT_USED_90_DAYS
        daysSinceUse > 30 -> UnusedCategory.NOT_USED_30_DAYS
        lastUsed == 0L -> UnusedCategory.NEVER_USED
    }
}
```

#### Repository Implementation
```kotlin
class UnusedAppRepositoryImpl(context: Context) : UnusedAppRepository {
    override suspend fun analyzeUnusedApps(): Result<List<UnusedApp>> {
        return withContext(Dispatchers.IO) {
            if (!hasUsageStatsPermission(context)) {
                return@withContext Result.failure(Exception("Permission required"))
            }
            
            val pm = context.packageManager
            val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
            
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (90 * 24 * 60 * 60 * 1000L)
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            
            val usageMap = usageStats.associateBy { it.packageName }
            
            val packages = pm.getInstalledPackages(0)
            val unusedApps = packages.mapNotNull { packageInfo ->
                val usage = usageMap[packageInfo.packageName]
                val lastUsed = usage?.lastTimeUsed ?: 0L
                val daysSinceUse = (endTime - lastUsed) / (24 * 60 * 60 * 1000)
                
                if (daysSinceUse >= 30 || lastUsed == 0L) {
                    val appInfo = packageInfo.applicationInfo
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    
                    // Get app size
                    val storageStatsManager = context.getSystemService(StorageStatsManager::class.java)
                    val uuid = context.packageManager.getApplicationInfo(packageInfo.packageName, 0).storageUuid
                    val stats = storageStatsManager.queryStatsForPackage(uuid, packageInfo.packageName, Process.myUserHandle())
                    val totalSize = stats.appBytes + stats.dataBytes + stats.cacheBytes
                    
                    UnusedApp(
                        packageName = packageInfo.packageName,
                        appName = appName,
                        lastUsedTime = lastUsed,
                        installedTime = packageInfo.firstInstallTime,
                        totalSize = totalSize,
                        category = when {
                            daysSinceUse >= 90 -> UnusedCategory.NOT_USED_90_DAYS
                            daysSinceUse >= 30 -> UnusedCategory.NOT_USED_30_DAYS
                            else -> UnusedCategory.NEVER_USED
                        }
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.totalSize }
            
            Result.success(unusedApps)
        }
    }
}
```

### UX Flow
```
┌─────────────────────────┐
│ Unused Apps             │
│ Grant Permission [→]    │
└─────────┬───────────────┘
          ↓ (after permission)
┌──────────────────────────────┐
│ Analyzing...                 │
└──────────┬───────────────────┘
           ↓
┌─────────────────────────────────┐
│ Found 15 unused apps (3.2GB)    │
│                                 │
│ ┌──90+ days (8 apps)──────────┐│
│ │ Facebook: 500MB [Uninstall] ││
│ │ Game X: 1.2GB [Uninstall]   ││
│ └─────────────────────────────┘│
│                                 │
│ ┌──30-90 days (5 apps)────────┐│
│ │ App Y: 200MB [Uninstall]    ││
│ └─────────────────────────────┘│
│                                 │
│ ┌──Never Used (2 apps)────────┐│
│ │ Bloatware: 100MB            ││
│ └─────────────────────────────┘│
└─────────────────────────────────┘
```

---

## 🤖 CHỨC NĂNG 5: ML Phân Loại Rác Thông Minh

### Mô Tả
Sử dụng TensorFlow Lite để phân loại file có nên xóa hay không dựa trên features.

### Model Architecture

#### Input Features (Vector)
```kotlin
data class FileFeatures(
    val extension: String,        // one-hot encoded
    val sizeCategory: Int,        // 0-5 (KB, MB, GB ranges)
    val ageInDays: Int,          // days since last modified
    val pathDepth: Int,          // depth in folder structure
    val isHidden: Boolean,       // starts with .
    val isInDownload: Boolean,
    val isInTemp: Boolean,
    val hasPreviewThumbnail: Boolean
)
```

#### Output
```kotlin
data class JunkPrediction(
    val confidence: Float,  // 0.0 - 1.0
    val category: JunkCategory
)

enum class JunkCategory {
    SAFE_TO_DELETE,    // confidence > 0.9
    MAYBE_JUNK,        // confidence 0.5 - 0.9
    KEEP               // confidence < 0.5
}
```

### Implementation

#### 1. Prepare TFLite Model
```python
# Training script (Python)
import tensorflow as tf

# Sample model
model = tf.keras.Sequential([
    tf.keras.layers.Input(shape=(10,)),  # 10 features
    tf.keras.layers.Dense(64, activation='relu'),
    tf.keras.layers.Dropout(0.2),
    tf.keras.layers.Dense(32, activation='relu'),
    tf.keras.layers.Dense(1, activation='sigmoid')  # Binary: junk or not
])

model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

# Train with labeled dataset
# ... training code ...

# Convert to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

with open('junk_classifier.tflite', 'wb') as f:
    f.write(tflite_model)
```

#### 2. Android Integration
```kotlin
class JunkClassifier(context: Context) {
    private val interpreter: Interpreter
    
    init {
        val model = loadModelFile(context, "junk_classifier.tflite")
        interpreter = Interpreter(model)
    }
    
    fun classify(file: File): JunkPrediction {
        val features = extractFeatures(file)
        val inputArray = featuresToArray(features)
        val outputArray = Array(1) { FloatArray(1) }
        
        interpreter.run(inputArray, outputArray)
        
        val confidence = outputArray[0][0]
        val category = when {
            confidence > 0.9f -> JunkCategory.SAFE_TO_DELETE
            confidence > 0.5f -> JunkCategory.MAYBE_JUNK
            else -> JunkCategory.KEEP
        }
        
        return JunkPrediction(confidence, category)
    }
    
    private fun extractFeatures(file: File): FileFeatures {
        return FileFeatures(
            extension = file.extension,
            sizeCategory = getSizeCategory(file.length()),
            ageInDays = ((System.currentTimeMillis() - file.lastModified()) / (24 * 60 * 60 * 1000)).toInt(),
            pathDepth = file.absolutePath.count { it == '/' },
            isHidden = file.name.startsWith("."),
            isInDownload = file.absolutePath.contains("/Download/"),
            isInTemp = file.absolutePath.contains("temp") || file.absolutePath.contains("cache"),
            hasPreviewThumbnail = false
        )
    }
    
    private fun featuresToArray(features: FileFeatures): Array<FloatArray> {
        // Convert features to normalized float array
        val array = FloatArray(10)
        // ... normalization logic ...
        return arrayOf(array)
    }
    
    private fun loadModelFile(context: Context, filename: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
```

#### 3. Integration with Junk Scanner
```kotlin
class JunkRepositoryImpl(
    context: Context,
    private val mlClassifier: JunkClassifier
) : JunkRepository {
    
    private suspend fun scanWithML(files: List<File>): List<JunkFile> {
        return files.map { file ->
            val prediction = mlClassifier.classify(file)
            
            JunkFile(
                path = file.absolutePath,
                name = file.name,
                size = file.length(),
                lastModified = file.lastModified(),
                type = determineJunkType(file),
                isSafe = prediction.category == JunkCategory.SAFE_TO_DELETE,
                mlConfidence = prediction.confidence
            )
        }.filter { it.mlConfidence > 0.5f } // Only suggest files with >50% confidence
    }
}
```

### Training Data Collection
```kotlin
// Collect user feedback to improve model
data class UserFeedback(
    val filePath: String,
    val features: FileFeatures,
    val userDeleted: Boolean,  // Ground truth
    val timestamp: Long
)

class FeedbackCollector(context: Context) {
    fun recordFeedback(file: File, deleted: Boolean) {
        // Store in Room database
        // Periodically upload to server for retraining
    }
}
```

---

## 🔍 CHỨC NĂNG 6: Tìm File Trùng Lặp

### Mô Tả
Tìm các file giống nhau dựa trên:
1. **Hash-based**: MD5/SHA-256 cho tất cả file
2. **Image similarity**: Perceptual hash cho ảnh

### Implementation

#### 1. Hash-based Duplicate Finder
```kotlin
data class DuplicateGroup(
    val hash: String,
    val files: List<DuplicateFile>,
    val totalSize: Long,
    val suggestedKeep: String // Path to keep (newest)
)

data class DuplicateFile(
    val path: String,
    val size: Long,
    val lastModified: Long
)

class HashDuplicateFinder {
    suspend fun findDuplicates(rootDir: File): List<DuplicateGroup> {
        return withContext(Dispatchers.IO) {
            val hashMap = mutableMapOf<String, MutableList<File>>()
            
            // Calculate hash for all files
            rootDir.walkTopDown()
                .filter { it.isFile }
                .forEach { file ->
                    val hash = calculateHash(file)
                    hashMap.getOrPut(hash) { mutableListOf() }.add(file)
                }
            
            // Filter only duplicates (hash appears more than once)
            hashMap.filter { it.value.size > 1 }
                .map { (hash, files) ->
                    val sortedFiles = files.sortedByDescending { it.lastModified() }
                    DuplicateGroup(
                        hash = hash,
                        files = sortedFiles.map { DuplicateFile(it.absolutePath, it.length(), it.lastModified()) },
                        totalSize = files.sumOf { it.length() },
                        suggestedKeep = sortedFiles.first().absolutePath // Keep newest
                    )
                }
        }
    }
    
    private fun calculateHash(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192)
        
        file.inputStream().use { input ->
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
```

#### 2. Image Similarity (Perceptual Hash)
```kotlin
class ImageDuplicateFinder(context: Context) {
    
    suspend fun findSimilarImages(imageFiles: List<File>): List<DuplicateGroup> {
        return withContext(Dispatchers.IO) {
            val phashMap = mutableMapOf<String, MutableList<File>>()
            
            imageFiles.forEach { file ->
                val phash = calculatePerceptualHash(file)
                phashMap.getOrPut(phash) { mutableListOf() }.add(file)
            }
            
            // Also find similar (not exact) images by comparing pHash distance
            val similarGroups = findSimilarByDistance(phashMap)
            
            similarGroups
        }
    }
    
    private fun calculatePerceptualHash(imageFile: File): String {
        // Load image
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath) ?: return ""
        
        // Resize to 8x8
        val resized = Bitmap.createScaledBitmap(bitmap, 8, 8, false)
        
        // Convert to grayscale and calculate average
        val pixels = IntArray(64)
        resized.getPixels(pixels, 0, 8, 0, 0, 8, 8)
        
        val grayPixels = pixels.map { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            (r + g + b) / 3
        }
        
        val average = grayPixels.average()
        
        // Generate hash: 1 if pixel > average, else 0
        val hash = grayPixels.joinToString("") { if (it > average) "1" else "0" }
        
        bitmap.recycle()
        resized.recycle()
        
        return hash
    }
    
    private fun hammingDistance(hash1: String, hash2: String): Int {
        return hash1.zip(hash2).count { (a, b) -> a != b }
    }
    
    private fun findSimilarByDistance(phashMap: Map<String, List<File>>): List<DuplicateGroup> {
        val similarGroups = mutableListOf<DuplicateGroup>()
        val hashes = phashMap.keys.toList()
        
        for (i in hashes.indices) {
            for (j in i + 1 until hashes.size) {
                val distance = hammingDistance(hashes[i], hashes[j])
                
                // If distance <= 5, consider similar
                if (distance <= 5) {
                    val files = phashMap[hashes[i]]!! + phashMap[hashes[j]]!!
                    similarGroups.add(
                        DuplicateGroup(
                            hash = "similar_${hashes[i]}_${hashes[j]}",
                            files = files.map { DuplicateFile(it.absolutePath, it.length(), it.lastModified()) },
                            totalSize = files.sumOf { it.length() },
                            suggestedKeep = files.maxByOrNull { it.lastModified() }!!.absolutePath
                        )
                    )
                }
            }
        }
        
        return similarGroups
    }
}
```

### UX Flow
```
┌───────────────────────┐
│ Duplicate Finder      │
│ [Scan for Duplicates] │
└──────────┬────────────┘
           ↓
┌────────────────────────┐
│ Scanning: 1250 files   │
│ Progress: 75%          │
└──────────┬─────────────┘
           ↓
┌─────────────────────────────────────┐
│ Found 45 duplicate groups (1.2GB)   │
│                                     │
│ ┌─Group 1: photo.jpg (3 copies)───┐│
│ │ ☑ /DCIM/photo.jpg (2MB) OLD     ││
│ │ ☑ /Download/photo.jpg (2MB)     ││
│ │ ☐ /Pictures/photo.jpg (2MB) NEW ││ ← Keep
│ │ [Delete Selected]               ││
│ └─────────────────────────────────┘│
│                                     │
│ ┌─Group 2: Similar images (90%)───┐│
│ │ img1.jpg, img2.jpg              ││
│ │ [Preview] [Delete Older]        ││
│ └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

---

## 📱 CHỨC NĂNG 7: Dọn Rác Messaging Apps

Sẽ tiếp tục trong file tiếp theo do giới hạn độ dài...
