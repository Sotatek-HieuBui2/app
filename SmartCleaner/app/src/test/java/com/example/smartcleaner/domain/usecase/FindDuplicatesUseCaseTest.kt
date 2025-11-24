package com.example.smartcleaner.domain.usecase

import com.example.smartcleaner.domain.model.DuplicateFile
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
 * Unit tests for FindDuplicatesUseCase
 */
class FindDuplicatesUseCaseTest {

    @Mock
    private lateinit var repository: DuplicateFinderRepository

    private lateinit var useCase: FindDuplicatesUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = FindDuplicatesUseCase(repository)
    }

    @Test
    fun `invoke finds duplicates successfully`() = runTest {
        // Given
        val duplicateGroup = DuplicateFile(
            hash = "abc123",
            files = listOf(
                File("/test/file1.txt"),
                File("/test/file2.txt")
            ),
            similarity = 1.0f
        )
        
        `when`(repository.findDuplicates(true, 0.95f)).thenReturn(
            flowOf(
                FindDuplicatesUseCase.ScanResult.Progress(0.5f),
                FindDuplicatesUseCase.ScanResult.Complete(listOf(duplicateGroup))
            )
        )

        // When
        val results = useCase(true, 0.95f).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.last() is FindDuplicatesUseCase.ScanResult.Complete)
        val complete = results.last() as FindDuplicatesUseCase.ScanResult.Complete
        assertEquals(1, complete.duplicates.size)
    }

    @Test
    fun `invoke emits progress updates`() = runTest {
        // Given
        `when`(repository.findDuplicates(true, 0.95f)).thenReturn(
            flowOf(
                FindDuplicatesUseCase.ScanResult.Progress(0.1f),
                FindDuplicatesUseCase.ScanResult.Progress(0.3f),
                FindDuplicatesUseCase.ScanResult.Progress(0.7f),
                FindDuplicatesUseCase.ScanResult.Progress(1.0f),
                FindDuplicatesUseCase.ScanResult.Complete(emptyList())
            )
        )

        // When
        val results = useCase(true, 0.95f).toList()

        // Then
        val progressUpdates = results.filterIsInstance<FindDuplicatesUseCase.ScanResult.Progress>()
        assertEquals(4, progressUpdates.size)
        assertEquals(0.1f, progressUpdates[0].percentage, 0.01f)
        assertEquals(0.3f, progressUpdates[1].percentage, 0.01f)
        assertEquals(0.7f, progressUpdates[2].percentage, 0.01f)
        assertEquals(1.0f, progressUpdates[3].percentage, 0.01f)
    }

    @Test
    fun `invoke handles empty results`() = runTest {
        // Given
        `when`(repository.findDuplicates(false, 1.0f)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(emptyList()))
        )

        // When
        val results = useCase(false, 1.0f).toList()

        // Then
        assertEquals(1, results.size)
        val complete = results.first() as FindDuplicatesUseCase.ScanResult.Complete
        assertTrue(complete.duplicates.isEmpty())
    }

    @Test
    fun `invoke respects similarity threshold`() = runTest {
        // Given
        val threshold = 0.85f
        
        `when`(repository.findDuplicates(true, threshold)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(emptyList()))
        )

        // When
        useCase(true, threshold).toList()

        // Then
        verify(repository).findDuplicates(true, threshold)
    }

    @Test
    fun `invoke handles repository errors`() = runTest {
        // Given
        val exception = RuntimeException("Scan failed")
        
        `when`(repository.findDuplicates(true, 0.95f)).thenThrow(exception)

        // When/Then
        try {
            useCase(true, 0.95f).toList()
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Scan failed", e.message)
        }
    }

    @Test
    fun `invoke filters by image inclusion flag`() = runTest {
        // Given
        `when`(repository.findDuplicates(false, 0.95f)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(emptyList()))
        )

        // When
        useCase(false, 0.95f).toList()

        // Then
        verify(repository).findDuplicates(false, 0.95f)
    }

    @Test
    fun `invoke handles multiple duplicate groups`() = runTest {
        // Given
        val groups = listOf(
            DuplicateFile("hash1", listOf(File("/a/1.txt"), File("/a/2.txt")), 1.0f),
            DuplicateFile("hash2", listOf(File("/b/1.txt"), File("/b/2.txt")), 1.0f),
            DuplicateFile("hash3", listOf(File("/c/1.txt"), File("/c/2.txt"), File("/c/3.txt")), 1.0f)
        )
        
        `when`(repository.findDuplicates(true, 0.95f)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(groups))
        )

        // When
        val results = useCase(true, 0.95f).toList()

        // Then
        val complete = results.last() as FindDuplicatesUseCase.ScanResult.Complete
        assertEquals(3, complete.duplicates.size)
        assertEquals(2, complete.duplicates[0].files.size)
        assertEquals(2, complete.duplicates[1].files.size)
        assertEquals(3, complete.duplicates[2].files.size)
    }

    @Test
    fun `invoke validates threshold range`() = runTest {
        // Given - Valid threshold
        val validThreshold = 0.95f
        
        `when`(repository.findDuplicates(true, validThreshold)).thenReturn(
            flowOf(FindDuplicatesUseCase.ScanResult.Complete(emptyList()))
        )

        // When
        useCase(true, validThreshold).toList()

        // Then
        verify(repository).findDuplicates(true, validThreshold)
    }
}
