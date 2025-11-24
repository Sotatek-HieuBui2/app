package com.smartcleaner.data.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.File
import java.security.MessageDigest
import kotlin.math.abs

/**
 * Utility for calculating file hashes and image perceptual hashes
 */
object HashUtil {
    
    /**
     * Calculate MD5 hash of a file
     */
    fun calculateMD5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Calculate SHA-256 hash of a file
     */
    fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Calculate perceptual hash (pHash) for image
     * Uses Discrete Cosine Transform (DCT) for image similarity
     * 
     * Algorithm:
     * 1. Resize image to 32x32
     * 2. Convert to grayscale
     * 3. Apply DCT
     * 4. Get top-left 8x8 frequencies
     * 5. Calculate median
     * 6. Generate 64-bit hash
     */
    fun calculatePerceptualHash(file: File): String? {
        return try {
            // Decode image
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
            
            // Resize to 32x32
            val resized = Bitmap.createScaledBitmap(bitmap, 32, 32, false)
            
            // Convert to grayscale
            val grayscale = Array(32) { DoubleArray(32) }
            for (y in 0 until 32) {
                for (x in 0 until 32) {
                    val pixel = resized.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)
                    // Standard grayscale conversion
                    grayscale[y][x] = (0.299 * r + 0.587 * g + 0.114 * b)
                }
            }
            
            // Apply DCT
            val dct = applyDCT(grayscale)
            
            // Get top-left 8x8 (low frequencies)
            val lowFreq = Array(8) { DoubleArray(8) }
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    lowFreq[y][x] = dct[y][x]
                }
            }
            
            // Calculate median
            val values = lowFreq.flatMap { it.toList() }
            val median = values.sorted()[values.size / 2]
            
            // Generate hash (64 bits)
            val hash = StringBuilder()
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    hash.append(if (lowFreq[y][x] > median) '1' else '0')
                }
            }
            
            // Convert binary to hex
            val hexHash = hash.toString().chunked(4).joinToString("") {
                it.toInt(2).toString(16)
            }
            
            bitmap.recycle()
            resized.recycle()
            
            hexHash
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Calculate Hamming distance between two hashes
     * Used to compare perceptual hashes
     * @return Distance (0-64 for 64-bit hash)
     */
    fun hammingDistance(hash1: String, hash2: String): Int {
        if (hash1.length != hash2.length) return Int.MAX_VALUE
        
        var distance = 0
        for (i in hash1.indices) {
            if (hash1[i] != hash2[i]) distance++
        }
        return distance
    }
    
    /**
     * Calculate similarity between two perceptual hashes
     * @return Similarity score 0.0 to 1.0
     */
    fun calculateSimilarity(hash1: String, hash2: String): Float {
        val distance = hammingDistance(hash1, hash2)
        val maxDistance = hash1.length * 4 // Each hex char = 4 bits
        return 1.0f - (distance.toFloat() / maxDistance)
    }
    
    /**
     * Apply Discrete Cosine Transform (DCT)
     * Simplified 2D DCT for perceptual hashing
     */
    private fun applyDCT(input: Array<DoubleArray>): Array<DoubleArray> {
        val size = input.size
        val output = Array(size) { DoubleArray(size) }
        
        for (u in 0 until size) {
            for (v in 0 until size) {
                var sum = 0.0
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        val cu = if (u == 0) 1.0 / kotlin.math.sqrt(2.0) else 1.0
                        val cv = if (v == 0) 1.0 / kotlin.math.sqrt(2.0) else 1.0
                        
                        val cosX = kotlin.math.cos((2 * x + 1) * u * Math.PI / (2 * size))
                        val cosY = kotlin.math.cos((2 * y + 1) * v * Math.PI / (2 * size))
                        
                        sum += cu * cv * input[x][y] * cosX * cosY
                    }
                }
                output[u][v] = sum / 4.0
            }
        }
        
        return output
    }
    
    /**
     * Calculate average hash (simpler alternative to pHash)
     * Faster but less accurate
     */
    fun calculateAverageHash(file: File): String? {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            val resized = Bitmap.createScaledBitmap(bitmap, 8, 8, false)
            
            // Calculate average grayscale
            var sum = 0.0
            val pixels = IntArray(64)
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val pixel = resized.getPixel(x, y)
                    val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                    pixels[y * 8 + x] = gray
                    sum += gray
                }
            }
            val average = sum / 64
            
            // Generate hash
            val hash = StringBuilder()
            for (gray in pixels) {
                hash.append(if (gray > average) '1' else '0')
            }
            
            bitmap.recycle()
            resized.recycle()
            
            hash.toString().chunked(4).joinToString("") {
                it.toInt(2).toString(16)
            }
        } catch (e: Exception) {
            null
        }
    }
}
