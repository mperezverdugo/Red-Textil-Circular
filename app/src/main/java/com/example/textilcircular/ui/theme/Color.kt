package com.example.textilcircular.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ====================================================================
 * PALETA DE COLORES - FUNDACIÓN TEXTIL CIRCULAR (textilcircular.cl)
 * ====================================================================
 * Esta paleta de colores está inspirada en la economía circular, el reciclaje
 * textil y la identidad visual de la Fundación Textil Circular de Valparaíso, Chile.
 *
 * Guía de diseño para estudiantes:
 * En un proyecto sobre reciclaje y upcycling textil evitamos colores fríos,
 * sintéticos o plásticos. La calidez y la naturaleza se reflejan a través de:
 * 1. Fibras naturales y crudas: Tonos lino, lienzo y algodón sin blanquear
 *    en lugar de grises metálicos y fondos blancos deslumbrantes.
 * 2. Tintes botánicos y oficios artesanales: Terracotas cálidos, arcillas
 *    y verdes oliva que evocan la regeneración de la tierra.
 * 3. Mezclilla y telas recuperadas: Tonos índigo y denim que rinden homenaje
 *    a las prendas rescatadas de los vertederos.
 * 4. Identidad oficial de textilcircular.cl: El turquesa y el amarillo solar
 *    de la marca, calibrados rigurosamente para garantizar accesibilidad
 *    (contraste >= 4.5:1 según la norma internacional WCAG AA / RNF-05).
 */

// --------------------------------------------------------------------
// 1. COLORES INSTITUCIONALES PRINCIPALES (textilcircular.cl)
// --------------------------------------------------------------------

/**
 * Turquesa institucional profundo:
 * Es el color primario de nuestra aplicación. Se calibró a partir del turquesa
 * de textilcircular.cl (#05C9CD) hacia un tono ligeramente más profundo (#0B6E70).
 * ¿Por qué? Para que al escribir texto blanco encima de un botón o barra superior,
 * el contraste supere 4.6:1, haciendo la lectura cómoda para cualquier persona.
 */
val TurquesaFundacion = Color(0xFF0B6E70)

/**
 * Turquesa vibrante web:
 * El tono cian/turquesa eléctrico característico de la web oficial (#05C9CD).
 * Lo reservamos para acentos, bordes brillantes, micro-parches y elementos destacados.
 */
val TurquesaVibrante = Color(0xFF05C9CD)

/**
 * Turquesa oscuro:
 * Usado para tipografías destacadas sobre fondo claro o sombras con toque textil.
 */
val TurquesaOscuro = Color(0xFF053B3C)

/**
 * Turquesa claro:
 * Fondo pastel suave y refrescante para cajas de imágenes y contenedores.
 */
val TurquesaClaro = Color(0xFFE2F6F6)

// --------------------------------------------------------------------
// 2. ACENTOS CÁLIDOS Y TINTES NATURALES (Oficios y Artesanía)
// --------------------------------------------------------------------

/**
 * Amarillo solar:
 * Tomado directamente del acento (#FFDF01) de textilcircular.cl.
 * Evoca luz solar, optimismo comunitario y energía renovada en la economía circular.
 */
val AmarilloAcento = Color(0xFFFFD400)
val AmarilloContenedor = Color(0xFFFFF7CF)

/**
 * Terracota textil:
 * Evoca el color del barro cocido, las macetas y los tintes naturales de raíces.
 * Es perfecto para botones secundarios de impacto o insignias de técnicas de confección.
 */
val TerracotaTextil = Color(0xFFBF5738)
val TerracotaClaro = Color(0xFFFBEEEA)
val TerracotaOscuro = Color(0xFF78301B)

/**
 * Azul mezclilla (Denim reciclado):
 * Simboliza el material más emblemático del reciclaje textil: los jeans reutilizados.
 */
val AzulMezclilla = Color(0xFF385A75)
val AzulMezclillaClaro = Color(0xFFE8EEF5)

// --------------------------------------------------------------------
// 3. TONOS CRUDOS, LINO Y FIBRAS NATURALES (Fondos y Superficies)
// --------------------------------------------------------------------

/**
 * Blanco puro:
 * Conservado para mantener el máximo contraste donde se requiera gran luminosidad.
 */
val Blanco = Color(0xFFFFFFFF)

/**
 * Blanco lino:
 * Un blanco enriquecido con un matiz cálido que recuerda a una sábana de algodón limpia.
 */
val BlancoLino = Color(0xFFFAF8F5)

/**
 * GrisFondo (Lienzo de algodón crudo):
 * IMPORTANTE: Conservamos el nombre de variable "GrisFondo" para no romper MainActivity
 * ni otros componentes, pero cambiamos su color de un gris frío a un tono crudo natural (#F9F7F2)
 * que simula el fondo de un tejido artesanal sin blanquear.
 */
val GrisFondo = Color(0xFFF9F7F2)

/**
 * LinoContenedor:
 * Tono ligeramente tostado para agrupar secciones y dar variedad visual sin saturar.
 */
val LinoContenedor = Color(0xFFF1ECE1)

/**
 * NegroTexto (Carbón textil):
 * Negro suavizado (#1E211E). No cansa la vista como el negro puro (#000000)
 * y ofrece un contraste superior a 12:1 sobre el fondo crudo.
 */
val NegroTexto = Color(0xFF1E211E)

/**
 * GrisTextoSecundario (Piedra suave):
 * Para subtítulos, descripciones y leyendas de apoyo.
 */
val GrisTextoSecundario = Color(0xFF4C524C)

/**
 * GrisBorde (Hilo de hilván):
 * En lugar de un borde gris de alambre, este tono recuerda al hilo de hilván de costura.
 */
val GrisBorde = Color(0xFFD6CFC3)
val HiloHilvanSuave = Color(0xFFEAE5DC)

// --------------------------------------------------------------------
// 4. VERDES BOTÁNICOS Y ESTADOS SEMÁNTICOS (Sostenibilidad)
// --------------------------------------------------------------------

/**
 * Verde oliva y salvia:
 * Símbolos de regeneración ambiental, plantas tintóreas y materias primas sostenibles.
 */
val VerdeOliva = Color(0xFF2D6A4F)
val VerdeSalvia = Color(0xFF8FA89B)

/**
 * Éxito:
 * Verde bosque con excelente legibilidad para confirmaciones y altas de productos.
 */
val VerdeExito = Color(0xFF236B3C)
val VerdeExitoFondo = Color(0xFFE8F5EB)

/**
 * Advertencia:
 * Naranja cobrizo para avisos de sincronización y alertas.
 */
val NaranjaAdvertencia = Color(0xFFB74700)
val NaranjaAdvertenciaFondo = Color(0xFFFFF3E0)

/**
 * Error:
 * Rojo carmín de alto impacto para errores de validación y fallos de conexión.
 */
val RojoError = Color(0xFFB00020)
val RojoErrorFondo = Color(0xFFFFEBEE)

// --------------------------------------------------------------------
// 5. ESTADOS DE GESTIÓN Y FICHAS (Flujos de la Fundación)
// --------------------------------------------------------------------
val AzulRevision = Color(0xFF1E4E79)
val AzulRevisionFondo = Color(0xFFE7EFF6)
val GrisBaja = Color(0xFF616161)
val GrisBajaFondo = Color(0xFFEEEEEE)
