package com.smartcleaner.presentation.duplicate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.DuplicateGroup
import com.smartcleaner.domain.model.DuplicateScanOptions
import com.smartcleaner.domain.usecase.duplicate.DeleteDuplicatesUseCase
import com.smartcleaner.domain.usecase.duplicate.FindDuplicatesUseCase
import com.smartcleaner.domain.usecase.duplicate.ScanProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DuplicateViewModel @Inject constructor(
    private val findDuplicatesUseCase: FindDuplicatesUseCase,
    private val deleteDuplicatesUseCase: DeleteDuplicatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DuplicateUiState>(DuplicateUiState.Idle)
    val uiState: StateFlow<DuplicateUiState> = _uiState.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Set<String>>(emptySet())
    val selectedFiles: StateFlow<Set<String>> = _selectedFiles.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    fun scanDuplicates() {
        scanForDuplicates()
    }

    fun scanForDuplicates(includeImages: Boolean = true, similarityThreshold: Float = 0.95f) {
        viewModelScope.launch {
            _uiState.value = DuplicateUiState.Scanning
            try {
                val directories = listOf(File("/storage/emulated/0"))
                val options = DuplicateScanOptions(
                    scanImages = includeImages,
                    imageSimilarityThreshold = similarityThreshold
                )
                
                findDuplicatesUseCase(directories, options).collect { progress ->
                    when (progress) {
                        is ScanProgress.Initializing -> {
                            _scanProgress.value = 0f
                        }
                        is ScanProgress.Scanning -> {
                            _scanProgress.value = progress.progress / 100f
                        }
                        is ScanProgress.Completed -> {
                            _uiState.value = DuplicateUiState.Success(progress.result.groups)
                            _scanProgress.value = 1f
                        }
                        is ScanProgress.Error -> {
                            _uiState.value = DuplicateUiState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DuplicateUiState.Error(e.message ?: "Scan failed")
            }
        }
    }

    fun deleteDuplicates() {
        val state = _uiState.value
        if (state !is DuplicateUiState.Success) return
        
        val filePaths = _selectedFiles.value.toList()
        if (filePaths.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = DuplicateUiState.Deleting
            try {
                val currentDuplicates = (_uiState.value as? DuplicateUiState.Success)?.duplicates ?: emptyList()
                var deletedCount = 0
                
                currentDuplicates.forEach { group ->
                    val filesToDeleteInGroup = group.files.filter { it.filePath in filePaths }
                    if (filesToDeleteInGroup.isNotEmpty()) {
                         val result = deleteDuplicatesUseCase(group.groupId, filesToDeleteInGroup.map { it.filePath })
                         if (result.isSuccess) {
                             deletedCount += result.getOrDefault(0)
                         }
                    }
                }
                
                // Rescan after deletion
                _selectedFiles.value = emptySet()
                scanDuplicates()
                
            } catch (e: Exception) {
                _uiState.value = DuplicateUiState.Error(e.message ?: "Delete failed")
            }
        }
    }

    fun toggleFileSelection(filePath: String) {
        val current = _selectedFiles.value.toMutableSet()
        if (current.contains(filePath)) {
            current.remove(filePath)
        } else {
            current.add(filePath)
        }
        _selectedFiles.value = current
    }

    fun selectGroupKeepFirst(group: DuplicateGroup) {
        val current = _selectedFiles.value.toMutableSet()
        // Keep first file, select rest for deletion
        group.files.drop(1).forEach { duplicateFile ->
            current.add(duplicateFile.filePath)
        }
        _selectedFiles.value = current
    }

    fun selectGroupKeepLargest(group: DuplicateGroup) {
        val current = _selectedFiles.value.toMutableSet()
        val largest = group.files.maxByOrNull { it.size }
        group.files.forEach { duplicateFile ->
            if (duplicateFile != largest) {
                current.add(duplicateFile.filePath)
            }
        }
        _selectedFiles.value = current
    }

    fun clearSelection() {
        _selectedFiles.value = emptySet()
    }

    fun getStatistics(): DuplicateStatistics? {
        val state = _uiState.value
        return if (state is DuplicateUiState.Success) {
            val totalGroups = state.duplicates.size
            val totalFiles = state.duplicates.sumOf { it.files.size }
            val wastedSpace = state.duplicates.sumOf { it.wastedSpace }
            val selectedSize = _selectedFiles.value.sumOf { path ->
                state.duplicates
                    .flatMap { it.files }
                    .find { it.filePath == path }
                    ?.size ?: 0L
            }

            DuplicateStatistics(
                totalGroups = totalGroups,
                totalFiles = totalFiles,
                wastedSpace = wastedSpace,
                selectedFiles = _selectedFiles.value.size,
                selectedSize = selectedSize
            )
        } else {
            null
        }
    }
}

sealed class DuplicateUiState {
    object Idle : DuplicateUiState()
    object Scanning : DuplicateUiState()
    object Deleting : DuplicateUiState()
    data class Success(val duplicates: List<DuplicateGroup>) : DuplicateUiState()
    data class Error(val message: String) : DuplicateUiState()
}

data class DuplicateStatistics(
    val totalGroups: Int,
    val totalFiles: Int,
    val wastedSpace: Long,
    val selectedFiles: Int,
    val selectedSize: Long
)
