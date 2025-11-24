package com.smartcleaner.domain.model

/**
 * Represents a leftover file or directory from an uninstalled app
 */
data class LeftoverFile(
    val path: String,
    val packageName: String,
    val appName: String,
    val size: Long,
    val lastModified: Long,
    val type: LeftoverType,
    val isDirectory: Boolean,
    val childrenCount: Int = 0,
    val previewPath: String? = null // For image/video preview
)

enum class LeftoverType {
    DATA,      // /Android/data/
    OBB,       // /Android/obb/
    MEDIA,     // /Android/media/
    DOWNLOAD,  // /Download/
    PICTURES,  // /Pictures/
    DCIM,      // /DCIM/
    DOCUMENTS, // /Documents/
    OTHER      // Custom app folders
}

/**
 * Grouped leftover files by app
 */
data class LeftoverGroup(
    val packageName: String,
    val appName: String,
    val totalSize: Long,
    val fileCount: Int,
    val files: List<LeftoverFile>,
    val appIcon: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LeftoverGroup
        return packageName == other.packageName
    }

    override fun hashCode(): Int = packageName.hashCode()
}

/**
 * Scan result
 */
data class LeftoverScanResult(
    val groups: List<LeftoverGroup>,
    val totalSize: Long,
    val totalFiles: Int,
    val scanDurationMs: Long
)
