package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PpuDarkNavyPrimary,
    onPrimary = Color(0xFF003355),
    primaryContainer = PpuDarkNavyContainer,
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = Color(0xFFF87171),
    secondaryContainer = Color(0xFF450A0A),
    tertiary = PpuGoldTertiary,
    background = PpuDarkBackground,
    surface = PpuDarkSurface,
    surfaceVariant = PpuDarkSurfaceVariant,
    onBackground = PpuDarkOnSurface,
    onSurface = PpuDarkOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PpuBluePrimary,
    onPrimary = Color.White,
    primaryContainer = PpuBlueContainer,
    onPrimaryContainer = PpuOnBlueContainer,
    secondary = PpuCrimsonSecondary,
    secondaryContainer = PpuCrimsonContainer,
    tertiary = PpuGoldTertiary,
    tertiaryContainer = PpuGoldContainer,
    background = PpuBackgroundLight,
    surface = PpuSurfaceLight,
    surfaceVariant = PpuSurfaceVariantLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
  )

@Composable
fun PpuPatnaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Keep legacy alias for safety
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = PpuPatnaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)

