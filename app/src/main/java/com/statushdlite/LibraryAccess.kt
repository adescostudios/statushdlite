package com.statushdlite

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

/**
 * The one persisted SAF folder grant (Download/StatusHD Lite) that both
 * the Library screen (lists files with it) and the Settings screen (shows
 * status / lets you revoke it) need to agree on. Pulled out of
 * LibraryScreen.kt so there's a single source of truth instead of two
 * copies of the same SharedPreferences key drifting apart.
 */
object LibraryAccess {

    private const val PREFS_NAME = "statushdlite_prefs"
    private const val KEY_LIBRARY_TREE_URI = "library_tree_uri"

    /** Best-effort hint so the system folder picker opens already inside
     *  Download/StatusHD Lite instead of forcing the user to navigate
     *  there manually. If this doesn't resolve on some device's storage
     *  layout the picker still opens fine — it just won't be pre-navigated. */
    fun downloadFolderHintUri(): Uri? = try {
        DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Download/StatusHD Lite"
        )
    } catch (e: Exception) {
        null
    }

    /** Returns the persisted tree Uri only if Android still actually holds
     *  that grant — it can be revoked outside the app (cleared app storage,
     *  revoked via system settings), so a stale SharedPreferences value
     *  alone isn't enough to trust. */
    fun getPersistedTreeUri(context: Context): Uri? {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LIBRARY_TREE_URI, null) ?: return null
        val uri = Uri.parse(stored)
        val stillHeld = context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission
        }
        return if (stillHeld) uri else null
    }

    fun persistTreeUri(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LIBRARY_TREE_URI, uri.toString())
            .apply()
    }

    /** Rejects any folder that isn't Download/StatusHD Lite. The download
     *  location is intentionally fixed for now — this SAF prompt exists
     *  only to get read/write consent for that one folder, not to let the
     *  user pick an arbitrary one (that's the deferred custom-folder
     *  feature). */
    fun matchesExpectedFolder(uri: Uri): Boolean {
        val docId = DocumentsContract.getTreeDocumentId(uri) ?: return false
        return docId.replace('\\', '/').trimEnd('/').endsWith("Download/StatusHD Lite", ignoreCase = true)
    }

    /** Releases the persisted grant (if any) and forgets it, so
     *  [getPersistedTreeUri] returns null afterwards — used by Settings'
     *  "Revoke Access". */
    fun revokeAccess(context: Context) {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LIBRARY_TREE_URI, null)
        if (stored != null) {
            val uri = Uri.parse(stored)
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LIBRARY_TREE_URI)
            .apply()
    }
}
