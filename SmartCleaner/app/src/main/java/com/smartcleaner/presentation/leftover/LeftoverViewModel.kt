package com.smartcleaner.presentation.leftover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.LeftoverGroup
import com.smartcleaner.domain.usecase.leftover.DeleteLeftoverFilesUseCase
import com.smartcleaner.domain.usecase.leftover.ScanLeftoverFilesUseCase
import com.smartcleaner.domain.usecase.leftover.ScanProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeftoverViewModel @Inject constructor(
    private val scanLeftoverFilesUseCase: ScanLeftoverFilesUseCase,
    private val deleteLeftoverFilesUseCase: DeleteLeftoverFilesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LeftoverUiState>(LeftoverUiState.Idle)
    val uiState: StateFlow<LeftoverUiState> = _uiState.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _uiState.value = LeftoverUiState.Scanning(0)
            
            try {
                scanLeftoverFilesUseCase().collect { progress ->
                    when (progress) {
                        is ScanProgress.Scanning -> {
                            _uiState.value = LeftoverUiState.Scanning(progress.progress)
                        }
                        is ScanProgress.Completed -> {
                            _uiState.value = LeftoverUiState.Success(
                                groups = progress.result.groups,
                                totalSize = progress.result.totalSize,
                                totalFiles = progress.result.totalFiles,
                                scanDuration = progress.result.scanDurationMs
                            )
                        }
                        is ScanProgress.Error -> {
                            _uiState.value = LeftoverUiState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LeftoverUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteLeftoverGroup(group: LeftoverGroup, backup: Boolean = true) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is LeftoverUiState.Success) {
                _uiState.update { 
                    (it as LeftoverUiState.Success).copy(isDeleting = true) 
                }
                
                val result = deleteLeftoverFilesUseCase(group, backup)
                
                if (result.isSuccess) {
                    val deleteResult = result.getOrNull()!!
                    
                    // Remove deleted group from list
                    _uiState.update { state ->
                        if (state is LeftoverUiState.Success) {
                            val newGroups = state.groups.filter { it.packageName != group.packageName }
                            state.copy(
                                groups = newGroups,
                                totalSize = state.totalSize - deleteResult.freedSpace,
                                totalFiles = state.totalFiles - deleteResult.deletedFiles,
                                isDeleting = false,
                                deleteSuccess = DeleteSuccess(
                                    appName = group.appName,
                                    freedSpace = deleteResult.freedSpace,
                                    deletedFiles = deleteResult.deletedFiles
                                )
                            )
                        } else {
                            state
                        }
                    }
                } else {
                    _uiState.update { state ->
                        if (state is LeftoverUiState.Success) {
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
            if (state is LeftoverUiState.Success) {
                state.copy(deleteSuccess = null)
            } else {
                state
            }
        }
    }

    fun clearError() {
        _uiState.update { state ->
            if (state is LeftoverUiState.Success) {
                state.copy(error = null)
            } else {
                state
            }
        }
    }
}

sealed class LeftoverUiState {
    object Idle : LeftoverUiState()
    data class Scanning(val progress: Int) : LeftoverUiState()
    data class Success(
        val groups: List<LeftoverGroup>,
        val totalSize: Long,
        val totalFiles: Int,
        val scanDuration: Long,
        val isDeleting: Boolean = false,
        val deleteSuccess: DeleteSuccess? = null,
        val error: String? = null
    ) : LeftoverUiState()
    data class Error(val message: String) : LeftoverUiState()
}

data class DeleteSuccess(
    val appName: String,
    val freedSpace: Long,
    val deletedFiles: Int
)
