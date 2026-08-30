package com.statushdlite.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing and corner-radius tokens taken directly from the design system's
 * `spacing` / `rounded` blocks (values converted from rem/px to dp at 1:1,
 * which matches Android's dp-per-density-independent-pixel baseline).
 */
object Spacing {
    val unit = 4.dp
    val containerMargin = 20.dp
    val gutter = 12.dp
    val stackSm = 8.dp
    val stackMd = 16.dp
    val stackLg = 32.dp
}

object Radius {
    val sm = 8.dp
    val default = 16.dp
    val md = 24.dp
    val lg = 32.dp
    val xl = 48.dp
    val full = 500.dp // effectively fully pill-shaped at any normal component height
}
