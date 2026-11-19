package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val MysticSnakesColorScheme = darkColorScheme(
    primary = RadiantGold,
    onPrimary = Color(0xFF1F1200),
    primaryContainer = RadiantAmber,
    onPrimaryContainer = Color.White,
    secondary = HopefulEmerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = HopefulMint,
    tertiary = DangerCrimson,
    onTertiary = Color.White,
    background = MysticDeepPlum,
    onBackground = MysticWarmCream,
    surface = MysticIndigoSurface,
    onSurface = MysticWarmCream,
    surfaceVariant = MysticCardSurface,
    onSurfaceVariant = Color(0xFFE2D4F0),
    error = DangerCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = MysticSnakesColorScheme,
            typography = Typography,
            content = content
        )
    }
}
