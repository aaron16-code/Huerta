package cl.duoc.huertoescolar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ColoresClaros = lightColorScheme(
    primary = Color(0xFF256348),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F1E7),
    onPrimaryContainer = Color(0xFF143D2B),

    secondary = Color(0xFF52645B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7EEE9),
    onSecondaryContainer = Color(0xFF24382D),

    tertiary = Color(0xFF775A20),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEBC0),
    onTertiaryContainer = Color(0xFF513B0A),

    background = Color(0xFFF4F6F5),
    onBackground = Color(0xFF19251F),
    surface = Color.White,
    onSurface = Color(0xFF19251F),
    surfaceVariant = Color(0xFFEDF1EE),
    onSurfaceVariant = Color(0xFF536259),

    outline = Color(0xFF75837A),
    outlineVariant = Color(0xFFDDE5DF),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFF95D5AD),
    onPrimary = Color(0xFF003921),
    primaryContainer = Color(0xFF185236),
    onPrimaryContainer = Color(0xFFB0F1C7),

    secondary = Color(0xFFB8CCBE),
    onSecondary = Color(0xFF24382D),
    secondaryContainer = Color(0xFF3B5042),
    onSecondaryContainer = Color(0xFFD4E8D9),

    tertiary = Color(0xFFE7C477),
    onTertiary = Color(0xFF402D00),
    tertiaryContainer = Color(0xFF5C4310),
    onTertiaryContainer = Color(0xFFFFE0A0),

    background = Color(0xFF101713),
    onBackground = Color(0xFFE0E9E2),
    surface = Color(0xFF18221C),
    onSurface = Color(0xFFE0E9E2),
    surfaceVariant = Color(0xFF26342B),
    onSurfaceVariant = Color(0xFFBECDC2),

    outline = Color(0xFF89998E),
    outlineVariant = Color(0xFF39493E),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun HuertoEscolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ColoresOscuros else ColoresClaros,
        typography = Typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}