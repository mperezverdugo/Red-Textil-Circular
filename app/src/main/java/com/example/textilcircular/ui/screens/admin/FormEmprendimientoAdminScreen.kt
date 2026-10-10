package com.example.textilcircular.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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

/**
 * Pantalla A4: Agregar / Editar Emprendimiento para Administradora (RF-28)
 * Formulario manual de alta y corrección de fichas.
 */
@Composable
fun FormEmprendimientoAdminScreen(
    emprendedoraExistente: Emprendedora?,
    onGuardarClick: (Emprendedora) -> Unit,
    onNavegarAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esModoEdicion = emprendedoraExistente != null

    var nombre by remember { mutableStateOf(emprendedoraExistente?.nombre ?: "") }
    var rut by remember { mutableStateOf(emprendedoraExistente?.rut ?: "") }
    var email by remember { mutableStateOf(emprendedoraExistente?.email ?: "") }
    var nombreComercial by remember { mutableStateOf(emprendedoraExistente?.nombreComercial ?: "") }
    var regionSeleccionada by remember { mutableStateOf(emprendedoraExistente?.region ?: "") }
    var comunaSeleccionada by remember { mutableStateOf(emprendedoraExistente?.comuna ?: "") }
    var instagram by remember { mutableStateOf(emprendedoraExistente?.instagram ?: "") }

    val rubrosSeleccionados = remember {
        mutableStateListOf<Rubro>().apply {
            if (emprendedoraExistente != null) {
                addAll(emprendedoraExistente.rubros)
            }
        }
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
                titulo = if (esModoEdicion) {
                    stringResource(id = R.string.admin_form_titulo_editar)
                } else {
                    stringResource(id = R.string.admin_form_titulo_crear)
                },
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
                BotonPrincipal(
                    texto = stringResource(id = R.string.accion_guardar),
                    onClick = {
                        val emp = Emprendedora(
                            id = emprendedoraExistente?.id ?: 0,
                            rut = rut,
                            nombre = nombre,
                            nombreComercial = nombreComercial,
                            region = regionSeleccionada,
                            comuna = comunaSeleccionada,
                            rubros = rubrosSeleccionados.toList(),
                            email = email,
                            instagram = instagram.ifBlank { null },
                            activa = emprendedoraExistente?.activa ?: true
                        )
                        onGuardarClick(emp)
                    },
                    habilitado = nombre.isNotBlank() && rut.isNotBlank() && email.isNotBlank() &&
                            nombreComercial.isNotBlank() && regionSeleccionada.isNotBlank() && comunaSeleccionada.isNotBlank()
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
                valor = rut,
                onValorCambiado = { rut = it },
                etiqueta = stringResource(id = R.string.registro_campo_rut),
                textoAyuda = stringResource(id = R.string.registro_campo_rut_ayuda)
            )

            CampoTexto(
                valor = email,
                onValorCambiado = { email = it },
                etiqueta = stringResource(id = R.string.registro_campo_correo),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            CampoTexto(
                valor = nombreComercial,
                onValorCambiado = { nombreComercial = it },
                etiqueta = stringResource(id = R.string.registro_campo_nombre_comercial)
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
fun FormEmprendimientoAdminScreenPreview() {
    TextilCircularTheme {
        FormEmprendimientoAdminScreen(
            emprendedoraExistente = null,
            onGuardarClick = {},
            onNavegarAtras = {}
        )
    }
}
