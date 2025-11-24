package com.smartcleaner.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.smartcleaner.domain.model.*
import com.smartcleaner.domain.repository.DashboardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DashboardRepository {

    private val activities = mutableListOf<CleaningActivity>()

    override suspend fun getDashboardData(): DashboardData {
        return withContext(Dispatchers.IO) {
            val storageInfo = getStorageInfo()
            val quickStats = getQuickStats()
            val recommendations = generateRecommendations(storageInfo, quickStats)
            val recentActivity = activities.takeLast(5).reversed()
            
            DashboardData(
                storageInfo = storageInfo,
                quickStats = quickStats,
                recommendations = recommendations,
                recentActivity = recentActivity
            )
        }
    }

    override suspend fun recordActivity(activity: CleaningActivity) {
        activities.add(activity)
        // Keep only last 100 activities
        if (activities.size > 100) {
            activities.removeAt(0)
        }
    }

    override suspend fun getCleaningHistory(limit: Int): List<CleaningActivity> {
        return activities.takeLast(limit).reversed()
    }

    override suspend fun clearHistory() {
        activities.clear()
    }

    private fun getStorageInfo(): StorageInfo {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.absolutePath)
        
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        
        val totalSpace = totalBlocks * blockSize
        val freeSpace = availableBlocks * blockSize
        val usedSpace = totalSpace - freeSpace
        
        return StorageInfo(
            totalSpace = totalSpace,
            usedSpace = usedSpace,
            freeSpace = freeSpace,
            usagePercentage = (usedSpace.toFloat() / totalSpace) * 100
        )
    }

    private fun getQuickStats(): QuickStats {
        // Would aggregate from various repositories
        return QuickStats(
            junkFileSize = 0,
            duplicateFileSize = 0,
            unusedAppsCount = 0,
            messagingMediaSize = 0,
            lastCleanDate = activities.lastOrNull()?.timestamp
        )
    }

    private fun generateRecommendations(
        storageInfo: StorageInfo,
        stats: QuickStats
    ): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        if (storageInfo.usagePercentage > 90) {
            recommendations.add(
                Recommendation(
                    id = "storage_critical",
                    type = RecommendationType.CLEAN_JUNK,
                    title = "Storage critically low!",
                    description = "Free up space immediately",
                    potentialSaving = stats.junkFileSize,
                    priority = Priority.HIGH
                )
            )
        }
        
        if (stats.unusedAppsCount > 5) {
            recommendations.add(
                Recommendation(
                    id = "unused_apps",
                    type = RecommendationType.UNINSTALL_APPS,
                    title = "Uninstall unused apps",
                    description = "${stats.unusedAppsCount} apps haven't been used",
                    potentialSaving = 0,
                    priority = Priority.MEDIUM
                )
            )
        }
        
        return recommendations
    }
}
