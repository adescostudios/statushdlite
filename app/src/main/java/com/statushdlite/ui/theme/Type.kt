package com.statushdlite.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.statushdlite.R

// The design system specifies "Hanken Grotesk" for UI text and "Geist"
// (its monospace cut, Geist Mono, for code/labels). Rather than bundling
// .ttf files in the APK, both are fetched on-device via Android's
// Downloadable Fonts API (Google Play services acts as the font
// provider) — see res/values/font_certs.xml for the provider's
// certificate hashes, copied verbatim from Google's own Jetchat sample.
// If Play services is unavailable, Compose falls back to the platform
// default automatically; nothing crashes.
@OptIn(ExperimentalTextApi::class)
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@OptIn(ExperimentalTextApi::class)
private val UiFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Hanken Grotesk"), fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Hanken Grotesk"), fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Hanken Grotesk"), fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Hanken Grotesk"), fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

@OptIn(ExperimentalTextApi::class)
val CodeFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Geist Mono"), fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Geist Mono"), fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Geist Mono"), fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Geist Mono"), fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.64).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.24).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CodeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CodeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.55.sp
    )
)

// Convenience aliases matching the design system's own names, so screen
// code can read e.g. AppType.codeSm instead of remembering which Material
// typography slot it was mapped onto.
object AppType {
    val headlineLg = Typography.headlineLarge
    val headlineMd = Typography.headlineMedium
    val headlineSm = Typography.headlineSmall
    val bodyLg = Typography.bodyLarge
    val bodyMd = Typography.bodyMedium
    val codeSm = Typography.labelLarge
    val labelCaps = Typography.labelSmall
}
