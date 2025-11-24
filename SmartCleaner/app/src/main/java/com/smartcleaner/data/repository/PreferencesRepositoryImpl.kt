package com.smartcleaner.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.smartcleaner.domain.model.*
import com.smartcleaner.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    companion object {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val AUTO_CLEAN_ENABLED = booleanPreferencesKey("auto_clean_enabled")
        val CLEAN_FREQUENCY = intPreferencesKey("clean_frequency")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STORAGE_ALERT_THRESHOLD = intPreferencesKey("storage_threshold")
        val ROOT_MODE_ENABLED = booleanPreferencesKey("root_mode")
        val CLOUD_BACKUP_ENABLED = booleanPreferencesKey("cloud_backup")
        val CLOUD_PROVIDER = stringPreferencesKey("cloud_provider")
    }

    override fun getPreferences(): Flow<AppPreferences> {
        return context.dataStore.data.map { prefs ->
            AppPreferences(
                theme = AppTheme.valueOf(prefs[THEME] ?: AppTheme.SYSTEM.name),
                useDynamicColors = prefs[DYNAMIC_COLORS] ?: true,
                notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: true,
                storageAlertThreshold = prefs[STORAGE_ALERT_THRESHOLD] ?: 90,
                rootModeEnabled = prefs[ROOT_MODE_ENABLED] ?: false
            )
        }
    }

    override suspend fun updatePreferences(preferences: AppPreferences) {
        context.dataStore.edit { prefs ->
            prefs[THEME] = preferences.theme.name
            prefs[DYNAMIC_COLORS] = preferences.useDynamicColors
            prefs[NOTIFICATIONS_ENABLED] = preferences.notificationsEnabled
            prefs[STORAGE_ALERT_THRESHOLD] = preferences.storageAlertThreshold
            prefs[ROOT_MODE_ENABLED] = preferences.rootModeEnabled
        }
    }

    override suspend fun getAutoCleanSchedule(): AutoCleanSchedule {
        val prefs = context.dataStore.data.map { it }.first()
        return AutoCleanSchedule(
            enabled = prefs[AUTO_CLEAN_ENABLED] ?: false,
            frequency = CleanFrequency.values()[prefs[CLEAN_FREQUENCY] ?: 2]
        )
    }

    override suspend fun updateAutoCleanSchedule(schedule: AutoCleanSchedule) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_CLEAN_ENABLED] = schedule.enabled
            prefs[CLEAN_FREQUENCY] = schedule.frequency.ordinal
        }
    }

    override suspend fun getCloudBackupConfig(): CloudBackupConfig {
        val prefs = context.dataStore.data.map { it }.first()
        return CloudBackupConfig(
            enabled = prefs[CLOUD_BACKUP_ENABLED] ?: false,
            provider = CloudProvider.valueOf(prefs[CLOUD_PROVIDER] ?: CloudProvider.GOOGLE_DRIVE.name)
        )
    }

    override suspend fun updateCloudBackupConfig(config: CloudBackupConfig) {
        context.dataStore.edit { prefs ->
            prefs[CLOUD_BACKUP_ENABLED] = config.enabled
            prefs[CLOUD_PROVIDER] = config.provider.name
        }
    }

    private suspend fun <T> Flow<T>.first(): T {
        var result: T? = null
        collect { value ->
            result = value
            return@collect
        }
        return result!!
    }
}
