package com.smartcleaner.data.repository

import android.content.Context
import com.smartcleaner.data.util.HashUtil
import com.smartcleaner.domain.model.*
import com.smartcleaner.domain.repository.DuplicateFinderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuplicateFinderRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DuplicateFinderRepository {

    private var cachedResult: DuplicateScanResult? = null
    private val groupsMap = mutableMapOf<String, DuplicateGroup>()

    companion object {
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        private val VIDEO_EXTENSIONS = setOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "3gp")
        private val DOCUMENT_EXTENSIONS = setOf("pdf", "doc", "docx", "txt", "xlsx", "pptx")
        private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "flac", "aac", "ogg", "m4a")
    }

    override suspend fun scanForDuplicates(
        directories: List<File>,
        options: DuplicateScanOptions
    ): Flow<Int> = flow {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            groupsMap.clear()
            
            emit(5)
            
            // Step 1: Collect all files (5-20%)
            val allFiles = mutableListOf<File>()
            directories.forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    collectFiles(dir, options, allFiles)
                }
            }
            
            emit(20)
            
            if (allFiles.isEmpty()) {
                cachedResult = DuplicateScanResult(
                    groups = emptyList(),
                    totalDuplicates = 0,
                    totalWastedSpace = 0,
                    scanDurationMs = System.currentTimeMillis() - startTime,
                    filesScanned = 0
                )
                emit(100)
                return@withContext
            }
            
            // Step 2: Calculate hashes (20-70%)
            val hashMap = mutableMapOf<String, MutableList<DuplicateFile>>()
            val imageHashMap = mutableMapOf<String, MutableList<DuplicateFile>>()
            
            allFiles.forEachIndexed { index, file ->
                try {
                    val extension = file.extension.lowercase()
                    
                    // Calculate file hash for exact matches
                    val hash = HashUtil.calculateMD5(file)
                    
                    val duplicateFile = DuplicateFile(
                        filePath = file.absolutePath,
                        fileName = file.name,
                        size = file.length(),
                        hash = hash,
                        lastModified = file.lastModified(),
                        groupId = hash
                    )
                    
                    hashMap.getOrPut(hash) { mutableListOf() }.add(duplicateFile)
                    
                    // Calculate perceptual hash for images
                    if (options.usePerceptualHash && extension in IMAGE_EXTENSIONS) {
                        val pHash = HashUtil.calculatePerceptualHash(file)
                        if (pHash != null) {
                            imageHashMap.getOrPut(pHash) { mutableListOf() }.add(
                                duplicateFile.copy(groupId = pHash)
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Skip files with errors
                }
                
                val progress = 20 + ((index + 1) * 50 / allFiles.size)
                if (progress % 5 == 0) {
                    emit(progress)
                }
            }
            
            emit(70)
            
            // Step 3: Group duplicates (70-85%)
            val duplicateGroups = mutableListOf<DuplicateGroup>()
            
            // Exact matches
            hashMap.forEach { (hash, files) ->
                if (files.size > 1) {
                    val totalSize = files.first().size
                    val group = DuplicateGroup(
                        groupId = hash,
                        files = files.sortedBy { it.lastModified },
                        duplicateType = DuplicateType.EXACT_MATCH,
                        totalSize = totalSize * files.size,
                        wastedSpace = totalSize * (files.size - 1),
                        similarity = 1.0f
                    )
                    duplicateGroups.add(group)
                    groupsMap[hash] = group
                }
            }
            
            emit(80)
            
            // Similar images (compare perceptual hashes)
            if (options.usePerceptualHash) {
                val processedGroups = mutableSetOf<String>()
                val pHashList = imageHashMap.keys.toList()
                
                for (i in pHashList.indices) {
                    val hash1 = pHashList[i]
                    if (hash1 in processedGroups) continue
                    
                    val similarGroup = mutableListOf(hash1)
                    
                    for (j in (i + 1) until pHashList.size) {
                        val hash2 = pHashList[j]
                        if (hash2 in processedGroups) continue
                        
                        val similarity = HashUtil.calculateSimilarity(hash1, hash2)
                        if (similarity >= options.imageSimilarityThreshold) {
                            similarGroup.add(hash2)
                            processedGroups.add(hash2)
                        }
                    }
                    
                    if (similarGroup.size > 1) {
                        val allFiles = similarGroup.flatMap { imageHashMap[it] ?: emptyList() }
                        if (allFiles.size > 1) {
                            val groupId = "similar_$hash1"
                            val totalSize = allFiles.sumOf { it.size }
                            val avgSize = totalSize / allFiles.size
                            
                            val group = DuplicateGroup(
                                groupId = groupId,
                                files = allFiles.sortedBy { it.lastModified },
                                duplicateType = DuplicateType.SIMILAR_IMAGE,
                                totalSize = totalSize,
                                wastedSpace = avgSize * (allFiles.size - 1),
                                similarity = options.imageSimilarityThreshold
                            )
                            duplicateGroups.add(group)
                            groupsMap[groupId] = group
                        }
                    }
                    
                    processedGroups.add(hash1)
                }
            }
            
            emit(90)
            
            // Step 4: Sort by wasted space
            val sortedGroups = duplicateGroups.sortedByDescending { it.wastedSpace }
            
            val scanDuration = System.currentTimeMillis() - startTime
            
            cachedResult = DuplicateScanResult(
                groups = sortedGroups,
                totalDuplicates = sortedGroups.sumOf { it.files.size - 1 },
                totalWastedSpace = sortedGroups.sumOf { it.wastedSpace },
                scanDurationMs = scanDuration,
                filesScanned = allFiles.size
            )
            
            emit(100)
        }
    }

    override suspend fun getScanResults(): DuplicateScanResult {
        return cachedResult ?: DuplicateScanResult(
            groups = emptyList(),
            totalDuplicates = 0,
            totalWastedSpace = 0,
            scanDurationMs = 0,
            filesScanned = 0
        )
    }

    override suspend fun getDuplicateGroup(groupId: String): DuplicateGroup? {
        return groupsMap[groupId]
    }

    override suspend fun deleteFiles(groupId: String, filePaths: List<String>): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val group = groupsMap[groupId] 
                    ?: return@withContext Result.failure(Exception("Group not found"))
                
                if (filePaths.size >= group.files.size) {
                    return@withContext Result.failure(
                        IllegalStateException("Must keep at least one file")
                    )
                }
                
                var deletedCount = 0
                filePaths.forEach { path ->
                    val file = File(path)
                    if (file.exists() && file.delete()) {
                        deletedCount++
                    }
                }
                
                // Update group
                val remainingFiles = group.files.filterNot { it.filePath in filePaths }
                if (remainingFiles.size > 1) {
                    groupsMap[groupId] = group.copy(files = remainingFiles)
                } else {
                    groupsMap.remove(groupId)
                }
                
                Result.success(deletedCount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun calculateFileHash(file: File): String {
        return withContext(Dispatchers.IO) {
            HashUtil.calculateMD5(file)
        }
    }

    override suspend fun calculateImageHash(file: File): String? {
        return withContext(Dispatchers.IO) {
            HashUtil.calculatePerceptualHash(file)
        }
    }

    override suspend fun compareImages(file1: File, file2: File): Float {
        return withContext(Dispatchers.IO) {
            try {
                val hash1 = HashUtil.calculatePerceptualHash(file1) ?: return@withContext 0f
                val hash2 = HashUtil.calculatePerceptualHash(file2) ?: return@withContext 0f
                HashUtil.calculateSimilarity(hash1, hash2)
            } catch (e: Exception) {
                0f
            }
        }
    }

    override suspend fun clearResults() {
        cachedResult = null
        groupsMap.clear()
    }

    // Private helper methods
    
    private fun collectFiles(
        directory: File,
        options: DuplicateScanOptions,
        output: MutableList<File>
    ) {
        try {
            val files = directory.listFiles() ?: return
            
            for (file in files) {
                if (file.isDirectory) {
                    if (!shouldExcludePath(file.absolutePath, options)) {
                        collectFiles(file, options, output)
                    }
                } else if (file.isFile) {
                    if (shouldIncludeFile(file, options)) {
                        output.add(file)
                    }
                }
            }
        } catch (e: Exception) {
            // Skip directories with permission issues
        }
    }
    
    private fun shouldIncludeFile(file: File, options: DuplicateScanOptions): Boolean {
        val extension = file.extension.lowercase()
        val size = file.length()
        
        // Size filter
        if (size < options.minFileSize || size > options.maxFileSize) {
            return false
        }
        
        // Type filter
        val matchesType = when {
            extension in IMAGE_EXTENSIONS -> options.scanImages
            extension in VIDEO_EXTENSIONS -> options.scanVideos
            extension in DOCUMENT_EXTENSIONS -> options.scanDocuments
            extension in AUDIO_EXTENSIONS -> options.scanAudio
            else -> true // Include other types by default
        }
        
        if (!matchesType) return false
        
        // Path filter
        if (shouldExcludePath(file.absolutePath, options)) {
            return false
        }
        
        return true
    }
    
    private fun shouldExcludePath(path: String, options: DuplicateScanOptions): Boolean {
        // Check exclude paths
        if (options.excludePaths.any { path.startsWith(it) }) {
            return true
        }
        
        // Check include paths (if specified)
        if (options.includePaths.isNotEmpty()) {
            return !options.includePaths.any { path.startsWith(it) }
        }
        
        return false
    }
}
