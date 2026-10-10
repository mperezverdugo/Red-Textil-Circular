package com.example.textilcircular.ui.screens.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Producto
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.components.DialogoConfirmacion
import com.example.textilcircular.ui.components.InsigniaPendiente
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme

/**
 * Pantalla 8: Detalle de Producto
 * Vista ampliada de la prenda o servicio con carrusel de fotografías (HorizontalPager)
 * y detalles del proceso de economía circular textil (RF-16, RF-18).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductoDetalleScreen(
    producto: Producto,
    onEditarClick: (Int) -> Unit,
    onEliminarClick: (Int) -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val totalPaginas = if (producto.fotos.isEmpty()) 1 else producto.fotos.size
    val pagerState = rememberPagerState(pageCount = { totalPaginas })

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.producto_detalle_titulo),
                onNavegarAtras = onNavegarAtras
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonPrincipal(
                    texto = stringResource(id = R.string.producto_detalle_boton_editar),
                    onClick = { onEditarClick(producto.id) }
                )
                BotonSecundario(
                    texto = stringResource(id = R.string.producto_detalle_boton_eliminar),
                    onClick = { mostrarDialogoEliminar = true }
                )
            }
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .verticalScroll(rememberScrollState())
        ) {
            // Carrusel fotográfico accesible con HorizontalPager
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pagina ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = stringResource(id = R.string.cd_icono_foto),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (producto.fotos.isNotEmpty()) {
                                    stringResource(id = R.string.producto_fotos_contador, pagina + 1, totalPaginas)
                                } else {
                                    stringResource(id = R.string.producto_sin_foto)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Indicador de puntos si tiene varias fotos
                if (totalPaginas > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(totalPaginas) { iterador ->
                            val esActivo = pagerState.currentPage == iterador
                            Box(
                                modifier = Modifier
                                    .size(if (esActivo) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (esActivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                            )
                        }
                    }
                }

                if (producto.pendienteSincronizar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        InsigniaPendiente()
                    }
                }
            }

            // Cuerpo del detalle
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = producto.descripcionCorta,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Sección opcional de Proceso de Fabricación Circular (RF-16)
                if (!producto.explicacionProceso.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(id = R.string.producto_detalle_proceso_titulo),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = producto.explicacionProceso,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Diálogo para eliminar producto
        if (mostrarDialogoEliminar) {
            DialogoConfirmacion(
                titulo = stringResource(id = R.string.producto_form_eliminar_titulo),
                mensaje = stringResource(id = R.string.producto_form_eliminar_mensaje),
                textoConfirmar = stringResource(id = R.string.accion_eliminar),
                esAccionPeligrosa = true,
                onConfirmar = {
                    mostrarDialogoEliminar = false
                    onEliminarClick(producto.id)
                },
                onCancelar = { mostrarDialogoEliminar = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductoDetalleScreenPreview() {
    TextilCircularTheme {
        ProductoDetalleScreen(
            producto = DatosDemo.ProductosDemo.first(),
            onEditarClick = {},
            onEliminarClick = {},
            onNavegarAtras = {}
        )
    }
}
