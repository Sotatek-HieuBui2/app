package com.example.smartcleaner.ui.classifier

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartcleaner.domain.model.JunkClassification
import com.example.smartcleaner.presentation.classifier.ClassifierScreen
import com.example.smartcleaner.presentation.classifier.ClassifierUiState
import com.example.smartcleaner.presentation.theme.SmartCleanerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * UI tests for ClassifierScreen
 */
@RunWith(AndroidJUnit4::class)
class ClassifierScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `idle state shows scan button`() {
        // Given
        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Idle,
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Start Classification").assertIsDisplayed()
    }

    @Test
    fun `success state displays classifications`() {
        // Given
        val classifications = listOf(
            JunkClassification(
                file = File("/test/file1.tmp"),
                category = "CACHE",
                confidence = 0.95f,
                shouldDelete = true,
                reason = "Cache file"
            ),
            JunkClassification(
                file = File("/test/file2.doc"),
                category = "SAFE",
                confidence = 0.90f,
                shouldDelete = false,
                reason = "Document"
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Success(
                        classifications = classifications,
                        progress = 1.0f
                    ),
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("file1.tmp").assertIsDisplayed()
        composeTestRule.onNodeWithText("file2.doc").assertIsDisplayed()
        composeTestRule.onNodeWithText("CACHE").assertIsDisplayed()
        composeTestRule.onNodeWithText("SAFE").assertIsDisplayed()
    }

    @Test
    fun `file selection works`() {
        // Given
        var toggledFile: String? = null
        val classifications = listOf(
            JunkClassification(
                file = File("/test/file1.tmp"),
                category = "JUNK",
                confidence = 0.95f,
                shouldDelete = true,
                reason = "Test"
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Success(classifications, 1.0f),
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = { toggledFile = it },
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("file1.tmp").performClick()

        // Then
        assert(toggledFile == "/test/file1.tmp")
    }

    @Test
    fun `select all button triggers callback`() {
        // Given
        var selectAllCalled = false
        val classifications = listOf(
            JunkClassification(
                file = File("/test/file1.tmp"),
                category = "JUNK",
                confidence = 0.95f,
                shouldDelete = true,
                reason = "Test"
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Success(classifications, 1.0f),
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = { selectAllCalled = true },
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Select All").performClick()

        // Then
        assert(selectAllCalled)
    }

    @Test
    fun `delete button visible when files selected`() {
        // Given
        val classifications = listOf(
            JunkClassification(
                file = File("/test/file1.tmp"),
                category = "JUNK",
                confidence = 0.95f,
                shouldDelete = true,
                reason = "Test"
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Success(classifications, 1.0f),
                    selectedFiles = setOf("/test/file1.tmp"),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Delete Selected (1)").assertIsDisplayed()
    }

    @Test
    fun `error state shows error message`() {
        // Given
        val errorMessage = "Classification failed"

        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Error(errorMessage),
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun `loading state shows progress indicator`() {
        // Given
        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Loading(0.5f),
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Classifying...").assertIsDisplayed()
    }

    @Test
    fun `confidence displayed correctly`() {
        // Given
        val classifications = listOf(
            JunkClassification(
                file = File("/test/file1.tmp"),
                category = "JUNK",
                confidence = 0.956f, // Should display as 95.6%
                shouldDelete = true,
                reason = "Test"
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Success(classifications, 1.0f),
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("95.6%", substring = true).assertExists()
    }

    @Test
    fun `back button triggers navigation`() {
        // Given
        var backCalled = false

        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Idle,
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
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
    fun `statistics displayed when available`() {
        // Given
        val classifications = listOf(
            JunkClassification(
                file = File("/test/file1.tmp"),
                category = "JUNK",
                confidence = 0.95f,
                shouldDelete = true,
                reason = "Test"
            ),
            JunkClassification(
                file = File("/test/file2.tmp"),
                category = "SAFE",
                confidence = 0.90f,
                shouldDelete = false,
                reason = "Test"
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                ClassifierScreen(
                    uiState = ClassifierUiState.Success(classifications, 1.0f),
                    selectedFiles = emptySet(),
                    onScanClick = {},
                    onFileToggle = {},
                    onDeleteClick = {},
                    onSelectAll = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Total Files: 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Deletable: 1").assertIsDisplayed()
    }
}
