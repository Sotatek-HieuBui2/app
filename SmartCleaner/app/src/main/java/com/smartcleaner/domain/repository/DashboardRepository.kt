package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.DashboardData
import com.smartcleaner.domain.model.CleaningActivity

/**
 * Repository for dashboard data
 */
interface DashboardRepository {
    /**
     * Get dashboard overview data
     */
    suspend fun getDashboardData(): DashboardData
    
    /**
     * Record cleaning activity
     */
    suspend fun recordActivity(activity: CleaningActivity)
    
    /**
     * Get cleaning history
     */
    suspend fun getCleaningHistory(limit: Int = 10): List<CleaningActivity>
    
    /**
     * Clear all history
     */
    suspend fun clearHistory()
}
