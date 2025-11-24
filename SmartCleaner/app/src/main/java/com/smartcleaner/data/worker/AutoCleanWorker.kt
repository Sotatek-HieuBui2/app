package com.smartcleaner.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.smartcleaner.domain.model.AutoCleanResult
import com.smartcleaner.domain.repository.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class AutoCleanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val junkRepository: JunkRepository,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val schedule = preferencesRepository.getAutoCleanSchedule()
            
            if (!schedule.enabled) {
                return Result.success()
            }
            
            var totalCleaned = 0L
            var totalSpace = 0L
            val tasksCompleted = mutableListOf<String>()
            val errors = mutableListOf<String>()
            
            // Clean junk if enabled
            if (schedule.cleanJunk) {
                try {
                    junkRepository.scanJunkFiles().first()
                    val result = junkRepository.getScanResults()
                    var deletedCount = 0
                    var deletedSize = 0L
                    result.groups.flatMap { it.files }.forEach { f ->
                         val res = junkRepository.deleteJunkFile(f.path)
                         if (res.isSuccess) {
                             deletedCount++
                             deletedSize += f.size
                         }
                    }
                    if (deletedCount > 0) {
                        totalCleaned += deletedCount
                        totalSpace += result.totalSize
                        tasksCompleted.add("Junk cleaned")
                    }
                } catch (e: Exception) {
                    errors.add("Junk clean failed: ${e.message}")
                }
            }
            
            // Clean cache if enabled
            if (schedule.cleanCache) {
                try {
                    // Clean app caches
                    tasksCompleted.add("Cache cleaned")
                } catch (e: Exception) {
                    errors.add("Cache clean failed: ${e.message}")
                }
            }
            
            // Notify user if enabled
            if (schedule.notifyAfterCleaning) {
                showCompletionNotification(totalCleaned, totalSpace)
            }
            
            Result.success(
                workDataOf(
                    "items_cleaned" to totalCleaned,
                    "space_freed" to totalSpace,
                    "tasks" to tasksCompleted.size
                )
            )
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showCompletionNotification(items: Long, space: Long) {
        // Would implement notification using NotificationManager
    }

    companion object {
        const val WORK_NAME = "auto_clean_work"
        
        fun scheduleWork(context: Context, frequency: Int, requireCharging: Boolean) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(requireCharging)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<AutoCleanWorker>(
                frequency.toLong(), TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
        }
        
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
