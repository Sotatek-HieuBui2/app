package com.example.smartcleaner.domain.usecase

import com.example.smartcleaner.domain.repository.EmptyFolderRepository
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
 * Unit tests for DeleteEmptyFoldersUseCase
 */
class DeleteEmptyFoldersUseCaseTest {

    @Mock
    private lateinit var repository: EmptyFolderRepository

    private lateinit var useCase: DeleteEmptyFoldersUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = DeleteEmptyFoldersUseCase(repository)
    }

    @Test
    fun `invoke deletes folders successfully`() = runTest {
        // Given
        val folders = listOf(
            File("/test/empty1"),
            File("/test/empty2")
        )
        
        `when`(repository.deleteEmptyFolders(folders)).thenReturn(
            flowOf(
                DeleteEmptyFoldersUseCase.DeleteResult.Progress(1, 0, 2),
                DeleteEmptyFoldersUseCase.DeleteResult.Progress(2, 0, 2),
                DeleteEmptyFoldersUseCase.DeleteResult.Complete(2, 0)
            )
        )

        // When
        val results = useCase(folders).toList()

        // Then
        assertEquals(3, results.size)
        val complete = results.last() as DeleteEmptyFoldersUseCase.DeleteResult.Complete
        assertEquals(2, complete.deletedCount)
        assertEquals(0, complete.failedCount)
    }

    @Test
    fun `invoke handles deletion failures`() = runTest {
        // Given
        val folders = listOf(
            File("/test/empty"),
            File("/system/protected")
        )
        
        `when`(repository.deleteEmptyFolders(folders)).thenReturn(
            flowOf(
                DeleteEmptyFoldersUseCase.DeleteResult.Progress(1, 0, 2),
                DeleteEmptyFoldersUseCase.DeleteResult.Progress(1, 1, 2),
                DeleteEmptyFoldersUseCase.DeleteResult.Complete(1, 1)
            )
        )

        // When
        val results = useCase(folders).toList()

        // Then
        val complete = results.last() as DeleteEmptyFoldersUseCase.DeleteResult.Complete
        assertEquals(1, complete.deletedCount)
        assertEquals(1, complete.failedCount)
    }

    @Test
    fun `invoke handles empty list`() = runTest {
        // Given
        val emptyList = emptyList<File>()
        
        `when`(repository.deleteEmptyFolders(emptyList)).thenReturn(
            flowOf(DeleteEmptyFoldersUseCase.DeleteResult.Complete(0, 0))
        )

        // When
        val results = useCase(emptyList).toList()

        // Then
        assertEquals(1, results.size)
        val complete = results.first() as DeleteEmptyFoldersUseCase.DeleteResult.Complete
        assertEquals(0, complete.deletedCount)
    }

    @Test
    fun `invoke emits progress updates`() = runTest {
        // Given
        val folders = (1..5).map { File("/test/empty$it") }
        val progressUpdates = (1..5).map { i ->
            DeleteEmptyFoldersUseCase.DeleteResult.Progress(i, 0, 5)
        } + DeleteEmptyFoldersUseCase.DeleteResult.Complete(5, 0)
        
        `when`(repository.deleteEmptyFolders(folders)).thenReturn(
            flowOf(*progressUpdates.toTypedArray())
        )

        // When
        val results = useCase(folders).toList()

        // Then
        val progress = results.filterIsInstance<DeleteEmptyFoldersUseCase.DeleteResult.Progress>()
        assertEquals(5, progress.size)
    }

    @Test
    fun `invoke propagates exceptions`() = runTest {
        // Given
        val folders = listOf(File("/test/folder"))
        val exception = RuntimeException("Delete failed")
        
        `when`(repository.deleteEmptyFolders(folders)).thenThrow(exception)

        // When/Then
        try {
            useCase(folders).toList()
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }
}
