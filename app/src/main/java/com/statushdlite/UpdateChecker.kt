package com.statushdlite

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Result of asking GitHub whether a newer release exists than the one
 *  currently installed. [UpdateAvailable.apkDownloadUrl] is null if the
 *  release exists but has no .apk file attached (e.g. a source-only
 *  release) — callers should fall back to [UpdateAvailable.releaseUrl] in
 *  that case. */
sealed class UpdateCheckResult {
    data object UpToDate : UpdateCheckResult()
    data class UpdateAvailable(
        val latestVersion: String,
        val releaseUrl: String,
        val apkDownloadUrl: String?
    ) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

/**
 * Talks to GitHub's public Releases API to see if a newer version of the
 * app has been published. Deliberately dependency-free: GitHub's REST API
 * returns plain JSON, and org.json ships with Android, so no Retrofit/
 * OkHttp is needed just for one button.
 *
 * [checkForUpdate] does blocking network I/O — always call it from a
 * background dispatcher (e.g. `withContext(Dispatchers.IO)`), never
 * directly from a Composable or the main thread.
 */
object UpdateChecker {

    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/adescostudios/statushdlite/releases/latest"

    fun checkForUpdate(currentVersionName: String): UpdateCheckResult {
        return try {
            val json = fetchJson(LATEST_RELEASE_URL)
            val tagName = json.getString("tag_name") // e.g. "v1.5.3"
            val releaseUrl = json.getString("html_url")
            val latestVersion = tagName.removePrefix("v")
            val apkDownloadUrl = findApkAssetUrl(json)

            if (isNewer(candidate = latestVersion, current = currentVersionName)) {
                UpdateCheckResult.UpdateAvailable(latestVersion, releaseUrl, apkDownloadUrl)
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Couldn't check for updates")
        }
    }

    /** The release job attaches exactly one .apk to each release (see
     *  android-build.yml), so the first asset ending in .apk is the one
     *  we want. Returns null if the release has no APK attached at all. */
    private fun findApkAssetUrl(releaseJson: JSONObject): String? {
        val assets = releaseJson.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.optString("name")
            if (name.endsWith(".apk", ignoreCase = true)) {
                return asset.getString("browser_download_url")
            }
        }
        return null
    }

    private fun fetchJson(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        return try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("GitHub returned HTTP $code")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    /** Compares two "major.minor.patch"-style version strings part by part
     *  as numbers, so e.g. "1.10.0" correctly counts as newer than "1.9.0"
     *  — a plain string comparison would get that backwards. A missing
     *  part on either side is treated as 0. */
    private fun isNewer(candidate: String, current: String): Boolean {
        val candidateParts = candidate.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(candidateParts.size, currentParts.size)
        for (i in 0 until length) {
            val c = candidateParts.getOrElse(i) { 0 }
            val cur = currentParts.getOrElse(i) { 0 }
            if (c != cur) return c > cur
        }
        return false
    }
}
