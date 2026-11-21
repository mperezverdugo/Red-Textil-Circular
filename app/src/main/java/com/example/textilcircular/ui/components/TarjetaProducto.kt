package com.example.textilcircular.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.textilcircular.R
import com.example.textilcircular.model.Producto
import com.example.textilcircular.ui.theme.TerracotaClaro
import com.example.textilcircular.ui.theme.TerracotaTextil
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * ====================================================================
 * TARJETA DE PRODUCTO - ESTÉTICA TEXTIL Y RETAZO RECICLADO
 * ====================================================================
 * Componente para mostrar productos en catálogos y vitrinas comunitarias (RF-14, RF-18).
 *
 * Decisiones de diseño UI/UX explicadas paso a paso:
 * 1. Simulación de costura (Pespunte perimetral): Dibujamos un trazado punteado
 *    utilizando `drawBehind` y `PathEffect.dashPathEffect`. Esto hace que cada
 *    tarjeta luzca como una muestra de tela cosida a mano por una costurera.
 * 2. Micro-interacción táctil elástica: Cuando el usuario toca la tarjeta, se activa
 *    una animación de elevación (de 2dp a 6dp) y una suave compresión elástica
 *    (escala 0.98f). Esto brinda una sensación física reconfortante, como presionar
 *    un retazo de tela acolchada.
 * 3. Parche de técnica artesanal: Si el producto contiene detalles de su confección
 *    o si se define el parámetro `etiquetaTextil`, aparece un distintivo con
 *    estilo de etiqueta de ropa en tono terracota artesanal.
 * 4. Compatibilidad estricta: Los parámetros originales (producto, onClick, modifier)
 *    se mantienen idénticos, permitiendo que todas las pantallas sigan funcionando
 *    sin tocar una sola línea de código en ui/screens/.
 */
@Composable
fun TarjetaProducto(
    producto: Producto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // --- Parámetros opcionales para enriquecer el rediseño textil ---
    mostrarEfectoCostura: Boolean = true,
    animarAlPresionar: Boolean = true,
    colorHiloCostura: Color = MaterialTheme.colorScheme.outline,
    imagenResId: Int? = null,
    etiquetaTextil: String? = null
) {
    // Escuchamos si el usuario tiene el dedo apoyado sobre la tarjeta
    val interactionSource = remember { MutableInteractionSource() }
    val estaPresionado by interactionSource.collectIsPressedAsState()

    // Animación de sombra (elevación) con resorte suave
    val elevacionAnimada by animateDpAsState(
        targetValue = if (animarAlPresionar && estaPresionado) 6.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ElevacionTarjetaProducto"
    )

    // Animación de escala para micro-interacción acolchada
    val escalaAnimada by animateFloatAsState(
        targetValue = if (animarAlPresionar && estaPresionado) 0.98f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "EscalaTarjetaProducto"
    )

    // Conversión de medidas para trazar las puntadas de hilo (pespunte)
    val grosorHiloPx = with(LocalDensity.current) { 1.5.dp.toPx() }
    val largoPuntadaPx = with(LocalDensity.current) { 6.dp.toPx() }
    val separacionPuntadaPx = with(LocalDensity.current) { 4.dp.toPx() }
    val radioEsquinaPx = with(LocalDensity.current) { 12.dp.toPx() }

    val efectoPespunte = remember(largoPuntadaPx, separacionPuntadaPx) {
        PathEffect.dashPathEffect(floatArrayOf(largoPuntadaPx, separacionPuntadaPx), 0f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escalaAnimada
                scaleY = escalaAnimada
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null, // La animación elástica propia brinda el feedback visual
                onClick = onClick
            )
            .drawBehind {
                // Dibujamos la costura perimetral artesanal
                if (mostrarEfectoCostura) {
                    drawRoundRect(
                        color = colorHiloCostura,
                        topLeft = Offset(grosorHiloPx / 2, grosorHiloPx / 2),
                        size = Size(size.width - grosorHiloPx, size.height - grosorHiloPx),
                        cornerRadius = CornerRadius(radioEsquinaPx, radioEsquinaPx),
                        style = Stroke(
                            width = grosorHiloPx,
                            pathEffect = efectoPespunte
                        )
                    )
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevacionAnimada)
    ) {
        Column {
            // Zona superior: Fotografía o retazo visual del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Si se provee una imagen en recursos la desplegamos, de lo contrario mostramos el placeholder
                if (imagenResId != null) {
                    Image(
                        painter = painterResource(id = imagenResId),
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = stringResource(id = R.string.cd_icono_foto),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = R.string.producto_placeholder_foto),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Insignia de elemento pendiente de sincronización en la nube (RF-35)
                if (producto.pendienteSincronizar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        InsigniaPendiente()
                    }
                }

                // Parche textil: Si tiene explicación de proceso o texto asignado
                val textoEtiqueta = etiquetaTextil ?: if (producto.explicacionProceso != null) "Upcycling" else null
                if (textoEtiqueta != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(
                                color = TerracotaClaro.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = textoEtiqueta,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TerracotaTextil
                        )
                    }
                }
            }

            // Zona inferior: Nombre y descripción con tipografía clara y espaciado generoso
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = producto.descripcionCorta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TarjetaProductoPreview() {
    TextilCircularTheme {
        TarjetaProducto(
            producto = Producto(
                nombre = "Bolso Upcycling Mezclilla",
                descripcionCorta = "Confeccionado a partir de jeans recuperados de la comunidad de Valparaíso.",
                pendienteSincronizar = true,
                explicacionProceso = "Doble costura con refuerzo en hilo reciclado."
            ),
            onClick = {}
        )
    }
}
