package com.statushdlite

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads an update APK (from the URL UpdateChecker found on the latest
 * GitHub release) and hands it to the system's own package installer.
 *
 * minSdk for this app is 26 (Oreo), which is also exactly the API level
 * that introduced both REQUEST_INSTALL_PACKAGES/canRequestPackageInstalls
 * and the FileProvider content:// requirement for install intents — so no
 * version-branching is needed here, unlike most install-flow code that has
 * to support older devices too.
 */
object UpdateInstaller {

    private const val DOWNLOAD_FILE_NAME = "update.apk"

    /** Whether this app currently has permission to trigger the system
     *  installer. False the first time — the person has to grant it once
     *  via [requestInstallPermissionIntent], not on every update. */
    fun canInstallPackages(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    /** Takes the person to the system settings screen where they can grant
     *  this app permission to install packages. Android requires this to
     *  be a real user action — no app, including this one, can grant it to
     *  itself. */
    fun requestInstallPermissionIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        )

    /**
     * Streams [apkUrl] into the app's cache dir, reporting 0-100 progress
     * via [onProgress] as bytes arrive. Blocking I/O — call this from
     * Dispatchers.IO, never the main thread.
     *
     * Throws on any network/IO failure; callers should catch and surface
     * the message rather than let it propagate to the UI thread.
     */
    fun downloadApk(context: Context, apkUrl: String, onProgress: (Int) -> Unit): File {
        val connection = URL(apkUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        // GitHub's asset download URLs redirect to S3 — HttpURLConnection
        // follows same-host redirects automatically, but this is a
        // cross-host redirect, so it has to be enabled explicitly.
        connection.instanceFollowRedirects = true

        return try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("Download failed (HTTP $code)")
            }
            val totalBytes = connection.contentLength // -1 if unknown
            val outFile = File(context.cacheDir, DOWNLOAD_FILE_NAME)

            connection.inputStream.use { input ->
                outFile.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead = 0L
                    var lastReportedPercent = -1
                    while (true) {
                        val n = input.read(buffer)
                        if (n == -1) break
                        output.write(buffer, 0, n)
                        bytesRead += n
                        if (totalBytes > 0) {
                            val percent = ((bytesRead * 100) / totalBytes).toInt()
                            if (percent != lastReportedPercent) {
                                lastReportedPercent = percent
                                onProgress(percent)
                            }
                        }
                    }
                }
            }
            outFile
        } finally {
            connection.disconnect()
        }
    }

    /** Launches the system package installer on an already-downloaded APK.
     *  Ends with the OS's own "Install this update?" confirmation — that
     *  tap can't be skipped, it's Android's security control, not
     *  something this app can (or should) bypass. */
    fun installApk(context: Context, apkFile: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
