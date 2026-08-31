package com.statushdlite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.statushdlite.components.BottomNavBar
import com.statushdlite.components.BottomNavTab
import com.statushdlite.components.ExternalLinkConfirmDialog
import com.statushdlite.components.GlassCard
import com.statushdlite.components.openExternalUrl
import com.statushdlite.ui.theme.Radius
import com.statushdlite.ui.theme.Spacing

private const val CONTACT_URL = "https://github.com/adescostudios/statushd-v1.5.2/issues"
private const val DOCS_URL = "https://github.com/adescostudios/statushd-v1.5.2#readme"
private const val COMMUNITY_URL = "https://github.com/adescostudios/statushd-v1.5.2/discussions"

private data class Guide(val title: String, val description: String, val detail: String, val icon: ImageVector)

private val guides = listOf(
    Guide(
        "FFmpeg Basics",
        "Core syntax, input/output flags, and basic stream mapping.",
        "Every command follows the same shape: ffmpeg -i <input> [options] <output>. " +
            "-i sets the source file, -vf applies a video filter chain (scaling, padding, " +
            "sharpening — this is where StatusHD Lite's letterboxing happens), and -c:v / " +
            "-c:a choose the video and audio codecs for the output file.",
        Icons.Filled.Code
    ),
    Guide(
        "Termux Setup",
        "Configure your Android environment for high-performance CLI encoding.",
        "Install Termux (F-Droid build is recommended over the outdated Play Store one), " +
            "then run \"pkg update && pkg install ffmpeg\" to get the ffmpeg binary this app's " +
            "commands depend on. Running \"termux-setup-storage\" once will also let Termux read " +
            "and write files outside its own sandbox.",
        Icons.Filled.Terminal
    ),
    Guide(
        "Advanced Flags",
        "Hardware acceleration, CRF control, and filtergraph mastery.",
        "The fixed command uses a CRF of 23 (0 = lossless, 51 = worst — 18\u201323 is usually " +
            "visually indistinguishable from the source). Advanced mode swaps CRF for a target " +
            "bitrate instead, which trades some quality consistency for a predictable file size. " +
            "Hardware-accelerated encoders vary by device and aren't used here for portability.",
        Icons.Filled.Tune
    )
)

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateLibrary: () -> Unit,
    onNavigateFAQ: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var pendingExternalUrl by remember { mutableStateOf<String?>(null) }

    val filteredGuides = remember(query) {
        if (query.isBlank()) {
            guides
        } else {
            guides.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.detail.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                BottomNavBar(
                    selected = BottomNavTab.HELP,
                    onHome = onNavigateHome,
                    onLibrary = onNavigateLibrary,
                    onHelp = {},
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
                    text = "StatusHD Lite",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.stackSm))

            Text(
                "Help & Documentation",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.stackSm))
            Text(
                "Master professional video encoding on mobile with our comprehensive technical guides and support resources.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.stackLg))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. bitrate, h.265, metadata...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(Radius.full)
            )

            Spacer(modifier = Modifier.height(Spacing.stackLg))

            SectionLabel("TECHNICAL GUIDES")
            Spacer(modifier = Modifier.height(Spacing.stackSm))

            if (filteredGuides.isEmpty()) {
                Text(
                    "No guides match \"$query\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.stackMd)
                )
            } else {
                filteredGuides.forEach { guide ->
                    GuideRow(guide)
                    Spacer(modifier = Modifier.height(Spacing.stackSm))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackSm))

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = Radius.default) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1A1C1D), Color(0xFF33363A))
                            )
                        )
                        .padding(Spacing.stackMd)
                ) {
                    Column {
                        SupportIllustration()
                        Spacer(modifier = Modifier.height(Spacing.stackMd))
                        Text(
                            "Direct Developer Support",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Can't find what you need? Join our professional community and get direct help from the StatusHD team.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.stackMd))
                        Button(
                            onClick = { pendingExternalUrl = CONTACT_URL },
                            shape = RoundedCornerShape(Radius.full)
                        ) {
                            Text("Contact Us", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackMd))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.stackSm)) {
                OutlineActionButton(
                    label = "DOCUMENTATION",
                    icon = Icons.Filled.Book,
                    onClick = { pendingExternalUrl = DOCS_URL },
                    modifier = Modifier.weight(1f)
                )
                OutlineActionButton(
                    label = "COMMUNITY",
                    icon = Icons.Filled.Forum,
                    onClick = { pendingExternalUrl = COMMUNITY_URL },
                    modifier = Modifier.weight(1f)
                )
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

/** Small vector illustration standing in for real brand artwork — layered
 * translucent circles behind a headset glyph. No image assets are bundled
 * in this app (see Type.kt: fonts are fetched, not bundled, either), so
 * this is built entirely from Compose primitives rather than a missing
 * drawable. */
@Composable
private fun SupportIllustration() {
    Box(
        modifier = Modifier.fillMaxWidth().height(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(Color.White.copy(alpha = 0.10f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.HeadsetMic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
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
private fun GuideRow(guide: Guide) {
    var expanded by remember { mutableStateOf(false) }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(Spacing.stackMd)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(Radius.sm)
                        )
                        .padding(10.dp)
                ) {
                    Icon(guide.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(Spacing.stackMd))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        guide.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        guide.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(Spacing.stackSm))
                Text(
                    guide.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OutlineActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, cornerRadius = Radius.full) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.height(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
