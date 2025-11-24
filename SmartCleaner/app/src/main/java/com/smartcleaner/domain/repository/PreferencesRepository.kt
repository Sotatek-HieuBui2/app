package com.smartcleaner.domain.repository

import com.smartcleaner.domain.model.AppPreferences
import com.smartcleaner.domain.model.AutoCleanSchedule
import com.smartcleaner.domain.model.CloudBackupConfig
import kotlinx.coroutines.flow.Flow

/**
 * Repository for app preferences and settings
 */
interface PreferencesRepository {
    /**
     * Get app preferences as Flow
     */
    fun getPreferences(): Flow<AppPreferences>
    
    /**
     * Update app preferences
     */
    suspend fun updatePreferences(preferences: AppPreferences)
    
    /**
     * Get auto clean schedule
     */
    suspend fun getAutoCleanSchedule(): AutoCleanSchedule
    
    /**
     * Update auto clean schedule
     */
    suspend fun updateAutoCleanSchedule(schedule: AutoCleanSchedule)
    
    /**
     * Get cloud backup config
     */
    suspend fun getCloudBackupConfig(): CloudBackupConfig
    
    /**
     * Update cloud backup config
     */
    suspend fun updateCloudBackupConfig(config: CloudBackupConfig)
}
