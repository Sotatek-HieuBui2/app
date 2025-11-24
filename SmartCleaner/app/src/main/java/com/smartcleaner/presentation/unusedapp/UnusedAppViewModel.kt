package com.smartcleaner.presentation.unusedapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.UnusedApp
import com.smartcleaner.domain.model.UnusedAppAnalysisResult
import com.smartcleaner.domain.model.UnusedCategory
import com.smartcleaner.domain.model.UsageStatsPermissionState
import com.smartcleaner.domain.repository.UnusedAppRepository
import com.smartcleaner.domain.usecase.unusedapp.AnalyzeUnusedAppsUseCase
import com.smartcleaner.domain.usecase.unusedapp.AnalysisProgress
import com.smartcleaner.domain.usecase.unusedapp.UninstallAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnusedAppViewModel @Inject constructor(
    private val analyzeUnusedAppsUseCase: AnalyzeUnusedAppsUseCase,
    private val uninstallAppUseCase: UninstallAppUseCase,
    private val repository: UnusedAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UnusedAppUiState>(UnusedAppUiState.Idle)
    val uiState: StateFlow<UnusedAppUiState> = _uiState.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _filterCategory = MutableStateFlow<UnusedCategory?>(null)
    val filterCategory: StateFlow<UnusedCategory?> = _filterCategory.asStateFlow()

    init {
        checkPermission()
    }

    private fun checkPermission() {
        viewModelScope.launch {
            val permissionState = repository.checkUsageStatsPermission()
            if (permissionState != UsageStatsPermissionState.GRANTED) {
                _uiState.value = UnusedAppUiState.PermissionRequired
            }
        }
    }

    fun requestPermission() {
        repository.requestUsageStatsPermission()
    }

    fun startAnalysis() {
        viewModelScope.launch {
            analyzeUnusedAppsUseCase().collect { progress ->
                when (progress) {
                    is AnalysisProgress.CheckingPermission -> {
                        _uiState.value = UnusedAppUiState.CheckingPermission
                    }
                    is AnalysisProgress.PermissionRequired -> {
                        _uiState.value = UnusedAppUiState.PermissionRequired
                    }
                    is AnalysisProgress.Analyzing -> {
                        _uiState.value = UnusedAppUiState.Analyzing(progress.progress)
                    }
                    is AnalysisProgress.Completed -> {
                        _uiState.value = UnusedAppUiState.Success(progress.result)
                        _selectedApps.value = emptySet()
                    }
                    is AnalysisProgress.Error -> {
                        _uiState.value = UnusedAppUiState.Error(progress.message)
                    }
                }
            }
        }
    }

    fun toggleAppSelection(packageName: String) {
        _selectedApps.value = if (_selectedApps.value.contains(packageName)) {
            _selectedApps.value - packageName
        } else {
            _selectedApps.value + packageName
        }
    }

    fun selectAllVisibleApps() {
        val currentState = _uiState.value
        if (currentState is UnusedAppUiState.Success) {
            val filteredApps = getFilteredApps(currentState.result)
            _selectedApps.value = filteredApps.map { it.packageName }.toSet()
        }
    }

    fun deselectAllApps() {
        _selectedApps.value = emptySet()
    }

    fun setFilterCategory(category: UnusedCategory?) {
        _filterCategory.value = category
    }

    fun uninstallSelectedApps() {
        viewModelScope.launch {
            val packagesToUninstall = _selectedApps.value.toList()
            if (packagesToUninstall.isEmpty()) return@launch

            // Uninstall first app (others require sequential user confirmation)
            val firstPackage = packagesToUninstall.first()
            uninstallAppUseCase(firstPackage)

            // Note: Due to Android limitations, we can only trigger one uninstall at a time
            // User needs to confirm each one separately in system dialog
        }
    }

    fun uninstallApp(packageName: String) {
        viewModelScope.launch {
            uninstallAppUseCase(packageName)
        }
    }

    fun getFilteredApps(result: UnusedAppAnalysisResult): List<UnusedApp> {
        val category = _filterCategory.value
        return if (category == null) {
            result.apps
        } else {
            result.apps.filter { it.category == category }
        }
    }

    fun getTotalSizeOfSelected(result: UnusedAppAnalysisResult): Long {
        return result.apps
            .filter { _selectedApps.value.contains(it.packageName) }
            .sumOf { it.totalSize }
    }

    fun getAppUsageDetails(packageName: String) {
        viewModelScope.launch {
            val details = repository.getAppUsageDetails(packageName, 30)
            // Could emit to a separate state if needed for detail view
        }
    }

    fun openAppSettings(packageName: String) {
        viewModelScope.launch {
            repository.clearAppData(packageName)
        }
    }
}

sealed class UnusedAppUiState {
    object Idle : UnusedAppUiState()
    object CheckingPermission : UnusedAppUiState()
    object PermissionRequired : UnusedAppUiState()
    data class Analyzing(val progress: Int) : UnusedAppUiState()
    data class Success(val result: UnusedAppAnalysisResult) : UnusedAppUiState()
    data class Error(val message: String) : UnusedAppUiState()
}
