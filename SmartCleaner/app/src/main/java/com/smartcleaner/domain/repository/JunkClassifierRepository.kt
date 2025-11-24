package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.ClassificationResult
import com.smartcleaner.domain.model.JunkClassification
import com.smartcleaner.domain.model.ModelInfo
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository for ML-based junk classification
 */
interface JunkClassifierRepository {
    /**
     * Initialize TensorFlow Lite model
     * @return Success if model loaded successfully
     */
    suspend fun initializeModel(): Result<ModelInfo>
    
    /**
     * Check if model is loaded and ready
     */
    suspend fun isModelReady(): Boolean
    
    /**
     * Classify a single file
     * @param file The file to classify
     * @return Classification result with confidence score
     */
    suspend fun classifyFile(file: File): JunkClassification?
    
    /**
     * Classify multiple files with progress updates
     * @param files List of files to classify
     * @return Flow emitting progress (0-100)
     */
    suspend fun classifyFiles(files: List<File>): Flow<Int>
    
    /**
     * Get classification results (after classifyFiles completes)
     */
    suspend fun getClassificationResults(): ClassificationResult
    
    /**
     * Get model information
     */
    suspend fun getModelInfo(): ModelInfo?
    
    /**
     * Extract features from file for classification
     * Features: extension, size, age, name patterns, location
     */
    suspend fun extractFeatures(file: File): FloatArray
    
    /**
     * Release model resources
     */
    suspend fun releaseModel()
}
