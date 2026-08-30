package com.statushdlite

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.statushdlite.components.BottomNavBar
import com.statushdlite.components.BottomNavTab
import com.statushdlite.components.GlassCard
import com.statushdlite.ui.theme.Spacing

// Library reads via the Storage Access Framework rather than java.io.File.
// Termux (a separate app/process) writes the converted files to public
// Download/StatusHD Lite storage, which this app has no direct filesystem
// access to on API 29+ without a user-granted SAF tree permission — see
// CommandGenerator.OUTPUT_DIR for where files actually land, and
// LibraryAccess for the shared permission bookkeeping (also used by the
// Settings screen). The download location itself stays fixed for now;
// only Library's read path changed.

@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateHelp: () -> Unit,
    onNavigateFAQ: () -> Unit
) {
    val context = LocalContext.current
    var treeUri by remember { mutableStateOf(LibraryAccess.getPersistedTreeUri(context)) }
    var folderMismatch by remember { mutableStateOf(false) }
    val files = remember { mutableStateListOf<DocumentFile>() }

    LaunchedEffect(treeUri) {
        val uri = treeUri
        files.clear()
        if (uri != null) {
            val tree = DocumentFile.fromTreeUri(context, uri)
            files.addAll(tree?.listFiles()?.filter { it.isFile } ?: emptyList())
        }
    }

    val openTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            if (LibraryAccess.matchesExpectedFolder(uri)) {
                LibraryAccess.persistTreeUri(context, uri)
                treeUri = uri
                folderMismatch = false
            } else {
                folderMismatch = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                BottomNavBar(
                    selected = BottomNavTab.LIBRARY,
                    onHome = onNavigateHome,
                    onLibrary = {},
                    onHelp = onNavigateHelp,
                    onFAQ = onNavigateFAQ
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = Spacing.containerMargin)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Divider()

            if (treeUri == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(Spacing.stackSm))
                    Text(
                        "Grant access to see your converted files",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Select Download \u2192 StatusHD Lite in the folder picker",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (folderMismatch) {
                        Spacer(modifier = Modifier.height(Spacing.stackSm))
                        Text(
                            "That wasn't the Download/StatusHD Lite folder \u2014 please pick that exact folder.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.stackMd))
                    Button(onClick = { openTreeLauncher.launch(LibraryAccess.downloadFolderHintUri()) }) {
                        Text("Choose Folder")
                    }
                }
            } else if (files.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(Spacing.stackSm))
                    Text(
                        "No converted files yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp)) {
                    items(files) { file ->
                        LibraryFileItem(file = file, onDelete = {
                            file.delete()
                            files.remove(file)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryFileItem(file: DocumentFile, onDelete: () -> Unit) {
    val context = LocalContext.current
    var confirmingDelete by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val mimeType = context.contentResolver.getType(file.uri) ?: "*/*"
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(file.uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching { context.startActivity(viewIntent) }
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name ?: "Unknown", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("${file.length() / 1024} KB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = context.contentResolver.getType(file.uri) ?: "*/*"
                    putExtra(Intent.EXTRA_STREAM, file.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share file via"))
            }) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
            }
            IconButton(onClick = { confirmingDelete = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
            tonalElevation = 0.dp,
            title = { Text("Delete this file?") },
            text = { Text("\"${file.name ?: "This file"}\" will be permanently deleted. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmingDelete = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") }
            }
        )
    }
}
