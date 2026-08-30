package com.statushdlite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.statushdlite.components.BottomNavBar
import com.statushdlite.components.BottomNavTab
import com.statushdlite.components.GlassCard
import com.statushdlite.ui.theme.Spacing

private data class FAQEntry(val question: String, val answer: String)

// Every answer here is grounded in what the app actually does (see
// CommandGenerator's constants) rather than generic marketing copy, so
// this list stays accurate as the source of truth changes.
private val faqEntries = listOf(
    FAQEntry(
        "How do I select a photo or video?",
        "Tap the \"Select Media\" area on the Home screen and choose an image or video from your gallery."
    ),
    FAQEntry(
        "What formats are supported?",
        "Images: JPG, JPEG, PNG, and WEBP. Videos: MP4, MOV, WEBM, and MKV."
    ),
    FAQEntry(
        "Does StatusHD Lite convert the file for me?",
        "No — it only generates the ffmpeg command. You need Termux (with ffmpeg installed) to actually run it and produce the output file."
    ),
    FAQEntry(
        "How do I copy the command?",
        "After selecting media, tap \"Copy to Termux\" at the bottom of Home. This copies the command and tries to open Termux so you can paste and run it."
    ),
    FAQEntry(
        "Where do converted files get saved?",
        "Termux writes them to Download/StatusHD Lite on your device's storage."
    ),
    FAQEntry(
        "Why is the Library tab empty?",
        "The Library tab needs one-time folder access to Download/StatusHD Lite before it can list your converted files — grant it from the Library screen or Settings."
    ),
    FAQEntry(
        "What does \"Advanced\" mode change?",
        "It lets you pick a resolution, bitrate, and output format (MP4 or WEBM) instead of using the fixed default command."
    ),
    FAQEntry(
        "What resolutions can I choose?",
        "480p (Compact), 720p (Default), and 1080p (Status HD) — all portrait, matching WhatsApp Status's aspect ratio."
    ),
    FAQEntry(
        "Is there a length limit on videos?",
        "Yes — video clips are capped at 30 seconds to match WhatsApp Status's own limit. Photos are looped into a 5-second clip."
    ),
    FAQEntry(
        "Do I need Termux installed?",
        "Yes. \"Copy to Termux\" will tell you if Termux isn't installed on your device — install it first, then try again."
    )
)

@Composable
fun FAQScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateLibrary: () -> Unit,
    onNavigateHelp: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                BottomNavBar(
                    selected = BottomNavTab.FAQ,
                    onHome = onNavigateHome,
                    onLibrary = onNavigateLibrary,
                    onHelp = onNavigateHelp,
                    onFAQ = {}
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
                    text = "FAQ / How to Use",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.stackSm))
            Text(
                "Frequently Asked Questions",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.stackMd))
            faqEntries.forEach { entry ->
                FAQItem(question = entry.question, answer = entry.answer)
            }
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Spacing.stackSm))
                Icon(
                    imageVector = if (expanded) Icons.Filled.Remove else Icons.Filled.Add,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                        .padding(3.dp)
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(answer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
