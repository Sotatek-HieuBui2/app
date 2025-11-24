package com.smartcleaner.domain.model

/**
 * Represents an empty folder
 */
data class EmptyFolder(
    val path: String,
    val name: String,
    val parentPath: String,
    val lastModified: Long,
    val depth: Int // Depth in folder hierarchy
)

/**
 * Result of empty folder scan
 */
data class EmptyFolderScanResult(
    val folders: List<EmptyFolder>,
    val totalCount: Int,
    val scanDurationMs: Long
)

/**
 * Options for scanning empty folders
 */
data class EmptyFolderScanOptions(
    val includeHiddenFolders: Boolean = false,
    val minDepth: Int = 0,
    val maxDepth: Int = 10,
    val excludePaths: List<String> = emptyList() // Paths to exclude from scan
)
