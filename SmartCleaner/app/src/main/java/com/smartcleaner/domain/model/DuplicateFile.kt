package com.smartcleaner.domain.model

/**
 * Represents a duplicate file entry
 */
data class DuplicateFile(
    val filePath: String,
    val fileName: String,
    val size: Long,
    val hash: String,
    val lastModified: Long,
    val groupId: String  // Hash or perceptual hash for grouping
)

/**
 * Group of duplicate files
 */
data class DuplicateGroup(
    val groupId: String,
    val files: List<DuplicateFile>,
    val duplicateType: DuplicateType,
    val totalSize: Long,
    val wastedSpace: Long,  // (count - 1) * size
    val similarity: Float = 1.0f  // 1.0 for exact match, <1.0 for similar images
)

/**
 * Type of duplicate detection
 */
enum class DuplicateType {
    EXACT_MATCH,        // Same MD5/SHA-256 hash
    SIMILAR_IMAGE,      // Similar perceptual hash (images only)
    SIMILAR_NAME,       // Same name, similar size
    SIMILAR_CONTENT     // Content-based similarity (future)
}

/**
 * Result of duplicate scan
 */
data class DuplicateScanResult(
    val groups: List<DuplicateGroup>,
    val totalDuplicates: Int,
    val totalWastedSpace: Long,
    val scanDurationMs: Long,
    val filesScanned: Int
)

/**
 * Scan options for duplicate finder
 */
data class DuplicateScanOptions(
    val scanImages: Boolean = true,
    val scanVideos: Boolean = true,
    val scanDocuments: Boolean = true,
    val scanAudio: Boolean = true,
    val minFileSize: Long = 1024, // Ignore files smaller than 1KB
    val maxFileSize: Long = Long.MAX_VALUE,
    val imageSimilarityThreshold: Float = 0.95f, // 95% similarity for images
    val usePerceptualHash: Boolean = true,
    val includePaths: List<String> = emptyList(),
    val excludePaths: List<String> = emptyList()
)

/**
 * Perceptual hash for image similarity
 */
data class ImageHash(
    val filePath: String,
    val pHash: String,  // Perceptual hash
    val width: Int,
    val height: Int,
    val size: Long
)
