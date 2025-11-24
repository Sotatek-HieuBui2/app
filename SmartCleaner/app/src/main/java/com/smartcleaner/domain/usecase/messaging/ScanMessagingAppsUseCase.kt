package com.smartcleaner.domain.usecase.messaging

import com.smartcleaner.domain.model.MessagingScanOptions
import com.smartcleaner.domain.model.MessagingScanResult
import com.smartcleaner.domain.repository.MessagingCleanerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case: Scan messaging apps for media
 */
class ScanMessagingAppsUseCase @Inject constructor(
    private val repository: MessagingCleanerRepository
) {
    suspend operator fun invoke(
        options: MessagingScanOptions = MessagingScanOptions()
    ): Flow<MessagingProgress> = flow {
        emit(MessagingProgress.CheckingApps)
        
        val installedApps = repository.getInstalledApps()
        
        if (installedApps.isEmpty()) {
            emit(MessagingProgress.Error("No messaging apps found"))
            return@flow
        }
        
        emit(MessagingProgress.Scanning(0, installedApps.size))
        
        try {
            repository.scanMessagingApps(options).collect { progress ->
                emit(MessagingProgress.Scanning(progress, installedApps.size))
            }
            
            val result = repository.getScanResults()
            emit(MessagingProgress.Completed(result))
        } catch (e: Exception) {
            emit(MessagingProgress.Error(e.message ?: "Scan failed"))
        }
    }
}

sealed class MessagingProgress {
    object CheckingApps : MessagingProgress()
    data class Scanning(val progress: Int, val appsCount: Int) : MessagingProgress()
    data class Completed(val result: MessagingScanResult) : MessagingProgress()
    data class Error(val message: String) : MessagingProgress()
}
