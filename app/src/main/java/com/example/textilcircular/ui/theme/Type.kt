package com.example.textilcircular.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ============================================================================
 * SISTEMA TIPOGRÁFICO - FUNDACIÓN TEXTIL CIRCULAR
 * ============================================================================
 * Jerarquía tipográfica accesible y cálida:
 * - Títulos en `FontWeight.Bold` con espaciado (`letterSpacing`) balanceado.
 * - Cuerpo de texto legible y accesible (14sp a 16sp) según RNF-05.
 * - Etiquetas y botones en `FontWeight.SemiBold` / `FontWeight.Bold`.
 *
 * Utiliza `FontFamily.Default` centralizada para permitir rápida integración
 * de fuentes propietarias (ej. Monument) sin alterar componentes individuales.
 */

private val FuenteBase = FontFamily.Default

val Typography = Typography(
    // Titulares principales y cabeceras de bienvenida
    headlineLarge = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),

    // Títulos de secciones, tarjetas y barras superiores (mínimo 22sp para AppBar)
    titleLarge = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),

    // Cuerpo de texto principal y descriptivo (altamente legible, 14sp - 16sp)
    bodyLarge = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Etiquetas de botones, chips, parches textiles e insignias
    labelLarge = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FuenteBase,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)
