package com.statushdlite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statushdlite.ui.theme.OnPrimary
import com.statushdlite.ui.theme.Primary
import com.statushdlite.ui.theme.PrimaryContainer
import com.statushdlite.ui.theme.Spacing

/**
 * The launch screen. Full-bleed brand gradient, logo mark, and a single
 * "Get started" CTA into the onboarding carousel.
 */
@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Primary, PrimaryContainer))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.containerMargin, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .border(2.dp, OnPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    color = OnPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(modifier = Modifier.padding(top = Spacing.stackLg)) {
                Text(
                    text = "StatusHD Lite",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnPrimary
                )
            }
        }

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = Spacing.containerMargin, vertical = 48.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = OnPrimary,
                contentColor = Primary
            )
        ) {
            Text(
                text = "Get started",
                modifier = Modifier.padding(vertical = 6.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
