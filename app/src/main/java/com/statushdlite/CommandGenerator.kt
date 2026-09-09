package com.statushdlite

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Result of resolving a picked media file [Uri] into something ffmpeg
 * (running inside Termux) can actually read from disk.
 */
data class ResolvedImage(
    val absolutePath: String,
    val nameWithoutExtension: String,
    val mediaType: MediaType = MediaType.IMAGE
)

/**
 * What kind of file was picked. [CommandGenerator.buildCommand] dispatches
 * on this to build either the still-image loop command or the video
 * re-encode command — picking one never affects the other's logic.
 */
enum class MediaType { IMAGE, VIDEO }

/** File extensions this app recognizes as an image, lowercase, no dot. */
val SupportedImageExtensions = setOf("jpg", "jpeg", "png", "webp")

/** File extensions this app recognizes as a video, lowercase, no dot. */
val SupportedVideoExtensions = setOf("mp4", "mov", "webm", "mkv")

/** Classifies a file by its extension. Defaults to [MediaType.IMAGE] for
 *  anything unrecognized, matching the app's original image-only behavior. */
fun detectMediaType(fileName: String): MediaType {
    val extension = fileName.substringAfterLast('.', "").lowercase(Locale.US)
    return if (extension in SupportedVideoExtensions) MediaType.VIDEO else MediaType.IMAGE
}

/** A selectable output resolution. Values are portrait, matching the
 * original 720x1612 status-video aspect ratio. */
data class Resolution(val label: String, val width: Int, val height: Int)

val ResolutionPresets = listOf(
    Resolution("480p (Compact)", 480, 1075),
    Resolution("720p (Default)", 720, 1612),
    Resolution("1080p (Status HD)", 1080, 2418)
)
val DefaultResolution = ResolutionPresets[1] // 720p, matches the fixed command exactly

enum class OutputFormat(
    val label: String,
    val extension: String,
    val videoCodec: String,
    val audioCodec: String
) {
    MP4("MP4", "mp4", "libx264", "aac"),
    WEBM("WEBM", "webm", "libvpx-vp9", "libopus")
}

/**
 * User-adjustable conversion settings. [useAdvanced] = false means "ignore
 * everything else below and use the original fixed command" — that stays
 * the default so the app behaves exactly as originally specified unless
 * the person explicitly opts into tweaking it.
 */
data class ConversionSettings(
    val useAdvanced: Boolean = false,
    val resolution: Resolution = DefaultResolution,
    // Matches the fixed path's own bitrate ceiling (see VIDEO_MAX_BITRATE) —
    // high enough for CRF-23-equivalent quality, low enough to keep a full
    // 30s status video safely clear of WhatsApp's ~16MB recompression
    // threshold even on busy footage. Going meaningfully higher than this
    // trades away that safety margin for little visible gain, since the
    // whole point is staying under the line, not getting close to it.
    val bitrateMbps: Double = 3.0,
    val format: OutputFormat = OutputFormat.MP4
)

/**
 * Pure, dependency-free logic for:
 *  1. Resolving a picked image into a real filesystem path.
 *  2. Building the ffmpeg command string for that image.
 *
 * No FFmpeg execution happens here or anywhere in this app — this object
 * only ever produces a String.
 */
object CommandGenerator {

    private const val OUTPUT_DIR = "/storage/emulated/0/Download/StatusHD Lite"
    // scale=W:H alone forces the output to those exact pixel dimensions no
    // matter what shape the source image is, which is what was stretching
    // photos that weren't already a 720:1612-ish portrait (i.e. almost all
    // of them — typical phone photos are ~3:4, ~9:16, or square).
    // force_original_aspect_ratio=decrease scales the image down to fit
    // *inside* the target box without distorting it, and pad centers that
    // correctly-proportioned image on a black canvas of the exact target
    // size — so the output dimensions (and therefore WhatsApp Status
    // compatibility) are unchanged, only the content within them.
    private const val FIXED_FILTER =
        "scale=720:1612:force_original_aspect_ratio=decrease:flags=lanczos," +
            "pad=720:1612:(ow-iw)/2:(oh-ih)/2:color=black," +
            "unsharp=5:5:0.8:5:5:0.0"

