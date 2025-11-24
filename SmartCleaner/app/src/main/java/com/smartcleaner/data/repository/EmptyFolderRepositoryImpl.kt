package com.smartcleaner.data.repository

import android.content.Context
import android.os.Environment
import com.smartcleaner.domain.model.EmptyFolder
import com.smartcleaner.domain.model.EmptyFolderScanOptions
import com.smartcleaner.domain.model.EmptyFolderScanResult
import com.smartcleaner.domain.repository.EmptyFolderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmptyFolderRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : EmptyFolderRepository {

    private var cachedScanResult: EmptyFolderScanResult? = null
    
    // System folders that should never be deleted
    private val systemExcludePaths = setOf(
        "/Android/data",
        "/Android/obb",
        "/Android/media",
        "/.android_secure",
        "/DCIM/.thumbnails",
        "/Music",
        "/Podcasts",
        "/Ringtones",
        "/Alarms",
        "/Notifications"
    )

    override suspend fun scanEmptyFolders(options: EmptyFolderScanOptions): Flow<Int> = flow {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            emit(5)
            
            val rootDir = Environment.getExternalStorageDirectory()
            if (!rootDir.exists() || !rootDir.isDirectory) {
                cachedScanResult = EmptyFolderScanResult(
                    folders = emptyList(),
                    totalCount = 0,
                    scanDurationMs = 0
                )
                emit(100)
                return@withContext
            }
            
            emit(10)
            
            val emptyFolders = mutableListOf<EmptyFolder>()
            val allDirs = mutableListOf<File>()
            
            // Step 1: Collect all directories (20%)
            collectDirectories(rootDir, allDirs, options)
            emit(30)
            
            // Step 2: Filter empty directories (60%)
            val totalDirs = allDirs.size
            allDirs.forEachIndexed { index, dir ->
                if (isFolderEmptyInternal(dir, options)) {
                    val depth = calculateDepth(dir, rootDir)
                    
                    emptyFolders.add(
                        EmptyFolder(
                            path = dir.absolutePath,
                            name = dir.name,
                            parentPath = dir.parent ?: "",
                            lastModified = dir.lastModified(),
                            depth = depth
                        )
                    )
                }
                
                // Update progress
                val progress = 30 + ((index + 1) * 60 / totalDirs)
                if (progress % 5 == 0) {
                    emit(progress)
                }
            }
            
            emit(90)
            
            // Step 3: Sort by depth (deepest first for safe deletion)
            val sortedFolders = emptyFolders.sortedByDescending { it.depth }
            
            val scanDuration = System.currentTimeMillis() - startTime
            
            cachedScanResult = EmptyFolderScanResult(
                folders = sortedFolders,
                totalCount = sortedFolders.size,
                scanDurationMs = scanDuration
            )
            
            emit(100)
        }
    }

    override suspend fun getScanResults(): EmptyFolderScanResult {
        return cachedScanResult ?: EmptyFolderScanResult(
            folders = emptyList(),
            totalCount = 0,
            scanDurationMs = 0
        )
    }

    override suspend fun deleteEmptyFolder(path: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = File(path)
                
                // Safety checks
                if (!folder.exists()) {
                    return@withContext Result.success(false)
                }
                
                if (!folder.isDirectory) {
                    return@withContext Result.failure(Exception("Not a directory"))
                }
                
                if (isSystemPath(path)) {
                    return@withContext Result.failure(Exception("Cannot delete system folder"))
                }
                
                // Double check if still empty
                if (!isFolderEmptyInternal(folder, EmptyFolderScanOptions())) {
                    return@withContext Result.failure(Exception("Folder is not empty"))
                }
                
                val deleted = folder.delete()
                
                // Update cache
                if (deleted) {
                    cachedScanResult = cachedScanResult?.copy(
                        folders = cachedScanResult!!.folders.filter { it.path != path },
                        totalCount = cachedScanResult!!.totalCount - 1
                    )
                }
                
                Result.success(deleted)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteEmptyFolders(paths: List<String>): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                var deletedCount = 0
                
                // Delete from deepest to shallowest
                val sortedPaths = paths.sortedByDescending { it.count { c -> c == '/' } }
                
                sortedPaths.forEach { path ->
                    val result = deleteEmptyFolder(path)
                    if (result.isSuccess && result.getOrNull() == true) {
                        deletedCount++
                    }
                }
                
                Result.success(deletedCount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteAllEmptyFolders(): Result<Int> {
        return withContext(Dispatchers.IO) {
            val result = cachedScanResult ?: return@withContext Result.success(0)
            deleteEmptyFolders(result.folders.map { it.path })
        }
    }

    override suspend fun isFolderEmpty(path: String): Boolean {
        return withContext(Dispatchers.IO) {
            val folder = File(path)
            isFolderEmptyInternal(folder, EmptyFolderScanOptions())
        }
    }

    // Private helper methods

    private fun collectDirectories(
        dir: File,
        result: MutableList<File>,
        options: EmptyFolderScanOptions,
        currentDepth: Int = 0
    ) {
        if (currentDepth > options.maxDepth) return
        if (currentDepth < options.minDepth) return
        
        // Check if path should be excluded
        val relativePath = dir.absolutePath.removePrefix(
            Environment.getExternalStorageDirectory().absolutePath
        )
        
        if (isSystemPath(relativePath)) return
        if (options.excludePaths.any { relativePath.startsWith(it) }) return
        
        // Skip hidden folders if option is set
        if (!options.includeHiddenFolders && dir.name.startsWith(".")) return
        
        try {
            val files = dir.listFiles() ?: return
            
            result.add(dir)
            
            // Recursively collect subdirectories
            files.filter { it.isDirectory }.forEach { subDir ->
                collectDirectories(subDir, result, options, currentDepth + 1)
            }
        } catch (e: SecurityException) {
            // Permission denied, skip
        }
    }

    private fun isFolderEmptyInternal(folder: File, options: EmptyFolderScanOptions): Boolean {
        if (!folder.isDirectory) return false
        
        try {
            val contents = folder.listFiles() ?: return false
            
            if (contents.isEmpty()) return true
            
            // Check if all contents are hidden files/folders that we should ignore
            if (!options.includeHiddenFolders) {
                val nonHiddenContents = contents.filter { !it.name.startsWith(".") }
                if (nonHiddenContents.isEmpty()) return true
            }
            
            return false
        } catch (e: SecurityException) {
            return false
        }
    }

    private fun calculateDepth(dir: File, rootDir: File): Int {
        val relativePath = dir.absolutePath.removePrefix(rootDir.absolutePath)
        return relativePath.count { it == '/' }
    }

    private fun isSystemPath(path: String): Boolean {
        return systemExcludePaths.any { path.startsWith(it) }
    }
}
