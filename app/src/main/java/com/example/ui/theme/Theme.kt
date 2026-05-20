package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantAccentBlue,
    secondary = ElegantAccentGreen,
    tertiary = ElegantAccentBlueDark,
    background = ElegantDarkBg,
    surface = ElegantDarkSurface,
    onBackground = ElegantDarkText,
    onSurface = ElegantDarkText,
    outline = ElegantDarkOutline
  )

private val LightColorScheme = DarkColorScheme // Elegant Dark is required

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for "Elegant Dark" vibe
  dynamicColor: Boolean = false, // Disable dynamic colors to keep Elegant Dark intact
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
