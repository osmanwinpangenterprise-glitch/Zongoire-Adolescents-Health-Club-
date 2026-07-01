package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4ADE80), // Emerald light accent
    secondary = Color(0xFFFBBF24), // Gold light accent
    tertiary = Color(0xFFFBBF24), // Gold/Amber accent
    background = Color(0xFF06150D), // Premium deepest forest black
    surface = Color(0xFF0D281B), // Rich dark emerald surface card
    onPrimary = Color(0xFF022C16),
    onSecondary = Color(0xFF451A03),
    onBackground = Color(0xFFFAF9F6),
    onSurface = Color(0xFFFAF9F6),
    surfaceVariant = Color(0xFF123B27),
    onSurfaceVariant = Color(0xFFD1FAE5),
    outline = Color(0xFF14532D)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryEmerald,
    onPrimary = Color.White,
    primaryContainer = PrimaryEmeraldMint,
    onPrimaryContainer = PrimaryEmeraldVeryDark,
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = EditorialSoftGreenBg,
    onSecondaryContainer = PrimaryEmeraldVeryDark,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = SlateBackground,
    onBackground = SlateTextDark,
    surface = SlateCardBg,
    onSurface = SlateTextDark,
    surfaceVariant = Color(0xFFF5F4EE), // Luxurious light ivory/cream
    onSurfaceVariant = SlateTextMuted,
    outline = SlateBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic colors to enforce the beautiful brand medical teal identity!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
