package com.example.smartcleaner.ui.duplicate

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartcleaner.domain.model.DuplicateFile
import com.example.smartcleaner.presentation.duplicate.DuplicateScreen
import com.example.smartcleaner.presentation.duplicate.DuplicateUiState
import com.example.smartcleaner.presentation.theme.SmartCleanerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * UI tests for DuplicateScreen
 */
@RunWith(AndroidJUnit4::class)
class DuplicateScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `idle state shows scan button`() {
        // Given
        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Idle,
                    selectedFiles = emptySet(),
                    scanProgress = 0f,
                    onScanClick = {},
                    onFileToggle = {},
                    onSelectGroup = {},
                    onDeleteClick = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Scan for Duplicates").assertIsDisplayed()
    }

    @Test
    fun `success state displays duplicate groups`() {
        // Given
        val duplicates = listOf(
            DuplicateFile(
                hash = "hash1",
                files = listOf(
                    File("/test/file1.txt"),
                    File("/test/file2.txt")
                ),
                similarity = 1.0f
            ),
            DuplicateFile(
                hash = "hash2",
                files = listOf(
                    File("/test/image1.jpg"),
                    File("/test/image2.jpg")
                ),
                similarity = 0.95f
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Success(duplicates),
                    selectedFiles = emptySet(),
                    scanProgress = 1.0f,
                    onScanClick = {},
                    onFileToggle = {},
                    onSelectGroup = {},
                    onDeleteClick = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("file1.txt").assertIsDisplayed()
        composeTestRule.onNodeWithText("file2.txt").assertIsDisplayed()
        composeTestRule.onNodeWithText("100%", substring = true).assertExists() // 100% similarity
        composeTestRule.onNodeWithText("95%", substring = true).assertExists() // 95% similarity
    }

    @Test
    fun `loading state shows progress indicator`() {
        // Given
        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Loading,
                    selectedFiles = emptySet(),
                    scanProgress = 0.5f,
                    onScanClick = {},
                    onFileToggle = {},
                    onSelectGroup = {},
                    onDeleteClick = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Scanning...").assertIsDisplayed()
        composeTestRule.onNodeWithText("50%", substring = true).assertExists()
    }

    @Test
    fun `error state shows error message`() {
        // Given
        val errorMessage = "Failed to scan for duplicates"

        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Error(errorMessage),
                    selectedFiles = emptySet(),
                    scanProgress = 0f,
                    onScanClick = {},
                    onFileToggle = {},
                    onSelectGroup = {},
                    onDeleteClick = {},
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
    fun `file selection works`() {
        // Given
        var toggledFile: String? = null
        val duplicates = listOf(
            DuplicateFile(
                hash = "hash1",
                files = listOf(File("/test/file1.txt")),
                similarity = 1.0f
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Success(duplicates),
                    selectedFiles = emptySet(),
                    scanProgress = 1.0f,
                    onScanClick = {},
                    onFileToggle = { toggledFile = it },
                    onSelectGroup = {},
                    onDeleteClick = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("file1.txt").performClick()

        // Then
        assert(toggledFile == "/test/file1.txt")
    }

    @Test
    fun `delete button visible when files selected`() {
        // Given
        val duplicates = listOf(
            DuplicateFile(
                hash = "hash1",
                files = listOf(File("/test/file1.txt")),
                similarity = 1.0f
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Success(duplicates),
                    selectedFiles = setOf("/test/file1.txt"),
                    scanProgress = 1.0f,
                    onScanClick = {},
                    onFileToggle = {},
                    onSelectGroup = {},
                    onDeleteClick = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Delete (1)").assertIsDisplayed()
    }

    @Test
    fun `statistics displayed correctly`() {
        // Given
        val duplicates = listOf(
            DuplicateFile(
                hash = "hash1",
                files = listOf(
                    File("/test/file1.txt").apply { 
                        // Mock would set length to 1000
                    },
                    File("/test/file2.txt")
                ),
                similarity = 1.0f
            )
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Success(duplicates),
                    selectedFiles = emptySet(),
                    scanProgress = 1.0f,
                    onScanClick = {},
                    onFileToggle = {},
                    onSelectGroup = {},
                    onDeleteClick = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Groups: 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Files: 2").assertIsDisplayed()
    }

    @Test
    fun `back button triggers navigation`() {
        // Given
        var backCalled = false

        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Idle,
                    selectedFiles = emptySet(),
                    scanProgress = 0f,
                    onScanClick = {},
                    onFileToggle = {},
                    onSelectGroup = {},
                    onDeleteClick = {},
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
    fun `scan button triggers scan action`() {
        // Given
        var scanCalled = false

        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Idle,
                    selectedFiles = emptySet(),
                    scanProgress = 0f,
                    onScanClick = { _, _ -> scanCalled = true },
                    onFileToggle = {},
                    onSelectGroup = {},
                    onDeleteClick = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Scan for Duplicates").performClick()

        // Then
        assert(scanCalled)
    }

    @Test
    fun `select group button works`() {
        // Given
        var selectedGroup: DuplicateFile? = null
        val group = DuplicateFile(
            hash = "hash1",
            files = listOf(
                File("/test/file1.txt"),
                File("/test/file2.txt")
            ),
            similarity = 1.0f
        )

        composeTestRule.setContent {
            SmartCleanerTheme {
                DuplicateScreen(
                    uiState = DuplicateUiState.Success(listOf(group)),
                    selectedFiles = emptySet(),
                    scanProgress = 1.0f,
                    onScanClick = {},
                    onFileToggle = {},
                    onSelectGroup = { selectedGroup = it },
                    onDeleteClick = {},
                    onClearSelection = {},
                    onBack = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Select All Except First").performClick()

        // Then
        assert(selectedGroup == group)
    }
}
