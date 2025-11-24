package com.smartcleaner.domain.usecase.junk

import com.smartcleaner.domain.model.JunkType
import com.smartcleaner.domain.repository.JunkRepository
import javax.inject.Inject

/**
 * Use case: Delete junk files
 * 
 * Input: List<JunkType> or "all"
 * Output: Result<CleanResult>
 * 
 * Process:
 * 1. For each junk type selected:
 *    - APP_CACHE: Call clearAllAppCache()
 *    - Others: Delete files via deleteJunkByType()
 * 2. Return total files deleted and space freed
 */
class CleanJunkFilesUseCase @Inject constructor(
    private val repository: JunkRepository
) {
    suspend operator fun invoke(
        types: List<JunkType>,
        clearAppCache: Boolean = true
    ): Result<CleanResult> {
        return try {
            var totalDeleted = 0
            var totalFreed = 0L
            val results = mutableMapOf<JunkType, Pair<Int, Long>>()
            
            types.forEach { type ->
                if (type == JunkType.APP_CACHE && clearAppCache) {
                    val cacheResult = repository.clearAllAppCache()
                    if (cacheResult.isSuccess) {
                        val freed = cacheResult.getOrThrow()
                        totalFreed += freed
                        results[type] = Pair(0, freed) // Unknown file count
                    }
                } else {
                    val deleteResult = repository.deleteJunkByType(type)
                    if (deleteResult.isSuccess) {
                        val (deleted, freed) = deleteResult.getOrThrow()
                        totalDeleted += deleted
                        totalFreed += freed
                        results[type] = Pair(deleted, freed)
                    }
                }
            }
            
            Result.success(
                CleanResult(
                    totalDeleted = totalDeleted,
                    totalFreed = totalFreed,
                    resultsByType = results
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class CleanResult(
    val totalDeleted: Int,
    val totalFreed: Long,
    val resultsByType: Map<JunkType, Pair<Int, Long>>
)
