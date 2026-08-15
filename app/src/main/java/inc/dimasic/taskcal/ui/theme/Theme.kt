package inc.dimasic.taskcal.ui.theme

import android.app.Activity
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
    primary = Color(0xFF7678E8),
    onPrimary = Color.White,

    primaryContainer = Color(0xFF24254A),
    onPrimaryContainer = Color(0xFFE4E2FF),

    secondary = Color(0xFF9A8CFF),
    onSecondary = Color.White,

    background = Color(0xFF0A0D13),
    onBackground = Color(0xFFF4F4F7),

    surface = Color(0xFF101319),
    onSurface = Color(0xFFF4F4F7),

    surfaceVariant = Color(0xFF202329),
    onSurfaceVariant = Color(0xFFA7A9B4),

    outline = Color(0xFF2D3038),

    error = Color(0xFFF06A72),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7678E8),
    onPrimary = Color.White,

    primaryContainer = Color(0xFF24254A),
    onPrimaryContainer = Color(0xFFE4E2FF),

    secondary = Color(0xFF9A8CFF),
    onSecondary = Color.White,

    background = Color(0xFF0A0D13),
    onBackground = Color(0xFFF4F4F7),

    surface = Color(0xFF101319),
    onSurface = Color(0xFFF4F4F7),

    surfaceVariant = Color(0xFF202329),
    onSurfaceVariant = Color(0xFFA7A9B4),

    outline = Color(0xFF2D3038),

    error = Color(0xFFF06A72),
    onError = Color.White

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TaskCalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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