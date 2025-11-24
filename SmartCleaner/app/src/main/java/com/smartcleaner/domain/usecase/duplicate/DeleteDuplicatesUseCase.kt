package com.smartcleaner.domain.usecase.duplicate

import com.smartcleaner.domain.repository.DuplicateFinderRepository
import javax.inject.Inject

/**
 * Use case: Delete duplicate files
 * 
 * Input: groupId, List<String> filePaths
 * Output: Result<Int> (number of files deleted)
 */
class DeleteDuplicatesUseCase @Inject constructor(
    private val repository: DuplicateFinderRepository
) {
    suspend operator fun invoke(groupId: String, filePaths: List<String>): Result<Int> {
        if (filePaths.isEmpty()) {
            return Result.failure(IllegalArgumentException("No files specified"))
        }
        
        // Ensure at least one file remains in the group
        val group = repository.getDuplicateGroup(groupId)
        if (group != null && filePaths.size >= group.files.size) {
            return Result.failure(IllegalStateException("Must keep at least one file"))
        }
        
        return repository.deleteFiles(groupId, filePaths)
    }
}
