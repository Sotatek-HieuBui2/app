package com.smartcleaner.domain.model

/**
 * ML-based classification result for a file
 */
data class JunkClassification(
    val filePath: String,
    val predictedCategory: JunkCategory,
    val confidence: Float,  // 0.0 to 1.0
    val isJunk: Boolean,
    val recommendations: List<String>
)

/**
 * Categories for ML classification
 */
enum class JunkCategory {
    SAFE_DOCUMENT,       // Keep - important documents
    SAFE_MEDIA,          // Keep - photos, videos
    SAFE_APP_DATA,       // Keep - app settings, databases
    TEMP_FILE,           // Delete - temporary files
    CACHE_FILE,          // Delete - cache data
    DUPLICATE,           // Review - potential duplicates
    LARGE_UNUSED,        // Review - large files not accessed recently
    LOG_FILE,            // Delete - log files
    BACKUP_FILE,         // Review - old backups
    UNKNOWN              // Manual review needed
}

/**
 * Batch classification result
 */
data class ClassificationResult(
    val totalFiles: Int,
    val classifications: List<JunkClassification>,
    val summary: ClassificationSummary,
    val processingTimeMs: Long
)

/**
 * Summary statistics for classification
 */
data class ClassificationSummary(
    val safeFiles: Int,
    val junkFiles: Int,
    val reviewFiles: Int,
    val totalJunkSize: Long,
    val categoryBreakdown: Map<JunkCategory, Int>
)

/**
 * Model metadata
 */
data class ModelInfo(
    val version: String,
    val accuracyScore: Float,
    val trainingDate: String,
    val supportedCategories: List<JunkCategory>
)
