package com.smartcleaner.domain.usecase.emptyfolder

import com.smartcleaner.domain.model.EmptyFolderScanOptions
import com.smartcleaner.domain.model.EmptyFolderScanResult
import com.smartcleaner.domain.repository.EmptyFolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case: Scan for empty folders
 * 
 * Input: EmptyFolderScanOptions (optional)
 * Output: Flow<EmptyFolderScanProgress>
 * 
 * Process:
 * 1. Walk through storage directory tree
 * 2. Check each directory if it's empty (no files, no subdirs)
 * 3. Respect scan options (hidden folders, depth, exclude paths)
 * 4. Collect all empty folders
 * 5. Return sorted by depth (deepest first for safe deletion)
 */
class ScanEmptyFoldersUseCase @Inject constructor(
    private val repository: EmptyFolderRepository
) {
    suspend operator fun invoke(
        options: EmptyFolderScanOptions = EmptyFolderScanOptions()
    ): Flow<EmptyFolderScanProgress> = flow {
        emit(EmptyFolderScanProgress.Scanning(0))
        
        try {
            repository.scanEmptyFolders(options).collect { progress ->
                emit(EmptyFolderScanProgress.Scanning(progress))
            }
            
            val result = repository.getScanResults()
            emit(EmptyFolderScanProgress.Completed(result))
        } catch (e: Exception) {
            emit(EmptyFolderScanProgress.Error(e.message ?: "Scan failed"))
        }
    }
}

sealed class EmptyFolderScanProgress {
    data class Scanning(val progress: Int) : EmptyFolderScanProgress()
    data class Completed(val result: EmptyFolderScanResult) : EmptyFolderScanProgress()
    data class Error(val message: String) : EmptyFolderScanProgress()
}
