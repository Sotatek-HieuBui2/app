package com.smartcleaner.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import com.smartcleaner.domain.model.*
import com.smartcleaner.domain.repository.MessagingCleanerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagingCleanerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MessagingCleanerRepository {

    private var cachedResult: MessagingScanResult? = null

    companion object {
        // WhatsApp paths
        private const val WHATSAPP_MEDIA = "WhatsApp/Media"
        private const val WHATSAPP_IMAGES = "WhatsApp/Media/WhatsApp Images"
        private const val WHATSAPP_VIDEO = "WhatsApp/Media/WhatsApp Video"
        private const val WHATSAPP_AUDIO = "WhatsApp/Media/WhatsApp Audio"
        private const val WHATSAPP_VOICE = "WhatsApp/Media/WhatsApp Voice Notes"
        private const val WHATSAPP_DOCUMENTS = "WhatsApp/Media/WhatsApp Documents"
        private const val WHATSAPP_STICKERS = "WhatsApp/Media/WhatsApp Stickers"
        private const val WHATSAPP_STATUS = "WhatsApp/Media/.Statuses"
        
        // Telegram paths
        private const val TELEGRAM_IMAGES = "Telegram/Telegram Images"
        private const val TELEGRAM_VIDEO = "Telegram/Telegram Video"
        private const val TELEGRAM_AUDIO = "Telegram/Telegram Audio"
        private const val TELEGRAM_DOCUMENTS = "Telegram/Telegram Documents"
        
        // Messenger paths (usually in Android/media)
        private const val MESSENGER_MEDIA = "Android/media/com.facebook.orca"
        
        // Instagram paths
        private const val INSTAGRAM_MEDIA = "Android/media/com.instagram.android"
    }

    override suspend fun getInstalledApps(): List<MessagingApp> {
        return withContext(Dispatchers.IO) {
            val pm = context.packageManager
            MessagingApp.values().filter { app ->
                try {
                    pm.getPackageInfo(app.packageName, 0)
                    true
                } catch (e: PackageManager.NameNotFoundException) {
                    false
                }
            }
        }
    }

    override suspend fun scanMessagingApps(options: MessagingScanOptions): Flow<Int> = flow {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val installedApps = getInstalledApps()
                .filter { it in options.selectedApps }
            
            emit(5)
            
            if (installedApps.isEmpty()) {
                cachedResult = MessagingScanResult(
                    appResults = emptyMap(),
                    totalSize = 0,
                    totalFiles = 0,
                    scanDurationMs = System.currentTimeMillis() - startTime
                )
                emit(100)
                return@withContext
            }
            
            val appResults = mutableMapOf<MessagingApp, AppMediaResult>()
            
            installedApps.forEachIndexed { index, app ->
                val appResult = scanApp(app, options)
                appResults[app] = appResult
                
                val progress = 5 + ((index + 1) * 90 / installedApps.size)
                emit(progress)
            }
            
            val totalSize = appResults.values.sumOf { it.totalSize }
            val totalFiles = appResults.values.sumOf { it.totalFiles }
            
            cachedResult = MessagingScanResult(
                appResults = appResults,
                totalSize = totalSize,
                totalFiles = totalFiles,
                scanDurationMs = System.currentTimeMillis() - startTime
            )
            
            emit(100)
        }
    }

    override suspend fun getScanResults(): MessagingScanResult {
        return cachedResult ?: MessagingScanResult(
            appResults = emptyMap(),
            totalSize = 0,
            totalFiles = 0,
            scanDurationMs = 0
        )
    }

    override suspend fun deleteMedia(filePaths: List<String>): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                var deletedCount = 0
                filePaths.forEach { path ->
                    val file = File(path)
                    if (file.exists() && file.delete()) {
                        deletedCount++
                    }
                }
                Result.success(deletedCount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAppMediaPath(app: MessagingApp): String? {
        return withContext(Dispatchers.IO) {
            val storage = Environment.getExternalStorageDirectory()
            when (app) {
                MessagingApp.WHATSAPP, MessagingApp.WHATSAPP_BUSINESS -> 
                    File(storage, WHATSAPP_MEDIA).takeIf { it.exists() }?.absolutePath
                MessagingApp.TELEGRAM -> 
                    File(storage, "Telegram").takeIf { it.exists() }?.absolutePath
                MessagingApp.MESSENGER -> 
                    File(storage, MESSENGER_MEDIA).takeIf { it.exists() }?.absolutePath
                MessagingApp.INSTAGRAM -> 
                    File(storage, INSTAGRAM_MEDIA).takeIf { it.exists() }?.absolutePath
                else -> null
            }
        }
    }

    override suspend fun clearResults() {
        cachedResult = null
    }

    // Private helper methods
    
    private fun scanApp(app: MessagingApp, options: MessagingScanOptions): AppMediaResult {
        val allMedia = mutableListOf<MessagingMedia>()
        val storage = Environment.getExternalStorageDirectory()
        
        when (app) {
            MessagingApp.WHATSAPP, MessagingApp.WHATSAPP_BUSINESS -> {
                if (options.scanImages) {
                    scanDirectory(File(storage, WHATSAPP_IMAGES), app, MessagingMediaType.IMAGE, allMedia, options)
                }
                if (options.scanVideos) {
                    scanDirectory(File(storage, WHATSAPP_VIDEO), app, MessagingMediaType.VIDEO, allMedia, options)
                }
                if (options.scanAudio) {
                    scanDirectory(File(storage, WHATSAPP_AUDIO), app, MessagingMediaType.AUDIO, allMedia, options)
                    scanDirectory(File(storage, WHATSAPP_VOICE), app, MessagingMediaType.VOICE_MESSAGE, allMedia, options)
                }
                if (options.scanDocuments) {
                    scanDirectory(File(storage, WHATSAPP_DOCUMENTS), app, MessagingMediaType.DOCUMENT, allMedia, options)
                }
                if (options.scanStickers) {
                    scanDirectory(File(storage, WHATSAPP_STICKERS), app, MessagingMediaType.STICKER, allMedia, options)
                }
                if (options.scanStatus) {
                    scanDirectory(File(storage, WHATSAPP_STATUS), app, MessagingMediaType.STATUS, allMedia, options)
                }
            }
            MessagingApp.TELEGRAM -> {
                if (options.scanImages) {
                    scanDirectory(File(storage, TELEGRAM_IMAGES), app, MessagingMediaType.IMAGE, allMedia, options)
                }
                if (options.scanVideos) {
                    scanDirectory(File(storage, TELEGRAM_VIDEO), app, MessagingMediaType.VIDEO, allMedia, options)
                }
                if (options.scanAudio) {
                    scanDirectory(File(storage, TELEGRAM_AUDIO), app, MessagingMediaType.AUDIO, allMedia, options)
                }
                if (options.scanDocuments) {
                    scanDirectory(File(storage, TELEGRAM_DOCUMENTS), app, MessagingMediaType.DOCUMENT, allMedia, options)
                }
            }
            MessagingApp.MESSENGER -> {
                scanDirectory(File(storage, MESSENGER_MEDIA), app, MessagingMediaType.IMAGE, allMedia, options)
            }
            MessagingApp.INSTAGRAM -> {
                scanDirectory(File(storage, INSTAGRAM_MEDIA), app, MessagingMediaType.IMAGE, allMedia, options)
            }
            else -> {
                // Other apps - try common paths
            }
        }
        
        // Group media by type
        val groups = groupMedia(allMedia)
        
        // Calculate breakdown
        val breakdown = allMedia.groupBy { it.mediaType }
            .mapValues { (_, files) ->
                MediaTypeStats(
                    count = files.size,
                    totalSize = files.sumOf { it.size }
                )
            }
        
        return AppMediaResult(
            app = app,
            isInstalled = true,
            groups = groups,
            totalSize = allMedia.sumOf { it.size },
            totalFiles = allMedia.size,
            breakdown = breakdown
        )
    }
    
    private fun scanDirectory(
        directory: File,
        app: MessagingApp,
        mediaType: MessagingMediaType,
        output: MutableList<MessagingMedia>,
        options: MessagingScanOptions
    ) {
        if (!directory.exists() || !directory.isDirectory) return
        
        try {
            val files = directory.listFiles() ?: return
            
            for (file in files) {
                if (file.isDirectory) {
                    scanDirectory(file, app, mediaType, output, options)
                } else if (file.isFile) {
                    if (shouldIncludeFile(file, options)) {
                        output.add(
                            MessagingMedia(
                                filePath = file.absolutePath,
                                fileName = file.name,
                                app = app,
                                mediaType = determineMediaType(file, mediaType),
                                size = file.length(),
                                dateModified = file.lastModified(),
                                isFromGroup = file.parent?.contains("Group") == true,
                                chatName = extractChatName(file)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Skip directories with permission issues
        }
    }
    
    private fun shouldIncludeFile(file: File, options: MessagingScanOptions): Boolean {
        val size = file.length()
        val age = System.currentTimeMillis() - file.lastModified()
        val ageInDays = TimeUnit.MILLISECONDS.toDays(age)
        
        if (size < options.minFileSize) return false
        if (options.minFileAge > 0 && ageInDays < options.minFileAge) return false
        
        return true
    }
    
    private fun determineMediaType(file: File, defaultType: MessagingMediaType): MessagingMediaType {
        val extension = file.extension.lowercase()
        
        return when {
            extension in setOf("jpg", "jpeg", "png", "gif", "webp") -> MessagingMediaType.IMAGE
            extension in setOf("mp4", "avi", "mkv", "3gp") -> MessagingMediaType.VIDEO
            extension in setOf("mp3", "wav", "aac", "ogg", "m4a") -> MessagingMediaType.AUDIO
            extension == "opus" -> MessagingMediaType.VOICE_MESSAGE
            extension in setOf("pdf", "doc", "docx", "txt", "xlsx") -> MessagingMediaType.DOCUMENT
            file.name.contains("thumb") -> MessagingMediaType.THUMBNAIL
            else -> defaultType
        }
    }
    
    private fun extractChatName(file: File): String? {
        // Try to extract chat/group name from directory structure
        val parentName = file.parentFile?.name
        return when {
            parentName?.startsWith("Private") == true -> "Private Chat"
            parentName?.startsWith("Group") == true -> parentName.removePrefix("Group ")
            else -> null
        }
    }
    
    private fun groupMedia(media: List<MessagingMedia>): List<MessagingMediaGroup> {
        return media.groupBy { "${it.app}_${it.mediaType}_${it.chatName ?: "unknown"}" }
            .map { (groupId, files) ->
                MessagingMediaGroup(
                    groupId = groupId,
                    app = files.first().app,
                    chatName = files.first().chatName,
                    mediaType = files.first().mediaType,
                    files = files.sortedBy { it.dateModified },
                    totalSize = files.sumOf { it.size },
                    oldestDate = files.minOfOrNull { it.dateModified } ?: 0,
                    newestDate = files.maxOfOrNull { it.dateModified } ?: 0
                )
            }
            .sortedByDescending { it.totalSize }
    }
}
