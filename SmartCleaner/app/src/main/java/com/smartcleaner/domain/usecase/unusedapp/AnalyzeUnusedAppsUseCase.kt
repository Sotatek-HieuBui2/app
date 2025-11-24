package com.smartcleaner.domain.usecase.unusedapp

import com.smartcleaner.domain.model.UnusedAppAnalysisResult
import com.smartcleaner.domain.model.UsageStatsPermissionState
import com.smartcleaner.domain.repository.UnusedAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case: Analyze unused apps using UsageStatsManager
 * 
 * Input: None
 * Output: Flow<AnalysisProgress>
 * 
 * Process:
 * 1. Check PACKAGE_USAGE_STATS permission
 * 2. Get all installed packages
 * 3. Query usage stats for past 90 days
 * 4. Calculate days since last use for each app
 * 5. Get app sizes via StorageStatsManager
 * 6. Categorize apps (never used, 30+ days, 90+ days)
 * 7. Sort by size descending
 */
class AnalyzeUnusedAppsUseCase @Inject constructor(
    private val repository: UnusedAppRepository
) {
    suspend operator fun invoke(): Flow<AnalysisProgress> = flow {
        emit(AnalysisProgress.CheckingPermission)
        
        // Check permission
        val permissionState = repository.checkUsageStatsPermission()
        
        if (permissionState != UsageStatsPermissionState.GRANTED) {
            emit(AnalysisProgress.PermissionRequired)
            return@flow
        }
        
        emit(AnalysisProgress.Analyzing(0))
        
        try {
            repository.analyzeUnusedApps().collect { progress ->
                emit(AnalysisProgress.Analyzing(progress))
            }
            
            val result = repository.getAnalysisResults()
            emit(AnalysisProgress.Completed(result))
        } catch (e: Exception) {
            emit(AnalysisProgress.Error(e.message ?: "Analysis failed"))
        }
    }
}

sealed class AnalysisProgress {
    object CheckingPermission : AnalysisProgress()
    object PermissionRequired : AnalysisProgress()
    data class Analyzing(val progress: Int) : AnalysisProgress()
    data class Completed(val result: UnusedAppAnalysisResult) : AnalysisProgress()
    data class Error(val message: String) : AnalysisProgress()
}
