package com.smartcleaner.domain.model

/**
 * Represents an unused or rarely used app
 */
data class UnusedApp(
    val packageName: String,
    val appName: String,
    val appIcon: ByteArray? = null,
    val lastUsedTime: Long,  // timestamp (0 if never used)
    val installedTime: Long,
    val totalSize: Long,     // app + data + cache size
    val cacheSize: Long,
    val dataSize: Long,
    val category: UnusedCategory,
    val daysSinceLastUse: Int,
    val isSystemApp: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UnusedApp
        return packageName == other.packageName
    }

    override fun hashCode(): Int = packageName.hashCode()
}

/**
 * Category of unused apps based on usage pattern
 */
enum class UnusedCategory {
    NEVER_USED,          // Never opened since installation
    NOT_USED_30_DAYS,    // Not used for 30-90 days
    NOT_USED_90_DAYS,    // Not used for 90+ days
    RARELY_USED          // Used but very infrequently
}

/**
 * Result of unused app analysis
 */
data class UnusedAppAnalysisResult(
    val apps: List<UnusedApp>,
    val totalSize: Long,
    val totalCount: Int,
    val breakdown: Map<UnusedCategory, UnusedCategoryStats>,
    val analysisDurationMs: Long
)

/**
 * Statistics for each unused category
 */
data class UnusedCategoryStats(
    val count: Int,
    val totalSize: Long
)

/**
 * Permission state for usage stats access
 */
enum class UsageStatsPermissionState {
    GRANTED,
    DENIED,
    NOT_REQUESTED
}
