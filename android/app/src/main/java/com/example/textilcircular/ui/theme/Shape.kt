package com.example.textilcircular.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Sistema de formas redondeadas consistentes para la app:
// - Píldoras y botones con esquinas suaves (24dp / cápsula)
// - Tarjetas y contenedores con esquinas redondeadas de 18dp a 20dp
// - Campos de texto con esquinas de 14dp
// - Diálogos y superficies flotantes con esquinas de 24dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

// Tokens reutilizables para componentes y pantallas
val FormaPildora = RoundedCornerShape(50)
val FormaBoton = RoundedCornerShape(24.dp)
val FormaCampo = RoundedCornerShape(14.dp)
val FormaTarjeta = RoundedCornerShape(18.dp)
val FormaTarjetaPequena = RoundedCornerShape(14.dp)
val FormaDialogo = RoundedCornerShape(24.dp)
val FormaCirculo = CircleShape