    // WhatsApp Status caps videos at 30 seconds. -t here is a ceiling, not a
    // forced length — ffmpeg simply stops re-encoding at that point, and has
    // no effect on a source clip that's already shorter.
    private const val VIDEO_MAX_DURATION_SECONDS = 30

    // WhatsApp re-encodes Status videos again on its own servers once they
    // cross an internal bitrate/size threshold — that second forced pass is
    // where most of the visible quality loss actually happens, not this
    // app's own encode. Plain -crf has no ceiling, so a busy/high-motion
    // clip can spike to several Mbps and get flagged, even though a
    // near-static looped photo never comes close (which is why photos were
    // fine and videos weren't). -maxrate/-bufsize cap the *peak* bitrate
    // CRF is allowed to spend without changing how it allocates bits
    // frame-to-frame, so simple content still looks exactly as it did
    // before — only busy footage gets reined in. At 3 Mbps a full 30s clip
    // tops out around 11-12MB (audio included), comfortably under
    // WhatsApp's ~16MB ceiling.
    private const val VIDEO_MAX_BITRATE = "3M"
    private const val VIDEO_BUFSIZE = "6M"

    /**
     * Resolves [uri] into a [ResolvedImage].
     *
     * First attempts to read the real, on-disk path via the MediaStore
     * DATA column (works for most local gallery images). If that is not
     * available (common on newer devices / other providers), the image
     * bytes are copied into the app's cache directory instead and that
     * path is used.
     */
    fun resolveImage(context: Context, uri: Uri): ResolvedImage {
        val displayName = queryDisplayName(context, uri)
        val nameWithoutExtension = displayName.substringBeforeLast('.', displayName)

        val realPath = queryRealPath(context, uri)
        val absolutePath = realPath ?: copyToCache(context, uri, displayName)

        return ResolvedImage(
            absolutePath = absolutePath,
            nameWithoutExtension = nameWithoutExtension,
            mediaType = detectMediaType(displayName)
        )
    }

    /**
     * Builds the ffmpeg command for [imagePath] / [imageName].
     *
     * [mediaType] picks which pipeline to build: looping a still image into
     * a short video, or re-encoding an existing video clip to Status-ready
     * dimensions. When [settings].useAdvanced is false (the default), this
     * returns the exact fixed command from the original spec (images) or
     * its video equivalent, untouched. When true, it swaps in the chosen
     * resolution / bitrate / output format. This is a plain string in both
     * cases — it is never executed by this app.
     */
    fun buildCommand(
        imagePath: String,
        imageName: String,
        settings: ConversionSettings = ConversionSettings(),
        mediaType: MediaType = MediaType.IMAGE
    ): String {
        return when (mediaType) {
            MediaType.IMAGE -> if (!settings.useAdvanced) {
                buildFixedCommand(imagePath, imageName)
            } else {
                buildAdvancedCommand(imagePath, imageName, settings)
            }
            MediaType.VIDEO -> if (!settings.useAdvanced) {
                buildFixedVideoCommand(imagePath, imageName)
            } else {
                buildAdvancedVideoCommand(imagePath, imageName, settings)
            }
        }
    }

    private fun buildFixedCommand(imagePath: String, imageName: String): String {
        val outputPath = "$OUTPUT_DIR/$imageName.mp4"
        return buildString {
            append("ffmpeg -loop 1 \\\n")
            append("-i \"$imagePath\" \\\n")
            append("-vf \"$FIXED_FILTER\" \\\n")
            append("-r 25 \\\n")
            append("-t 5 \\\n")
            append("-c:v libx264 \\\n")
            append("-crf 23 \\\n")
            append("-preset medium \\\n")
            append("-pix_fmt yuv420p \\\n")
            append("\"$outputPath\"")
        }
    }

