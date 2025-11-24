package com.smartcleaner.domain.usecase.root

import com.smartcleaner.domain.model.RootOperationResult
import com.smartcleaner.domain.repository.RootRepository
import javax.inject.Inject

/**
 * Use case: Clean system cache with root
 */
class CleanSystemCacheUseCase @Inject constructor(
    private val repository: RootRepository
) {
    suspend operator fun invoke(): RootOperationResult {
        // Clean both system cache and dalvik cache
        val systemResult = repository.cleanSystemCache()
        val dalvikResult = repository.cleanDalvikCache()
        
        return RootOperationResult(
            success = systemResult.success && dalvikResult.success,
            message = "System cache: ${systemResult.message}\nDalvik cache: ${dalvikResult.message}",
            output = "${systemResult.output}\n${dalvikResult.output}"
        )
    }
}
