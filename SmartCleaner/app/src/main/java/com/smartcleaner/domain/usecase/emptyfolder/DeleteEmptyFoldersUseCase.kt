package com.smartcleaner.domain.usecase.emptyfolder

import com.smartcleaner.domain.repository.EmptyFolderRepository
import javax.inject.Inject

/**
 * Use case: Delete empty folders
 * 
 * Input: List of folder paths or delete all
 * Output: Result<DeleteResult>
 * 
 * Process:
 * 1. Delete folders from deepest to shallowest (safe order)
 * 2. Skip if folder is no longer empty (race condition)
 * 3. Return count of successfully deleted folders
 */
class DeleteEmptyFoldersUseCase @Inject constructor(
    private val repository: EmptyFolderRepository
) {
    suspend operator fun invoke(
        folderPaths: List<String>
    ): Result<DeleteResult> {
        return try {
            val result = repository.deleteEmptyFolders(folderPaths)
            
            if (result.isSuccess) {
                val deletedCount = result.getOrThrow()
                Result.success(
                    DeleteResult(
                        deletedCount = deletedCount,
                        failedCount = folderPaths.size - deletedCount
                    )
                )
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Delete failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAll(): Result<DeleteResult> {
        return try {
            val result = repository.deleteAllEmptyFolders()
            
            if (result.isSuccess) {
                val deletedCount = result.getOrThrow()
                Result.success(
                    DeleteResult(
                        deletedCount = deletedCount,
                        failedCount = 0
                    )
                )
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Delete failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class DeleteResult(
    val deletedCount: Int,
    val failedCount: Int
)
