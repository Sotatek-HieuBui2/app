package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.UnusedApp
import com.smartcleaner.domain.model.UnusedAppAnalysisResult
import com.smartcleaner.domain.model.UsageStatsPermissionState
import kotlinx.coroutines.flow.Flow

/**
 * Repository for unused app analysis operations
 */
interface UnusedAppRepository {
    /**
     * Check if PACKAGE_USAGE_STATS permission is granted
     */
    suspend fun checkUsageStatsPermission(): UsageStatsPermissionState
    
    /**
     * Open settings to grant PACKAGE_USAGE_STATS permission
     */
    fun requestUsageStatsPermission()
    
    /**
     * Analyze installed apps for usage patterns
     * @return Flow emitting analysis progress (0-100)
     */
    suspend fun analyzeUnusedApps(): Flow<Int>
    
    /**
     * Get analysis results
     */
    suspend fun getAnalysisResults(): UnusedAppAnalysisResult
    
    /**
     * Uninstall an app
     * @param packageName The package to uninstall
     */
    suspend fun uninstallApp(packageName: String): Result<Boolean>
    
    /**
     * Get detailed usage stats for specific app
     * @param packageName The package name
     * @param daysBack Number of days to look back
     */
    suspend fun getAppUsageDetails(
        packageName: String,
        daysBack: Int = 30
    ): AppUsageDetails?
    
    /**
     * Clear app data (requires user confirmation)
     */
    suspend fun clearAppData(packageName: String): Result<Boolean>
}

/**
 * Detailed usage statistics for an app
 */
data class AppUsageDetails(
    val packageName: String,
    val totalTimeInForeground: Long, // milliseconds
    val totalTimesUsed: Int,
    val lastTimeUsed: Long,
    val dailyUsage: Map<String, Long> // date -> usage time in ms
)
