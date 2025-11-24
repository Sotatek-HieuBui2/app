package com.smartcleaner.data.repository

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import com.smartcleaner.domain.model.AppCacheInfo
import com.smartcleaner.domain.model.JunkFile
import com.smartcleaner.domain.model.JunkGroup
import com.smartcleaner.domain.model.JunkScanResult
import com.smartcleaner.domain.model.JunkType
import com.smartcleaner.domain.repository.JunkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JunkRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : JunkRepository {

    private var cachedScanResult: JunkScanResult? = null

    override suspend fun scanJunkFiles(largeSizeThresholdMB: Int): Flow<Int> = flow {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val thresholdBytes = largeSizeThresholdMB * 1024L * 1024L
            
            val allJunkFiles = mutableListOf<JunkFile>()
            
            // Step 1: Scan app cache (15%)
            emit(5)
            val cacheFiles = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                scanAppCache()
            } else {
                emptyList()
            }
            allJunkFiles.addAll(cacheFiles)
            emit(15)
            
            // Step 2: Scan temp files (15%)
            val tempFiles = scanFilesByExtensions(
                listOf(".tmp", ".temp"),
                JunkType.TEMP_FILES
            )
            allJunkFiles.addAll(tempFiles)
            emit(30)
            
            // Step 3: Scan log files (10%)
            val logFiles = scanFilesByExtensions(
                listOf(".log"),
                JunkType.LOG_FILES
            )
            allJunkFiles.addAll(logFiles)
            emit(40)
            
            // Step 4: Scan backup files (10%)
            val backupFiles = scanFilesByExtensions(
                listOf(".bak", ".backup", ".old"),
                JunkType.BACKUP_FILES
            )
            allJunkFiles.addAll(backupFiles)
            emit(50)
            
            // Step 5: Scan APK files in Download (15%)
            val apkFiles = scanApkFiles()
            allJunkFiles.addAll(apkFiles)
            emit(65)
            
            // Step 6: Scan large files (15%)
            val largeFiles = scanLargeFiles(thresholdBytes)
            allJunkFiles.addAll(largeFiles)
            emit(80)
            
            // Step 7: Scan thumbnail cache (10%)
            val thumbnails = scanThumbnailCache()
            allJunkFiles.addAll(thumbnails)
            emit(90)
            
            // Group by type
            val groups = groupByType(allJunkFiles)
            
            val totalSize = groups.sumOf { it.totalSize }
            val totalFiles = groups.sumOf { it.fileCount }
            val scanDuration = System.currentTimeMillis() - startTime
            
            cachedScanResult = JunkScanResult(
                groups = groups,
                totalSize = totalSize,
                totalFiles = totalFiles,
                scanDurationMs = scanDuration
            )
            
            emit(100)
        }
    }

    override suspend fun getScanResults(): JunkScanResult {
        return cachedScanResult ?: JunkScanResult(
            groups = emptyList(),
            totalSize = 0,
            totalFiles = 0,
            scanDurationMs = 0
        )
    }

    override suspend fun deleteJunkByType(type: JunkType): Result<Pair<Int, Long>> {
        return withContext(Dispatchers.IO) {
            try {
                val group = cachedScanResult?.groups?.find { it.type == type }
                    ?: return@withContext Result.success(Pair(0, 0L))
                
                var deletedCount = 0
                var freedSpace = 0L
                
                group.files.forEach { junkFile ->
                    val file = File(junkFile.path)
                    if (file.exists() && file.delete()) {
                        deletedCount++
                        freedSpace += junkFile.size
                    }
                }
                
                // Update cached results
                cachedScanResult = cachedScanResult?.copy(
                    groups = cachedScanResult!!.groups.filter { it.type != type }
                )
                
                Result.success(Pair(deletedCount, freedSpace))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteJunkFile(path: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                Result.success(file.delete())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun clearAllAppCache(): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) 
                        as StorageStatsManager
                    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) 
                        as StorageManager
                    val uuid = storageManager.getUuidForPath(context.filesDir)
                    
                    val pm = context.packageManager
                    val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getInstalledPackages(0)
                    }
                    
                    var totalCacheSize = 0L
                    
                    packages.forEach { packageInfo ->
                        try {
                            val stats = storageStatsManager.queryStatsForPackage(
                                uuid,
                                packageInfo.packageName,
                                android.os.Process.myUserHandle()
                            )
                            totalCacheSize += stats.cacheBytes
                        } catch (e: Exception) {
                            // Permission denied for system apps
                        }
                    }
                    
                    // Note: Actually clearing cache requires CLEAR_APP_CACHE permission
                    // or root access. This returns estimated size only.
                    Result.success(totalCacheSize)
                } else {
                    Result.failure(Exception("Requires Android 8.0+"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun clearAppCache(packageName: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // This requires CLEAR_APP_CACHE permission which is only available
                // for system apps or via root
                // Alternative: Use reflection to call PackageManager.deleteApplicationCacheFiles()
                Result.success(false)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAppCacheInfo(): List<AppCacheInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) 
                    as StorageStatsManager
                val storageManager = context.getSystemService(Context.STORAGE_SERVICE) 
                    as StorageManager
                val uuid = storageManager.getUuidForPath(context.filesDir)
                
                val pm = context.packageManager
                val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledPackages(0)
                }
                
                packages.mapNotNull { packageInfo ->
                    try {
                        val stats = storageStatsManager.queryStatsForPackage(
                            uuid,
                            packageInfo.packageName,
                            android.os.Process.myUserHandle()
                        )
                        
                        val appInfo = pm.getApplicationInfo(packageInfo.packageName, 0)
                        val appName = pm.getApplicationLabel(appInfo).toString()
                        
                        AppCacheInfo(
                            packageName = packageInfo.packageName,
                            appName = appName,
                            cacheSize = stats.cacheBytes,
                            dataSize = stats.dataBytes,
                            codeSize = stats.appBytes
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.filter { it.cacheSize > 0 }
                    .sortedByDescending { it.cacheSize }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun clearThumbnailCache(): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val thumbnailDir = File(
                    Environment.getExternalStorageDirectory(),
                    ".thumbnails"
                )
                
                if (!thumbnailDir.exists()) {
                    return@withContext Result.success(0L)
                }
                
                val size = calculateSize(thumbnailDir)
                thumbnailDir.deleteRecursively()
                
                Result.success(size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Private helper methods

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scanAppCache(): List<JunkFile> {
        return try {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) 
                as StorageStatsManager
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) 
                as StorageManager
            val uuid = storageManager.getUuidForPath(context.filesDir)
            
            val pm = context.packageManager
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }
            
            packages.mapNotNull { packageInfo ->
                try {
                    val stats = storageStatsManager.queryStatsForPackage(
                        uuid,
                        packageInfo.packageName,
                        android.os.Process.myUserHandle()
                    )
                    
                    if (stats.cacheBytes > 0) {
                        val appInfo = pm.getApplicationInfo(packageInfo.packageName, 0)
                        val appName = pm.getApplicationLabel(appInfo).toString()
                        
                        JunkFile(
                            path = "", // Cache path varies by app
                            name = "$appName cache",
                            size = stats.cacheBytes,
                            lastModified = System.currentTimeMillis(),
                            type = JunkType.APP_CACHE,
                            packageName = packageInfo.packageName
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun scanFilesByExtensions(
        extensions: List<String>,
        type: JunkType
    ): List<JunkFile> {
        val junkFiles = mutableListOf<JunkFile>()
        val searchDirs = listOf(
            Environment.getExternalStorageDirectory(),
            context.cacheDir,
            context.externalCacheDir
        ).filterNotNull()
        
        searchDirs.forEach { dir ->
            dir.walkTopDown()
                .maxDepth(5) // Limit depth for performance
                .filter { it.isFile }
                .filter { file -> extensions.any { file.name.endsWith(it, ignoreCase = true) } }
                .forEach { file ->
                    junkFiles.add(
                        JunkFile(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            type = type
                        )
                    )
                }
        }
        
        return junkFiles
    }

    private fun scanApkFiles(): List<JunkFile> {
        val downloadDir = File(Environment.getExternalStorageDirectory(), "Download")
        if (!downloadDir.exists()) return emptyList()
        
        return downloadDir.listFiles()
            ?.filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
            ?.map { file ->
                JunkFile(
                    path = file.absolutePath,
                    name = file.name,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    type = JunkType.APK_FILES
                )
            } ?: emptyList()
    }

    private fun scanLargeFiles(thresholdBytes: Long): List<JunkFile> {
        val junkFiles = mutableListOf<JunkFile>()
        val searchDirs = listOf(
            Environment.getExternalStorageDirectory()
        )
        
        searchDirs.forEach { dir ->
            dir.walkTopDown()
                .maxDepth(4)
                .filter { it.isFile && it.length() >= thresholdBytes }
                .forEach { file ->
                    junkFiles.add(
                        JunkFile(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            type = JunkType.LARGE_FILES
                        )
                    )
                }
        }
        
        return junkFiles.sortedByDescending { it.size }
    }

    private fun scanThumbnailCache(): List<JunkFile> {
        val thumbnailDir = File(
            Environment.getExternalStorageDirectory(),
            ".thumbnails"
        )
        
        if (!thumbnailDir.exists()) return emptyList()
        
        return thumbnailDir.walkTopDown()
            .filter { it.isFile }
            .map { file ->
                JunkFile(
                    path = file.absolutePath,
                    name = file.name,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    type = JunkType.THUMBNAIL_CACHE
                )
            }.toList()
    }

    private fun groupByType(files: List<JunkFile>): List<JunkGroup> {
        return files.groupBy { it.type }
            .map { (type, groupFiles) ->
                JunkGroup(
                    type = type,
                    files = groupFiles.sortedByDescending { it.size },
                    totalSize = groupFiles.sumOf { it.size },
                    fileCount = groupFiles.size
                )
            }
            .sortedByDescending { it.totalSize }
    }

    private fun calculateSize(file: File): Long {
        return if (file.isDirectory) {
            file.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        } else {
            file.length()
        }
    }
}
