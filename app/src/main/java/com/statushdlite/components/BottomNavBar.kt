package com.statushdlite.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration

enum class BottomNavTab { HOME, LIBRARY, HELP, FAQ }

/**
 * The pill-shaped bottom navigation bar shown on every screen. All four
 * tabs are real destinations reachable from anywhere in the app — see
 * AppNavigation.navigateToTab for how a tap jumps to that section without
 * piling up duplicate back-stack entries. The tab matching the current
 * screen calls back into a no-op lambda (tapping "Help" while already on
 * Help does nothing) rather than navigating to itself.
 */
@Composable
fun BottomNavBar(
    selected: BottomNavTab,
    onHome: () -> Unit,
    onLibrary: () -> Unit,
    onHelp: () -> Unit,
    onFAQ: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = if (LocalConfiguration.current.screenWidthDp < 400) 8.dp else 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(if (LocalConfiguration.current.screenWidthDp < 400) 4.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIcon(Icons.Filled.Home, "Home", selected == BottomNavTab.HOME, onHome)
            NavIcon(Icons.Filled.Folder, "Library", selected == BottomNavTab.LIBRARY, onLibrary)
            NavIcon(Icons.Filled.Help, "Help", selected == BottomNavTab.HELP, onHelp)
            NavIcon(Icons.Filled.QuestionAnswer, "FAQ", selected == BottomNavTab.FAQ, onFAQ)
        }
    }
}

@Composable
private fun NavIcon(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = onClick) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
