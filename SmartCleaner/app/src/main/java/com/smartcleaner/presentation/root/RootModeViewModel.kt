package com.smartcleaner.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.RootOperationResult
import com.smartcleaner.domain.model.RootStatus
import com.smartcleaner.domain.usecase.root.CheckRootAccessUseCase
import com.smartcleaner.domain.usecase.root.CleanSystemCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootModeViewModel @Inject constructor(
    private val checkRootAccessUseCase: CheckRootAccessUseCase,
    private val cleanSystemCacheUseCase: CleanSystemCacheUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RootModeUiState>(RootModeUiState.Checking)
    val uiState: StateFlow<RootModeUiState> = _uiState.asStateFlow()

    private val _operationInProgress = MutableStateFlow(false)
    val operationInProgress: StateFlow<Boolean> = _operationInProgress.asStateFlow()

    private val _operationResult = MutableStateFlow<RootOperationResult?>(null)
    val operationResult: StateFlow<RootOperationResult?> = _operationResult.asStateFlow()

    init {
        checkRootAccess()
    }

    fun checkRootAccess() {
        viewModelScope.launch {
            _uiState.value = RootModeUiState.Checking
            try {
                val status = checkRootAccessUseCase()
                _uiState.value = when (status) {
                    RootStatus.ROOTED_GRANTED -> RootModeUiState.Granted
                    RootStatus.ROOTED_DENIED -> RootModeUiState.Error("Root permission denied")
                    RootStatus.NOT_ROOTED -> RootModeUiState.NotRooted
                    RootStatus.CHECKING -> RootModeUiState.Checking
                }
            } catch (e: Exception) {
                _uiState.value = RootModeUiState.Error(e.message ?: "Root check failed")
            }
        }
    }

    fun cleanSystemCache() {
        executeRootOperation("Cleaning system cache") {
            cleanSystemCacheUseCase()
        }
    }

    fun cleanDalvikCache() {
        executeRootOperation("Cleaning dalvik cache") {
            // Assuming cleanSystemCacheUseCase handles both or we need another use case
            // For now, reusing cleanSystemCacheUseCase as it cleans both in implementation
            cleanSystemCacheUseCase() 
        }
    }

    fun disableBloatware(packageName: String) {
        // Placeholder for disable bloatware use case
    }

    fun clearAppData(packageName: String) {
        // Placeholder for clear app data use case
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }

    private fun executeRootOperation(operationName: String, operation: suspend () -> RootOperationResult) {
        viewModelScope.launch {
            _operationInProgress.value = true
            try {
                val result = operation()
                _operationResult.value = result
            } catch (e: Exception) {
                _operationResult.value = RootOperationResult(false, e.message ?: "Operation failed")
            } finally {
                _operationInProgress.value = false
            }
        }
    }
}

sealed class RootModeUiState {
    object Checking : RootModeUiState()
    object NotRooted : RootModeUiState()
    object Granted : RootModeUiState()
    data class Error(val message: String) : RootModeUiState()
}
