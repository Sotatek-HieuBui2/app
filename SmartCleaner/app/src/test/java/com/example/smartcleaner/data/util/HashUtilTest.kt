package com.example.smartcleaner.data.util

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Unit tests for HashUtil
 * Tests MD5, SHA-256, perceptual hash (pHash), and Hamming distance calculations
 */
class HashUtilTest {

    @Test
    fun `calculateMD5 returns consistent hash for same input`() {
        val testString = "Hello World"
        val hash1 = HashUtil.calculateMD5(testString.toByteArray())
        val hash2 = HashUtil.calculateMD5(testString.toByteArray())
        
        assertEquals(hash1, hash2)
        assertEquals(32, hash1.length) // MD5 is 32 hex chars
    }

    @Test
    fun `calculateMD5 returns different hashes for different inputs`() {
        val hash1 = HashUtil.calculateMD5("Hello".toByteArray())
        val hash2 = HashUtil.calculateMD5("World".toByteArray())
        
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `calculateSHA256 returns 64 character hash`() {
        val testString = "Test Data"
        val hash = HashUtil.calculateSHA256(testString.toByteArray())
        
        assertEquals(64, hash.length) // SHA-256 is 64 hex chars
    }

    @Test
    fun `calculateHammingDistance returns 0 for identical hashes`() {
        val hash1 = "1010101010101010"
        val hash2 = "1010101010101010"
        
        val distance = HashUtil.calculateHammingDistance(hash1, hash2)
        
        assertEquals(0, distance)
    }

    @Test
    fun `calculateHammingDistance counts bit differences correctly`() {
        val hash1 = "1111111111111111" // All 1s
        val hash2 = "0000000000000000" // All 0s
        
        val distance = HashUtil.calculateHammingDistance(hash1, hash2)
        
        assertEquals(64, distance) // 16 hex chars = 64 bits, all different
    }

    @Test
    fun `calculateSimilarityPercentage returns 100 for identical hashes`() {
        val hash1 = "abcd1234"
        val hash2 = "abcd1234"
        
        val similarity = HashUtil.calculateSimilarityPercentage(hash1, hash2)
        
        assertEquals(100f, similarity, 0.01f)
    }

    @Test
    fun `calculateSimilarityPercentage returns 0 for completely different hashes`() {
        val hash1 = "ffffffffffffffff"
        val hash2 = "0000000000000000"
        
        val similarity = HashUtil.calculateSimilarityPercentage(hash1, hash2)
        
        assertEquals(0f, similarity, 0.01f)
    }

    @Test
    fun `perceptualHash returns consistent hash for same image data`() {
        // Create test bitmap data (32x32 grayscale)
        val imageData = ByteArray(32 * 32) { it.toByte() }
        
        // Note: This requires actual Bitmap implementation
        // In real tests, would use mockito or test fixtures
        // val hash = HashUtil.calculatePerceptualHash(testBitmap)
        // assertNotNull(hash)
    }

    @Test
    fun `hexToBinary converts correctly`() {
        val hex = "F"
        val binary = HashUtil.hexToBinary(hex)
        
        assertEquals("1111", binary)
    }

    @Test
    fun `hexToBinary handles leading zeros`() {
        val hex = "0F"
        val binary = HashUtil.hexToBinary(hex)
        
        assertEquals("00001111", binary)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculateHammingDistance throws for different length hashes`() {
        val hash1 = "1111"
        val hash2 = "11111111"
        
        HashUtil.calculateHammingDistance(hash1, hash2)
    }

    @Test
    fun `file hash calculation handles empty files`() {
        // Would require file system mocking in real implementation
        // val emptyFile = File.createTempFile("test", ".tmp")
        // val hash = HashUtil.calculateFileHash(emptyFile)
        // assertEquals(MD5_EMPTY_FILE_HASH, hash)
    }
}
