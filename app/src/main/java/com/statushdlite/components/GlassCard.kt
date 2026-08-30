package com.statushdlite.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.statushdlite.ui.theme.Radius

/**
 * The frosted, translucent-with-hairline-border look already used by the
 * bottom nav pill and the Home "About" dialog, pulled out into one shared
 * surface so every card/popup in the app reads as the same material
 * instead of each screen mixing its own opaque [Surface] color. This is
 * a styling pass, not a new component shape — swap-in replacement for a
 * plain `Surface(...)`/`Card(...)` wherever one wraps a card or dialog.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Radius.default,
    fillAlpha: Float = 0.7f,
    borderAlpha: Float = 0.55f,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = fillAlpha),
        border = BorderStroke(1.dp, Color.White.copy(alpha = borderAlpha)),
        shadowElevation = 4.dp
    ) {
        content()
    }
}
