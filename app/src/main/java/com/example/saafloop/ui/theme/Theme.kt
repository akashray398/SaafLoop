package com.example.saafloop.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val NeoDarkColorScheme = darkColorScheme(
    primary = NeoElectricLime,
    onPrimary = NeoMidnightBackground,
    primaryContainer = NeoElevatedSurface,
    onPrimaryContainer = NeoPrimaryText,
    secondary = NeoMint,
    onSecondary = NeoMidnightBackground,
    secondaryContainer = NeoSecondarySurface,
    onSecondaryContainer = NeoPrimaryText,
    tertiary = NeoControlledCyan,
    onTertiary = NeoMidnightBackground,
    background = NeoMidnightBackground,
    onBackground = NeoPrimaryText,
    surface = NeoPrimarySurface,
    onSurface = NeoPrimaryText,
    surfaceVariant = NeoElevatedSurface,
    onSurfaceVariant = NeoSecondaryText,
    outline = NeoSubtleBorder,
    outlineVariant = NeoSubtleBorder.copy(alpha = 0.6f),
    error = NeoError,
    onError = NeoMidnightBackground
)

private val NeoLightColorScheme = lightColorScheme(
    primary = NeoLightForestPrimaryAction,
    onPrimary = NeoLightPrimarySurface,
    primaryContainer = NeoLightPaleMintSurface,
    onPrimaryContainer = NeoLightDeepForestText,
    secondary = NeoLightForestPrimaryAction,
    onSecondary = NeoLightPrimarySurface,
    secondaryContainer = NeoLightElevatedSurface,
    onSecondaryContainer = NeoLightDeepForestText,
    tertiary = NeoLightForestPrimaryAction,
    onTertiary = NeoLightPrimarySurface,
    background = NeoLightBackground,
    onBackground = NeoLightDeepForestText,
    surface = NeoLightPrimarySurface,
    onSurface = NeoLightDeepForestText,
    surfaceVariant = NeoLightElevatedSurface,
    onSurfaceVariant = NeoLightSecondaryText,
    outline = NeoLightBorder,
    outlineVariant = NeoLightBorder.copy(alpha = 0.6f),
    error = NeoError,
    onError = NeoLightPrimarySurface
)

@Composable
fun SaafLoopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to ensure SaafLoop's distinctive Neo-Eco visual identity is preserved
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> NeoDarkColorScheme
        else -> NeoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
