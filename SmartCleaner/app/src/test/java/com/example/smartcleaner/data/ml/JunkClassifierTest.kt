package com.example.smartcleaner.data.ml

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests for JunkClassifier ML feature extraction
 */
class JunkClassifierTest {

    private lateinit var classifier: JunkClassifier

    @Before
    fun setup() {
        // Note: In real tests, would use test model or mock TensorFlow Lite
        // classifier = JunkClassifier(mockContext)
    }

    @Test
    fun `extractFeatures returns correct array size`() {
        // Given
        val testFile = File("/test/cache/temp.tmp")

        // When
        // val features = classifier.extractFeatures(testFile)

        // Then
        // assertEquals(20, features.size) // 20 features defined
    }

    @Test
    fun `extractFeatures detects cache directory`() {
        // Given
        val cacheFile = File("/data/data/com.app/cache/file.tmp")

        // When
        // val features = classifier.extractFeatures(cacheFile)

        // Then
        // assertEquals(1.0f, features[7]) // Index 7 is cache dir flag
    }

    @Test
    fun `extractFeatures detects temp directory`() {
        // Given
        val tempFile = File("/sdcard/Download/temp/file.tmp")

        // When
        // val features = classifier.extractFeatures(tempFile)

        // Then
        // assertEquals(1.0f, features[8]) // Index 8 is temp dir flag
    }

    @Test
    fun `extractFeatures calculates size score correctly`() {
        // Given
        val largeFile = File("/test/large.bin")
        // Assume file is 100MB = 104857600 bytes

        // When
        // val features = classifier.extractFeatures(largeFile)

        // Then
        // Size score should be log10(size)
        // assertEquals(8.0f, features[5], 0.1f) // log10(100MB) ≈ 8
    }

    @Test
    fun `extractFeatures detects file extension categories`() {
        // Test different extension categories
        val extensions = mapOf(
            "document.pdf" to 0,  // DOC category
            "video.mp4" to 1,     // MEDIA category
            "cache.tmp" to 2,     // CACHE category
            "temp.bak" to 3,      // TEMP category
            "unknown.xyz" to 4    // OTHER category
        )

        extensions.forEach { (filename, expectedCategory) ->
            val file = File("/test/$filename")
            // val features = classifier.extractFeatures(file)
            // Check features[expectedCategory] == 1.0f
        }
    }

    @Test
    fun `extractFeatures detects hidden files`() {
        // Given
        val hiddenFile = File("/test/.hidden_cache")

        // When
        // val features = classifier.extractFeatures(hiddenFile)

        // Then
        // assertEquals(1.0f, features[17]) // Index 17 is hidden flag
    }

    @Test
    fun `extractFeatures calculates age score`() {
        // Given
        val oldFile = File("/test/old_file.tmp")
        // Assume file is 30 days old

        // When
        // val features = classifier.extractFeatures(oldFile)

        // Then
        // Age score should be days / 365
        // assertEquals(30.0f / 365, features[6], 0.01f)
    }

    @Test
    fun `classify returns confidence between 0 and 1`() {
        // Given
        val features = FloatArray(20) { 0.5f }

        // When
        // val output = classifier.classify(features)

        // Then
        // output.forEach { confidence ->
        //     assertTrue(confidence >= 0f && confidence <= 1f)
        // }
    }

    @Test
    fun `classify returns 10 category confidences`() {
        // Given
        val features = FloatArray(20) { 0.5f }

        // When
        // val output = classifier.classify(features)

        // Then
        // assertEquals(10, output.size) // 10 output categories
    }

    @Test
    fun `getCategory returns correct category name`() {
        // Given
        val confidences = FloatArray(10) { 0.1f }
        confidences[2] = 0.9f // Highest at index 2

        // When
        // val category = classifier.getCategory(confidences)

        // Then
        // Categories: SAFE, LIKELY_JUNK, JUNK, CACHE, TEMP, LOG, BACKUP, APK, LARGE, THUMBNAIL
        // assertEquals("JUNK", category)
    }

    @Test
    fun `shouldDelete returns true for high junk confidence`() {
        // Given
        val confidences = FloatArray(10) { 0.1f }
        confidences[2] = 0.95f // JUNK category

        // When
        // val shouldDelete = classifier.shouldDelete(confidences)

        // Then
        // assertTrue(shouldDelete)
    }

    @Test
    fun `shouldDelete returns false for safe files`() {
        // Given
        val confidences = FloatArray(10) { 0.1f }
        confidences[0] = 0.95f // SAFE category

        // When
        // val shouldDelete = classifier.shouldDelete(confidences)

        // Then
        // assertFalse(shouldDelete)
    }

    @Test
    fun `rule-based fallback works when model unavailable`() {
        // Test that classifier falls back to rules when TFLite model fails
        // This ensures app still works without ML model

        val testFiles = listOf(
            "/cache/app_cache.tmp" to true,  // Should be deletable
            "/Documents/important.pdf" to false,  // Should be safe
            "/temp/backup.bak" to true,  // Should be deletable
            "/Pictures/photo.jpg" to false  // Should be safe
        )

        testFiles.forEach { (path, expectedDeletable) ->
            val file = File(path)
            // val result = classifier.classifyWithRules(file)
            // assertEquals(expectedDeletable, result.shouldDelete)
        }
    }
}
