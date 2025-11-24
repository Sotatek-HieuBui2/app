package com.example.smartcleaner.domain.usecase

import com.example.smartcleaner.domain.repository.DuplicateFinderRepository
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
 * Unit tests for DeleteDuplicatesUseCase
 */
class DeleteDuplicatesUseCaseTest {

    @Mock
    private lateinit var repository: DuplicateFinderRepository

    private lateinit var useCase: DeleteDuplicatesUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = DeleteDuplicatesUseCase(repository)
    }

    @Test
    fun `invoke deletes files successfully`() = runTest {
        // Given
        val files = listOf(
            File("/test/dup1.txt"),
            File("/test/dup2.txt")
        )
        
        `when`(repository.deleteDuplicates(files)).thenReturn(
            flowOf(
                DeleteDuplicatesUseCase.DeleteResult.Progress(1, 2),
                DeleteDuplicatesUseCase.DeleteResult.Progress(2, 2),
                DeleteDuplicatesUseCase.DeleteResult.Complete(2, 0)
            )
        )

        // When
        val results = useCase(files).toList()

        // Then
        assertEquals(3, results.size)
        val complete = results.last() as DeleteDuplicatesUseCase.DeleteResult.Complete
        assertEquals(2, complete.deletedCount)
        assertEquals(0, complete.failedCount)
    }

    @Test
    fun `invoke handles partial failures`() = runTest {
        // Given
        val files = listOf(
            File("/test/dup1.txt"),
            File("/test/dup2.txt"),
            File("/system/protected.txt")
        )
        
        `when`(repository.deleteDuplicates(files)).thenReturn(
            flowOf(
                DeleteDuplicatesUseCase.DeleteResult.Progress(1, 3),
                DeleteDuplicatesUseCase.DeleteResult.Progress(2, 3),
                DeleteDuplicatesUseCase.DeleteResult.Complete(2, 1)
            )
        )

        // When
        val results = useCase(files).toList()

        // Then
        val complete = results.last() as DeleteDuplicatesUseCase.DeleteResult.Complete
        assertEquals(2, complete.deletedCount)
        assertEquals(1, complete.failedCount)
    }

    @Test
    fun `invoke handles empty file list`() = runTest {
        // Given
        val emptyList = emptyList<File>()
        
        `when`(repository.deleteDuplicates(emptyList)).thenReturn(
            flowOf(DeleteDuplicatesUseCase.DeleteResult.Complete(0, 0))
        )

        // When
        val results = useCase(emptyList).toList()

        // Then
        assertEquals(1, results.size)
        val complete = results.first() as DeleteDuplicatesUseCase.DeleteResult.Complete
        assertEquals(0, complete.deletedCount)
        assertEquals(0, complete.failedCount)
    }

    @Test
    fun `invoke emits progress for each file`() = runTest {
        // Given
        val files = (1..5).map { File("/test/file$it.txt") }
        val progressUpdates = (1..5).map { i ->
            DeleteDuplicatesUseCase.DeleteResult.Progress(i, 5)
        } + DeleteDuplicatesUseCase.DeleteResult.Complete(5, 0)
        
        `when`(repository.deleteDuplicates(files)).thenReturn(
            flowOf(*progressUpdates.toTypedArray())
        )

        // When
        val results = useCase(files).toList()

        // Then
        assertEquals(6, results.size) // 5 progress + 1 complete
        val progress = results.filterIsInstance<DeleteDuplicatesUseCase.DeleteResult.Progress>()
        assertEquals(5, progress.size)
    }

    @Test
    fun `invoke calculates correct progress percentage`() = runTest {
        // Given
        val files = (1..4).map { File("/test/file$it.txt") }
        
        `when`(repository.deleteDuplicates(files)).thenReturn(
            flowOf(
                DeleteDuplicatesUseCase.DeleteResult.Progress(1, 4), // 25%
                DeleteDuplicatesUseCase.DeleteResult.Progress(2, 4), // 50%
                DeleteDuplicatesUseCase.DeleteResult.Progress(3, 4), // 75%
                DeleteDuplicatesUseCase.DeleteResult.Progress(4, 4), // 100%
                DeleteDuplicatesUseCase.DeleteResult.Complete(4, 0)
            )
        )

        // When
        val results = useCase(files).toList()

        // Then
        val progress = results.filterIsInstance<DeleteDuplicatesUseCase.DeleteResult.Progress>()
        assertEquals(0.25f, progress[0].deletedCount.toFloat() / progress[0].total, 0.01f)
        assertEquals(0.50f, progress[1].deletedCount.toFloat() / progress[1].total, 0.01f)
        assertEquals(0.75f, progress[2].deletedCount.toFloat() / progress[2].total, 0.01f)
        assertEquals(1.00f, progress[3].deletedCount.toFloat() / progress[3].total, 0.01f)
    }

    @Test
    fun `invoke propagates repository exceptions`() = runTest {
        // Given
        val files = listOf(File("/test/file.txt"))
        val exception = RuntimeException("Delete failed")
        
        `when`(repository.deleteDuplicates(files)).thenThrow(exception)

        // When/Then
        try {
            useCase(files).toList()
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }

    @Test
    fun `invoke handles all files failing`() = runTest {
        // Given
        val files = listOf(
            File("/system/protected1.txt"),
            File("/system/protected2.txt")
        )
        
        `when`(repository.deleteDuplicates(files)).thenReturn(
            flowOf(DeleteDuplicatesUseCase.DeleteResult.Complete(0, 2))
        )

        // When
        val results = useCase(files).toList()

        // Then
        val complete = results.last() as DeleteDuplicatesUseCase.DeleteResult.Complete
        assertEquals(0, complete.deletedCount)
        assertEquals(2, complete.failedCount)
    }
}
