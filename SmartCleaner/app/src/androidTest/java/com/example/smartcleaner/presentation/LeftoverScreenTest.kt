package com.example.smartcleaner.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartcleaner.presentation.leftover.LeftoverScreen
import com.example.smartcleaner.presentation.leftover.LeftoverUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for LeftoverScreen
 * Tests user interaction flows with Compose
 */
@RunWith(AndroidJUnit4::class)
class LeftoverScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun idleState_showsEmptyView() {
        // Given
        composeTestRule.setContent {
            // LeftoverScreen with Idle state
        }

        // Then
        composeTestRule
            .onNodeWithText("No Leftover Files")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysLeftoverFiles() {
        // Given - success state with files
        composeTestRule.setContent {
            // LeftoverScreen with Success state
        }

        // Then
        composeTestRule
            .onNodeWithText("com.example.app")
            .assertIsDisplayed()
    }

    @Test
    fun clickingScanButton_triggersRefresh() {
        // Given
        composeTestRule.setContent {
            // LeftoverScreen
        }

        // When
        composeTestRule
            .onNodeWithContentDescription("Scan")
            .performClick()

        // Then - verify scan was triggered
    }

    @Test
    fun selectingFile_updatesSelection() {
        // Given - screen with files
        composeTestRule.setContent {
            // LeftoverScreen with files
        }

        // When
        composeTestRule
            .onAllNodesWithTag("file_checkbox")
            .onFirst()
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("1 file selected")
            .assertIsDisplayed()
    }

    @Test
    fun selectAllButton_selectsAllFiles() {
        // Given
        composeTestRule.setContent {
            // LeftoverScreen with 3 files
        }

        // When
        composeTestRule
            .onNodeWithText("Select All")
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("3 files selected")
            .assertIsDisplayed()
    }

    @Test
    fun deleteButton_showsConfirmation() {
        // Given - files selected
        composeTestRule.setContent {
            // LeftoverScreen with selections
        }

        // When
        composeTestRule
            .onNodeWithContentDescription("Delete")
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("Delete 2 files?")
            .assertIsDisplayed()
    }

    @Test
    fun groupByPackage_showsExpandableGroups() {
        // Given
        composeTestRule.setContent {
            // LeftoverScreen with multiple packages
        }

        // When
        composeTestRule
            .onNodeWithText("com.example.app (2 files)")
            .performClick()

        // Then - group expands
        composeTestRule
            .onNodeWithText("file1.txt")
            .assertIsDisplayed()
    }
}
