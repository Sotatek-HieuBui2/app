package com.smartcleaner.presentation.classifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.JunkClassification
import com.smartcleaner.domain.model.JunkCategory
import com.smartcleaner.domain.usecase.classifier.ClassifyJunkFilesUseCase
import com.smartcleaner.domain.usecase.classifier.ClassificationProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ClassifierViewModel @Inject constructor(
    private val classifyJunkFilesUseCase: ClassifyJunkFilesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassifierUiState>(ClassifierUiState.Idle)
    val uiState: StateFlow<ClassifierUiState> = _uiState.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Set<String>>(emptySet())
    val selectedFiles: StateFlow<Set<String>> = _selectedFiles.asStateFlow()

    fun classifyFiles(files: List<File>) {
        viewModelScope.launch {
            _uiState.value = ClassifierUiState.Loading
            try {
                classifyJunkFilesUseCase(files).collect { progress ->
                    when (progress) {
                        is ClassificationProgress.Initializing -> {
                            _uiState.value = ClassifierUiState.Loading
                        }
                        is ClassificationProgress.Classifying -> {
                            _uiState.value = ClassifierUiState.Loading
                        }
                        is ClassificationProgress.Completed -> {
                            _uiState.value = ClassifierUiState.Success(
                                classifications = progress.result.classifications,
                                progress = 1f
                            )
                        }
                        is ClassificationProgress.Error -> {
                            _uiState.value = ClassifierUiState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ClassifierUiState.Error(e.message ?: "Classification failed")
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

    fun selectAll() {
        val state = _uiState.value
        if (state is ClassifierUiState.Success) {
            _selectedFiles.value = state.classifications
                .filter { it.isJunk }
                .map { it.filePath }
                .toSet()
        }
    }

    fun clearSelection() {
        _selectedFiles.value = emptySet()
    }

    fun filterByCategory(category: JunkCategory): List<JunkClassification> {
        val state = _uiState.value
        return if (state is ClassifierUiState.Success) {
            state.classifications.filter { it.predictedCategory == category }
        } else {
            emptyList()
        }
    }

    fun getStatistics(): ClassifierStatistics? {
        val state = _uiState.value
        return if (state is ClassifierUiState.Success) {
            val total = state.classifications.size
            val deletable = state.classifications.count { it.isJunk }
            val totalSize = state.classifications.size * 1000L // Estimate
            val deletableSize = state.classifications
                .filter { it.isJunk }
                .size * 1000L
            
            val categoryBreakdown = state.classifications
                .groupBy { it.predictedCategory }
                .mapValues { it.value.size }

            ClassifierStatistics(
                totalFiles = total,
                deletableFiles = deletable,
                totalSize = totalSize,
                deletableSize = deletableSize,
                categoryBreakdown = categoryBreakdown,
                averageConfidence = state.classifications.map { it.confidence }.average().toFloat()
            )
        } else {
            null
        }
    }
}

sealed class ClassifierUiState {
    object Idle : ClassifierUiState()
    object Loading : ClassifierUiState()
    data class Success(
        val classifications: List<JunkClassification>,
        val progress: Float = 1f
    ) : ClassifierUiState()
    data class Error(val message: String) : ClassifierUiState()
}

data class ClassifierStatistics(
    val totalFiles: Int,
    val deletableFiles: Int,
    val totalSize: Long,
    val deletableSize: Long,
    val categoryBreakdown: Map<JunkCategory, Int>,
    val averageConfidence: Float
)
