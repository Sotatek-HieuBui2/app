package com.smartcleaner.domain.usecase.duplicate

import com.smartcleaner.domain.model.DuplicateScanOptions
import com.smartcleaner.domain.model.DuplicateScanResult
import com.smartcleaner.domain.repository.DuplicateFinderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

/**
 * Use case: Find duplicate files
 * 
 * Input: List<File> directories, DuplicateScanOptions
 * Output: Flow<ScanProgress>
 * 
 * Process:
 * 1. Traverse directories recursively
 * 2. Filter by file type and size
 * 3. Calculate MD5/SHA-256 for exact matches
 * 4. Calculate perceptual hash for images
 * 5. Group duplicates
 * 6. Sort by wasted space
 */
class FindDuplicatesUseCase @Inject constructor(
    private val repository: DuplicateFinderRepository
) {
    suspend operator fun invoke(
        directories: List<File>,
        options: DuplicateScanOptions = DuplicateScanOptions()
    ): Flow<ScanProgress> = flow {
        emit(ScanProgress.Initializing)
        
        if (directories.isEmpty()) {
            emit(ScanProgress.Error("No directories to scan"))
            return@flow
        }
        
        emit(ScanProgress.Scanning(0))
        
        try {
            repository.scanForDuplicates(directories, options).collect { progress ->
                emit(ScanProgress.Scanning(progress))
            }
            
            val result = repository.getScanResults()
            emit(ScanProgress.Completed(result))
        } catch (e: Exception) {
            emit(ScanProgress.Error(e.message ?: "Scan failed"))
        }
    }
}

sealed class ScanProgress {
    object Initializing : ScanProgress()
    data class Scanning(val progress: Int) : ScanProgress()
    data class Completed(val result: DuplicateScanResult) : ScanProgress()
    data class Error(val message: String) : ScanProgress()
}
