package com.smartcleaner.domain.model

import androidx.work.Constraints
import androidx.work.NetworkType

/**
 * Auto cleaning schedule configuration
 */
data class AutoCleanSchedule(
    val enabled: Boolean = false,
    val frequency: CleanFrequency = CleanFrequency.WEEKLY,
    val timeOfDay: Int = 2, // 2 AM default
    val cleanJunk: Boolean = true,
    val cleanCache: Boolean = true,
    val cleanDuplicates: Boolean = false,
    val cleanMessaging: Boolean = false,
    val requireCharging: Boolean = true,
    val requireWifi: Boolean = false,
    val notifyBeforeCleaning: Boolean = true,
    val notifyAfterCleaning: Boolean = true
)

enum class CleanFrequency(val days: Int) {
    DAILY(1),
    EVERY_3_DAYS(3),
    WEEKLY(7),
    BIWEEKLY(14),
    MONTHLY(30)
}

/**
 * Auto clean work result
 */
data class AutoCleanResult(
    val timestamp: Long,
    val itemsCleaned: Int,
    val spaceFreed: Long,
    val tasksCompleted: List<String>,
    val errors: List<String>
)
