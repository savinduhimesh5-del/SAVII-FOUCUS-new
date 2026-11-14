package com.savii.focus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFF8B5CF6)
private val DarkScheme = darkColorScheme(
    primary = Purple, secondary = Color(0xFFB9A0FF),
    background = Color(0xFF0D0B14), surface = Color(0xFF191522),
    onPrimary = Color.White, onBackground = Color(0xFFF7F4FF), onSurface = Color(0xFFF7F4FF)
)
private val LightScheme = lightColorScheme(
    primary = Color(0xFF7042D8), secondary = Color(0xFF7655B8),
    background = Color(0xFFF8F6FC), surface = Color.White
)

@Composable
fun SaviiTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
}
