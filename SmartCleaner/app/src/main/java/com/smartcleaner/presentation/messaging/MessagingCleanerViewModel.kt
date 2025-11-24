package com.smartcleaner.presentation.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.MessagingApp
import com.smartcleaner.domain.model.MessagingMedia
import com.smartcleaner.domain.model.MessagingMediaType
import com.smartcleaner.domain.model.MessagingScanOptions
import com.smartcleaner.domain.usecase.messaging.DeleteMessagingMediaUseCase
import com.smartcleaner.domain.usecase.messaging.MessagingProgress
import com.smartcleaner.domain.usecase.messaging.ScanMessagingAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagingCleanerViewModel @Inject constructor(
    private val scanMessagingAppsUseCase: ScanMessagingAppsUseCase,
    private val deleteMessagingMediaUseCase: DeleteMessagingMediaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MessagingCleanerUiState>(MessagingCleanerUiState.Idle)
    val uiState: StateFlow<MessagingCleanerUiState> = _uiState.asStateFlow()

    private val _selectedMedia = MutableStateFlow<Set<String>>(emptySet())
    val selectedMedia: StateFlow<Set<String>> = _selectedMedia.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<MessagingApp>>(MessagingApp.values().toSet())
    val selectedApps: StateFlow<Set<MessagingApp>> = _selectedApps.asStateFlow()

    fun scanApps() {
        viewModelScope.launch {
            _uiState.value = MessagingCleanerUiState.Scanning
            try {
                val options = MessagingScanOptions(
                    scanImages = true,
                    scanVideos = true,
                    scanAudio = true
                )
                
                scanMessagingAppsUseCase(options).collect { progress ->
                    when (progress) {
                        is MessagingProgress.CheckingApps -> {
                            _uiState.value = MessagingCleanerUiState.Scanning
                        }
                        is MessagingProgress.Scanning -> {
                            _uiState.value = MessagingCleanerUiState.Scanning
                        }
                        is MessagingProgress.Completed -> {
                            val allMedia = progress.result.appResults.values
                                .flatMap { it.groups }
                                .flatMap { it.files }
                            _uiState.value = MessagingCleanerUiState.Success(allMedia)
                        }
                        is MessagingProgress.Error -> {
                            _uiState.value = MessagingCleanerUiState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MessagingCleanerUiState.Error(e.message ?: "Scan failed")
            }
        }
    }

    fun deleteSelected() {
        val state = _uiState.value
        if (state !is MessagingCleanerUiState.Success) return

        val filesToDelete = _selectedMedia.value.toList()

        if (filesToDelete.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = MessagingCleanerUiState.Deleting
            try {
                val result = deleteMessagingMediaUseCase(filesToDelete)
                if (result.isSuccess) {
                    _selectedMedia.value = emptySet()
                    scanApps() // Rescan after deletion
                } else {
                    _uiState.value = MessagingCleanerUiState.Error(result.exceptionOrNull()?.message ?: "Delete failed")
                }
            } catch (e: Exception) {
                _uiState.value = MessagingCleanerUiState.Error(e.message ?: "Delete failed")
            }
        }
    }

    fun toggleMediaSelection(filePath: String) {
        val current = _selectedMedia.value.toMutableSet()
        if (current.contains(filePath)) {
            current.remove(filePath)
        } else {
            current.add(filePath)
        }
        _selectedMedia.value = current
    }

    fun toggleAppSelection(app: MessagingApp) {
        val current = _selectedApps.value.toMutableSet()
        if (current.contains(app)) {
            current.remove(app)
        } else {
            current.add(app)
        }
        _selectedApps.value = current
    }

    fun selectAllByApp(app: MessagingApp) {
        val state = _uiState.value
        if (state is MessagingCleanerUiState.Success) {
            val current = _selectedMedia.value.toMutableSet()
            state.media
                .filter { it.app == app }
                .forEach { current.add(it.filePath) }
            _selectedMedia.value = current
        }
    }

    fun selectAllByType(mediaType: MessagingMediaType) {
        val state = _uiState.value
        if (state is MessagingCleanerUiState.Success) {
            val current = _selectedMedia.value.toMutableSet()
            state.media
                .filter { it.mediaType == mediaType }
                .forEach { current.add(it.filePath) }
            _selectedMedia.value = current
        }
    }

    fun clearSelection() {
        _selectedMedia.value = emptySet()
    }

    fun getStatistics(): MessagingStatistics? {
        val state = _uiState.value
        return if (state is MessagingCleanerUiState.Success) {
            val totalFiles = state.media.size
            val totalSize = state.media.sumOf { it.size }
            val selectedSize = _selectedMedia.value.sumOf { path ->
                state.media.find { it.filePath == path }?.size ?: 0L
            }

            val appBreakdown = state.media.groupBy { it.app }
                .mapValues { it.value.size to it.value.sumOf { media -> media.size } }

            val typeBreakdown = state.media.groupBy { it.mediaType }
                .mapValues { it.value.size to it.value.sumOf { media -> media.size } }

            MessagingStatistics(
                totalFiles = totalFiles,
                totalSize = totalSize,
                selectedFiles = _selectedMedia.value.size,
                selectedSize = selectedSize,
                appBreakdown = appBreakdown,
                typeBreakdown = typeBreakdown
            )
        } else {
            null
        }
    }
}

sealed class MessagingCleanerUiState {
    object Idle : MessagingCleanerUiState()
    object Scanning : MessagingCleanerUiState()
    object Deleting : MessagingCleanerUiState()
    data class Success(val media: List<MessagingMedia>) : MessagingCleanerUiState()
    data class Error(val message: String) : MessagingCleanerUiState()
}

data class MessagingStatistics(
    val totalFiles: Int,
    val totalSize: Long,
    val selectedFiles: Int,
    val selectedSize: Long,
    val appBreakdown: Map<MessagingApp, Pair<Int, Long>>, // app -> (count, size)
    val typeBreakdown: Map<MessagingMediaType, Pair<Int, Long>>  // mediaType -> (count, size)
)
