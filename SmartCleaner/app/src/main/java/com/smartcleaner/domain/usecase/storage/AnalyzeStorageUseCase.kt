package com.smartcleaner.domain.usecase.storage

import com.smartcleaner.domain.model.StorageAnalysis
import com.smartcleaner.domain.model.StorageAnalysisOptions
import com.smartcleaner.domain.repository.StorageAnalyzerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case: Analyze storage usage
 */
class AnalyzeStorageUseCase @Inject constructor(
    private val repository: StorageAnalyzerRepository
) {
    suspend operator fun invoke(
        options: StorageAnalysisOptions = StorageAnalysisOptions()
    ): Flow<AnalysisProgress> = flow {
        emit(AnalysisProgress.Starting)
        
        try {
            repository.analyzeStorage(options).collect { progress ->
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
    object Starting : AnalysisProgress()
    data class Analyzing(val progress: Int) : AnalysisProgress()
    data class Completed(val result: StorageAnalysis) : AnalysisProgress()
    data class Error(val message: String) : AnalysisProgress()
}
