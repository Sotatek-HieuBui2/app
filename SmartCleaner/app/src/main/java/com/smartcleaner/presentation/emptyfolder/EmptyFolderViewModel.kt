package com.smartcleaner.presentation.emptyfolder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.EmptyFolder
import com.smartcleaner.domain.model.EmptyFolderScanOptions
import com.smartcleaner.domain.usecase.emptyfolder.DeleteEmptyFoldersUseCase
import com.smartcleaner.domain.usecase.emptyfolder.EmptyFolderScanProgress
import com.smartcleaner.domain.usecase.emptyfolder.ScanEmptyFoldersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmptyFolderViewModel @Inject constructor(
    private val scanEmptyFoldersUseCase: ScanEmptyFoldersUseCase,
    private val deleteEmptyFoldersUseCase: DeleteEmptyFoldersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmptyFolderUiState>(EmptyFolderUiState.Idle)
    val uiState: StateFlow<EmptyFolderUiState> = _uiState.asStateFlow()

    private val _selectedFolders = MutableStateFlow<Set<String>>(emptySet())
    val selectedFolders: StateFlow<Set<String>> = _selectedFolders.asStateFlow()

    fun startScan(options: EmptyFolderScanOptions = EmptyFolderScanOptions()) {
        viewModelScope.launch {
            _uiState.value = EmptyFolderUiState.Scanning(0)
            _selectedFolders.value = emptySet()
            
            try {
                scanEmptyFoldersUseCase(options).collect { progress ->
                    when (progress) {
                        is EmptyFolderScanProgress.Scanning -> {
                            _uiState.value = EmptyFolderUiState.Scanning(progress.progress)
                        }
                        is EmptyFolderScanProgress.Completed -> {
                            _uiState.value = EmptyFolderUiState.Success(
                                folders = progress.result.folders,
                                totalCount = progress.result.totalCount,
                                scanDuration = progress.result.scanDurationMs
                            )
                        }
                        is EmptyFolderScanProgress.Error -> {
                            _uiState.value = EmptyFolderUiState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = EmptyFolderUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFolderSelection(folderPath: String) {
        _selectedFolders.update { current ->
            if (folderPath in current) {
                current - folderPath
            } else {
                current + folderPath
            }
        }
    }

    fun selectAll() {
        val currentState = _uiState.value
        if (currentState is EmptyFolderUiState.Success) {
            _selectedFolders.value = currentState.folders.map { it.path }.toSet()
        }
    }

    fun deselectAll() {
        _selectedFolders.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is EmptyFolderUiState.Success) {
                _uiState.update { 
                    (it as EmptyFolderUiState.Success).copy(isDeleting = true) 
                }
                
                val selectedPaths = _selectedFolders.value.toList()
                val result = deleteEmptyFoldersUseCase(selectedPaths)
                
                if (result.isSuccess) {
                    val deleteResult = result.getOrNull()!!
                    
                    // Remove deleted folders from list
                    _uiState.update { state ->
                        if (state is EmptyFolderUiState.Success) {
                            val remainingFolders = state.folders.filter { 
                                it.path !in selectedPaths 
                            }
                            state.copy(
                                folders = remainingFolders,
                                totalCount = remainingFolders.size,
                                isDeleting = false,
                                deleteSuccess = DeleteSuccess(
                                    deletedCount = deleteResult.deletedCount,
                                    failedCount = deleteResult.failedCount
                                )
                            )
                        } else {
                            state
                        }
                    }
                    
                    _selectedFolders.value = emptySet()
                } else {
                    _uiState.update { state ->
                        if (state is EmptyFolderUiState.Success) {
                            state.copy(
                                isDeleting = false,
                                error = result.exceptionOrNull()?.message ?: "Delete failed"
                            )
                        } else {
                            state
                        }
                    }
                }
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is EmptyFolderUiState.Success) {
                _uiState.update { 
                    (it as EmptyFolderUiState.Success).copy(isDeleting = true) 
                }
                
                val result = deleteEmptyFoldersUseCase.deleteAll()
                
                if (result.isSuccess) {
                    val deleteResult = result.getOrNull()!!
                    
                    _uiState.update { state ->
                        if (state is EmptyFolderUiState.Success) {
                            state.copy(
                                folders = emptyList(),
                                totalCount = 0,
                                isDeleting = false,
                                deleteSuccess = DeleteSuccess(
                                    deletedCount = deleteResult.deletedCount,
                                    failedCount = deleteResult.failedCount
                                )
                            )
                        } else {
                            state
                        }
                    }
                    
                    _selectedFolders.value = emptySet()
                } else {
                    _uiState.update { state ->
                        if (state is EmptyFolderUiState.Success) {
                            state.copy(
                                isDeleting = false,
                                error = result.exceptionOrNull()?.message ?: "Delete failed"
                            )
                        } else {
                            state
                        }
                    }
                }
            }
        }
    }

    fun clearDeleteSuccess() {
        _uiState.update { state ->
            if (state is EmptyFolderUiState.Success) {
                state.copy(deleteSuccess = null)
            } else {
                state
            }
        }
    }

    fun clearError() {
        _uiState.update { state ->
            if (state is EmptyFolderUiState.Success) {
                state.copy(error = null)
            } else {
                state
            }
        }
    }
}

sealed class EmptyFolderUiState {
    object Idle : EmptyFolderUiState()
    data class Scanning(val progress: Int) : EmptyFolderUiState()
    data class Success(
        val folders: List<EmptyFolder>,
        val totalCount: Int,
        val scanDuration: Long,
        val isDeleting: Boolean = false,
        val deleteSuccess: DeleteSuccess? = null,
        val error: String? = null
    ) : EmptyFolderUiState()
    data class Error(val message: String) : EmptyFolderUiState()
}

data class DeleteSuccess(
    val deletedCount: Int,
    val failedCount: Int
)
