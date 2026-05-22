package ru.bookmaster.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFFF59E0B),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

@Composable
fun BookMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}