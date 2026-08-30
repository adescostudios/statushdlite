package com.statushdlite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.statushdlite.ui.theme.HairlineBorder
import com.statushdlite.ui.theme.Radius
import com.statushdlite.ui.theme.Spacing
import com.statushdlite.ui.theme.TerminalBackground
import com.statushdlite.ui.theme.TerminalGreen

private data class WorkflowStep(
    val stepLabel: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

private val steps = listOf(
    WorkflowStep(
        "STEP 01",
        "Upload Photos",
        "Import high-resolution source images directly from your gallery or cloud storage.",
        Icons.Filled.PhotoCamera
    ),
    WorkflowStep(
        "STEP 02",
        "Generate Commands",
        "Automate FFmpeg strings optimized for mobile processing power and output quality.",
        Icons.Filled.Code
    ),
    WorkflowStep(
        "STEP 03",
        "Run in Termux",
        "Execute your scripts locally using the Termux environment for low-latency results.",
        Icons.Filled.Refresh
    )
)

/**
 * The single onboarding overview screen: all three workflow steps shown
 * as stacked cards, then Next / Skip Intro to move on to the permission
 * screen.
 */
@Composable
fun OnboardingScreen(onBack: () -> Unit, onFinished: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Skip intro") },
                        onClick = {
                            menuExpanded = false
                            onFinished()
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.containerMargin)
        ) {
            Text(
                text = "Technical Workflow",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.stackSm))
            Text(
                text = "Simplified FFmpeg automation for high-fidelity media processing.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.stackLg))

            steps.forEachIndexed { index, step ->
                StepCard(step)
                if (index != steps.lastIndex) {
                    Spacer(modifier = Modifier.height(Spacing.stackMd))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.stackLg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                steps.forEachIndexed { index, _ ->
                    val active = index == 0
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (active) 24.dp else 8.dp)
                            .background(
                                color = if (active) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.stackLg))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.containerMargin, vertical = Spacing.stackMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onFinished,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.full)
            ) {
                Text("Next", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.height(Spacing.stackSm))
            TextButton(onClick = onFinished) {
                Text(
                    text = "SKIP INTRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepCard(step: WorkflowStep) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.default),
        border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(Spacing.stackMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = step.stepLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing.stackSm))
            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.stackMd))
            StepVisual(step)
        }
    }
}

/** Decorative visual per step. No bundled photo assets are used here —
 * flat brand-colored placeholders stand in for the stock imagery shown
 * in the mockups. */
@Composable
private fun StepVisual(step: WorkflowStep) {
    when (step.stepLabel) {
        "STEP 01" -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ),
                    RoundedCornerShape(Radius.sm)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }

        "STEP 02" -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBackground, RoundedCornerShape(Radius.sm))
                .padding(Spacing.stackSm)
        ) {
            Text(
                text = "$ ffmpeg -i input.jpg -vf \"scale=1920:-1\"",
                style = MaterialTheme.typography.labelLarge,
                color = TerminalGreen
            )
            Text(
                text = "# generating optimized buffer...",
                style = MaterialTheme.typography.labelLarge,
                color = TerminalGreen.copy(alpha = 0.6f)
            )
        }

        else -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
