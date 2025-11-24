package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.LeftoverGroup
import com.smartcleaner.domain.model.LeftoverScanResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for leftover files operations
 */
interface LeftoverRepository {
    /**
     * Scan for leftover files from uninstalled apps
     * @return Flow emitting scan progress (0-100)
     */
    suspend fun scanLeftoverFiles(): Flow<Int>
    
    /**
     * Get scan results
     */
    suspend fun getScanResults(): LeftoverScanResult
    
    /**
     * Delete leftover files for specific app
     * @param packageName The package name of the uninstalled app
     * @return Number of files deleted
     */
    suspend fun deleteLeftoverFiles(packageName: String): Result<Int>
    
    /**
     * Delete specific leftover file or directory
     * @param path The file path to delete
     */
    suspend fun deleteLeftoverFile(path: String): Result<Boolean>
    
    /**
     * Get list of installed package names
     */
    suspend fun getInstalledPackages(): List<String>
    
    /**
     * Check if package is installed
     */
    suspend fun isPackageInstalled(packageName: String): Boolean
    
    /**
     * Get app name from package name (from cached uninstalled apps)
     */
    suspend fun getAppNameForPackage(packageName: String): String?
    
    /**
     * Save leftover files to backup before deletion
     */
    suspend fun backupBeforeDelete(group: LeftoverGroup): Result<String>
}
