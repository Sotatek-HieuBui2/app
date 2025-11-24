package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.AppCacheInfo
import com.smartcleaner.domain.model.JunkGroup
import com.smartcleaner.domain.model.JunkScanResult
import com.smartcleaner.domain.model.JunkType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for system junk operations
 */
interface JunkRepository {
    /**
     * Scan for system junk files
     * @param largeSizeThresholdMB Threshold for large files in MB (default 100)
     * @return Flow emitting scan progress (0-100)
     */
    suspend fun scanJunkFiles(largeSizeThresholdMB: Int = 100): Flow<Int>
    
    /**
     * Get scan results
     */
    suspend fun getScanResults(): JunkScanResult
    
    /**
     * Delete junk files by type
     * @param type The junk type to delete
     * @return Number of files deleted and space freed
     */
    suspend fun deleteJunkByType(type: JunkType): Result<Pair<Int, Long>>
    
    /**
     * Delete specific junk file
     */
    suspend fun deleteJunkFile(path: String): Result<Boolean>
    
    /**
     * Clear app cache for all apps
     * Requires CLEAR_APP_CACHE permission or root
     */
    suspend fun clearAllAppCache(): Result<Long>
    
    /**
     * Clear app cache for specific app
     */
    suspend fun clearAppCache(packageName: String): Result<Boolean>
    
    /**
     * Get cache info for all installed apps
     */
    suspend fun getAppCacheInfo(): List<AppCacheInfo>
    
    /**
     * Clear thumbnails cache
     */
    suspend fun clearThumbnailCache(): Result<Long>
}
