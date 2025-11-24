package com.smartcleaner.data.ml

import android.content.Context
import android.os.Environment
import com.smartcleaner.domain.model.JunkCategory
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * TensorFlow Lite classifier for junk files
 * 
 * Model Architecture:
 * - Input: Feature vector [20 features]
 *   - File extension (one-hot encoded)
 *   - File size (normalized)
 *   - File age (days since last modified)
 *   - File location (path patterns)
 *   - File name patterns (regex matches)
 * - Output: Probability distribution over 10 categories
 * 
 * Features:
 * [0-4]: Extension category (doc, media, cache, temp, other)
 * [5]: Size score (log scale normalized)
 * [6]: Age score (days normalized)
 * [7]: Is in cache dir
 * [8]: Is in temp dir
 * [9]: Is in download dir
 * [10]: Has temp extension (.tmp, .bak, .cache)
 * [11]: Has log extension
 * [12]: Name contains "cache"
 * [13]: Name contains "temp"
 * [14]: Name contains "backup"
 * [15]: Name contains timestamp pattern
 * [16]: Access time (days since last access)
 * [17]: Is hidden file
 * [18]: Parent folder is cache
 * [19]: File count in same folder
 */
@Singleton
class JunkClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val inputSize = 20
    private val outputSize = 10
    
    companion object {
        private const val MODEL_FILE = "junk_classifier.tflite"
        
        // Extension categories
        private val DOC_EXTENSIONS = setOf("pdf", "doc", "docx", "txt", "xlsx", "pptx")
        private val MEDIA_EXTENSIONS = setOf("jpg", "jpeg", "png", "mp4", "mp3", "gif", "webp")
        private val CACHE_EXTENSIONS = setOf("cache", "cached")
        private val TEMP_EXTENSIONS = setOf("tmp", "temp", "bak", "backup", "old")
        private val LOG_EXTENSIONS = setOf("log", "logs")
        
        // Category mapping (must match model training)
        private val CATEGORY_MAP = mapOf(
            0 to JunkCategory.SAFE_DOCUMENT,
            1 to JunkCategory.SAFE_MEDIA,
            2 to JunkCategory.SAFE_APP_DATA,
            3 to JunkCategory.TEMP_FILE,
            4 to JunkCategory.CACHE_FILE,
            5 to JunkCategory.DUPLICATE,
            6 to JunkCategory.LARGE_UNUSED,
            7 to JunkCategory.LOG_FILE,
            8 to JunkCategory.BACKUP_FILE,
            9 to JunkCategory.UNKNOWN
        )
    }
    
    /**
     * Initialize TensorFlow Lite interpreter
     */
    fun initialize(): Result<Unit> {
        return try {
            // For now, we'll create a mock model since we don't have the actual .tflite file
            // In production, uncomment this:
            // val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
            // interpreter = Interpreter(modelBuffer)
            
            // Mock initialization
            interpreter = null // Will use rule-based classification as fallback
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract features from a file
     */
    fun extractFeatures(file: File): FloatArray {
        val features = FloatArray(inputSize)
        
        val extension = file.extension.lowercase()
        val currentTime = System.currentTimeMillis()
        val ageInDays = TimeUnit.MILLISECONDS.toDays(currentTime - file.lastModified())
        val sizeInMB = file.length() / (1024.0 * 1024.0)
        
        // [0-4]: Extension category (one-hot)
        when {
            extension in DOC_EXTENSIONS -> features[0] = 1f
            extension in MEDIA_EXTENSIONS -> features[1] = 1f
            extension in CACHE_EXTENSIONS -> features[2] = 1f
            extension in TEMP_EXTENSIONS -> features[3] = 1f
            else -> features[4] = 1f
        }
        
        // [5]: Size score (log scale, normalized 0-1)
        features[5] = (Math.log10(sizeInMB + 1.0) / 3.0).toFloat().coerceIn(0f, 1f)
        
        // [6]: Age score (normalized, 0-365 days)
        features[6] = (ageInDays / 365f).coerceIn(0f, 1f)
        
        // [7-9]: Location flags
        val path = file.absolutePath.lowercase()
        features[7] = if (path.contains("cache")) 1f else 0f
        features[8] = if (path.contains("temp") || path.contains("tmp")) 1f else 0f
        features[9] = if (path.contains("download")) 1f else 0f
        
        // [10-11]: Extension patterns
        features[10] = if (extension in TEMP_EXTENSIONS) 1f else 0f
        features[11] = if (extension in LOG_EXTENSIONS) 1f else 0f
        
        // [12-15]: Name patterns
        val name = file.name.lowercase()
        features[12] = if (name.contains("cache")) 1f else 0f
        features[13] = if (name.contains("temp")) 1f else 0f
        features[14] = if (name.contains("backup") || name.contains("bak")) 1f else 0f
        features[15] = if (name.matches(Regex(".*\\d{8,}.*"))) 1f else 0f // timestamp pattern
        
        // [16]: Access time (if available)
        try {
            val accessTime = file.lastModified() // Android doesn't expose last access time easily
            val accessAgeInDays = TimeUnit.MILLISECONDS.toDays(currentTime - accessTime)
            features[16] = (accessAgeInDays / 365f).coerceIn(0f, 1f)
        } catch (e: Exception) {
            features[16] = 0f
        }
        
        // [17]: Is hidden
        features[17] = if (file.isHidden || name.startsWith(".")) 1f else 0f
        
        // [18]: Parent folder is cache
        features[18] = if (file.parent?.lowercase()?.contains("cache") == true) 1f else 0f
        
        // [19]: File count in same folder (normalized)
        val siblingCount = file.parentFile?.listFiles()?.size ?: 0
        features[19] = (siblingCount / 100f).coerceIn(0f, 1f)
        
        return features
    }
    
    /**
     * Run inference on features
     */
    fun classify(features: FloatArray): Pair<JunkCategory, Float> {
        // If model is loaded, use TensorFlow Lite
        interpreter?.let { interp ->
            val inputBuffer = ByteBuffer.allocateDirect(inputSize * 4).apply {
                order(ByteOrder.nativeOrder())
                features.forEach { putFloat(it) }
            }
            
            val outputBuffer = ByteBuffer.allocateDirect(outputSize * 4).apply {
                order(ByteOrder.nativeOrder())
            }
            
            interp.run(inputBuffer, outputBuffer)
            
            outputBuffer.rewind()
            val probabilities = FloatArray(outputSize) { outputBuffer.float }
            
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val confidence = probabilities[maxIndex]
            val category = CATEGORY_MAP[maxIndex] ?: JunkCategory.UNKNOWN
            
            return category to confidence
        }
        
        // Fallback: Rule-based classification
        return ruleBasedClassification(features)
    }
    
    /**
     * Rule-based classification (fallback when model not available)
     */
    private fun ruleBasedClassification(features: FloatArray): Pair<JunkCategory, Float> {
        val isDoc = features[0] > 0f
        val isMedia = features[1] > 0f
        val isCacheExt = features[2] > 0f
        val isTempExt = features[3] > 0f
        
        val inCacheDir = features[7] > 0f
        val inTempDir = features[8] > 0f
        val hasTempExt = features[10] > 0f
        val hasLogExt = features[11] > 0f
        val nameHasCache = features[12] > 0f
        val nameHasTemp = features[13] > 0f
        val nameHasBackup = features[14] > 0f
        
        val ageScore = features[6]
        val sizeScore = features[5]
        
        // Priority rules
        return when {
            // Definite junk
            hasLogExt -> JunkCategory.LOG_FILE to 0.95f
            isCacheExt || (inCacheDir && nameHasCache) -> JunkCategory.CACHE_FILE to 0.90f
            isTempExt || (inTempDir && nameHasTemp) -> JunkCategory.TEMP_FILE to 0.90f
            nameHasBackup && ageScore > 0.5f -> JunkCategory.BACKUP_FILE to 0.85f
            
            // Large unused files
            sizeScore > 0.7f && ageScore > 0.5f -> JunkCategory.LARGE_UNUSED to 0.75f
            
            // Safe files
            isDoc && ageScore < 0.3f -> JunkCategory.SAFE_DOCUMENT to 0.85f
            isMedia && ageScore < 0.5f -> JunkCategory.SAFE_MEDIA to 0.80f
            
            // Cache by location
            inCacheDir -> JunkCategory.CACHE_FILE to 0.70f
            inTempDir -> JunkCategory.TEMP_FILE to 0.70f
            
            // Default
            else -> JunkCategory.UNKNOWN to 0.50f
        }
    }
    
    /**
     * Classify multiple files
     */
    fun classifyBatch(files: List<File>): List<Pair<File, Pair<JunkCategory, Float>>> {
        return files.map { file ->
            val features = extractFeatures(file)
            val result = classify(features)
            file to result
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        interpreter?.close()
        interpreter = null
    }
}
