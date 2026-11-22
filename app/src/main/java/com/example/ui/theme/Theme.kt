package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTerracottaDark,
    onPrimary = Color(0xFF381507),
    primaryContainer = Color(0xFF5E2713),
    onPrimaryContainer = PrimaryContainer,
    secondary = SecondarySageDark,
    onSecondary = Color(0xFF14291A),
    secondaryContainer = Color(0xFF2B4132),
    onSecondaryContainer = SecondaryContainer,
    tertiary = TertiaryWarmDark,
    onTertiary = Color(0xFF381D00),
    tertiaryContainer = Color(0xFF523000),
    onTertiaryContainer = TertiaryContainer,
    background = DarkChestnutBackground,       // Warm roasted chestnut (NOT black, NOT blue)
    surface = DarkChestnutSurface,             // Warm roasted cacao
    surfaceVariant = DarkChestnutSurfaceVariant,
    onBackground = OnDarkChestnut,
    onSurface = OnDarkChestnut,
    onSurfaceVariant = Color(0xFFC7B7A7),
    outline = Color(0xFF8C7B6D),
    outlineVariant = Color(0xFF544438),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTerracotta,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondarySage,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = TertiaryWarm,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = WarmLinenBackground,          // Warm organic linen (NOT black, NOT white, NOT blue)
    surface = WarmBisqueSurface,               // Warm bisque surface
    surfaceVariant = WarmSandSurfaceVariant,   // Warm sand clay container
    onBackground = OnWarmBackground,           // Deep roasted espresso brown
    onSurface = OnWarmSurface,
    onSurfaceVariant = OnWarmSurfaceVariant,
    outline = Color(0xFF9E8A79),
    outlineVariant = WarmBorderStroke,
    error = EmergencyRed,
    onError = OnEmergencyRed,
    errorContainer = EmergencyRedContainer,
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun SwasthyaSathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
