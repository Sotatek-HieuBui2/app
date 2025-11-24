package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.RootOperationResult
import com.smartcleaner.domain.model.RootStatus
import com.smartcleaner.domain.model.SystemApp
import com.smartcleaner.domain.model.SystemPartitionInfo

/**
 * Repository for root operations using LibSu
 */
interface RootRepository {
    /**
     * Check if device is rooted and request access
     */
    suspend fun checkRootAccess(): RootStatus
    
    /**
     * Request root permission
     */
    suspend fun requestRootPermission(): RootStatus
    
    /**
     * Clean system cache (requires root)
     */
    suspend fun cleanSystemCache(): RootOperationResult
    
    /**
     * Clean dalvik cache (requires root)
     */
    suspend fun cleanDalvikCache(): RootOperationResult
    
    /**
     * Get system partition info
     */
    suspend fun getSystemPartitionInfo(): List<SystemPartitionInfo>
    
    /**
     * Get list of system apps
     */
    suspend fun getSystemApps(): List<SystemApp>
    
    /**
     * Disable system app (requires root)
     */
    suspend fun disableSystemApp(packageName: String): RootOperationResult
    
    /**
     * Execute custom shell command as root
     */
    suspend fun executeCommand(command: String): RootOperationResult
}
