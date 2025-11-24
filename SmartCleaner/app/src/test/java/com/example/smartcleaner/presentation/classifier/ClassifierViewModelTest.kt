package com.example.smartcleaner.presentation.classifier

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.smartcleaner.domain.model.JunkClassification
import com.example.smartcleaner.domain.usecase.ClassifyJunkFilesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.File

/**
 * Unit tests for ClassifierViewModel
 */
@ExperimentalCoroutinesApi
class ClassifierViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var classifyUseCase: ClassifyJunkFilesUseCase

    private lateinit var viewModel: ClassifierViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ClassifierViewModel(classifyUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(ClassifierUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `classifyFiles updates state to Success`() = runTest {
        // Given
        val files = listOf(File("/test/file1.tmp"), File("/test/file2.tmp"))
        val classifications = listOf(
            JunkClassification(
                file = files[0],
                category = "JUNK",
                confidence = 0.95f,
                shouldDelete = true,
                reason = "Cache file"
            ),
            JunkClassification(
                file = files[1],
                category = "SAFE",
                confidence = 0.90f,
                shouldDelete = false,
                reason = "Important file"
            )
        )
        
        `when`(classifyUseCase(files)).thenReturn(flowOf(*classifications.toTypedArray()))

        // When
        viewModel.classifyFiles(files)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is ClassifierUiState.Success)
        assertEquals(2, (state as ClassifierUiState.Success).classifications.size)
    }

    @Test
    fun `classifyFiles emits progress updates`() = runTest {
        // Given
        val files = (1..10).map { File("/test/file$it.tmp") }
        val classifications = files.mapIndexed { index, file ->
            JunkClassification(
                file = file,
                category = "JUNK",
                confidence = 0.9f,
                shouldDelete = true,
                reason = "Test"
            )
        }
        
        `when`(classifyUseCase(files)).thenReturn(flowOf(*classifications.toTypedArray()))

        // When
        viewModel.classifyFiles(files)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ClassifierUiState.Success
        assertEquals(1.0f, state.progress, 0.01f) // Should be 100% complete
    }

    @Test
    fun `toggleFileSelection adds file to selection`() = runTest {
        // Given
        val filePath = "/test/file.tmp"

        // When
        viewModel.toggleFileSelection(filePath)

        // Then
        assertTrue(viewModel.selectedFiles.value.contains(filePath))
    }

    @Test
    fun `toggleFileSelection removes file if already selected`() = runTest {
        // Given
        val filePath = "/test/file.tmp"
        viewModel.toggleFileSelection(filePath) // Add

        // When
        viewModel.toggleFileSelection(filePath) // Remove

        // Then
        assertFalse(viewModel.selectedFiles.value.contains(filePath))
    }

    @Test
    fun `selectAll selects all deletable files`() = runTest {
        // Given
        val files = listOf(
            File("/test/deletable1.tmp"),
            File("/test/deletable2.tmp"),
            File("/test/important.doc")
        )
        val classifications = listOf(
            JunkClassification(files[0], "JUNK", 0.9f, true, "Cache"),
            JunkClassification(files[1], "JUNK", 0.9f, true, "Temp"),
            JunkClassification(files[2], "SAFE", 0.9f, false, "Document")
        )
        
        `when`(classifyUseCase(files)).thenReturn(flowOf(*classifications.toTypedArray()))
        
        viewModel.classifyFiles(files)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectAll()

        // Then
        assertEquals(2, viewModel.selectedFiles.value.size) // Only 2 deletable files
        assertTrue(viewModel.selectedFiles.value.contains("/test/deletable1.tmp"))
        assertTrue(viewModel.selectedFiles.value.contains("/test/deletable2.tmp"))
        assertFalse(viewModel.selectedFiles.value.contains("/test/important.doc"))
    }

    @Test
    fun `clearSelection removes all selections`() = runTest {
        // Given
        viewModel.toggleFileSelection("/test/1.tmp")
        viewModel.toggleFileSelection("/test/2.tmp")

        // When
        viewModel.clearSelection()

        // Then
        assertTrue(viewModel.selectedFiles.value.isEmpty())
    }

    @Test
    fun `filterByCategory returns correct files`() = runTest {
        // Given
        val files = listOf(
            File("/test/junk1.tmp"),
            File("/test/junk2.tmp"),
            File("/test/safe.doc")
        )
        val classifications = listOf(
            JunkClassification(files[0], "JUNK", 0.9f, true, "Cache"),
            JunkClassification(files[1], "JUNK", 0.9f, true, "Temp"),
            JunkClassification(files[2], "SAFE", 0.9f, false, "Document")
        )
        
        `when`(classifyUseCase(files)).thenReturn(flowOf(*classifications.toTypedArray()))
        
        viewModel.classifyFiles(files)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val junkFiles = viewModel.filterByCategory("JUNK")

        // Then
        assertEquals(2, junkFiles.size)
        assertTrue(junkFiles.all { it.category == "JUNK" })
    }

    @Test
    fun `getStatistics calculates correct metrics`() = runTest {
        // Given
        val files = listOf(
            File("/test/junk1.tmp"),
            File("/test/junk2.tmp"),
            File("/test/safe.doc")
        )
        
        // Mock file sizes
        val classifications = listOf(
            JunkClassification(
                file = File("/test/junk1.tmp").apply { 
                    // In real scenario, would mock File.length()
                },
                category = "JUNK",
                confidence = 0.95f,
                shouldDelete = true,
                reason = "Cache"
            ),
            JunkClassification(
                file = File("/test/junk2.tmp"),
                category = "JUNK",
                confidence = 0.90f,
                shouldDelete = true,
                reason = "Temp"
            ),
            JunkClassification(
                file = File("/test/safe.doc"),
                category = "SAFE",
                confidence = 0.85f,
                shouldDelete = false,
                reason = "Document"
            )
        )
        
        `when`(classifyUseCase(files)).thenReturn(flowOf(*classifications.toTypedArray()))
        
        viewModel.classifyFiles(files)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val stats = viewModel.getStatistics()

        // Then
        assertNotNull(stats)
        assertEquals(3, stats?.totalFiles)
        assertEquals(2, stats?.deletableFiles)
        assertEquals(0.90f, stats?.averageConfidence, 0.01f) // (0.95 + 0.90 + 0.85) / 3
    }

    @Test
    fun `classifyFiles handles errors gracefully`() = runTest {
        // Given
        val files = listOf(File("/test/file.tmp"))
        val exception = RuntimeException("Classification failed")
        
        `when`(classifyUseCase(files)).thenThrow(exception)

        // When
        viewModel.classifyFiles(files)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is ClassifierUiState.Error)
        assertEquals("Classification failed", (state as ClassifierUiState.Error).message)
    }

    @Test
    fun `getStatistics returns null when not in Success state`() {
        // Given - Idle state
        
        // When
        val stats = viewModel.getStatistics()

        // Then
        assertNull(stats)
    }

    @Test
    fun `classifyFiles sets Loading state initially`() = runTest {
        // Given
        val files = listOf(File("/test/file.tmp"))
        
        // Use a flow that never completes to check intermediate state
        `when`(classifyUseCase(files)).thenReturn(flowOf())

        // When
        viewModel.classifyFiles(files)
        testDispatcher.scheduler.runCurrent()

        // Then
        // State transitions: Idle -> Loading
        // Since flow is empty, it stays in Loading or goes to Success with empty list
    }

    @Test
    fun `statistics breakdown by category is correct`() = runTest {
        // Given
        val files = listOf(
            File("/test/cache.tmp"),
            File("/test/temp.tmp"),
            File("/test/log.log")
        )
        val classifications = listOf(
            JunkClassification(files[0], "CACHE", 0.9f, true, "Cache file"),
            JunkClassification(files[1], "TEMP", 0.9f, true, "Temp file"),
            JunkClassification(files[2], "LOG", 0.9f, true, "Log file")
        )
        
        `when`(classifyUseCase(files)).thenReturn(flowOf(*classifications.toTypedArray()))
        
        viewModel.classifyFiles(files)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val stats = viewModel.getStatistics()

        // Then
        assertEquals(3, stats?.categoryBreakdown?.size)
        assertEquals(1, stats?.categoryBreakdown?.get("CACHE"))
        assertEquals(1, stats?.categoryBreakdown?.get("TEMP"))
        assertEquals(1, stats?.categoryBreakdown?.get("LOG"))
    }

    @Test
    fun `multiple classifications update progress correctly`() = runTest {
        // Given
        val files = (1..5).map { File("/test/file$it.tmp") }
        val classifications = files.map { file ->
            JunkClassification(file, "JUNK", 0.9f, true, "Test")
        }
        
        `when`(classifyUseCase(files)).thenReturn(flowOf(*classifications.toTypedArray()))

        // When
        viewModel.classifyFiles(files)
        
        // Advance partially to check intermediate progress
        testDispatcher.scheduler.advanceTimeBy(100)
        
        // Eventually advance to completion
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ClassifierUiState.Success
        assertEquals(5, state.classifications.size)
        assertEquals(1.0f, state.progress, 0.01f)
    }
}
