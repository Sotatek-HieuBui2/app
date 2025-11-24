package com.smartcleaner.presentation.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.StorageAnalysis
import com.smartcleaner.domain.usecase.storage.AnalysisProgress
import com.smartcleaner.domain.usecase.storage.AnalyzeStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageAnalyzerViewModel @Inject constructor(
    private val analyzeStorageUseCase: AnalyzeStorageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StorageAnalyzerUiState>(StorageAnalyzerUiState.Idle)
    val uiState: StateFlow<StorageAnalyzerUiState> = _uiState.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.OVERVIEW)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    init {
        analyzeStorage()
    }

    fun analyzeStorage() {
        viewModelScope.launch {
            _uiState.value = StorageAnalyzerUiState.Analyzing(0)
            try {
                analyzeStorageUseCase().collect { progress ->
                    when (progress) {
                        is AnalysisProgress.Starting -> {
                            _uiState.value = StorageAnalyzerUiState.Analyzing(0)
                        }
                        is AnalysisProgress.Analyzing -> {
                            _uiState.value = StorageAnalyzerUiState.Analyzing(progress.progress)
                        }
                        is AnalysisProgress.Completed -> {
                            _uiState.value = StorageAnalyzerUiState.Success(progress.result)
                        }
                        is AnalysisProgress.Error -> {
                            _uiState.value = StorageAnalyzerUiState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = StorageAnalyzerUiState.Error(e.message ?: "Analysis failed")
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun navigateToFolder(path: String) {
        // Navigate into folder for detailed view
        val state = _uiState.value
        if (state is StorageAnalyzerUiState.Success) {
            // Find node in tree and update view
        }
    }
}

sealed class StorageAnalyzerUiState {
    object Idle : StorageAnalyzerUiState()
    data class Analyzing(val progress: Int) : StorageAnalyzerUiState()
    data class Success(val analysis: StorageAnalysis) : StorageAnalyzerUiState()
    data class Error(val message: String) : StorageAnalyzerUiState()
}

enum class ViewMode {
    OVERVIEW,
    TREEMAP,
    CATEGORIES,
    LARGEST_FILES,
    TRENDS
}
