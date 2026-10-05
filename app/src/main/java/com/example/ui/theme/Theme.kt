package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CinemaColorScheme = darkColorScheme(
    primary = CinemaGold,
    onPrimary = Color.Black,
    primaryContainer = CinemaGoldContainer,
    onPrimaryContainer = CinemaGold,
    secondary = CinemaCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00363D),
    onSecondaryContainer = CinemaCyan,
    tertiary = CinemaMagenta,
    onTertiary = Color.White,
    background = CinemaObsidian,
    onBackground = CinemaTextPrimary,
    surface = CinemaSurface,
    onSurface = CinemaTextPrimary,
    surfaceVariant = CinemaSurfaceElevated,
    onSurfaceVariant = CinemaTextSecondary,
    outline = CinemaSurfaceBorder,
    error = CinemaDarkError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinemaColorScheme,
        typography = Typography,
        content = content
    )
}
