package com.smartcleaner.domain.usecase.junk

import com.smartcleaner.domain.model.JunkScanResult
import com.smartcleaner.domain.repository.JunkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case: Scan for system junk files
 * 
 * Input: largeSizeThresholdMB (default 100MB)
 * Output: Flow<JunkScanProgress>
 * 
 * Process:
 * 1. Scan app cache (via StorageStatsManager on API 26+)
 * 2. Find temp files (.tmp, .temp) in common directories
 * 3. Find log files (.log)
 * 4. Find backup files (.bak, .backup)
 * 5. Find APK files in Download
 * 6. Find large files > threshold
 * 7. Find thumbnail cache
 * 8. Group by type and return
 */
class ScanJunkFilesUseCase @Inject constructor(
    private val repository: JunkRepository
) {
    suspend operator fun invoke(
        largeSizeThresholdMB: Int = 100
    ): Flow<JunkScanProgress> = flow {
        emit(JunkScanProgress.Scanning(0))
        
        repository.scanJunkFiles(largeSizeThresholdMB).collect { progress ->
            emit(JunkScanProgress.Scanning(progress))
        }
        
        val result = repository.getScanResults()
        emit(JunkScanProgress.Completed(result))
    }
}

sealed class JunkScanProgress {
    data class Scanning(val progress: Int) : JunkScanProgress()
    data class Completed(val result: JunkScanResult) : JunkScanProgress()
    data class Error(val message: String) : JunkScanProgress()
}
