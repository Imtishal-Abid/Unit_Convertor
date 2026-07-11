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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = BentoOnPrimaryContainer,
    primaryContainer = BentoPrimary,
    onPrimaryContainer = BentoPrimaryContainer,
    secondaryContainer = PurpleGrey80,
    onSecondaryContainer = BentoTextDark,
    background = BentoTextDark,
    onBackground = BentoBg,
    surface = Color(0xFF211F24),
    onSurface = BentoBg,
    outline = BentoBorder,
    surfaceVariant = Color(0xFF2D2A31)
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = BentoWhite,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoTextDark,
    background = BentoBg,
    onBackground = BentoTextDark,
    surface = BentoWhite,
    onSurface = BentoTextDark,
    outline = BentoBorder,
    surfaceVariant = BentoSecondaryContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set default to false to ensure the beautiful Bento Grid theme always shines through
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
