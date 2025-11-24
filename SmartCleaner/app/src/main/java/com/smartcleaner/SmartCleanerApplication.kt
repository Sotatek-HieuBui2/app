package com.smartcleaner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmartCleanerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_CLEANING,
                    "Cleaning Progress",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows progress when cleaning files"
                },
                NotificationChannel(
                    CHANNEL_LEFTOVER,
                    "Leftover Files Detected",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifies when leftover files are found after app uninstall"
                },
                NotificationChannel(
                    CHANNEL_AUTO_CLEAN,
                    "Auto Clean Complete",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifies when scheduled cleaning is complete"
                }
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    companion object {
        const val CHANNEL_CLEANING = "cleaning_progress"
        const val CHANNEL_LEFTOVER = "leftover_detected"
        const val CHANNEL_AUTO_CLEAN = "auto_clean_complete"
    }
}
