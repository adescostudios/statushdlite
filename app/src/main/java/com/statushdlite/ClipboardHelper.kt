package com.statushdlite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Thin wrapper around the platform ClipboardManager. Nothing else.
 */
object ClipboardHelper {

    fun copyToClipboard(context: Context, text: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ffmpeg_command", text)
        clipboardManager.setPrimaryClip(clip)
    }
}
