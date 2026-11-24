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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Producto
import com.example.textilcircular.ui.theme.HiloCostura
import com.example.textilcircular.ui.theme.TerracotaClaro
import com.example.textilcircular.ui.theme.TerracotaTextil
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaClaro
import com.example.textilcircular.ui.theme.TurquesaFundacion

/**
 * ============================================================================
 * FUNCIÓN AUXILIAR DE FOTOGRAFÍAS REALES LOCALES (.webp)
 * ============================================================================
 * Asigna automáticamente una fotografía real local ubicada en `res/drawable-nodpi/`
 * según el nombre o categoría del producto textil.
 *
 * Si los archivos .webp aún no se han copiado a la carpeta, retorna `null` para
 * que la tarjeta active de inmediato el fallback artesanal con `ic_textil_retazo`.
 */
@Composable
fun obtenerFotoProductoLocal(producto: Producto): Painter? {
    val context = LocalContext.current
    val nombre = producto.nombre.lowercase()

    val recursoNombre = when {
        nombre.contains("bolso") || nombre.contains("denim") || nombre.contains("jean") ->
            "img_producto_bolso_denim"
        nombre.contains("estuche") || nombre.contains("lino") || nombre.contains("neceser") ->
            "img_producto_estuche_lino"
        nombre.contains("prenda") || nombre.contains("chaqueta") || nombre.contains("cojin") || nombre.contains("recicla") ->
            "img_producto_prenda_reciclada"
        else -> "img_producto_generico_textil"
    }

    val resId = remember(recursoNombre) {
        context.resources.getIdentifier(recursoNombre, "drawable", context.packageName)
    }

    return if (resId != 0) {
        painterResource(id = resId)
    } else {
        null
    }
}

/*
 * ============================================================================
 * BLOQUE DE REFERENCIA ESTÁTICA DIRECTA (.webp en res/drawable-nodpi/)
 * ============================================================================
 * Apenas copies los archivos .webp correspondientes en res/drawable-nodpi/,
 * también puedes usar referencia estática directa de compilador:
 *
 * @Composable
 * fun obtenerFotoEstatica(producto: Producto): Painter {
 *     return when {
 *         producto.nombre.contains("bolso", ignoreCase = true) ->
 *             painterResource(id = R.drawable.img_producto_bolso_denim)
 *         producto.nombre.contains("estuche", ignoreCase = true) ->
 *             painterResource(id = R.drawable.img_producto_estuche_lino)
 *         else ->
 *             painterResource(id = R.drawable.img_producto_prenda_reciclada)
 *     }
 * }
 * ============================================================================
 */

/**
 * Tarjeta de visualización para productos en cuadrícula de catálogo (RF-14, RF-18).
 * Diseñada bajo el concepto de "Retazo Textil Reciclado":
 * 1. Pespunte de costura perimetral artesanal (dashPathEffect).
 * 2. Micro-interacción táctil elástica (Spring con escala y elevación).
 * 3. Parche textil artesanal identificador de "Upcycling" o proceso sostenible.
 * 4. Fotografía real con recorte proporcional (ContentScale.Crop) o respaldo elegante
 *    con el isotipo textil `ic_textil_retazo` (reemplaza íconos genéricos de compras).
 *
 * Mantiene intactos todos los parámetros originales garantizando retrocompatibilidad total.
 */
@Composable
fun TarjetaProducto(
    producto: Producto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarEfectoCostura: Boolean = true,
    animarAlPresionar: Boolean = true,
    colorHiloCostura: Color? = null,
    etiquetaTextil: String? = null
) {
    // Captura del estado de interacción táctil
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Micro-interacción elástica inspirada en tejido textil
    val escalaAnimada by animateFloatAsState(
        targetValue = if (animarAlPresionar && isPressed) 0.98f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "escalaTarjetaRetazo"
    )

    // Elevación sutil que sube de 2dp a 6dp al presionar
    val elevacionAnimada by animateDpAsState(
        targetValue = if (animarAlPresionar && isPressed) 6.dp else 2.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevacionTarjetaRetazo"
    )

    val colorHilo = colorHiloCostura ?: HiloCostura
    val fotoPainter = obtenerFotoProductoLocal(producto = producto)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escalaAnimada
                scaleY = escalaAnimada
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .drawWithContent {
                // Dibuja el fondo y contenido base de la tarjeta
                drawContent()

                // Pespunte de costura perimetral artesanal
                if (mostrarEfectoCostura) {
                    val grosorTrazoPx = 1.5.dp.toPx()
                    val puntadaPx = 6.dp.toPx()
                    val separacionPx = 4.dp.toPx()
                    val margenInternoPx = 3.dp.toPx()
                    val radioEsquinaPx = 9.dp.toPx()

                    val efectoCostura = PathEffect.dashPathEffect(
                        floatArrayOf(puntadaPx, separacionPx),
                        0f
                    )

                    val mitadGrosor = grosorTrazoPx / 2f
                    val left = margenInternoPx + mitadGrosor
                    val top = margenInternoPx + mitadGrosor
                    val right = size.width - margenInternoPx - mitadGrosor
                    val bottom = size.height - margenInternoPx - mitadGrosor

                    drawRoundRect(
                        color = colorHilo,
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        cornerRadius = CornerRadius(radioEsquinaPx, radioEsquinaPx),
                        style = Stroke(
                            width = grosorTrazoPx,
                            pathEffect = efectoCostura
                        )
                    )
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevacionAnimada,
            pressedElevation = elevacionAnimada
        )
    ) {
        Column {
            // Contenedor visual de imagen o fallback de retazo textil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(TurquesaClaro),
                contentAlignment = Alignment.Center
            ) {
                if (fotoPainter != null) {
                    // Fotografía real local con recorte proporcional adaptado al contenedor
                    Image(
                        painter = fotoPainter,
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback artesanal elegante: Retazo Textil con hilván y puntadas
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_textil_retazo),
                            contentDescription = stringResource(id = R.string.cd_icono_foto),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Creación Textil",
                            style = MaterialTheme.typography.labelSmall,
                            color = TurquesaFundacion,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Insignia de sincronización pendiente
                if (producto.pendienteSincronizar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        InsigniaPendiente()
                    }
                }

                // Parche textil artesanal
                val textoParche = etiquetaTextil ?: if (producto.explicacionProceso != null) "Upcycling" else null
                if (textoParche != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = TerracotaClaro,
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = textoParche,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TerracotaTextil
                        )
                    }
                }
            }

            // Información descriptiva del producto
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TarjetaProductoPreview() {
    TextilCircularTheme {
        TarjetaProducto(
            producto = Producto(
                nombre = "Bolso Upcycling Mezclilla",
                descripcionCorta = "Confeccionado a partir de jeans recuperados de la comunidad.",
                explicacionProceso = "Proceso circular con hilado reciclado",
                pendienteSincronizar = true
            ),
            onClick = {}
        )
    }
}
