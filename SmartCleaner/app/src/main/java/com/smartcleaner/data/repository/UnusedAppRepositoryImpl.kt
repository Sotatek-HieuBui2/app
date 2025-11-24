package com.smartcleaner.data.repository

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.smartcleaner.domain.model.*
import com.smartcleaner.domain.repository.AppUsageDetails
import com.smartcleaner.domain.repository.UnusedAppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnusedAppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UnusedAppRepository {

    private var cachedAnalysisResult: UnusedAppAnalysisResult? = null

    override suspend fun checkUsageStatsPermission(): UsageStatsPermissionState {
        return withContext(Dispatchers.IO) {
            try {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
                
                when (mode) {
                    AppOpsManager.MODE_ALLOWED -> UsageStatsPermissionState.GRANTED
                    AppOpsManager.MODE_IGNORED, AppOpsManager.MODE_ERRORED -> UsageStatsPermissionState.DENIED
                    else -> UsageStatsPermissionState.NOT_REQUESTED
                }
            } catch (e: Exception) {
                UsageStatsPermissionState.NOT_REQUESTED
            }
        }
    }

    override fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override suspend fun analyzeUnusedApps(): Flow<Int> = flow {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            emit(5)
            
            // Get usage stats manager
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) 
                as UsageStatsManager
            
            val pm = context.packageManager
            
            // Get all installed packages (10%)
            emit(10)
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }
            
            emit(20)
            
            // Query usage stats for last 90 days (20%)
            val endTime = System.currentTimeMillis()
            val startTimeStats = endTime - TimeUnit.DAYS.toMillis(90)
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTimeStats,
                endTime
            )
            
            // Create map for quick lookup
            val usageMap = usageStats.associateBy { it.packageName }
            
            emit(40)
            
            // Analyze each package (40%)
            val unusedApps = mutableListOf<UnusedApp>()
            val totalPackages = packages.size
            
            packages.forEachIndexed { index, packageInfo ->
                try {
                    // Skip our own app
                    if (packageInfo.packageName == context.packageName) {
                        return@forEachIndexed
                    }
                    
                    val appInfo = packageInfo.applicationInfo
                    
                    // Get usage info
                    val usage = usageMap[packageInfo.packageName]
                    val lastUsedTime = usage?.lastTimeUsed ?: 0L
                    
                    val currentTime = System.currentTimeMillis()
                    val daysSinceUse = if (lastUsedTime > 0) {
                        TimeUnit.MILLISECONDS.toDays(currentTime - lastUsedTime).toInt()
                    } else {
                        Int.MAX_VALUE
                    }
                    
                    // Determine category
                    val category = when {
                        lastUsedTime == 0L -> UnusedCategory.NEVER_USED
                        daysSinceUse >= 90 -> UnusedCategory.NOT_USED_90_DAYS
                        daysSinceUse >= 30 -> UnusedCategory.NOT_USED_30_DAYS
                        else -> null // Skip recently used apps
                    }
                    
                    if (category != null) {
                        // Get app size
                        val (totalSize, cacheSize, dataSize) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getAppSize(packageInfo.packageName)
                        } else {
                            Triple(0L, 0L, 0L)
                        }
                        
                        // Get app name and icon
                        val appName = if (appInfo != null) pm.getApplicationLabel(appInfo).toString() else packageInfo.packageName
                        val appIcon = if (appInfo != null) getAppIconBytes(appInfo) else ByteArray(0)
                        
                        val isSystemApp = if (appInfo != null) (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 else false
                        
                        unusedApps.add(
                            UnusedApp(
                                packageName = packageInfo.packageName,
                                appName = appName,
                                appIcon = appIcon,
                                lastUsedTime = lastUsedTime,
                                installedTime = packageInfo.firstInstallTime,
                                totalSize = totalSize,
                                cacheSize = cacheSize,
                                dataSize = dataSize,
                                category = category,
                                daysSinceLastUse = if (daysSinceUse == Int.MAX_VALUE) -1 else daysSinceUse,
                                isSystemApp = isSystemApp
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip this app if error
                }
                
                // Update progress
                val progress = 40 + ((index + 1) * 50 / totalPackages)
                if (progress % 5 == 0) {
                    emit(progress)
                }
            }
            
            emit(90)
            
            // Sort by size descending
            val sortedApps = unusedApps.sortedByDescending { it.totalSize }
            
            // Calculate breakdown
            val breakdown = sortedApps.groupBy { it.category }
                .mapValues { (_, apps) ->
                    UnusedCategoryStats(
                        count = apps.size,
                        totalSize = apps.sumOf { it.totalSize }
                    )
                }
            
            val analysisDuration = System.currentTimeMillis() - startTime
            
            cachedAnalysisResult = UnusedAppAnalysisResult(
                apps = sortedApps,
                totalSize = sortedApps.sumOf { it.totalSize },
                totalCount = sortedApps.size,
                breakdown = breakdown,
                analysisDurationMs = analysisDuration
            )
            
            emit(100)
        }
    }

    override suspend fun getAnalysisResults(): UnusedAppAnalysisResult {
        return cachedAnalysisResult ?: UnusedAppAnalysisResult(
            apps = emptyList(),
            totalSize = 0,
            totalCount = 0,
            breakdown = emptyMap(),
            analysisDurationMs = 0
        )
    }

    override suspend fun uninstallApp(packageName: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAppUsageDetails(
        packageName: String,
        daysBack: Int
    ): AppUsageDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) 
                    as UsageStatsManager
                
                val endTime = System.currentTimeMillis()
                val startTime = endTime - TimeUnit.DAYS.toMillis(daysBack.toLong())
                
                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                
                val appStats = usageStats.filter { it.packageName == packageName }
                
                if (appStats.isEmpty()) return@withContext null
                
                val totalTime = appStats.sumOf { it.totalTimeInForeground }
                val totalTimesUsed = appStats.sumOf { 
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.totalTimeForegroundServiceUsed.toInt()
                    } else {
                        0
                    }
                }
                val lastUsed = appStats.maxOfOrNull { it.lastTimeUsed } ?: 0L
                
                // Daily breakdown
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dailyUsage = appStats.associate { stat ->
                    sdf.format(Date(stat.lastTimeStamp)) to stat.totalTimeInForeground
                }
                
                AppUsageDetails(
                    packageName = packageName,
                    totalTimeInForeground = totalTime,
                    totalTimesUsed = totalTimesUsed,
                    lastTimeUsed = lastUsed,
                    dailyUsage = dailyUsage
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun clearAppData(packageName: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Open app info screen for user to clear data manually
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Private helper methods

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAppSize(packageName: String): Triple<Long, Long, Long> {
        return try {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) 
                as StorageStatsManager
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) 
                as StorageManager
            
            val uuid = storageManager.getUuidForPath(context.filesDir)
            
            val stats = storageStatsManager.queryStatsForPackage(
                uuid,
                packageName,
                Process.myUserHandle()
            )
            
            val totalSize = stats.appBytes + stats.dataBytes + stats.cacheBytes
            val cacheSize = stats.cacheBytes
            val dataSize = stats.dataBytes
            
            Triple(totalSize, cacheSize, dataSize)
        } catch (e: Exception) {
            Triple(0L, 0L, 0L)
        }
    }

    private fun getAppIconBytes(appInfo: ApplicationInfo): ByteArray? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(appInfo)
            val bitmap = drawableToBitmap(drawable)
            
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        
        return bitmap
    }
}
