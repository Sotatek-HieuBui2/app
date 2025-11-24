package com.example.smartcleaner.presentation.duplicate

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.smartcleaner.domain.model.DuplicateFile
import com.example.smartcleaner.domain.usecase.DeleteDuplicatesUseCase
import com.example.smartcleaner.domain.usecase.FindDuplicatesUseCase
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
 * Unit tests for DuplicateViewModel
 */
@ExperimentalCoroutinesApi
class DuplicateViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var findUseCase: FindDuplicatesUseCase

    @Mock
    private lateinit var deleteUseCase: DeleteDuplicatesUseCase

    private lateinit var viewModel: DuplicateViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = DuplicateViewModel(findUseCase, deleteUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(DuplicateUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `scanForDuplicates emits progress and complete`() = runTest {
        // Given
        val duplicateGroup = DuplicateFile(
            hash = "abc123",
            files = listOf(
                File("/test/file1.txt"),
                File("/test/file2.txt")
            ),
            similarity = 1.0f
        )
        
        `when`(findUseCase(true, 0.95f)).thenReturn(
            flowOf(
                FindDuplicatesUseCase.ScanResult.Progress(0.5f),
                FindDuplicatesUseCase.ScanResult.Progress(1.0f),
                FindDuplicatesUseCase.ScanResult.Complete(listOf(duplicateGroup))
            )
        )

        // When
        viewModel.scanForDuplicates(true, 0.95f)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is DuplicateUiState.Success)
        assertEquals(1, (state as DuplicateUiState.Success).duplicates.size)
        assertEquals(1.0f, viewModel.scanProgress.value, 0.01f)
    }

    @Test
    fun `toggleFileSelection adds and removes files`() = runTest {
        // Given
        val filePath = "/test/file.txt"

        // When - Add
        viewModel.toggleFileSelection(filePath)

        // Then
        assertTrue(viewModel.selectedFiles.value.contains(filePath))

        // When - Remove
        viewModel.toggleFileSelection(filePath)

        // Then
        assertFalse(viewModel.selectedFiles.value.contains(filePath))
    }

    @Test
    fun `selectGroupKeepFirst selects all except first file`() = runTest {
        // Given
        val group = DuplicateFile(
            hash = "abc123",
            files = listOf(
                File("/test/file1.txt"),
                File("/test/file2.txt"),
                File("/test/file3.txt")
            ),
            similarity = 1.0f
        )
        
        `when`(findUseCase(true, 0.95f)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(listOf(group)))
        )
        
        viewModel.scanForDuplicates(true, 0.95f)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectGroupKeepFirst(group)

        // Then
        assertEquals(2, viewModel.selectedFiles.value.size)
        assertFalse(viewModel.selectedFiles.value.contains("/test/file1.txt"))
        assertTrue(viewModel.selectedFiles.value.contains("/test/file2.txt"))
        assertTrue(viewModel.selectedFiles.value.contains("/test/file3.txt"))
    }

    @Test
    fun `selectGroupKeepLargest selects all except largest file`() = runTest {
        // Given
        val file1 = mock(File::class.java).apply {
            `when`(absolutePath).thenReturn("/test/small.txt")
            `when`(length()).thenReturn(100L)
        }
        val file2 = mock(File::class.java).apply {
            `when`(absolutePath).thenReturn("/test/large.txt")
            `when`(length()).thenReturn(1000L)
        }
        val file3 = mock(File::class.java).apply {
            `when`(absolutePath).thenReturn("/test/medium.txt")
            `when`(length()).thenReturn(500L)
        }
        
        val group = DuplicateFile(
            hash = "abc123",
            files = listOf(file1, file2, file3),
            similarity = 1.0f
        )
        
        `when`(findUseCase(true, 0.95f)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(listOf(group)))
        )
        
        viewModel.scanForDuplicates(true, 0.95f)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectGroupKeepLargest(group)

        // Then
        assertEquals(2, viewModel.selectedFiles.value.size)
        assertTrue(viewModel.selectedFiles.value.contains("/test/small.txt"))
        assertFalse(viewModel.selectedFiles.value.contains("/test/large.txt")) // Kept
        assertTrue(viewModel.selectedFiles.value.contains("/test/medium.txt"))
    }

    @Test
    fun `clearSelection removes all selections`() = runTest {
        // Given
        viewModel.toggleFileSelection("/test/1.txt")
        viewModel.toggleFileSelection("/test/2.txt")

        // When
        viewModel.clearSelection()

        // Then
        assertTrue(viewModel.selectedFiles.value.isEmpty())
    }

    @Test
    fun `deleteDuplicates calls delete use case`() = runTest {
        // Given
        val group = DuplicateFile(
            hash = "abc123",
            files = listOf(
                File("/test/file1.txt"),
                File("/test/file2.txt")
            ),
            similarity = 1.0f
        )
        
        `when`(findUseCase(true, 0.95f)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(listOf(group)))
        )
        
        `when`(deleteUseCase(anyList())).thenReturn(
            flowOf(
                DeleteDuplicatesUseCase.DeleteResult.Progress(1, 2),
                DeleteDuplicatesUseCase.DeleteResult.Complete(1, 0)
            )
        )
        
        viewModel.scanForDuplicates(true, 0.95f)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.toggleFileSelection("/test/file1.txt")

        // When
        viewModel.deleteDuplicates()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(deleteUseCase, times(1)).invoke(anyList())
        assertTrue(viewModel.selectedFiles.value.isEmpty()) // Selection cleared after delete
    }

    @Test
    fun `getStatistics calculates correct metrics`() = runTest {
        // Given
        val file1 = mock(File::class.java).apply {
            `when`(absolutePath).thenReturn("/test/file1.txt")
            `when`(length()).thenReturn(1000L)
        }
        val file2 = mock(File::class.java).apply {
            `when`(absolutePath).thenReturn("/test/file2.txt")
            `when`(length()).thenReturn(1000L)
        }
        val file3 = mock(File::class.java).apply {
            `when`(absolutePath).thenReturn("/test/file3.txt")
            `when`(length()).thenReturn(2000L)
        }
        val file4 = mock(File::class.java).apply {
            `when`(absolutePath).thenReturn("/test/file4.txt")
            `when`(length()).thenReturn(2000L)
        }
        
        val groups = listOf(
            DuplicateFile("hash1", listOf(file1, file2), 1.0f),
            DuplicateFile("hash2", listOf(file3, file4), 1.0f)
        )
        
        `when`(findUseCase(true, 0.95f)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(groups))
        )
        
        viewModel.scanForDuplicates(true, 0.95f)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.toggleFileSelection("/test/file1.txt")

        // When
        val stats = viewModel.getStatistics()

        // Then
        assertNotNull(stats)
        assertEquals(2, stats?.totalGroups)
        assertEquals(4, stats?.totalFiles)
        assertEquals(3000L, stats?.wastedSpace) // 1000 + 2000 (keep one copy per group)
        assertEquals(1, stats?.selectedFiles)
        assertEquals(1000L, stats?.selectedSize)
    }

    @Test
    fun `scanForDuplicates handles errors`() = runTest {
        // Given
        val exception = RuntimeException("Scan failed")
        
        `when`(findUseCase(true, 0.95f)).thenThrow(exception)

        // When
        viewModel.scanForDuplicates(true, 0.95f)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is DuplicateUiState.Error)
        assertEquals("Scan failed", (state as DuplicateUiState.Error).message)
    }

    @Test
    fun `deleteDuplicates handles empty selection`() = runTest {
        // Given
        val group = DuplicateFile(
            hash = "abc123",
            files = listOf(File("/test/file1.txt")),
            similarity = 1.0f
        )
        
        `when`(findUseCase(true, 0.95f)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(listOf(group)))
        )
        
        viewModel.scanForDuplicates(true, 0.95f)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - Try to delete with nothing selected
        viewModel.deleteDuplicates()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should not call delete use case
        verify(deleteUseCase, never()).invoke(anyList())
    }

    @Test
    fun `scanProgress updates during scan`() = runTest {
        // Given
        val progressUpdates = listOf(0.1f, 0.3f, 0.5f, 0.7f, 1.0f)
        val flows = progressUpdates.map { FindDuplicatesUseCase.ScanResult.Progress(it) } +
                    FindDuplicatesUseCase.ScanResult.Complete(emptyList())
        
        `when`(findUseCase(true, 0.95f)).thenReturn(flowOf(*flows.toTypedArray()))

        // When
        viewModel.scanForDuplicates(true, 0.95f)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1.0f, viewModel.scanProgress.value, 0.01f)
    }

    @Test
    fun `getStatistics returns null when not in Success state`() {
        // Given - Idle state
        
        // When
        val stats = viewModel.getStatistics()

        // Then
        assertNull(stats)
    }
}
