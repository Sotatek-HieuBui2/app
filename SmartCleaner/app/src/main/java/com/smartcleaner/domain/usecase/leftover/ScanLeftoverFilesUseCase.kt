package com.smartcleaner.domain.usecase.leftover

import com.smartcleaner.domain.model.LeftoverScanResult
import com.smartcleaner.domain.repository.LeftoverRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: Scan for leftover files from uninstalled apps
 * 
 * Input: None
 * Output: Flow<ScanProgress> -> emits progress 0-100, then final result
 * 
 * Process:
 * 1. Get list of currently installed packages
 * 2. Scan common directories (Android/data, obb, Download, etc.)
 * 3. Check if folder/file belongs to uninstalled app
 * 4. Calculate size and collect metadata
 * 5. Group by package name
 */
class ScanLeftoverFilesUseCase @Inject constructor(
    private val repository: LeftoverRepository
) {
    suspend operator fun invoke(): Flow<ScanProgress> = kotlinx.coroutines.flow.flow {
        // Emit scanning started
        emit(ScanProgress.Scanning(0))
        
        // Start scanning and collect progress
        repository.scanLeftoverFiles().collect { progress ->
            emit(ScanProgress.Scanning(progress))
        }
        
        // Get final results
        val result = repository.getScanResults()
        emit(ScanProgress.Completed(result))
    }
}

sealed class ScanProgress {
    data class Scanning(val progress: Int) : ScanProgress()
    data class Completed(val result: LeftoverScanResult) : ScanProgress()
    data class Error(val message: String) : ScanProgress()
}
