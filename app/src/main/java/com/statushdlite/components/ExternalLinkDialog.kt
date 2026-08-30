package com.statushdlite.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Confirmation shown before leaving the app for ANY external URL —
 * Contact/Documentation/Community on Help, the GitHub link on Settings,
 * and anywhere else an outbound link gets added later. "I Understand" is
 * the only path through; dismissing (Cancel, tap outside, back press)
 * keeps the person in the app and opens nothing.
 *
 * Usage at each call site: hold `var pendingExternalUrl by
 * remember { mutableStateOf<String?>(null) }`, set it on the link's
 * onClick instead of opening anything directly, and render this dialog
 * when it's non-null:
 *
 * ```
 * pendingExternalUrl?.let { url ->
 *     ExternalLinkConfirmDialog(
 *         url = url,
 *         onConfirm = { openExternalUrl(context, url); pendingExternalUrl = null },
 *         onDismiss = { pendingExternalUrl = null }
 *     )
 * }
 * ```
 */
@Composable
fun ExternalLinkConfirmDialog(
    url: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
        tonalElevation = 0.dp,
        title = { Text("Leaving StatusHD Lite") },
        text = {
            Text(
                "You're about to be redirected to an external website outside the app:\n\n" +
                    url +
                    "\n\nStatusHD Lite isn't responsible for the content, availability, or " +
                    "privacy practices of external sites. Continue?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("I Understand") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Opens [url] in the person's browser. Never call this directly from a
 * click handler — always gate it behind [ExternalLinkConfirmDialog] first,
 * per the app-wide rule that outbound links always confirm before leaving.
 */
fun openExternalUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
}
