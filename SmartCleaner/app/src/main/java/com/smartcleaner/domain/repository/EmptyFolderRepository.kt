package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.EmptyFolder
import com.smartcleaner.domain.model.EmptyFolderScanOptions
import com.smartcleaner.domain.model.EmptyFolderScanResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository for empty folder operations
 */
interface EmptyFolderRepository {
    /**
     * Scan for empty folders
     * @param options Scan options (include hidden, depth limits, etc.)
     * @return Flow emitting scan progress (0-100)
     */
    suspend fun scanEmptyFolders(
        options: EmptyFolderScanOptions = EmptyFolderScanOptions()
    ): Flow<Int>
    
    /**
     * Get scan results
     */
    suspend fun getScanResults(): EmptyFolderScanResult
    
    /**
     * Delete specific empty folder
     * @param path The folder path to delete
     */
    suspend fun deleteEmptyFolder(path: String): Result<Boolean>
    
    /**
     * Delete multiple empty folders
     * @param paths List of folder paths to delete
     * @return Number of successfully deleted folders
     */
    suspend fun deleteEmptyFolders(paths: List<String>): Result<Int>
    
    /**
     * Delete all empty folders from scan results
     */
    suspend fun deleteAllEmptyFolders(): Result<Int>
    
    /**
     * Check if a folder is empty
     */
    suspend fun isFolderEmpty(path: String): Boolean
}
