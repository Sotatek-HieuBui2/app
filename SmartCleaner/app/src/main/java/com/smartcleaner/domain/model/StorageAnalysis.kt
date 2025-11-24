package com.smartcleaner.domain.model

/**
 * Storage analysis result with TreeMap visualization data
 */
data class StorageAnalysis(
    val totalSize: Long,
    val usedSize: Long,
    val freeSize: Long,
    val nodes: List<StorageNode>,
    val fileTypeBreakdown: Map<FileCategory, FileTypeStats>,
    val largestFiles: List<LargeFile>,
    val analysisDurationMs: Long
)

/**
 * Node in the storage tree (for TreeMap visualization)
 */
data class StorageNode(
    val path: String,
    val name: String,
    val size: Long,
    val percentage: Float,
    val depth: Int,
    val children: List<StorageNode> = emptyList(),
    val fileCount: Int = 0,
    val category: FileCategory = FileCategory.OTHER
)

/**
 * File category for color coding
 */
enum class FileCategory(val color: Long) {
    IMAGES(0xFFE91E63),      // Pink
    VIDEOS(0xFFFF5722),      // Orange
    AUDIO(0xFF9C27B0),       // Purple
    DOCUMENTS(0xFF2196F3),   // Blue
    APPS(0xFF4CAF50),        // Green
    SYSTEM(0xFF607D8B),      // Gray
    DOWNLOADS(0xFFFF9800),   // Amber
    CACHE(0xFF795548),       // Brown
    OTHER(0xFF9E9E9E);       // Light Gray
    
    companion object {
        fun fromExtension(extension: String): FileCategory {
            return when (extension.lowercase()) {
                in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg") -> IMAGES
                in setOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "3gp") -> VIDEOS
                in setOf("mp3", "wav", "flac", "aac", "ogg", "m4a") -> AUDIO
                in setOf("pdf", "doc", "docx", "txt", "xlsx", "pptx", "csv") -> DOCUMENTS
                in setOf("apk", "xapk") -> APPS
                in setOf("cache", "tmp", "temp", "log") -> CACHE
                else -> OTHER
            }
        }
    }
}

/**
 * File type statistics
 */
data class FileTypeStats(
    val category: FileCategory,
    val totalSize: Long,
    val fileCount: Int,
    val percentage: Float,
    val extensions: Map<String, Long>  // extension -> size
)

/**
 * Large file entry
 */
data class LargeFile(
    val path: String,
    val name: String,
    val size: Long,
    val extension: String,
    val category: FileCategory,
    val lastModified: Long,
    val lastAccessed: Long?
)

/**
 * Storage trend data point
 */
data class StorageTrendData(
    val timestamp: Long,
    val usedSize: Long,
    val freeSize: Long
)

/**
 * Storage analysis options
 */
data class StorageAnalysisOptions(
    val maxDepth: Int = 5,
    val minNodeSize: Long = 1024 * 1024,  // 1MB minimum
    val includeLargestFiles: Int = 50,     // Top 50 largest files
    val excludePaths: List<String> = emptyList()
)
