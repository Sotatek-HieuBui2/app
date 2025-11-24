package com.smartcleaner.domain.model

/**
 * Supported messaging apps
 */
enum class MessagingApp(val packageName: String, val displayName: String) {
    WHATSAPP("com.whatsapp", "WhatsApp"),
    WHATSAPP_BUSINESS("com.whatsapp.w4b", "WhatsApp Business"),
    TELEGRAM("org.telegram.messenger", "Telegram"),
    MESSENGER("com.facebook.orca", "Messenger"),
    INSTAGRAM("com.instagram.android", "Instagram"),
    VIBER("com.viber.voip", "Viber"),
    LINE("jp.naver.line.android", "LINE"),
    WECHAT("com.tencent.mm", "WeChat");
    
    companion object {
        fun fromPackageName(packageName: String): MessagingApp? {
            return values().find { it.packageName == packageName }
        }
    }
}

/**
 * Type of media in messaging apps
 */
enum class MessagingMediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    VOICE_MESSAGE,
    DOCUMENT,
    STICKER,
    GIF,
    STATUS,          // WhatsApp status, Instagram story
    THUMBNAIL,       // Video thumbnails
    PROFILE_PICTURE,
    UNKNOWN
}

/**
 * Media file from messaging app
 */
data class MessagingMedia(
    val filePath: String,
    val fileName: String,
    val app: MessagingApp,
    val mediaType: MessagingMediaType,
    val size: Long,
    val dateModified: Long,
    val isFromGroup: Boolean = false,
    val chatName: String? = null,
    val thumbnailPath: String? = null
)

/**
 * Group of media files from the same chat/sender
 */
data class MessagingMediaGroup(
    val groupId: String,
    val app: MessagingApp,
    val chatName: String?,
    val mediaType: MessagingMediaType,
    val files: List<MessagingMedia>,
    val totalSize: Long,
    val oldestDate: Long,
    val newestDate: Long
)

/**
 * Result of messaging app scan
 */
data class MessagingScanResult(
    val appResults: Map<MessagingApp, AppMediaResult>,
    val totalSize: Long,
    val totalFiles: Int,
    val scanDurationMs: Long
)

/**
 * Media result for a specific app
 */
data class AppMediaResult(
    val app: MessagingApp,
    val isInstalled: Boolean,
    val groups: List<MessagingMediaGroup>,
    val totalSize: Long,
    val totalFiles: Int,
    val breakdown: Map<MessagingMediaType, MediaTypeStats>
)

/**
 * Statistics for each media type
 */
data class MediaTypeStats(
    val count: Int,
    val totalSize: Long
)

/**
 * Scan options for messaging apps
 */
data class MessagingScanOptions(
    val scanImages: Boolean = true,
    val scanVideos: Boolean = true,
    val scanAudio: Boolean = true,
    val scanDocuments: Boolean = true,
    val scanStickers: Boolean = true,
    val scanStatus: Boolean = true,  // WhatsApp status, stories
    val scanThumbnails: Boolean = true,
    val minFileAge: Long = 0,  // Only scan files older than X days
    val minFileSize: Long = 0,
    val selectedApps: Set<MessagingApp> = MessagingApp.values().toSet()
)
