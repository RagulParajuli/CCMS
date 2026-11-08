package com.ragul.ccms.ui.theme

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
    primary = Color(0xFFB8C3FF),
    onPrimary = Color(0xFF00105C),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF00390B),
    background = Color(0xFF0E1A10),
    onBackground = Color(0xFFE1F0E2),
    surface = Color(0xFF16251A),
    onSurface = Color(0xFFE1F0E2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = AuthPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    background = AuthBackground,
    onBackground = AuthTextPrimary,
    surface = Color.White,
    onSurface = AuthTextPrimary,
    error = Color(0xFFB3261E),
    onError = Color.White,
)

@Composable
fun CCMSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // The app has a fixed green/blue identity, so dynamic colour is opt-in.
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
        content = content,
    )
}
