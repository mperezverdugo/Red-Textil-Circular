package com.example.textilcircular.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores en modo claro con buen contraste para que sea legible.
private val LightColorScheme = lightColorScheme(
    primary = TurquesaFundacion,
    onPrimary = Blanco,
    primaryContainer = TurquesaClaro,
    onPrimaryContainer = TurquesaOscuro,

    secondary = AmarilloAcento,
    onSecondary = NegroTexto,
    secondaryContainer = AmarilloContenedor,
    onSecondaryContainer = NegroTexto,

    tertiary = TurquesaOscuro,
    onTertiary = Blanco,

    background = GrisFondo,
    onBackground = NegroTexto,

    surface = Blanco,
    onSurface = NegroTexto,
    surfaceVariant = GrisFondo,
    onSurfaceVariant = GrisTextoSecundario,

    error = RojoError,
    onError = Blanco,
    errorContainer = RojoErrorFondo,
    onErrorContainer = RojoError,

    outline = GrisBorde,
    outlineVariant = GrisBorde
)

// Tema principal de la app que aplica los colores y tipografías a todas las pantallas.
@Composable
fun TextilCircularTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = TurquesaFundacion.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
