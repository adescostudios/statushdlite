package com.statushdlite

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.statushdlite.components.BottomNavBar
import com.statushdlite.components.BottomNavTab
import com.statushdlite.components.OutlinedDropdownField
import com.statushdlite.ui.theme.CodeFontFamily
import com.statushdlite.ui.theme.Radius
import com.statushdlite.ui.theme.Spacing
import com.statushdlite.ui.theme.TerminalBackground
import com.statushdlite.ui.theme.TerminalGreen
import kotlinx.coroutines.launch

/** Dashed rounded-rect border, used for the empty source-media picker. */
private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
    dashWidth: Dp = 8.dp,
    gapWidth: Dp = 6.dp
): Modifier = this.drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth.toPx(), gapWidth.toPx()), 0f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onBack: () -> Unit,
    onNavigateHelp: () -> Unit,
    onNavigateFAQ: () -> Unit,
    onNavigateLibrary: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var resolvedImage by remember { mutableStateOf<ResolvedImage?>(null) }
    var settings by remember { mutableStateOf(AppPreferences.loadDefaultSettings(context)) }
    var commandText by remember { mutableStateOf("") }
    var bitrateText by remember { mutableStateOf(settings.bitrateMbps.toString()) }
    var resolutionMenuExpanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    fun regenerate() {
        val image = resolvedImage ?: return
        commandText = CommandGenerator.buildCommand(
            imagePath = image.absolutePath,
            imageName = image.nameWithoutExtension,
            settings = settings,
            mediaType = image.mediaType
        )
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val resolved = CommandGenerator.resolveImage(context, uri)
            resolvedImage = resolved
            regenerate()
        }
    }
    val launchPicker = {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                BottomNavBar(
                selected = BottomNavTab.HOME,
                onHome = {},
                onLibrary = onNavigateLibrary,
                onHelp = onNavigateHelp,
                onFAQ = onNavigateFAQ
)
            }
        }
    ) { padding ->
        BoxWithConstraints {
            val isCompact = maxWidth < 400.dp
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isCompact) 8.dp else Spacing.containerMargin)
            ) {
            // Top bar
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
                    text = "StatusHD Lite",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onNavigateSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(Icons.Filled.Info, contentDescription = "About")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackSm))

            SectionLabel("SOURCE MEDIA")
            Spacer(modifier = Modifier.height(Spacing.stackSm))

            val image = resolvedImage
            if (image == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .dashedBorder(MaterialTheme.colorScheme.primary, Radius.sm)
                        .clickable { launchPicker() }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(Spacing.stackSm))
                        Text(
                            "Select Media",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Images: ${SupportedImageExtensions.joinToString(", ") { it.uppercase() }}  ·  " +
                                "Video: ${SupportedVideoExtensions.joinToString(", ") { it.uppercase() }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val isVideo = image.mediaType == MediaType.VIDEO
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { launchPicker() },
                    shape = RoundedCornerShape(Radius.sm),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVideo) Icons.Filled.Videocam else Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(Spacing.stackSm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                image.nameWithoutExtension,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Tap to change",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(Spacing.stackSm))
                        Surface(
                            shape = RoundedCornerShape(Radius.full),
                            color = if (isVideo) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (isVideo) "VIDEO" else "IMAGE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isVideo) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackLg))

            com.statushdlite.components.GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("CONVERSION PARAMETERS")
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Advanced",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = settings.useAdvanced,
                    onCheckedChange = {
                        settings = settings.copy(useAdvanced = it)
                        regenerate()
                    }
                )
            }
            Spacer(modifier = Modifier.height(Spacing.stackSm))

            val fieldsEnabled = settings.useAdvanced

            // Resolution
            Box {
                OutlinedDropdownField(
                    label = "RESOLUTION",
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
                                settings = settings.copy(resolution = preset)
                                resolutionMenuExpanded = false
                                regenerate()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackSm))

            // Bitrate
            OutlinedTextField(
                value = if (fieldsEnabled) bitrateText else "Fixed quality (CRF 23)",
                onValueChange = { input ->
                    bitrateText = input
                    input.toDoubleOrNull()?.let {
                        settings = settings.copy(bitrateMbps = it)
                        regenerate()
                    }
                },
                enabled = fieldsEnabled,
                label = { Text("BITRATE (VBR)", style = MaterialTheme.typography.labelSmall) },
                trailingIcon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.sm)
            )

            Spacer(modifier = Modifier.height(Spacing.stackSm))

            // Output format
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.stackSm)) {
                OutputFormat.entries.forEach { format ->
                    val selected = settings.format == format
                    Surface(
                        shape = RoundedCornerShape(Radius.full),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.clickable(enabled = fieldsEnabled) {
                            settings = settings.copy(format = format)
                            regenerate()
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
            }
            }

            Spacer(modifier = Modifier.height(Spacing.stackLg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("COMMAND TERMINAL")
                Spacer(modifier = Modifier.weight(1f))
                val ready = commandText.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (ready) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (ready) "READY" else "NO MEDIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ready) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(Spacing.stackSm))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalBackground, RoundedCornerShape(Radius.sm))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TrafficDot(Color(0xFFFF5F56))
                        TrafficDot(Color(0xFFFFBD2E))
                        TrafficDot(Color(0xFF27C93F))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            if (commandText.isNotBlank()) {
                                ClipboardHelper.copyToClipboard(context, commandText)
                                scope.launch { snackbarHostState.showSnackbar("Command copied") }
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.stackSm))
                SelectionContainer {
                    Text(
                        text = commandText.ifBlank {
                            "No command yet.\nSelect a photo or video above to begin."
                        },
                        fontFamily = CodeFontFamily,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (commandText.isBlank()) Color.Gray else TerminalGreen,
                        modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackLg))

            Button(
                onClick = {
                    if (commandText.isBlank()) return@Button
                    ClipboardHelper.copyToClipboard(context, commandText)
                    val launched = TermuxLauncher.openTermux(context)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (launched) "Command copied" else "Termux is not installed."
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.full)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy to Termux", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(96.dp)) // room above the floating bottom nav
        }
        }
    }
    if (showInfoDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
            tonalElevation = 0.dp,
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            },
            title = { Text("About StatusHD Lite") },
            text = {
                Text(
                    "StatusHD Lite only generates an ffmpeg command for Termux — it never runs " +
                        "ffmpeg itself. Copy the command and paste it into Termux to actually convert " +
                        "your photo or video."
                )
            }
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

@Composable
private fun TrafficDot(color: Color) {
    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
}


