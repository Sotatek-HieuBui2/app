package com.smartcleaner.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import com.smartcleaner.domain.model.LeftoverFile
import com.smartcleaner.domain.model.LeftoverGroup
import com.smartcleaner.domain.model.LeftoverScanResult
import com.smartcleaner.domain.model.LeftoverType
import com.smartcleaner.domain.repository.LeftoverRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeftoverRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LeftoverRepository {

    private var cachedScanResult: LeftoverScanResult? = null
    private val installedPackages = mutableSetOf<String>()
    private val uninstalledAppNames = mutableMapOf<String, String>() // packageName -> appName

    override suspend fun scanLeftoverFiles(): Flow<Int> = flow {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            // Step 1: Get installed packages (10%)
            emit(5)
            refreshInstalledPackages()
            emit(10)
            
            val leftoverFiles = mutableListOf<LeftoverFile>()
            
            // Step 2: Scan Android/data (30%)
            emit(15)
            val dataFiles = scanDirectory(
                getExternalStorageDirectory("Android/data"),
                LeftoverType.DATA
            )
            leftoverFiles.addAll(dataFiles)
            emit(30)
            
            // Step 3: Scan Android/obb (20%)
            val obbFiles = scanDirectory(
                getExternalStorageDirectory("Android/obb"),
                LeftoverType.OBB
            )
            leftoverFiles.addAll(obbFiles)
            emit(50)
            
            // Step 4: Scan Android/media (15%)
            val mediaFiles = scanDirectory(
                getExternalStorageDirectory("Android/media"),
                LeftoverType.MEDIA
            )
            leftoverFiles.addAll(mediaFiles)
            emit(65)
            
            // Step 5: Scan Download folder for APK and app-specific folders (10%)
            val downloadFiles = scanDownloadFolder()
            leftoverFiles.addAll(downloadFiles)
            emit(75)
            
            // Step 6: Scan Pictures/DCIM for app folders (10%)
            val pictureFiles = scanMediaFolders()
            leftoverFiles.addAll(pictureFiles)
            emit(85)
            
            // Step 7: Group by package (10%)
            val groups = groupByPackage(leftoverFiles)
            emit(95)
            
            val totalSize = groups.sumOf { it.totalSize }
            val totalFiles = groups.sumOf { it.fileCount }
            val scanDuration = System.currentTimeMillis() - startTime
            
            cachedScanResult = LeftoverScanResult(
                groups = groups,
                totalSize = totalSize,
                totalFiles = totalFiles,
                scanDurationMs = scanDuration
            )
            
            emit(100)
        }
    }

    override suspend fun getScanResults(): LeftoverScanResult {
        return cachedScanResult ?: LeftoverScanResult(
            groups = emptyList(),
            totalSize = 0,
            totalFiles = 0,
            scanDurationMs = 0
        )
    }

    override suspend fun deleteLeftoverFiles(packageName: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val group = cachedScanResult?.groups?.find { it.packageName == packageName }
                    ?: return@withContext Result.failure(Exception("Package not found"))
                
                var deletedCount = 0
                
                group.files.forEach { leftoverFile ->
                    val file = File(leftoverFile.path)
                    if (file.exists()) {
                        val deleted = if (file.isDirectory) {
                            file.deleteRecursively()
                        } else {
                            file.delete()
                        }
                        if (deleted) deletedCount++
                    }
                }
                
                // Update cached results
                cachedScanResult = cachedScanResult?.copy(
                    groups = cachedScanResult!!.groups.filter { it.packageName != packageName }
                )
                
                Result.success(deletedCount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteLeftoverFile(path: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                val deleted = if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
                Result.success(deleted)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getInstalledPackages(): List<String> {
        return installedPackages.toList()
    }

    override suspend fun isPackageInstalled(packageName: String): Boolean {
        return installedPackages.contains(packageName)
    }

    override suspend fun getAppNameForPackage(packageName: String): String? {
        return uninstalledAppNames[packageName]
    }

    override suspend fun backupBeforeDelete(group: LeftoverGroup): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                if (!backupDir.exists()) backupDir.mkdirs()
                
                val timestamp = System.currentTimeMillis()
                val backupFolder = File(backupDir, "${group.packageName}_$timestamp")
                backupFolder.mkdirs()
                
                // Copy files to backup (simplified - real implementation would use SAF)
                group.files.forEach { leftoverFile ->
                    val sourceFile = File(leftoverFile.path)
                    if (sourceFile.exists()) {
                        val destFile = File(backupFolder, sourceFile.name)
                        sourceFile.copyRecursively(destFile, overwrite = true)
                    }
                }
                
                Result.success(backupFolder.absolutePath)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Private helper methods

    private fun refreshInstalledPackages() {
        installedPackages.clear()
        val pm = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }
        packages.forEach { packageInfo ->
            installedPackages.add(packageInfo.packageName)
        }
    }

    private fun scanDirectory(directory: File?, type: LeftoverType): List<LeftoverFile> {
        if (directory == null || !directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        val leftoverFiles = mutableListOf<LeftoverFile>()
        
        directory.listFiles()?.forEach { file ->
            val packageName = file.name
            
            // Check if this is a leftover (package not installed)
            if (!installedPackages.contains(packageName)) {
                val size = calculateSize(file)
                val childCount = if (file.isDirectory) file.listFiles()?.size ?: 0 else 0
                
                // Try to get app name from package name
                val appName = uninstalledAppNames.getOrPut(packageName) {
                    packageName.split(".").lastOrNull()?.capitalize() ?: packageName
                }
                
                leftoverFiles.add(
                    LeftoverFile(
                        path = file.absolutePath,
                        packageName = packageName,
                        appName = appName,
                        size = size,
                        lastModified = file.lastModified(),
                        type = type,
                        isDirectory = file.isDirectory,
                        childrenCount = childCount,
                        previewPath = findPreviewFile(file)
                    )
                )
            }
        }
        
        return leftoverFiles
    }

    private fun scanDownloadFolder(): List<LeftoverFile> {
        val downloadDir = getExternalStorageDirectory("Download")
        if (downloadDir == null || !downloadDir.exists()) return emptyList()
        
        val leftoverFiles = mutableListOf<LeftoverFile>()
        
        // Look for APK files and app-specific folders
        downloadDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "apk") {
                // Try to extract package name from APK
                val packageName = extractPackageFromApk(file)
                if (packageName != null && !installedPackages.contains(packageName)) {
                    leftoverFiles.add(
                        LeftoverFile(
                            path = file.absolutePath,
                            packageName = packageName,
                            appName = file.nameWithoutExtension,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            type = LeftoverType.DOWNLOAD,
                            isDirectory = false
                        )
                    )
                }
            }
        }
        
        return leftoverFiles
    }

    private fun scanMediaFolders(): List<LeftoverFile> {
        // Scan Pictures and DCIM for app-specific folders
        val leftoverFiles = mutableListOf<LeftoverFile>()
        
        listOf("Pictures", "DCIM").forEach { folderName ->
            val dir = getExternalStorageDirectory(folderName)
            dir?.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    // Check if folder name matches uninstalled app pattern
                    val possiblePackage = file.name.lowercase()
                    val isLeftover = uninstalledAppNames.keys.any { 
                        possiblePackage.contains(it.lowercase()) 
                    }
                    
                    if (isLeftover) {
                        leftoverFiles.add(
                            LeftoverFile(
                                path = file.absolutePath,
                                packageName = file.name,
                                appName = file.name,
                                size = calculateSize(file),
                                lastModified = file.lastModified(),
                                type = if (folderName == "Pictures") LeftoverType.PICTURES 
                                      else LeftoverType.DCIM,
                                isDirectory = true,
                                childrenCount = file.listFiles()?.size ?: 0,
                                previewPath = findPreviewFile(file)
                            )
                        )
                    }
                }
            }
        }
        
        return leftoverFiles
    }

    private fun groupByPackage(files: List<LeftoverFile>): List<LeftoverGroup> {
        return files.groupBy { it.packageName }
            .map { (packageName, groupFiles) ->
                LeftoverGroup(
                    packageName = packageName,
                    appName = groupFiles.first().appName,
                    totalSize = groupFiles.sumOf { it.size },
                    fileCount = groupFiles.size,
                    files = groupFiles.sortedByDescending { it.size }
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

    private fun findPreviewFile(directory: File): String? {
        if (!directory.isDirectory) return null
        
        // Find first image file for preview
        return directory.walkTopDown()
            .firstOrNull { 
                it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp") 
            }?.absolutePath
    }

    private fun extractPackageFromApk(apkFile: File): String? {
        // Simplified - real implementation would use PackageManager
        return try {
            val pm = context.packageManager
            val packageInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
            packageInfo?.packageName
        } catch (e: Exception) {
            null
        }
    }

    private fun getExternalStorageDirectory(relativePath: String): File? {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            File(Environment.getExternalStorageDirectory(), relativePath)
        } else {
            null
        }
    }
}
