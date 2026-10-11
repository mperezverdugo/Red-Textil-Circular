package com.example.textilcircular.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.ui.components.BarraSuperior
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.CampoTexto
import com.example.textilcircular.ui.components.ChipsRubros
import com.example.textilcircular.ui.components.SelectorDesplegable
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.ui.theme.TextilCircularTheme

// Pantalla para modificar los datos personales y de contacto de la emprendedora.
@Composable
fun EditarPerfilScreen(
    emprendedora: Emprendedora,
    onGuardarCambios: (Emprendedora) -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf(emprendedora.nombre) }
    var nombreComercial by remember { mutableStateOf(emprendedora.nombreComercial) }
    var regionSeleccionada by remember { mutableStateOf(emprendedora.region) }
    var comunaSeleccionada by remember { mutableStateOf(emprendedora.comuna) }
    var instagram by remember { mutableStateOf(emprendedora.instagram ?: "") }

    val rubrosSeleccionados = remember {
        mutableStateListOf<Rubro>().apply { addAll(emprendedora.rubros) }
    }

    val comunasDisponibles = remember(regionSeleccionada) {
        if (regionSeleccionada.isNotBlank()) {
            DatosDemo.RegionesConComunas[regionSeleccionada] ?: emptyList()
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.editar_perfil_titulo),
                onNavegarAtras = onNavegarAtras
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // TODO: guardar los cambios en SQLite y cuando haya servidor, enviarlos a la API
                BotonPrincipal(
                    texto = stringResource(id = R.string.accion_guardar),
                    onClick = {
                        val emprendedoraActualizada = emprendedora.copy(
                            nombre = nombre,
                            nombreComercial = nombreComercial,
                            region = regionSeleccionada,
                            comuna = comunaSeleccionada,
                            rubros = rubrosSeleccionados.toList(),
                            instagram = instagram.ifBlank { null }
                        )
                        onGuardarCambios(emprendedoraActualizada)
                    },
                    habilitado = nombre.isNotBlank() && nombreComercial.isNotBlank() && regionSeleccionada.isNotBlank() && comunaSeleccionada.isNotBlank()
                )
            }
        },
        modifier = modifier
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CampoTexto(
                valor = nombre,
                onValorCambiado = { nombre = it },
                etiqueta = stringResource(id = R.string.registro_campo_nombre),
                textoAyuda = stringResource(id = R.string.registro_campo_nombre_ayuda)
            )

            CampoTexto(
                valor = nombreComercial,
                onValorCambiado = { nombreComercial = it },
                etiqueta = stringResource(id = R.string.registro_campo_nombre_comercial),
                textoAyuda = stringResource(id = R.string.registro_campo_nombre_comercial_ayuda)
            )

            SelectorDesplegable(
                opciones = DatosDemo.ListaRegiones,
                seleccionado = regionSeleccionada,
                onOpcionSeleccionada = { nuevaRegion ->
                    regionSeleccionada = nuevaRegion
                    comunaSeleccionada = ""
                },
                etiqueta = stringResource(id = R.string.registro_campo_region)
            )

            SelectorDesplegable(
                opciones = comunasDisponibles,
                seleccionado = comunaSeleccionada,
                onOpcionSeleccionada = { comunaSeleccionada = it },
                etiqueta = stringResource(id = R.string.registro_campo_comuna),
                habilitado = regionSeleccionada.isNotBlank()
            )

            Text(
                text = stringResource(id = R.string.registro_rubros_explicacion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ChipsRubros(
                rubrosSeleccionados = rubrosSeleccionados,
                onRubroToggled = { rubro ->
                    if (rubrosSeleccionados.contains(rubro)) {
                        rubrosSeleccionados.remove(rubro)
                    } else {
                        rubrosSeleccionados.add(rubro)
                    }
                }
            )

            CampoTexto(
                valor = instagram,
                onValorCambiado = { instagram = it },
                etiqueta = stringResource(id = R.string.registro_campo_instagram),
                textoAyuda = stringResource(id = R.string.registro_campo_instagram_ayuda)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditarPerfilScreenPreview() {
    TextilCircularTheme {
        EditarPerfilScreen(
            emprendedora = DatosDemo.EmprendedoraDemo,
            onGuardarCambios = {},
            onNavegarAtras = {}
        )
    }
}
