package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.StorageAnalysis
import com.smartcleaner.domain.model.StorageAnalysisOptions
import com.smartcleaner.domain.model.StorageTrendData
import kotlinx.coroutines.flow.Flow

/**
 * Repository for storage analysis
 */
interface StorageAnalyzerRepository {
    /**
     * Analyze storage with progress updates
     * @param options Analysis options
     * @return Flow emitting progress (0-100)
     */
    suspend fun analyzeStorage(
        options: StorageAnalysisOptions = StorageAnalysisOptions()
    ): Flow<Int>
    
    /**
     * Get analysis results
     */
    suspend fun getAnalysisResults(): StorageAnalysis
    
    /**
     * Get storage trend history
     * @param days Number of days to retrieve
     */
    suspend fun getStorageTrend(days: Int = 30): List<StorageTrendData>
    
    /**
     * Record current storage snapshot for trend
     */
    suspend fun recordStorageSnapshot()
    
    /**
     * Get storage info (total, used, free)
     */
    suspend fun getStorageInfo(): Triple<Long, Long, Long>
    
    /**
     * Clear cached analysis
     */
    suspend fun clearAnalysis()
}
