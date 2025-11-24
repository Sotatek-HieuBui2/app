package com.smartcleaner.domain.usecase.classifier

import com.smartcleaner.domain.model.ClassificationResult
import com.smartcleaner.domain.repository.JunkClassifierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

/**
 * Use case: Classify files using ML model
 * 
 * Input: List<File>
 * Output: Flow<ClassificationProgress>
 * 
 * Process:
 * 1. Check if model is loaded
 * 2. Extract features from each file
 * 3. Run inference
 * 4. Apply post-processing rules
 * 5. Generate recommendations
 */
class ClassifyJunkFilesUseCase @Inject constructor(
    private val repository: JunkClassifierRepository
) {
    suspend operator fun invoke(files: List<File>): Flow<ClassificationProgress> = flow {
        emit(ClassificationProgress.Initializing)
        
        // Check model
        if (!repository.isModelReady()) {
            val initResult = repository.initializeModel()
            if (initResult.isFailure) {
                emit(ClassificationProgress.Error("Failed to load ML model"))
                return@flow
            }
        }
        
        if (files.isEmpty()) {
            emit(ClassificationProgress.Error("No files to classify"))
            return@flow
        }
        
        emit(ClassificationProgress.Classifying(0, files.size))
        
        try {
            repository.classifyFiles(files).collect { progress ->
                emit(ClassificationProgress.Classifying(progress, files.size))
            }
            
            val result = repository.getClassificationResults()
            emit(ClassificationProgress.Completed(result))
        } catch (e: Exception) {
            emit(ClassificationProgress.Error(e.message ?: "Classification failed"))
        }
    }
}

sealed class ClassificationProgress {
    object Initializing : ClassificationProgress()
    data class Classifying(val progress: Int, val total: Int) : ClassificationProgress()
    data class Completed(val result: ClassificationResult) : ClassificationProgress()
    data class Error(val message: String) : ClassificationProgress()
}
