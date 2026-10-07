package cl.duoc.huertoescolar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VerdeOscuro,
    onPrimary = FondoOscuro,
    primaryContainer = VerdeHuerto,
    onPrimaryContainer = TextoOscuro,
    secondary = TierraSuave,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = SuperficieOscura,
    onSurface = TextoOscuro
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeHuerto,
    onPrimary = SuperficieHuerto,
    primaryContainer = VerdeSuave,
    onPrimaryContainer = TextoHuerto,
    secondary = Tierra,
    onSecondary = SuperficieHuerto,
    secondaryContainer = TierraSuave,
    onSecondaryContainer = TextoHuerto,
    tertiary = VerdeHoja,
    background = FondoHuerto,
    onBackground = TextoHuerto,
    surface = SuperficieHuerto,
    onSurface = TextoHuerto,
    surfaceVariant = VerdeMuySuave,
    onSurfaceVariant = TextoSecundario
)

@Composable
fun HuertoEscolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
