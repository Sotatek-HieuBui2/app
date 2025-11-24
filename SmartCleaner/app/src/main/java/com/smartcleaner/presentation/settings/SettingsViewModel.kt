package com.smartcleaner.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcleaner.domain.model.AppPreferences
import com.smartcleaner.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<AppPreferences?> = preferencesRepository.getPreferences()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updatePreferences(preferences: AppPreferences) {
        viewModelScope.launch {
            preferencesRepository.updatePreferences(preferences)
        }
    }
}
