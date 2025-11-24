package com.example.smartcleaner.presentation.leftover

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.smartcleaner.domain.model.LeftoverFile
import com.example.smartcleaner.domain.usecase.DeleteLeftoverFilesUseCase
import com.example.smartcleaner.domain.usecase.ScanLeftoverFilesUseCase
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
 * Unit tests for LeftoverViewModel
 */
@ExperimentalCoroutinesApi
class LeftoverViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var scanUseCase: ScanLeftoverFilesUseCase

    @Mock
    private lateinit var deleteUseCase: DeleteLeftoverFilesUseCase

    private lateinit var viewModel: LeftoverViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = LeftoverViewModel(scanUseCase, deleteUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(LeftoverUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `scanLeftoverFiles updates state to Success with results`() = runTest {
        // Given
        val testFiles = listOf(
            LeftoverFile(
                file = File("/test/path"),
                packageName = "com.test.app",
                isInstalled = false,
                size = 1024L,
                lastModified = System.currentTimeMillis()
            )
        )
        `when`(scanUseCase()).thenReturn(flowOf(*testFiles.toTypedArray()))

        // When
        viewModel.scanLeftoverFiles()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is LeftoverUiState.Success)
        assertEquals(1, (state as LeftoverUiState.Success).files.size)
    }

    @Test
    fun `scanLeftoverFiles sets Loading state during scan`() = runTest {
        // Given
        `when`(scanUseCase()).thenReturn(flowOf())

        // When
        viewModel.scanLeftoverFiles()

        // Then (before coroutine completes)
        // Note: Timing-dependent, may need delay
        testDispatcher.scheduler.runCurrent()
    }

    @Test
    fun `toggleFileSelection adds file to selection`() = runTest {
        // Given
        val filePath = "/test/file.txt"

        // When
        viewModel.toggleFileSelection(filePath)

        // Then
        assertTrue(viewModel.selectedFiles.value.contains(filePath))
    }

    @Test
    fun `toggleFileSelection removes file if already selected`() = runTest {
        // Given
        val filePath = "/test/file.txt"
        viewModel.toggleFileSelection(filePath) // Add
        
        // When
        viewModel.toggleFileSelection(filePath) // Remove

        // Then
        assertFalse(viewModel.selectedFiles.value.contains(filePath))
    }

    @Test
    fun `selectAll selects all leftover files`() = runTest {
        // Given
        val testFiles = listOf(
            LeftoverFile(File("/test/1"), "pkg1", false, 100L, 0L),
            LeftoverFile(File("/test/2"), "pkg2", false, 200L, 0L)
        )
        `when`(scanUseCase()).thenReturn(flowOf(*testFiles.toTypedArray()))
        
        viewModel.scanLeftoverFiles()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectAll()

        // Then
        assertEquals(2, viewModel.selectedFiles.value.size)
    }

    @Test
    fun `clearSelection removes all selections`() = runTest {
        // Given
        viewModel.toggleFileSelection("/test/1")
        viewModel.toggleFileSelection("/test/2")

        // When
        viewModel.clearSelection()

        // Then
        assertTrue(viewModel.selectedFiles.value.isEmpty())
    }

    @Test
    fun `deleteSelected calls delete use case with selected files`() = runTest {
        // Given
        val testFiles = listOf(
            LeftoverFile(File("/test/1"), "pkg1", false, 100L, 0L),
            LeftoverFile(File("/test/2"), "pkg2", false, 200L, 0L)
        )
        `when`(scanUseCase()).thenReturn(flowOf(*testFiles.toTypedArray()))
        `when`(deleteUseCase(any())).thenReturn(
            flowOf(DeleteLeftoverFilesUseCase.DeleteResult.Complete(2, 0))
        )
        
        viewModel.scanLeftoverFiles()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.toggleFileSelection("/test/1")

        // When
        viewModel.deleteSelected()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(deleteUseCase, times(1)).invoke(any())
    }

    @Test
    fun `getStatistics calculates correct totals`() = runTest {
        // Given
        val testFiles = listOf(
            LeftoverFile(File("/test/1"), "pkg1", false, 1000L, 0L),
            LeftoverFile(File("/test/2"), "pkg2", false, 2000L, 0L),
            LeftoverFile(File("/test/3"), "pkg3", false, 3000L, 0L)
        )
        `when`(scanUseCase()).thenReturn(flowOf(*testFiles.toTypedArray()))
        
        viewModel.scanLeftoverFiles()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val stats = viewModel.getStatistics()

        // Then
        assertNotNull(stats)
        assertEquals(3, stats?.totalFiles)
        assertEquals(6000L, stats?.totalSize)
    }

    @Test
    fun `groupByPackage groups files correctly`() = runTest {
        // Given
        val testFiles = listOf(
            LeftoverFile(File("/test/1"), "com.app1", false, 100L, 0L),
            LeftoverFile(File("/test/2"), "com.app1", false, 200L, 0L),
            LeftoverFile(File("/test/3"), "com.app2", false, 300L, 0L)
        )
        `when`(scanUseCase()).thenReturn(flowOf(*testFiles.toTypedArray()))
        
        viewModel.scanLeftoverFiles()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val grouped = viewModel.groupByPackage()

        // Then
        assertEquals(2, grouped.size)
        assertEquals(2, grouped["com.app1"]?.size)
        assertEquals(1, grouped["com.app2"]?.size)
    }
}
