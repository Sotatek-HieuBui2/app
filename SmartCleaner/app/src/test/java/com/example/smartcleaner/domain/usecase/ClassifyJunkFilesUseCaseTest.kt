package com.example.smartcleaner.domain.usecase

import com.example.smartcleaner.domain.model.JunkClassification
import com.example.smartcleaner.domain.repository.JunkClassifierRepository
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
 * Unit tests for ClassifyJunkFilesUseCase
 */
class ClassifyJunkFilesUseCaseTest {

    @Mock
    private lateinit var repository: JunkClassifierRepository

    private lateinit var useCase: ClassifyJunkFilesUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ClassifyJunkFilesUseCase(repository)
    }

    @Test
    fun `invoke classifies files successfully`() = runTest {
        // Given
        val files = listOf(
            File("/test/cache.tmp"),
            File("/test/document.doc")
        )
        
        val classifications = listOf(
            JunkClassification(files[0], "CACHE", 0.95f, true, "Cache file"),
            JunkClassification(files[1], "SAFE", 0.90f, false, "Document")
        )
        
        `when`(repository.classifyFiles(files)).thenReturn(
            flowOf(*classifications.toTypedArray())
        )

        // When
        val results = useCase(files).toList()

        // Then
        assertEquals(2, results.size)
        assertEquals("CACHE", results[0].category)
        assertEquals("SAFE", results[1].category)
    }

    @Test
    fun `invoke handles empty file list`() = runTest {
        // Given
        val emptyList = emptyList<File>()
        
        `when`(repository.classifyFiles(emptyList)).thenReturn(flowOf())

        // When
        val results = useCase(emptyList).toList()

        // Then
        assertTrue(results.isEmpty())
    }

    @Test
    fun `invoke processes files sequentially`() = runTest {
        // Given
        val files = (1..5).map { File("/test/file$it.tmp") }
        val classifications = files.map { file ->
            JunkClassification(file, "JUNK", 0.9f, true, "Test")
        }
        
        `when`(repository.classifyFiles(files)).thenReturn(
            flowOf(*classifications.toTypedArray())
        )

        // When
        val results = useCase(files).toList()

        // Then
        assertEquals(5, results.size)
        verify(repository, times(1)).classifyFiles(files)
    }

    @Test
    fun `invoke maintains classification confidence`() = runTest {
        // Given
        val file = File("/test/file.tmp")
        val classification = JunkClassification(
            file = file,
            category = "CACHE",
            confidence = 0.956f, // Specific confidence value
            shouldDelete = true,
            reason = "Cache file"
        )
        
        `when`(repository.classifyFiles(listOf(file))).thenReturn(
            flowOf(classification)
        )

        // When
        val results = useCase(listOf(file)).toList()

        // Then
        assertEquals(0.956f, results[0].confidence, 0.001f)
    }

    @Test
    fun `invoke preserves file paths`() = runTest {
        // Given
        val filePath = "/storage/emulated/0/cache/temp.tmp"
        val file = File(filePath)
        val classification = JunkClassification(
            file = file,
            category = "TEMP",
            confidence = 0.9f,
            shouldDelete = true,
            reason = "Temporary file"
        )
        
        `when`(repository.classifyFiles(listOf(file))).thenReturn(
            flowOf(classification)
        )

        // When
        val results = useCase(listOf(file)).toList()

        // Then
        assertEquals(filePath, results[0].file.absolutePath)
    }

    @Test
    fun `invoke handles classification errors gracefully`() = runTest {
        // Given
        val files = listOf(File("/test/file.tmp"))
        val exception = RuntimeException("Classification failed")
        
        `when`(repository.classifyFiles(files)).thenThrow(exception)

        // When/Then
        try {
            useCase(files).toList()
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Classification failed", e.message)
        }
    }

    @Test
    fun `invoke correctly identifies deletable files`() = runTest {
        // Given
        val files = listOf(
            File("/test/junk.tmp"),
            File("/test/important.doc")
        )
        val classifications = listOf(
            JunkClassification(files[0], "JUNK", 0.95f, true, "Junk"),
            JunkClassification(files[1], "SAFE", 0.90f, false, "Important")
        )
        
        `when`(repository.classifyFiles(files)).thenReturn(
            flowOf(*classifications.toTypedArray())
        )

        // When
        val results = useCase(files).toList()

        // Then
        assertTrue(results[0].shouldDelete)
        assertFalse(results[1].shouldDelete)
    }

    @Test
    fun `invoke provides classification reasons`() = runTest {
        // Given
        val file = File("/test/cache.tmp")
        val expectedReason = "Detected as cache file by ML model"
        val classification = JunkClassification(
            file = file,
            category = "CACHE",
            confidence = 0.95f,
            shouldDelete = true,
            reason = expectedReason
        )
        
        `when`(repository.classifyFiles(listOf(file))).thenReturn(
            flowOf(classification)
        )

        // When
        val results = useCase(listOf(file)).toList()

        // Then
        assertEquals(expectedReason, results[0].reason)
    }

    @Test
    fun `invoke handles large file batches`() = runTest {
        // Given - 100 files
        val files = (1..100).map { File("/test/file$it.tmp") }
        val classifications = files.map { file ->
            JunkClassification(file, "JUNK", 0.9f, true, "Test")
        }
        
        `when`(repository.classifyFiles(files)).thenReturn(
            flowOf(*classifications.toTypedArray())
        )

        // When
        val results = useCase(files).toList()

        // Then
        assertEquals(100, results.size)
    }

    @Test
    fun `invoke supports multiple categories`() = runTest {
        // Given
        val files = listOf(
            File("/test/cache.tmp"),
            File("/test/log.log"),
            File("/test/temp.tmp"),
            File("/test/thumbnail.jpg")
        )
        val classifications = listOf(
            JunkClassification(files[0], "CACHE", 0.95f, true, "Cache"),
            JunkClassification(files[1], "LOG", 0.90f, true, "Log"),
            JunkClassification(files[2], "TEMP", 0.92f, true, "Temp"),
            JunkClassification(files[3], "THUMBNAIL", 0.88f, true, "Thumbnail")
        )
        
        `when`(repository.classifyFiles(files)).thenReturn(
            flowOf(*classifications.toTypedArray())
        )

        // When
        val results = useCase(files).toList()

        // Then
        val categories = results.map { it.category }.toSet()
        assertEquals(4, categories.size)
        assertTrue(categories.contains("CACHE"))
        assertTrue(categories.contains("LOG"))
        assertTrue(categories.contains("TEMP"))
        assertTrue(categories.contains("THUMBNAIL"))
    }
}
