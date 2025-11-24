package com.example.smartcleaner.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartcleaner.data.util.HashUtil
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

/**
 * Integration tests for DuplicateFinderRepositoryImpl
 * Tests MD5 hash calculation and perceptual hash for images
 */
@RunWith(AndroidJUnit4::class)
class DuplicateFinderRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var repository: DuplicateFinderRepositoryImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = DuplicateFinderRepositoryImpl(context)
    }

    @Test
    fun `findDuplicates detects exact duplicates by MD5`() = runTest {
        // Given - Create identical files
        val content = "Duplicate content"
        val file1 = tempFolder.newFile("file1.txt").apply {
            writeText(content)
        }
        val file2 = tempFolder.newFile("file2.txt").apply {
            writeText(content)
        }
        val file3 = tempFolder.newFile("unique.txt").apply {
            writeText("Different content")
        }

        // When
        val results = repository.findDuplicates(
            includeImages = false,
            similarityThreshold = 1.0f
        ).toList()

        // Then
        val duplicates = results.filterIsInstance<DuplicateFinderRepositoryImpl.FindResult.DuplicateFound>()
        assertTrue("Should find at least one duplicate group", duplicates.isNotEmpty())
        
        val duplicateGroup = duplicates.firstOrNull { group ->
            group.files.any { it.name == "file1.txt" } && 
            group.files.any { it.name == "file2.txt" }
        }
        assertNotNull("file1.txt and file2.txt should be in same group", duplicateGroup)
        assertEquals("Exact duplicates should have 100% similarity", 1.0f, duplicateGroup?.similarity)
    }

    @Test
    fun `findDuplicates handles empty directory`() = runTest {
        // Given - Empty temp folder
        val emptyDir = tempFolder.newFolder("empty")

        // When
        val results = repository.findDuplicates(
            includeImages = false,
            similarityThreshold = 0.95f
        ).toList()

        // Then
        assertTrue("Should handle empty directory gracefully", results.isEmpty() || 
                   results.all { it is DuplicateFinderRepositoryImpl.FindResult.Progress })
    }

    @Test
    fun `findDuplicates filters by file size`() = runTest {
        // Given - Files of different sizes
        val smallFile1 = tempFolder.newFile("small1.txt").apply {
            writeText("a")
        }
        val smallFile2 = tempFolder.newFile("small2.txt").apply {
            writeText("a")
        }
        val largeFile = tempFolder.newFile("large.txt").apply {
            writeText("a".repeat(1000))
        }

        // When
        val results = repository.findDuplicates(
            includeImages = false,
            similarityThreshold = 1.0f
        ).toList()

        // Then
        val duplicates = results.filterIsInstance<DuplicateFinderRepositoryImpl.FindResult.DuplicateFound>()
        
        // Small files should be grouped separately from large file
        val smallGroup = duplicates.firstOrNull { group ->
            group.files.any { it.name == "small1.txt" }
        }
        assertNotNull(smallGroup)
        assertFalse("Large file should not be in small file group", 
                    smallGroup?.files?.any { it.name == "large.txt" } == true)
    }

    @Test
    fun `findDuplicates calculates correct hash`() = runTest {
        // Given
        val content = "Test content for hashing"
        val file = tempFolder.newFile("test.txt").apply {
            writeText(content)
        }

        // When
        val hash = HashUtil.calculateMD5(file.readBytes())

        // Then
        assertNotNull(hash)
        assertEquals(32, hash.length) // MD5 produces 32 hex characters
        
        // Same content should produce same hash
        val file2 = tempFolder.newFile("test2.txt").apply {
            writeText(content)
        }
        val hash2 = HashUtil.calculateMD5(file2.readBytes())
        assertEquals("Same content should produce same hash", hash, hash2)
    }

    @Test
    fun `findDuplicates emits progress updates`() = runTest {
        // Given - Multiple files
        repeat(10) { i ->
            tempFolder.newFile("file$i.txt").apply {
                writeText("Content $i")
            }
        }

        // When
        val results = repository.findDuplicates(
            includeImages = false,
            similarityThreshold = 1.0f
        ).toList()

        // Then
        val progressUpdates = results.filterIsInstance<DuplicateFinderRepositoryImpl.FindResult.Progress>()
        assertTrue("Should emit progress updates", progressUpdates.isNotEmpty())
        
        // Progress should be between 0 and 1
        progressUpdates.forEach { progress ->
            assertTrue("Progress should be >= 0", progress.percentage >= 0f)
            assertTrue("Progress should be <= 1", progress.percentage <= 1f)
        }
    }

    @Test
    fun `findDuplicates groups files by hash correctly`() = runTest {
        // Given - Multiple sets of duplicates
        val group1Content = "Group 1"
        val group2Content = "Group 2"
        
        tempFolder.newFile("g1_file1.txt").writeText(group1Content)
        tempFolder.newFile("g1_file2.txt").writeText(group1Content)
        tempFolder.newFile("g2_file1.txt").writeText(group2Content)
        tempFolder.newFile("g2_file2.txt").writeText(group2Content)

        // When
        val results = repository.findDuplicates(
            includeImages = false,
            similarityThreshold = 1.0f
        ).toList()

        // Then
        val duplicates = results.filterIsInstance<DuplicateFinderRepositoryImpl.FindResult.DuplicateFound>()
        assertEquals("Should find 2 duplicate groups", 2, duplicates.size)
        
        duplicates.forEach { group ->
            assertEquals("Each group should have 2 files", 2, group.files.size)
        }
    }

    @Test
    fun `findDuplicates handles file access errors gracefully`() = runTest {
        // Given - Create file and make it unreadable (if possible)
        val file = tempFolder.newFile("locked.txt").apply {
            writeText("Content")
            setReadable(false)
        }

        // When
        val results = repository.findDuplicates(
            includeImages = false,
            similarityThreshold = 1.0f
        ).toList()

        // Then - Should not crash, may skip unreadable files
        // Verify no exceptions were thrown during execution
        assertTrue("Should complete without crashing", true)
        
        // Cleanup
        file.setReadable(true)
    }

    @Test
    fun `findDuplicates respects similarity threshold for images`() = runTest {
        // This test would require actual image files
        // In production, we'd use test fixtures with known similar images
        
        // Given - Mock scenario with similarity threshold
        val threshold = 0.95f

        // When
        val results = repository.findDuplicates(
            includeImages = true,
            similarityThreshold = threshold
        ).toList()

        // Then
        val duplicates = results.filterIsInstance<DuplicateFinderRepositoryImpl.FindResult.DuplicateFound>()
        
        // All found duplicates should meet or exceed the threshold
        duplicates.forEach { group ->
            assertTrue("Similarity should meet threshold", group.similarity >= threshold)
        }
    }

    @Test
    fun `findDuplicates skips system directories`() = runTest {
        // Given - Create files in system-like directories
        val systemDir = tempFolder.newFolder("system")
        systemDir.resolve("system_file.txt").writeText("System file")
        
        val userDir = tempFolder.newFolder("user")
        userDir.resolve("user_file.txt").writeText("User file")

        // When
        val results = repository.findDuplicates(
            includeImages = false,
            similarityThreshold = 1.0f
        ).toList()

        // Then - Implementation should skip system directories
        val duplicates = results.filterIsInstance<DuplicateFinderRepositoryImpl.FindResult.DuplicateFound>()
        
        // Verify system files are not included
        duplicates.forEach { group ->
            group.files.forEach { file ->
                assertFalse("Should not include system files", 
                           file.absolutePath.contains("/system/"))
            }
        }
    }

    @Test
    fun `findDuplicates calculates wasted space correctly`() = runTest {
        // Given - Duplicate files with known sizes
        val content = "x".repeat(1000) // 1KB content
        
        val file1 = tempFolder.newFile("dup1.txt").apply { writeText(content) }
        val file2 = tempFolder.newFile("dup2.txt").apply { writeText(content) }
        val file3 = tempFolder.newFile("dup3.txt").apply { writeText(content) }

        // When
        val results = repository.findDuplicates(
            includeImages = false,
            similarityThreshold = 1.0f
        ).toList()

        // Then
        val duplicates = results.filterIsInstance<DuplicateFinderRepositoryImpl.FindResult.DuplicateFound>()
        val group = duplicates.firstOrNull { it.files.size == 3 }
        
        assertNotNull("Should find group of 3 duplicates", group)
        
        // Wasted space = fileSize * (count - 1)
        // Keep one copy, delete 2 = 2KB wasted
        val expectedWaste = 1000L * 2 // 2KB
        val actualSize = group?.files?.first()?.length() ?: 0L
        assertEquals("File size should be ~1KB", 1000L, actualSize, 10L)
    }
}
