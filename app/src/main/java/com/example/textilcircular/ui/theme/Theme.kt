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
 * ====================================================================
 * ESQUEMA DE DISEÑO MATERIAL 3 - TEXTIL CIRCULAR
 * ====================================================================
 *
 * Guía pedagógica sobre la jerarquía visual de Material 3:
 * En lugar de pintar elementos con colores sueltos, Material 3 asigna "roles semánticos":
 *
 * 1. primary (TurquesaFundacion): Es la voz principal de la marca. Se usa en botones
 *    principales, pestañas seleccionadas y elementos de mayor jerarquía.
 * 2. secondary (TerracotaTextil): Tono artesanal cálido que complementa al turquesa,
 *    ideal para representar oficios y técnicas de confección.
 * 3. tertiary (AmarilloAcento): Tomado directamente de textilcircular.cl para destacar
 *    ofertas, estados energéticos o llamados especiales de atención.
 * 4. background (GrisFondo): El lienzo base de la aplicación. Ahora es un tono
 *    crudo natural (algodón/lino) que elimina la frialdad de las pantallas comunes.
 * 5. surface (Blanco / LinoContenedor): El material donde "descansan" las tarjetas,
 *    listas y diálogos, manteniendo alta legibilidad.
 * 6. outline (GrisBorde): Los bordes que dividen las secciones, diseñados con
 *    el color suave de un hilo de hilván textil.
 */
private val LightColorScheme = lightColorScheme(
    primary = TurquesaFundacion,
    onPrimary = Blanco,
    primaryContainer = TurquesaClaro,
    onPrimaryContainer = TurquesaOscuro,

    secondary = TerracotaTextil,
    onSecondary = Blanco,
    secondaryContainer = TerracotaClaro,
    onSecondaryContainer = TerracotaOscuro,

    tertiary = AmarilloAcento,
    onTertiary = NegroTexto,
    tertiaryContainer = AmarilloContenedor,
    onTertiaryContainer = NegroTexto,

    background = GrisFondo,             // Tono lino/crudo natural
    onBackground = NegroTexto,

    surface = Blanco,
    onSurface = NegroTexto,
    surfaceVariant = LinoContenedor,    // Fondo suave para cajas secundarias
    onSurfaceVariant = GrisTextoSecundario,

    error = RojoError,
    onError = Blanco,
    errorContainer = RojoErrorFondo,
    onErrorContainer = RojoError,

    outline = GrisBorde,                // Hilo de costura sutil
    outlineVariant = HiloHilvanSuave
)

/**
 * Tema central de la aplicación Textil Circular.
 *
 * Mantenemos la aplicación en un tema claro intencional para asegurar consistencia
 * con la identidad visual institucional y facilitar la lectura en exteriores a la
 * comunidad de artesanas y emprendedoras.
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
                // Sincronizamos la barra de estado superior con el turquesa institucional
                it.statusBarColor = TurquesaFundacion.toArgb()
                // Iconos de batería y reloj en blanco para asegurar contraste óptimo
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
