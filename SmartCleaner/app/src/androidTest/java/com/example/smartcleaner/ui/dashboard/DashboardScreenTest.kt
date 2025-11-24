package com.example.smartcleaner.ui.dashboard

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartcleaner.domain.model.Dashboard
import com.example.smartcleaner.presentation.dashboard.DashboardScreen
import com.example.smartcleaner.presentation.dashboard.DashboardUiState
import com.example.smartcleaner.presentation.theme.SmartCleanerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for DashboardScreen
 */
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `idle state shows loading indicator`() {
        // Given
        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Idle,
                    onRefresh = {},
                    onNavigateToFeature = {},
                    onBack = {}
                )
            }
        }

        // Then - Should show initial state or loading
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    @Test
    fun `success state displays statistics`() {
        // Given
        val dashboard = Dashboard(
            totalJunkSize = 500 * 1024 * 1024L, // 500 MB
            leftoverFilesCount = 15,
            emptyFoldersCount = 8,
            duplicateFilesCount = 12,
            unusedAppsCount = 5,
            lastScanTime = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Success(dashboard),
                    onRefresh = {},
                    onNavigateToFeature = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("500 MB", substring = true).assertExists()
        composeTestRule.onNodeWithText("15", substring = true).assertExists()
        composeTestRule.onNodeWithText("8", substring = true).assertExists()
        composeTestRule.onNodeWithText("12", substring = true).assertExists()
        composeTestRule.onNodeWithText("5", substring = true).assertExists()
    }

    @Test
    fun `loading state shows progress indicator`() {
        // Given
        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Loading,
                    onRefresh = {},
                    onNavigateToFeature = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("loading_indicator").assertExists()
    }

    @Test
    fun `error state shows error message`() {
        // Given
        val errorMessage = "Failed to load dashboard"

        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Error(errorMessage),
                    onRefresh = {},
                    onNavigateToFeature = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `refresh button triggers callback`() {
        // Given
        var refreshCalled = false
        val dashboard = Dashboard(
            totalJunkSize = 0L,
            leftoverFilesCount = 0,
            emptyFoldersCount = 0,
            duplicateFilesCount = 0,
            unusedAppsCount = 0,
            lastScanTime = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Success(dashboard),
                    onRefresh = { refreshCalled = true },
                    onNavigateToFeature = {},
                    onBack = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Refresh").performClick()

        // Then
        assert(refreshCalled)
    }

    @Test
    fun `feature cards are clickable`() {
        // Given
        var navigatedFeature: String? = null
        val dashboard = Dashboard(
            totalJunkSize = 0L,
            leftoverFilesCount = 0,
            emptyFoldersCount = 0,
            duplicateFilesCount = 0,
            unusedAppsCount = 0,
            lastScanTime = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Success(dashboard),
                    onRefresh = {},
                    onNavigateToFeature = { navigatedFeature = it },
                    onBack = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Leftover Files").performClick()

        // Then
        assert(navigatedFeature == "leftover")
    }

    @Test
    fun `back button triggers navigation`() {
        // Given
        var backCalled = false

        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Idle,
                    onRefresh = {},
                    onNavigateToFeature = {},
                    onBack = { backCalled = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Then
        assert(backCalled)
    }

    @Test
    fun `last scan time displayed`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val dashboard = Dashboard(
            totalJunkSize = 0L,
            leftoverFilesCount = 0,
            emptyFoldersCount = 0,
            duplicateFilesCount = 0,
            unusedAppsCount = 0,
            lastScanTime = currentTime
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Success(dashboard),
                    onRefresh = {},
                    onNavigateToFeature = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Last Scan:", substring = true).assertExists()
    }

    @Test
    fun `all feature categories displayed`() {
        // Given
        val dashboard = Dashboard(
            totalJunkSize = 0L,
            leftoverFilesCount = 1,
            emptyFoldersCount = 1,
            duplicateFilesCount = 1,
            unusedAppsCount = 1,
            lastScanTime = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Success(dashboard),
                    onRefresh = {},
                    onNavigateToFeature = {},
                    onBack = {}
                )
            }
        }

        // Then - Verify all feature cards are present
        composeTestRule.onNodeWithText("Leftover Files").assertIsDisplayed()
        composeTestRule.onNodeWithText("Empty Folders").assertIsDisplayed()
        composeTestRule.onNodeWithText("Duplicate Files").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unused Apps").assertIsDisplayed()
    }

    @Test
    fun `storage size formatted correctly`() {
        // Given
        val dashboard = Dashboard(
            totalJunkSize = 1536 * 1024 * 1024L, // 1.5 GB
            leftoverFilesCount = 0,
            emptyFoldersCount = 0,
            duplicateFilesCount = 0,
            unusedAppsCount = 0,
            lastScanTime = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Success(dashboard),
                    onRefresh = {},
                    onNavigateToFeature = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("1.5 GB", substring = true).assertExists()
    }
}
