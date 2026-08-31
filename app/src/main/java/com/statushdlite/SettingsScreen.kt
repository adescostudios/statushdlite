package com.statushdlite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.statushdlite.components.BottomNavBar
import com.statushdlite.components.BottomNavTab
import com.statushdlite.components.ExternalLinkConfirmDialog
import com.statushdlite.components.GlassCard
import com.statushdlite.components.OutlinedDropdownField
import com.statushdlite.components.openExternalUrl
import com.statushdlite.ui.theme.Radius
import com.statushdlite.ui.theme.Spacing
import kotlinx.coroutines.launch

private const val REPO_URL = "https://github.com/adescostudios/statushd-v1.5.2"

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateLibrary: () -> Unit,
    onNavigateHelp: () -> Unit,
    onNavigateFAQ: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Default conversion settings — what a fresh Home screen starts with.
    var settings by remember { mutableStateOf(AppPreferences.loadDefaultSettings(context)) }
    var bitrateText by remember { mutableStateOf(settings.bitrateMbps.toString()) }
    var resolutionMenuExpanded by remember { mutableStateOf(false) }

    fun updateSettings(new: ConversionSettings) {
        settings = new
        AppPreferences.saveDefaultSettings(context, new)
    }

    // Library folder access.
    var treeUri by remember { mutableStateOf(LibraryAccess.getPersistedTreeUri(context)) }

    // App-wide "confirm before leaving" gate for outbound links.
    var pendingExternalUrl by remember { mutableStateOf<String?>(null) }

    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "\u2014"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                BottomNavBar(
                    selected = BottomNavTab.HOME,
                    onHome = onNavigateHome,
                    onLibrary = onNavigateLibrary,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.containerMargin)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.stackSm))

            // ---- Conversion defaults ---------------------------------
            SectionLabel("CONVERSION DEFAULTS")
            Spacer(modifier = Modifier.height(Spacing.stackSm))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "These are the values Home starts with every time you pick a new photo or video.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.stackMd))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Advanced mode by default",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = settings.useAdvanced,
                            onCheckedChange = { updateSettings(settings.copy(useAdvanced = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.stackSm))

                    val fieldsEnabled = settings.useAdvanced

                    Box {
                        OutlinedDropdownField(
                            label = "DEFAULT RESOLUTION",
                            value = settings.resolution.label,
                            enabled = fieldsEnabled,
                            onClick = { if (fieldsEnabled) resolutionMenuExpanded = true },
                            trailingIcon = Icons.Filled.ArrowDropDown
                        )
                        DropdownMenu(
                            expanded = resolutionMenuExpanded,
                            onDismissRequest = { resolutionMenuExpanded = false }
                        ) {
                            ResolutionPresets.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset.label) },
                                    onClick = {
                                        updateSettings(settings.copy(resolution = preset))
                                        resolutionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.stackSm))

                    OutlinedTextField(
                        value = if (fieldsEnabled) bitrateText else "Fixed quality (CRF 23)",
                        onValueChange = { input ->
                            bitrateText = input
                            input.toDoubleOrNull()?.let { updateSettings(settings.copy(bitrateMbps = it)) }
                        },
                        enabled = fieldsEnabled,
                        label = { Text("DEFAULT BITRATE (VBR)", style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.sm)
                    )

                    Spacer(modifier = Modifier.height(Spacing.stackSm))

                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.stackSm)) {
                        OutputFormat.entries.forEach { format ->
                            val selected = settings.format == format
                            Surface(
                                shape = RoundedCornerShape(Radius.full),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.clickable(enabled = fieldsEnabled) {
                                    updateSettings(settings.copy(format = format))
                                }
                            ) {
                                Text(
                                    text = format.label,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.stackMd))

                    OutlinedButton(
                        onClick = {
                            AppPreferences.resetToFactoryDefaults(context)
                            val reset = AppPreferences.FactoryDefaults
                            settings = reset
                            bitrateText = reset.bitrateMbps.toString()
                            scope.launch { snackbarHostState.showSnackbar("Reset to defaults") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.full)
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset to Defaults", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackLg))

            // ---- Library access ---------------------------------------
            SectionLabel("LIBRARY ACCESS")
            Spacer(modifier = Modifier.height(Spacing.stackSm))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (treeUri != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.stackSm))
                        Text(
                            if (treeUri != null) "Access granted to Download/StatusHD Lite" else "No folder access granted yet",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Controls whether the Library tab can list, share, or delete your converted files.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (treeUri != null) {
                        Spacer(modifier = Modifier.height(Spacing.stackMd))
                        OutlinedButton(
                            onClick = {
                                LibraryAccess.revokeAccess(context)
                                treeUri = null
                                scope.launch { snackbarHostState.showSnackbar("Library access revoked") }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Radius.full),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Revoke Access", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackLg))

            // ---- About ---------------------------------------------------
            SectionLabel("ABOUT")
            Spacer(modifier = Modifier.height(Spacing.stackSm))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(Spacing.stackSm))
                        Column {
                            Text("StatusHD Lite", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Version $versionName \u00b7 Built by Adesco Studios",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.stackMd))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pendingExternalUrl = REPO_URL },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "View source on GitHub",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Filled.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    pendingExternalUrl?.let { url ->
        ExternalLinkConfirmDialog(
            url = url,
            onConfirm = {
                openExternalUrl(context, url)
                pendingExternalUrl = null
            },
            onDismiss = { pendingExternalUrl = null }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
