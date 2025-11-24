package com.smartcleaner.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.smartcleaner.domain.model.*
import com.smartcleaner.domain.repository.StorageAnalyzerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageAnalyzerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StorageAnalyzerRepository {

    private var cachedAnalysis: StorageAnalysis? = null
    private val trendDataStore = mutableListOf<StorageTrendData>()

    companion object {
        private val SYSTEM_PATHS = setOf(
            "/system",
            "/data/system",
            "/data/data",
            "/proc",
            "/sys"
        )
    }

    override suspend fun analyzeStorage(options: StorageAnalysisOptions): Flow<Int> = flow {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            emit(5)
            
            // Get storage info
            val (totalSize, usedSize, freeSize) = getStorageInfo()
            
            emit(10)
            
            // Analyze root directory
            val rootPath = Environment.getExternalStorageDirectory()
            val rootNode = analyzeDirectory(
                directory = rootPath,
                depth = 0,
                maxDepth = options.maxDepth,
                minSize = options.minNodeSize,
                excludePaths = options.excludePaths,
                progressCallback = { progress ->
                    // Report 10-80%
                    val adjustedProgress = 10 + (progress * 70 / 100)
                    // emit(adjustedProgress) - Can't emit inside callback easily, handle externally
                }
            )
            
            emit(80)
            
            // Flatten tree for file type analysis
            val allFiles = mutableListOf<LargeFile>()
            collectAllFiles(rootPath, allFiles, options)
            
            emit(85)
            
            // Calculate file type breakdown
            val fileTypeMap = mutableMapOf<FileCategory, MutableList<LargeFile>>()
            allFiles.forEach { file ->
                fileTypeMap.getOrPut(file.category) { mutableListOf() }.add(file)
            }
            
            val fileTypeBreakdown = fileTypeMap.mapValues { (category, files) ->
                val extensionMap = files.groupBy { it.extension }
                    .mapValues { (_, extFiles) -> extFiles.sumOf { it.size } }
                
                FileTypeStats(
                    category = category,
                    totalSize = files.sumOf { it.size },
                    fileCount = files.size,
                    percentage = (files.sumOf { it.size }.toFloat() / usedSize) * 100,
                    extensions = extensionMap
                )
            }
            
            emit(90)
            
            // Get largest files
            val largestFiles = allFiles
                .sortedByDescending { it.size }
                .take(options.includeLargestFiles)
            
            emit(95)
            
            val analysisDuration = System.currentTimeMillis() - startTime
            
            cachedAnalysis = StorageAnalysis(
                totalSize = totalSize,
                usedSize = usedSize,
                freeSize = freeSize,
                nodes = rootNode?.children ?: emptyList(),
                fileTypeBreakdown = fileTypeBreakdown,
                largestFiles = largestFiles,
                analysisDurationMs = analysisDuration
            )
            
            // Record snapshot
            recordStorageSnapshot()
            
            emit(100)
        }
    }

    override suspend fun getAnalysisResults(): StorageAnalysis {
        return cachedAnalysis ?: StorageAnalysis(
            totalSize = 0,
            usedSize = 0,
            freeSize = 0,
            nodes = emptyList(),
            fileTypeBreakdown = emptyMap(),
            largestFiles = emptyList(),
            analysisDurationMs = 0
        )
    }

    override suspend fun getStorageTrend(days: Int): List<StorageTrendData> {
        return withContext(Dispatchers.IO) {
            val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            trendDataStore.filter { it.timestamp >= cutoffTime }
        }
    }

    override suspend fun recordStorageSnapshot() {
        withContext(Dispatchers.IO) {
            val (total, used, free) = getStorageInfo()
            val snapshot = StorageTrendData(
                timestamp = System.currentTimeMillis(),
                usedSize = used,
                freeSize = free
            )
            trendDataStore.add(snapshot)
            
            // Keep only last 90 days
            val cutoff = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L)
            trendDataStore.removeAll { it.timestamp < cutoff }
        }
    }

    override suspend fun getStorageInfo(): Triple<Long, Long, Long> {
        return withContext(Dispatchers.IO) {
            try {
                val path = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.absolutePath)
                
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                val availableBlocks = stat.availableBlocksLong
                
                val totalSize = totalBlocks * blockSize
                val freeSize = availableBlocks * blockSize
                val usedSize = totalSize - freeSize
                
                Triple(totalSize, usedSize, freeSize)
            } catch (e: Exception) {
                Triple(0L, 0L, 0L)
            }
        }
    }

    override suspend fun clearAnalysis() {
        cachedAnalysis = null
    }

    // Private helper methods
    
    private fun analyzeDirectory(
        directory: File,
        depth: Int,
        maxDepth: Int,
        minSize: Long,
        excludePaths: List<String>,
        progressCallback: ((Int) -> Unit)? = null
    ): StorageNode? {
        if (depth > maxDepth) return null
        if (!directory.exists() || !directory.isDirectory) return null
        if (shouldExclude(directory.absolutePath, excludePaths)) return null
        
        try {
            val files = directory.listFiles() ?: return null
            val children = mutableListOf<StorageNode>()
            var totalSize = 0L
            var fileCount = 0
            
            for (file in files) {
                if (file.isDirectory) {
                    val childNode = analyzeDirectory(
                        directory = file,
                        depth = depth + 1,
                        maxDepth = maxDepth,
                        minSize = minSize,
                        excludePaths = excludePaths,
                        progressCallback = progressCallback
                    )
                    if (childNode != null && childNode.size >= minSize) {
                        children.add(childNode)
                        totalSize += childNode.size
                    }
                } else if (file.isFile) {
                    val size = file.length()
                    totalSize += size
                    fileCount++
                    
                    // Add as child node if large enough and at leaf level
                    if (size >= minSize && depth == maxDepth) {
                        children.add(
                            StorageNode(
                                path = file.absolutePath,
                                name = file.name,
                                size = size,
                                percentage = 0f, // Will calculate later
                                depth = depth + 1,
                                children = emptyList(),
                                fileCount = 1,
                                category = FileCategory.fromExtension(file.extension)
                            )
                        )
                    }
                }
            }
            
            if (totalSize == 0L) return null
            
            // Calculate percentages
            val childrenWithPercentage = children.map { child ->
                child.copy(percentage = (child.size.toFloat() / totalSize) * 100)
            }.sortedByDescending { it.size }
            
            return StorageNode(
                path = directory.absolutePath,
                name = directory.name,
                size = totalSize,
                percentage = 0f, // Will be calculated by parent
                depth = depth,
                children = childrenWithPercentage,
                fileCount = fileCount,
                category = inferCategory(directory)
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun collectAllFiles(
        directory: File,
        output: MutableList<LargeFile>,
        options: StorageAnalysisOptions,
        currentDepth: Int = 0
    ) {
        if (currentDepth > options.maxDepth) return
        if (!directory.exists() || !directory.isDirectory) return
        if (shouldExclude(directory.absolutePath, options.excludePaths)) return
        
        try {
            val files = directory.listFiles() ?: return
            
            for (file in files) {
                if (file.isDirectory) {
                    collectAllFiles(file, output, options, currentDepth + 1)
                } else if (file.isFile) {
                    val size = file.length()
                    if (size >= options.minNodeSize) {
                        output.add(
                            LargeFile(
                                path = file.absolutePath,
                                name = file.name,
                                size = size,
                                extension = file.extension,
                                category = FileCategory.fromExtension(file.extension),
                                lastModified = file.lastModified(),
                                lastAccessed = null // Android doesn't expose this easily
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Skip directories with permission issues
        }
    }
    
    private fun shouldExclude(path: String, excludePaths: List<String>): Boolean {
        if (SYSTEM_PATHS.any { path.startsWith(it) }) return true
        return excludePaths.any { path.startsWith(it) }
    }
    
    private fun inferCategory(directory: File): FileCategory {
        val name = directory.name.lowercase()
        return when {
            name.contains("download") -> FileCategory.DOWNLOADS
            name.contains("dcim") || name.contains("pictures") || name.contains("images") -> FileCategory.IMAGES
            name.contains("video") || name.contains("movies") -> FileCategory.VIDEOS
            name.contains("music") || name.contains("audio") || name.contains("sounds") -> FileCategory.AUDIO
            name.contains("documents") -> FileCategory.DOCUMENTS
            name.contains("cache") -> FileCategory.CACHE
            name.contains("android") -> FileCategory.SYSTEM
            else -> FileCategory.OTHER
        }
    }
}
