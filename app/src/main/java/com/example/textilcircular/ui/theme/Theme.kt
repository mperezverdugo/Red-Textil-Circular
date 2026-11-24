package com.example.textilcircular.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * ============================================================================
 * ESQUEMA DE DISEÑO MATERIAL 3 - TEXTIL CIRCULAR
 * ============================================================================
 * Esquema exclusivamente claro que proyecta calidez textil, fibras naturales
 * y contraste accesible (WCAG AA). Propaga automáticamente los colores
 * a todas las pantallas sin requerir cambios individuales en `ui/screens/`.
 */
private val LightColorScheme = lightColorScheme(
    // Identidad institucional primaria: Turquesa accesible con texto blanco de alto contraste
    primary = TurquesaFundacion,
    onPrimary = Blanco,
    primaryContainer = TurquesaClaro,
    onPrimaryContainer = TurquesaOscuro,

    // Identidad secundaria: Terracota cálido de costura artesanal
    secondary = TerracotaTextil,
    onSecondary = Blanco,
    secondaryContainer = TerracotaClaro,
    onSecondaryContainer = TerracotaOscuro,

    // Identidad terciaria: Verde musgo botánico para notas de sostenibilidad
    tertiary = VerdeMusgo,
    onTertiary = Blanco,
    tertiaryContainer = AmarilloContenedor,
    onTertiaryContainer = NegroTexto,

    // Fondos y superficies: Base orgánica de lino crudo y blanco lino artesanal
    background = GrisFondo,
    onBackground = NegroTexto,
    surface = BlancoLino,
    onSurface = NegroTexto,
    surfaceVariant = LinoContenedor,
    onSurfaceVariant = GrisTextoSecundario,

    // Estados de error y retroalimentación crítica
    error = RojoError,
    onError = Blanco,
    errorContainer = RojoErrorFondo,
    onErrorContainer = RojoError,

    // Bordes y separadores: Hilván suave y gris hilado
    outline = GrisBorde,
    outlineVariant = HiloHilvanSuave
)

/**
 * Tema principal de la aplicación.
 * Garantiza consistencia de marca, aplicando la paleta oficial y configurando
 * la barra de estado con el color institucional TurquesaFundacion e iconos blancos.
 */
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
                // Barra de estado en TurquesaFundacion con iconos en blanco para máxima legibilidad
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
