package com.smartcleaner.domain.model

/**
 * App preferences and settings
 */
data class AppPreferences(
    // Theme
    val theme: AppTheme = AppTheme.SYSTEM,
    val useDynamicColors: Boolean = true,
    
    // Cleaning preferences
    val autoCleanSchedule: AutoCleanSchedule = AutoCleanSchedule(),
    val confirmBeforeDelete: Boolean = true,
    val backupBeforeClean: Boolean = false,
    
    // Notifications
    val notificationsEnabled: Boolean = true,
    val storageAlertThreshold: Int = 90, // Alert when storage > 90%
    val realTimeMonitoring: Boolean = false,
    
    // Privacy
    val analyticsEnabled: Boolean = false,
    val crashReportsEnabled: Boolean = true,
    
    // Advanced
    val rootModeEnabled: Boolean = false,
    val aggressiveCleaning: Boolean = false,
    val showHiddenFiles: Boolean = false
)

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Cloud backup configuration
 */
data class CloudBackupConfig(
    val enabled: Boolean = false,
    val provider: CloudProvider = CloudProvider.GOOGLE_DRIVE,
    val autoBackup: Boolean = false,
    val backupFrequency: CleanFrequency = CleanFrequency.WEEKLY,
    val includeSettings: Boolean = true,
    val includeCleaningHistory: Boolean = true,
    val lastBackupTime: Long? = null
)

enum class CloudProvider {
    GOOGLE_DRIVE,
    DROPBOX,
    ONE_DRIVE,
    LOCAL_ONLY
}
