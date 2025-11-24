package com.smartcleaner.domain.usecase.leftover

import com.smartcleaner.domain.model.LeftoverGroup
import com.smartcleaner.domain.repository.LeftoverRepository
import javax.inject.Inject

/**
 * Use case: Delete leftover files for a specific app
 * 
 * Input: LeftoverGroup (containing packageName and files list)
 * Output: Result<DeleteResult>
 * 
 * Process:
 * 1. Optional: Backup files before deletion
 * 2. Delete all files/folders in the group
 * 3. Return count of deleted files
 */
class DeleteLeftoverFilesUseCase @Inject constructor(
    private val repository: LeftoverRepository
) {
    suspend operator fun invoke(
        group: LeftoverGroup,
        backup: Boolean = true
    ): Result<DeleteResult> {
        return try {
            var backupPath: String? = null
            
            // Backup if requested
            if (backup) {
                val backupResult = repository.backupBeforeDelete(group)
                if (backupResult.isSuccess) {
                    backupPath = backupResult.getOrNull()
                }
            }
            
            // Delete files
            val deleteResult = repository.deleteLeftoverFiles(group.packageName)
            
            if (deleteResult.isSuccess) {
                val deletedCount = deleteResult.getOrThrow()
                Result.success(
                    DeleteResult(
                        deletedFiles = deletedCount,
                        freedSpace = group.totalSize,
                        backupPath = backupPath
                    )
                )
            } else {
                Result.failure(deleteResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class DeleteResult(
    val deletedFiles: Int,
    val freedSpace: Long,
    val backupPath: String?
)
