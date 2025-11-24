package com.smartcleaner.data.repository

import android.content.Context
import com.smartcleaner.data.ml.JunkClassifier
import com.smartcleaner.domain.model.*
import com.smartcleaner.domain.repository.JunkClassifierRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JunkClassifierRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifier: JunkClassifier
) : JunkClassifierRepository {

    private var modelInfo: ModelInfo? = null
    private var cachedResult: ClassificationResult? = null
    private var isInitialized = false

    override suspend fun initializeModel(): Result<ModelInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val initResult = classifier.initialize()
                
                if (initResult.isSuccess) {
                    isInitialized = true
                    modelInfo = ModelInfo(
                        version = "1.0.0",
                        accuracyScore = 0.87f,
                        trainingDate = "2025-01-15",
                        supportedCategories = JunkCategory.values().toList()
                    )
                    Result.success(modelInfo!!)
                } else {
                    Result.failure(initResult.exceptionOrNull() 
                        ?: Exception("Failed to initialize model"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun isModelReady(): Boolean {
        return isInitialized
    }

    override suspend fun classifyFile(file: File): JunkClassification? {
        return withContext(Dispatchers.IO) {
            try {
                if (!isInitialized) {
                    initializeModel()
                }
                
                val features = classifier.extractFeatures(file)
                val (category, confidence) = classifier.classify(features)
                
                val isJunk = category in listOf(
                    JunkCategory.TEMP_FILE,
                    JunkCategory.CACHE_FILE,
                    JunkCategory.LOG_FILE
                )
                
                val recommendations = generateRecommendations(category, confidence, file)
                
                JunkClassification(
                    filePath = file.absolutePath,
                    predictedCategory = category,
                    confidence = confidence,
                    isJunk = isJunk,
                    recommendations = recommendations
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun classifyFiles(files: List<File>): Flow<Int> = flow {
        withContext(Dispatchers.IO) {
            if (!isInitialized) {
                initializeModel()
            }
            
            emit(0)
            
            val startTime = System.currentTimeMillis()
            val classifications = mutableListOf<JunkClassification>()
            
            files.forEachIndexed { index, file ->
                try {
                    val features = classifier.extractFeatures(file)
                    val (category, confidence) = classifier.classify(features)
                    
                    val isJunk = category in listOf(
                        JunkCategory.TEMP_FILE,
                        JunkCategory.CACHE_FILE,
                        JunkCategory.LOG_FILE
                    )
                    
                    val recommendations = generateRecommendations(category, confidence, file)
                    
                    classifications.add(
                        JunkClassification(
                            filePath = file.absolutePath,
                            predictedCategory = category,
                            confidence = confidence,
                            isJunk = isJunk,
                            recommendations = recommendations
                        )
                    )
                } catch (e: Exception) {
                    // Skip failed classifications
                }
                
                val progress = ((index + 1) * 100 / files.size)
                if (progress % 5 == 0 || index == files.size - 1) {
                    emit(progress)
                }
            }
            
            val processingTime = System.currentTimeMillis() - startTime
            
            // Calculate summary
            val categoryBreakdown = classifications.groupingBy { it.predictedCategory }.eachCount()
            val safeCount = classifications.count { 
                it.predictedCategory in listOf(
                    JunkCategory.SAFE_DOCUMENT,
                    JunkCategory.SAFE_MEDIA,
                    JunkCategory.SAFE_APP_DATA
                )
            }
            val junkCount = classifications.count { it.isJunk }
            val reviewCount = classifications.size - safeCount - junkCount
            
            val totalJunkSize = files.zip(classifications)
                .filter { (_, classification) -> classification.isJunk }
                .sumOf { (file, _) -> file.length() }
            
            cachedResult = ClassificationResult(
                totalFiles = files.size,
                classifications = classifications,
                summary = ClassificationSummary(
                    safeFiles = safeCount,
                    junkFiles = junkCount,
                    reviewFiles = reviewCount,
                    totalJunkSize = totalJunkSize,
                    categoryBreakdown = categoryBreakdown
                ),
                processingTimeMs = processingTime
            )
            
            emit(100)
        }
    }

    override suspend fun getClassificationResults(): ClassificationResult {
        return cachedResult ?: ClassificationResult(
            totalFiles = 0,
            classifications = emptyList(),
            summary = ClassificationSummary(
                safeFiles = 0,
                junkFiles = 0,
                reviewFiles = 0,
                totalJunkSize = 0,
                categoryBreakdown = emptyMap()
            ),
            processingTimeMs = 0
        )
    }

    override suspend fun getModelInfo(): ModelInfo? {
        return modelInfo
    }

    override suspend fun extractFeatures(file: File): FloatArray {
        return withContext(Dispatchers.IO) {
            classifier.extractFeatures(file)
        }
    }

    override suspend fun releaseModel() {
        withContext(Dispatchers.IO) {
            classifier.release()
            isInitialized = false
        }
    }

    // Private helper methods
    
    private fun generateRecommendations(
        category: JunkCategory,
        confidence: Float,
        file: File
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        when (category) {
            JunkCategory.SAFE_DOCUMENT -> {
                recommendations.add("This appears to be an important document")
                if (confidence < 0.7f) {
                    recommendations.add("Consider backing up to cloud storage")
                }
            }
            JunkCategory.SAFE_MEDIA -> {
                recommendations.add("Keep this media file")
                if (file.length() > 50 * 1024 * 1024) {
                    recommendations.add("Large file - consider compressing or moving to external storage")
                }
            }
            JunkCategory.SAFE_APP_DATA -> {
                recommendations.add("Required by an application")
                recommendations.add("Do not delete unless uninstalling the app")
            }
            JunkCategory.TEMP_FILE -> {
                recommendations.add("Safe to delete - temporary file")
                recommendations.add("Can free up ${formatSize(file.length())}")
            }
            JunkCategory.CACHE_FILE -> {
                recommendations.add("Safe to delete - cache data")
                recommendations.add("Will be regenerated if needed")
            }
            JunkCategory.LOG_FILE -> {
                recommendations.add("Safe to delete - log file")
                recommendations.add("Only needed for debugging")
            }
            JunkCategory.DUPLICATE -> {
                recommendations.add("Possible duplicate file")
                recommendations.add("Verify content before deleting")
            }
            JunkCategory.LARGE_UNUSED -> {
                recommendations.add("Large file not recently accessed")
                recommendations.add("Consider moving to external storage or cloud")
            }
            JunkCategory.BACKUP_FILE -> {
                recommendations.add("Old backup file")
                if (confidence > 0.8f) {
                    recommendations.add("Likely safe to delete if newer backups exist")
                } else {
                    recommendations.add("Verify before deletion")
                }
            }
            JunkCategory.UNKNOWN -> {
                recommendations.add("Unable to classify with confidence")
                recommendations.add("Manual review recommended")
                if (confidence < 0.5f) {
                    recommendations.add("Low confidence - keep if unsure")
                }
            }
        }
        
        return recommendations
    }
    
    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
