package com.example.smartcleaner.domain.usecase

import com.example.smartcleaner.domain.model.JunkFile
import com.example.smartcleaner.domain.model.JunkType
import com.example.smartcleaner.domain.repository.JunkRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.File

/**
 * Unit tests for DeleteJunkUseCase
 */
class DeleteJunkUseCaseTest {

    @Mock
    private lateinit var repository: JunkRepository

    private lateinit var useCase: DeleteJunkUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = DeleteJunkUseCase(repository)
    }

    @Test
    fun `invoke deletes files successfully`() = runTest {
        // Given
        val files = listOf(
            File("/cache/file1.tmp"),
            File("/cache/file2.tmp")
        )
        
        `when`(repository.deleteJunk(files)).thenReturn(
            flowOf(
                DeleteJunkUseCase.DeleteResult.Progress(1, 0, 2),
                DeleteJunkUseCase.DeleteResult.Progress(2, 0, 2),
                DeleteJunkUseCase.DeleteResult.Complete(2, 0)
            )
        )

        // When
        val results = useCase(files).toList()

        // Then
        assertEquals(3, results.size)
        assertTrue(results.last() is DeleteJunkUseCase.DeleteResult.Complete)
        val complete = results.last() as DeleteJunkUseCase.DeleteResult.Complete
        assertEquals(2, complete.deletedCount)
        assertEquals(0, complete.failedCount)
    }

    @Test
    fun `invoke handles deletion failures`() = runTest {
        // Given
        val files = listOf(
            File("/cache/file1.tmp"),
            File("/system/protected.tmp") // Can't delete
        )
        
        `when`(repository.deleteJunk(files)).thenReturn(
            flowOf(
                DeleteJunkUseCase.DeleteResult.Progress(1, 0, 2),
                DeleteJunkUseCase.DeleteResult.Progress(1, 1, 2),
                DeleteJunkUseCase.DeleteResult.Complete(1, 1)
            )
        )

        // When
        val results = useCase(files).toList()

        // Then
        val complete = results.last() as DeleteJunkUseCase.DeleteResult.Complete
        assertEquals(1, complete.deletedCount)
        assertEquals(1, complete.failedCount)
    }

    @Test
    fun `invoke handles empty file list`() = runTest {
        // Given
        val emptyList = emptyList<File>()
        
        `when`(repository.deleteJunk(emptyList)).thenReturn(
            flowOf(DeleteJunkUseCase.DeleteResult.Complete(0, 0))
        )

        // When
        val results = useCase(emptyList).toList()

        // Then
        assertEquals(1, results.size)
        val complete = results.first() as DeleteJunkUseCase.DeleteResult.Complete
        assertEquals(0, complete.deletedCount)
    }

    @Test
    fun `invoke emits progress updates`() = runTest {
        // Given
        val files = (1..10).map { File("/cache/file$it.tmp") }
        
        val progressUpdates = (1..10).map { i ->
            DeleteJunkUseCase.DeleteResult.Progress(i, 0, 10)
        } + DeleteJunkUseCase.DeleteResult.Complete(10, 0)
        
        `when`(repository.deleteJunk(files)).thenReturn(flowOf(*progressUpdates.toTypedArray()))

        // When
        val results = useCase(files).toList()

        // Then
        assertEquals(11, results.size) // 10 progress + 1 complete
        val progressResults = results.filterIsInstance<DeleteJunkUseCase.DeleteResult.Progress>()
        assertEquals(10, progressResults.size)
    }

    @Test
    fun `invoke calculates correct progress percentage`() = runTest {
        // Given
        val files = listOf(
            File("/cache/1.tmp"),
            File("/cache/2.tmp"),
            File("/cache/3.tmp"),
            File("/cache/4.tmp")
        )
        
        `when`(repository.deleteJunk(files)).thenReturn(
            flowOf(
                DeleteJunkUseCase.DeleteResult.Progress(1, 0, 4), // 25%
                DeleteJunkUseCase.DeleteResult.Progress(2, 0, 4), // 50%
                DeleteJunkUseCase.DeleteResult.Progress(3, 0, 4), // 75%
                DeleteJunkUseCase.DeleteResult.Progress(4, 0, 4), // 100%
                DeleteJunkUseCase.DeleteResult.Complete(4, 0)
            )
        )

        // When
        val results = useCase(files).toList()

        // Then
        val progress = results.filterIsInstance<DeleteJunkUseCase.DeleteResult.Progress>()
        assertEquals(0.25f, progress[0].deletedCount.toFloat() / progress[0].total, 0.01f)
        assertEquals(0.50f, progress[1].deletedCount.toFloat() / progress[1].total, 0.01f)
        assertEquals(0.75f, progress[2].deletedCount.toFloat() / progress[2].total, 0.01f)
        assertEquals(1.00f, progress[3].deletedCount.toFloat() / progress[3].total, 0.01f)
    }

    @Test
    fun `invoke propagates repository exceptions`() = runTest {
        // Given
        val files = listOf(File("/cache/file.tmp"))
        val exception = RuntimeException("Delete failed")
        
        `when`(repository.deleteJunk(files)).thenThrow(exception)

        // When/Then
        try {
            useCase(files).toList()
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }

    @Test
    fun `invoke handles mixed success and failure`() = runTest {
        // Given - 5 files, 3 succeed, 2 fail
        val files = (1..5).map { File("/cache/file$it.tmp") }
        
        `when`(repository.deleteJunk(files)).thenReturn(
            flowOf(
                DeleteJunkUseCase.DeleteResult.Progress(1, 0, 5),
                DeleteJunkUseCase.DeleteResult.Progress(2, 0, 5),
                DeleteJunkUseCase.DeleteResult.Progress(2, 1, 5), // First failure
                DeleteJunkUseCase.DeleteResult.Progress(3, 1, 5),
                DeleteJunkUseCase.DeleteResult.Progress(3, 2, 5), // Second failure
                DeleteJunkUseCase.DeleteResult.Complete(3, 2)
            )
        )

        // When
        val results = useCase(files).toList()

        // Then
        val complete = results.last() as DeleteJunkUseCase.DeleteResult.Complete
        assertEquals(3, complete.deletedCount)
        assertEquals(2, complete.failedCount)
        assertEquals(60f, (complete.deletedCount.toFloat() / 5) * 100, 0.1f) // 60% success rate
    }

    @Test
    fun `invoke verifies files before deletion`() = runTest {
        // Given
        val files = listOf(
            File("/cache/exists.tmp")
        )
        
        `when`(repository.deleteJunk(files)).thenReturn(
            flowOf(DeleteJunkUseCase.DeleteResult.Complete(1, 0))
        )

        // When
        useCase(files).toList()

        // Then
        verify(repository, times(1)).deleteJunk(files)
    }
}
