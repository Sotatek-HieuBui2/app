package com.smartcleaner.domain.usecase.messaging

import com.smartcleaner.domain.repository.MessagingCleanerRepository
import javax.inject.Inject

/**
 * Use case: Delete messaging media files
 */
class DeleteMessagingMediaUseCase @Inject constructor(
    private val repository: MessagingCleanerRepository
) {
    suspend operator fun invoke(filePaths: List<String>): Result<Int> {
        if (filePaths.isEmpty()) {
            return Result.failure(IllegalArgumentException("No files specified"))
        }
        
        return repository.deleteMedia(filePaths)
    }
}
