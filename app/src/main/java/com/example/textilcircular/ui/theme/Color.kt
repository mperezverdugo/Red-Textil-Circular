package com.example.textilcircular.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ============================================================================
 * IDENTIDAD VISUAL "TEXTIL CIRCULAR" (Inspirada en textilcircular.cl)
 * ============================================================================
 * Paleta orgánica, artesanal y botánica inspirada en fibras naturales,
 * tintes ecológicos, mezclilla recuperada y retazos de upcycling.
 *
 * Criterios de accesibilidad:
 * Cumple estrictamente con la pauta WCAG AA (ratio de contraste >= 4.5:1 para
 * texto normal y 3:1 para elementos de interfaz y texto en gran escala).
 */

// ----------------------------------------------------------------------------
// 1. COLORES INSTITUCIONALES
// ----------------------------------------------------------------------------
// Turquesa accesible de la Fundación: tono principal para botones y barras.
// Contraste sobre Blanco = 6.07:1 (Supera con creces WCAG AA 4.5:1).
val TurquesaFundacion = Color(0xFF0B6E70)
// Variante de alta energía para acentos decorativos e indicadores de foco.
val TurquesaVibrante = Color(0xFF05C9CD)
// Variante profunda para textos sobre fondos claros y contenedores primarios.
val TurquesaOscuro = Color(0xFF053B3C)
// Fondo de contenedores y superficies suaves con matriz turquesa lavada.
val TurquesaClaro = Color(0xFFE2F6F6)

// ----------------------------------------------------------------------------
// 2. ACENTOS CÁLIDOS Y BOTÁNICOS (Retazos y Tintes Naturales)
// ----------------------------------------------------------------------------
// Amarillo artesanal: evoca la luz solar, hilos dorados y bobinas de costura.
val AmarilloAcento = Color(0xFFFFD400)
// Contenedor suave de amarillo para badges y llamadas de atención amigables.
val AmarilloContenedor = Color(0xFFFFF7CF)

// Terracota textil: evoca arcilla, cuero, hilvanes y pespuntes de costura rústica.
val TerracotaTextil = Color(0xFFBF5738)
val TerracotaClaro = Color(0xFFFBEEEA)
val TerracotaOscuro = Color(0xFF78301B)

// Verdes botánicos: simbolizan sostenibilidad, reutilización y economía circular.
val VerdeMusgo = Color(0xFF4A5D43)
val VerdeOliva = Color(0xFF2D6A4F)
val VerdeSalvia = Color(0xFF8FA89B)

// Mezclilla reciclada: tributo al upcycling de prendas denim recuperadas.
val AzulMezclilla = Color(0xFF385A75)
val AzulMezclillaClaro = Color(0xFFE8EEF5)

// ----------------------------------------------------------------------------
// 3. FONDOS Y SUPERFICIES EN FIBRAS NATURALES (Lino y Algodón Crudo)
// ----------------------------------------------------------------------------
// GrisFondo: Mantiene el nombre de variable original para compatibilidad con MainActivity,
// asignándole el color Lino Natural Crudo (#F9F7F2) para bañar sutilmente toda la aplicación.
val GrisFondo = Color(0xFFF9F7F2)
val LinoCrudo = Color(0xFFF9F7F2)
val LinoContenedor = Color(0xFFF1ECE1)
val BlancoLino = Color(0xFFFAF8F5)
val Blanco = Color(0xFFFFFFFF)

// Tipografía y bordes neutros con tinte cenizo orgánico (evita negros puros artificiales).
val NegroTexto = Color(0xFF1E211E)
val GrisTextoSecundario = Color(0xFF4C524C)
val GrisBorde = Color(0xFFD6CFC3)

// Hilos decorativos para pespuntes y líneas de hilván punteadas.
val HiloHilvanSuave = Color(0xFFEAE5DC)
val HiloCostura = Color(0xFFBF5738)

// ----------------------------------------------------------------------------
// 4. ESTADOS SEMÁNTICOS Y DE GESTIÓN (Alto Contraste y Accesibilidad)
// ----------------------------------------------------------------------------
// Verde Éxito: Sincronización completa, publicaciones activas.
val VerdeExito = Color(0xFF236B3C)
val VerdeExitoFondo = Color(0xFFE8F5EB)

// Naranja Advertencia: Pendiente de sincronizar o cambios no guardados.
val NaranjaAdvertencia = Color(0xFFB74700)
val NaranjaAdvertenciaFondo = Color(0xFFFFF3E0)

// Rojo Error: Errores de validación o fallas de red.
val RojoError = Color(0xFFB00020)
val RojoErrorFondo = Color(0xFFFFEBEE)

// Azul Revisión: Fichas de emprendimiento en proceso de moderación administrativa.
val AzulRevision = Color(0xFF1E4E79)
val AzulRevisionFondo = Color(0xFFE7EFF6)

// Gris Baja: Fichas de emprendedoras o productos desactivados.
val GrisBaja = Color(0xFF616161)
val GrisBajaFondo = Color(0xFFEEEEEE)
