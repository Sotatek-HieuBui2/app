package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.DuplicateGroup
import com.smartcleaner.domain.model.DuplicateScanOptions
import com.smartcleaner.domain.model.DuplicateScanResult
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository for duplicate file detection
 */
interface DuplicateFinderRepository {
    /**
     * Scan for duplicate files with progress updates
     * @param directories Directories to scan
     * @param options Scan options
     * @return Flow emitting progress (0-100)
     */
    suspend fun scanForDuplicates(
        directories: List<File>,
        options: DuplicateScanOptions = DuplicateScanOptions()
    ): Flow<Int>
    
    /**
     * Get scan results (after scan completes)
     */
    suspend fun getScanResults(): DuplicateScanResult
    
    /**
     * Get specific duplicate group
     */
    suspend fun getDuplicateGroup(groupId: String): DuplicateGroup?
    
    /**
     * Delete files from a duplicate group
     * @param groupId The group ID
     * @param filePaths Files to delete (keep at least one)
     */
    suspend fun deleteFiles(groupId: String, filePaths: List<String>): Result<Int>
    
    /**
     * Calculate MD5 hash of a file
     */
    suspend fun calculateFileHash(file: File): String
    
    /**
     * Calculate perceptual hash for image
     */
    suspend fun calculateImageHash(file: File): String?
    
    /**
     * Compare two images for similarity
     * @return Similarity score 0.0 to 1.0
     */
    suspend fun compareImages(file1: File, file2: File): Float
    
    /**
     * Clear cached scan results
     */
    suspend fun clearResults()
}
