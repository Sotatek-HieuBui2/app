package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.MessagingApp
import com.smartcleaner.domain.model.MessagingScanOptions
import com.smartcleaner.domain.model.MessagingScanResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository for messaging app media cleaning
 */
interface MessagingCleanerRepository {
    /**
     * Check which messaging apps are installed
     */
    suspend fun getInstalledApps(): List<MessagingApp>
    
    /**
     * Scan messaging apps for media files
     * @param options Scan options
     * @return Flow emitting progress (0-100)
     */
    suspend fun scanMessagingApps(
        options: MessagingScanOptions = MessagingScanOptions()
    ): Flow<Int>
    
    /**
     * Get scan results
     */
    suspend fun getScanResults(): MessagingScanResult
    
    /**
     * Delete selected media files
     * @param filePaths List of file paths to delete
     */
    suspend fun deleteMedia(filePaths: List<String>): Result<Int>
    
    /**
     * Get media directory path for an app
     */
    suspend fun getAppMediaPath(app: MessagingApp): String?
    
    /**
     * Clear cached results
     */
    suspend fun clearResults()
}
