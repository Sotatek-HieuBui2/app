package com.smartcleaner.domain.model

/**
 * Types of system junk files
 */
enum class JunkType {
    APP_CACHE,      // Application cache
    TEMP_FILES,     // .tmp, .temp files
    LOG_FILES,      // .log files
    BACKUP_FILES,   // .bak, .backup files
    APK_FILES,      // APK files in Download
    LARGE_FILES,    // Files > threshold (default 100MB)
    THUMBNAIL_CACHE // .thumbnails
}

/**
 * Represents a junk file
 */
data class JunkFile(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val type: JunkType,
    val packageName: String? = null, // For app cache
    val isSafe: Boolean = true // ML prediction
)

/**
 * Grouped junk files by type
 */
data class JunkGroup(
    val type: JunkType,
    val files: List<JunkFile>,
    val totalSize: Long,
    val fileCount: Int
)

/**
 * System junk scan result
 */
data class JunkScanResult(
    val groups: List<JunkGroup>,
    val totalSize: Long,
    val totalFiles: Int,
    val scanDurationMs: Long
)

/**
 * Cache info for an app
 */
data class AppCacheInfo(
    val packageName: String,
    val appName: String,
    val cacheSize: Long,
    val dataSize: Long,
    val codeSize: Long
)
