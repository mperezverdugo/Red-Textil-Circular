package com.example.textilcircular.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía del sistema de diseño de la Fundación Textil Circular.
 *
 * Requisitos:
 * - RNF-05: Accesibilidad y legibilidad (Cuerpo mínimo 16sp, Títulos mínimo 22sp).
 * - TODO(confirmar con la Fundación): La tipografía oficial "Monument" está pendiente de entrega técnica.
 *   Se utiliza FontFamily.Default centralizada para poder reemplazarla en este único archivo.
 */

private val FuenteBase = FontFamily.Default

val Typography = Typography(
    // Títulos de alto impacto y cabeceras de bienvenida
    headlineLarge = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Títulos de secciones y barras superiores (mínimo 22sp según pauta)
    titleLarge = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Texto de cuerpo principal (mínimo 16sp según pauta)
    bodyLarge = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    // Etiquetas de botones e insignias (claras y legibles)
    labelLarge = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
