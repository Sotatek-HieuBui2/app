package com.smartcleaner.domain.model

/**
 * Root access status
 */
enum class RootStatus {
    NOT_ROOTED,
    ROOTED_GRANTED,
    ROOTED_DENIED,
    CHECKING
}

/**
 * Root operation result
 */
data class RootOperationResult(
    val success: Boolean,
    val message: String,
    val output: String? = null
)

/**
 * System partition info (requires root)
 */
data class SystemPartitionInfo(
    val path: String,
    val totalSize: Long,
    val usedSize: Long,
    val freeSize: Long,
    val percentage: Float
)

/**
 * System app info
 */
data class SystemApp(
    val packageName: String,
    val appName: String,
    val size: Long,
    val canBeDisabled: Boolean,
    val isBloatware: Boolean
)
