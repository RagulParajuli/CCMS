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

val DarkBlueColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

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
    onSurfaceVariant = AuthTextSecondary,
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
