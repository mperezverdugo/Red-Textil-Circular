package com.example.textilcircular.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textilcircular.R
import com.example.textilcircular.model.Producto
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.EstadoVacio
import com.example.textilcircular.ui.components.IndicadorCargando
import com.example.textilcircular.ui.components.TarjetaProducto
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider

// Pantalla del catálogo conectada al ViewModel que gestiona las creaciones textiles.
@Composable
fun CatalogoScreen(
    onProductoClick: (Int) -> Unit,
    onAgregarProductoClick: () -> Unit,
    modifier: Modifier = Modifier,
    simularListasVacias: Boolean = false,
    viewModel: CatalogoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CatalogoContent(
        productos = if (simularListasVacias) emptyList() else uiState.productos,
        estaCargando = uiState.estaCargando,
        onProductoClick = onProductoClick,
        onAgregarProductoClick = onAgregarProductoClick,
        modifier = modifier
    )
}

// Sobrecarga de conveniencia para pruebas y previsualizaciones.
@Composable
fun CatalogoScreen(
    productos: List<Producto>,
    onProductoClick: (Int) -> Unit,
    onAgregarProductoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CatalogoContent(
        productos = productos,
        estaCargando = false,
        onProductoClick = onProductoClick,
        onAgregarProductoClick = onAgregarProductoClick,
        modifier = modifier
    )
}

// Contenido visual del catálogo de creaciones textiles.
@Composable
fun CatalogoContent(
    productos: List<Producto>,
    estaCargando: Boolean,
    onProductoClick: (Int) -> Unit,
    onAgregarProductoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.catalogo_titulo)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarProductoClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.catalogo_fab_agregar)
                )
            }
        },
        modifier = modifier
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
        ) {
            when {
                estaCargando -> {
                    IndicadorCargando(
                        mensaje = stringResource(id = R.string.estado_cargando),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                productos.isEmpty() -> {
                    EstadoVacio(
                        titulo = stringResource(id = R.string.catalogo_vacio_titulo),
                        mensaje = stringResource(id = R.string.catalogo_vacio_mensaje),
                        textoAccion = stringResource(id = R.string.inicio_boton_agregar_producto),
                        onAccionClick = onAgregarProductoClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = stringResource(id = R.string.catalogo_bajada),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(productos, key = { it.id }) { producto ->
                                TarjetaProducto(
                                    producto = producto,
                                    onClick = { onProductoClick(producto.id) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CatalogoScreenPreview() {
    TextilCircularTheme {
        CatalogoScreen(
            productos = DatosDemo.ProductosDemo,
            onProductoClick = {},
            onAgregarProductoClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CatalogoScreenVacioPreview() {
    TextilCircularTheme {
        CatalogoScreen(
            productos = emptyList(),
            onProductoClick = {},
            onAgregarProductoClick = {}
        )
    }
}
