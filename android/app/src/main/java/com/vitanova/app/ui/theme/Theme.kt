package com.vitanova.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

private val VitaNovaDarkColorScheme = darkColorScheme(
    primary = VitaGreen,
    onPrimary = VitaTextOnPrimary,
    primaryContainer = VitaGreenSurface,
    onPrimaryContainer = VitaGreenLight,
    secondary = VitaCyan,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF0A2530),
    onSecondaryContainer = VitaCyanLight,
    tertiary = VitaInfo,
    onTertiary = Color(0xFF002B75),
    tertiaryContainer = Color(0xFF0D1F3D),
    onTertiaryContainer = Color(0xFFB3CCFF),
    error = VitaError,
    onError = Color(0xFF690014),
    errorContainer = VitaErrorContainer,
    onErrorContainer = Color(0xFFFFB3B8),
    background = VitaBackground,
    onBackground = VitaTextPrimary,
    surface = VitaSurface,
    onSurface = VitaTextPrimary,
    surfaceVariant = VitaSurfaceVariant,
    onSurfaceVariant = VitaTextSecondary,
    outline = VitaOutline,
    outlineVariant = VitaOutlineVariant,
    inverseSurface = VitaTextPrimary,
    inverseOnSurface = VitaBackground,
    inversePrimary = VitaGreenDark,
    surfaceTint = VitaGreen
)

@Composable
fun VitaNovaTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context).copy(
                background = VitaBackground,
                surface = VitaSurface
            )
        }
        else -> VitaNovaDarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? ComponentActivity
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(
                    android.graphics.Color.TRANSPARENT
                ),
                navigationBarStyle = SystemBarStyle.dark(
                    android.graphics.Color.TRANSPARENT
                )
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VitaNovaTypography,
        content = content
    )
}
