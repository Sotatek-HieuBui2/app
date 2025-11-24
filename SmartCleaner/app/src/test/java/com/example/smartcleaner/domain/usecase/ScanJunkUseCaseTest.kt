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
 * Unit tests for ScanJunkUseCase
 */
class ScanJunkUseCaseTest {

    @Mock
    private lateinit var repository: JunkRepository

    private lateinit var useCase: ScanJunkUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ScanJunkUseCase(repository)
    }

    @Test
    fun `invoke scans all junk types`() = runTest {
        // Given
        val cacheFiles = listOf(
            JunkFile(
                file = File("/cache/app_cache"),
                type = JunkType.APP_CACHE,
                packageName = "com.example.app",
                size = 1024L
            )
        )
        val tempFiles = listOf(
            JunkFile(
                file = File("/tmp/temp.tmp"),
                type = JunkType.TEMP_FILES,
                packageName = null,
                size = 512L
            )
        )
        
        `when`(repository.scanJunk()).thenReturn(
            flowOf(*cacheFiles.toTypedArray(), *tempFiles.toTypedArray())
        )

        // When
        val results = useCase().toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.type == JunkType.APP_CACHE })
        assertTrue(results.any { it.type == JunkType.TEMP_FILES })
        verify(repository, times(1)).scanJunk()
    }

    @Test
    fun `invoke groups files by type correctly`() = runTest {
        // Given
        val testFiles = listOf(
            JunkFile(File("/cache/1"), JunkType.APP_CACHE, "app1", 100L),
            JunkFile(File("/cache/2"), JunkType.APP_CACHE, "app2", 200L),
            JunkFile(File("/tmp/1"), JunkType.TEMP_FILES, null, 300L)
        )
        
        `when`(repository.scanJunk()).thenReturn(flowOf(*testFiles.toTypedArray()))

        // When
        val results = useCase().toList()

        // Then
        val cacheCount = results.count { it.type == JunkType.APP_CACHE }
        val tempCount = results.count { it.type == JunkType.TEMP_FILES }
        
        assertEquals(2, cacheCount)
        assertEquals(1, tempCount)
    }

    @Test
    fun `invoke calculates total size correctly`() = runTest {
        // Given
        val testFiles = listOf(
            JunkFile(File("/cache/1"), JunkType.APP_CACHE, "app1", 1000L),
            JunkFile(File("/cache/2"), JunkType.APP_CACHE, "app2", 2000L),
            JunkFile(File("/tmp/1"), JunkType.TEMP_FILES, null, 3000L)
        )
        
        `when`(repository.scanJunk()).thenReturn(flowOf(*testFiles.toTypedArray()))

        // When
        val results = useCase().toList()
        val totalSize = results.sumOf { it.size }

        // Then
        assertEquals(6000L, totalSize)
    }

    @Test
    fun `invoke handles scan with no results`() = runTest {
        // Given
        `when`(repository.scanJunk()).thenReturn(flowOf())

        // When
        val results = useCase().toList()

        // Then
        assertTrue(results.isEmpty())
    }

    @Test
    fun `invoke handles large file counts efficiently`() = runTest {
        // Given
        val largeFileList = (1..1000).map { i ->
            JunkFile(
                file = File("/cache/file$i"),
                type = JunkType.APP_CACHE,
                packageName = "com.app$i",
                size = 100L * i
            )
        }
        
        `when`(repository.scanJunk()).thenReturn(flowOf(*largeFileList.toTypedArray()))

        // When
        val startTime = System.currentTimeMillis()
        val results = useCase().toList()
        val duration = System.currentTimeMillis() - startTime

        // Then
        assertEquals(1000, results.size)
        // Should complete quickly (< 1 second for memory operations)
        assertTrue(duration < 1000)
    }
}
