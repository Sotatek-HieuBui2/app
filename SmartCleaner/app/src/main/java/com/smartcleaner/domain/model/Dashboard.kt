package com.smartcleaner.domain.model

/**
 * Dashboard overview data
 */
data class DashboardData(
    val storageInfo: StorageInfo,
    val quickStats: QuickStats,
    val recommendations: List<Recommendation>,
    val recentActivity: List<CleaningActivity>
)

/**
 * Storage information for dashboard
 */
data class StorageInfo(
    val totalSpace: Long,
    val usedSpace: Long,
    val freeSpace: Long,
    val usagePercentage: Float
)

/**
 * Quick statistics
 */
data class QuickStats(
    val junkFileSize: Long,
    val duplicateFileSize: Long,
    val unusedAppsCount: Int,
    val messagingMediaSize: Long,
    val lastCleanDate: Long?
)

/**
 * Cleaning recommendation
 */
data class Recommendation(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val potentialSaving: Long,
    val priority: Priority
)

enum class RecommendationType {
    CLEAN_JUNK,
    DELETE_DUPLICATES,
    UNINSTALL_APPS,
    CLEAN_MESSAGING,
    CLEAR_CACHE,
    EMPTY_FOLDERS
}

enum class Priority {
    HIGH, MEDIUM, LOW
}

/**
 * Cleaning activity history
 */
data class CleaningActivity(
    val id: Long,
    val timestamp: Long,
    val type: CleaningType,
    val itemsCleaned: Int,
    val spaceFreed: Long
)

enum class CleaningType {
    JUNK_CLEANED,
    DUPLICATES_DELETED,
    APP_UNINSTALLED,
    MESSAGING_CLEANED,
    FOLDERS_CLEANED,
    MANUAL_CLEAN
}