    private fun buildAdvancedCommand(
        imagePath: String,
        imageName: String,
        settings: ConversionSettings
    ): String {
        val (width, height) = settings.resolution.width to settings.resolution.height
        val filter = "scale=$width:$height:force_original_aspect_ratio=decrease:flags=lanczos," +
            "pad=$width:$height:(ow-iw)/2:(oh-ih)/2:color=black," +
            "unsharp=5:5:0.8:5:5:0.0"
        val bitrate = String.format(Locale.US, "%.1fM", settings.bitrateMbps)
        val outputPath = "$OUTPUT_DIR/$imageName.${settings.format.extension}"
        return buildString {
            append("ffmpeg -loop 1 \\\n")
            append("-i \"$imagePath\" \\\n")
            append("-vf \"$filter\" \\\n")
            append("-r 25 \\\n")
            append("-t 5 \\\n")
            append("-c:v ${settings.format.videoCodec} \\\n")
            append("-b:v $bitrate \\\n")
            append("-pix_fmt yuv420p \\\n")
            append("\"$outputPath\"")
        }
    }

    /**
     * Fixed-command video pipeline: re-encodes an existing clip to the
     * same 720x1612 letterboxed frame and sharpening as the image path,
     * capped to [VIDEO_MAX_DURATION_SECONDS], keeping its audio track.
     * Unlike the image command, there's no `-loop 1` — the source's own
     * frames and timeline drive the output.
     */
    private fun buildFixedVideoCommand(videoPath: String, videoName: String): String {
        val outputPath = "$OUTPUT_DIR/$videoName.mp4"
        return buildString {
            append("ffmpeg -i \"$videoPath\" \\\n")
            append("-vf \"$FIXED_FILTER\" \\\n")
            append("-t $VIDEO_MAX_DURATION_SECONDS \\\n")
            append("-c:v libx264 \\\n")
            append("-crf 23 \\\n")
            append("-maxrate $VIDEO_MAX_BITRATE \\\n")
            append("-bufsize $VIDEO_BUFSIZE \\\n")
            append("-preset medium \\\n")
            append("-pix_fmt yuv420p \\\n")
            append("-c:a aac \\\n")
            append("-b:a 128k \\\n")
            append("-movflags +faststart \\\n")
            append("\"$outputPath\"")
        }
    }

    private fun buildAdvancedVideoCommand(
        videoPath: String,
        videoName: String,
        settings: ConversionSettings
    ): String {
        val (width, height) = settings.resolution.width to settings.resolution.height
        val filter = "scale=$width:$height:force_original_aspect_ratio=decrease:flags=lanczos," +
            "pad=$width:$height:(ow-iw)/2:(oh-ih)/2:color=black," +
            "unsharp=5:5:0.8:5:5:0.0"
        val bitrate = String.format(Locale.US, "%.1fM", settings.bitrateMbps)
        // Same VBV-capping idea as the fixed path: -b:v alone is only an
        // *average* target, so a busy stretch of footage can still spike
        // well above it locally. maxrate/bufsize keep it from wandering
        // too far past whatever average the person chose here.
        val maxrate = String.format(Locale.US, "%.1fM", settings.bitrateMbps * 1.15)
        val bufsize = String.format(Locale.US, "%.1fM", settings.bitrateMbps * 2)
        val outputPath = "$OUTPUT_DIR/$videoName.${settings.format.extension}"
        return buildString {
            append("ffmpeg -i \"$videoPath\" \\\n")
            append("-vf \"$filter\" \\\n")
            append("-t $VIDEO_MAX_DURATION_SECONDS \\\n")
            append("-c:v ${settings.format.videoCodec} \\\n")
            append("-b:v $bitrate \\\n")
            append("-maxrate $maxrate \\\n")
            append("-bufsize $bufsize \\\n")
            append("-pix_fmt yuv420p \\\n")
            append("-c:a ${settings.format.audioCodec} \\\n")
            append("-b:a 128k \\\n")
            if (settings.format == OutputFormat.MP4) {
                append("-movflags +faststart \\\n")
            }
            append("\"$outputPath\"")
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String {
        var name = "image_${System.currentTimeMillis()}.jpg"
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1 && cursor.moveToFirst()) {
                cursor.getString(index)?.let { name = it }
            }
        }
        return name
    }

    private fun queryRealPath(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATA),
                null, null, null
            )?.use { cursor ->
                val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                if (index != -1 && cursor.moveToFirst()) {
                    val path = cursor.getString(index)
                    if (!path.isNullOrBlank() && File(path).exists()) path else null
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun copyToCache(context: Context, uri: Uri, displayName: String): String {
        val outFile = File(context.cacheDir, displayName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        return outFile.absolutePath
    }
}
