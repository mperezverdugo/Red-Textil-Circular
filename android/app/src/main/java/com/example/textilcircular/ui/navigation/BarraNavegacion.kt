package com.example.textilcircular.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textilcircular.R
import com.example.textilcircular.ui.theme.AmarilloAcento
import com.example.textilcircular.ui.theme.Blanco
import com.example.textilcircular.ui.theme.TextilCircularTheme

data class ItemNavegacion(
    val ruta: String,
    val etiquetaResId: Int,
    val icono: ImageVector
)

val ItemsEmprendedora = listOf(
    ItemNavegacion(Ruta.Inicio.ruta, R.string.nav_inicio, Icons.Default.Home),
    ItemNavegacion(Ruta.Catalogo.ruta, R.string.nav_catalogo, Icons.Default.ShoppingCart),
    ItemNavegacion(Ruta.Mensajes.ruta, R.string.nav_mensajes, Icons.Default.Email),
    ItemNavegacion(Ruta.Perfil.ruta, R.string.nav_perfil, Icons.Default.Person)
)

val ItemsAdministradora = listOf(
    ItemNavegacion(Ruta.PanelAdmin.ruta, R.string.nav_admin_panel, Icons.Default.Info),
    ItemNavegacion(Ruta.EmprendimientosAdmin.ruta, R.string.nav_admin_emprendimientos, Icons.AutoMirrored.Filled.List),
    ItemNavegacion(Ruta.AutomatizacionesAdmin.ruta, R.string.nav_admin_automatizaciones, Icons.Default.Refresh),
    ItemNavegacion(Ruta.CuentaAdmin.ruta, R.string.nav_admin_cuenta, Icons.Default.Person)
)

val ItemsCliente = listOf(
    ItemNavegacion(Ruta.Explorar.ruta, R.string.nav_cliente_explorar, Icons.Default.ShoppingCart),
    ItemNavegacion(Ruta.EducacionCliente.ruta, R.string.nav_educacion, Icons.Default.Info),
    ItemNavegacion(Ruta.MensajesCliente.ruta, R.string.nav_cliente_mensajes, Icons.Default.Email),
    ItemNavegacion(Ruta.PerfilCliente.ruta, R.string.nav_cliente_perfil, Icons.Default.Person)
)

// Colores de la barra flotante estilo cápsula editorial
private val ColorFondoBarraFlotante = Color(0xFF142423)
private val ColorIndicadorActivo = Color(0xFF223B39)
private val FormaCapsulaNavegacion = RoundedCornerShape(32.dp)

// Barra de navegación inferior flotante tipo cápsula para las pantallas de la emprendedora.
@Composable
fun BarraNavegacionEmprendedora(
    rutaActual: String?,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(elevation = 10.dp, shape = FormaCapsulaNavegacion)
            .clip(FormaCapsulaNavegacion),
        containerColor = ColorFondoBarraFlotante,
        contentColor = Blanco
    ) {
        ItemsEmprendedora.forEach { item ->
            val seleccionado = rutaActual == item.ruta
            NavigationBarItem(
                selected = seleccionado,
                onClick = { onNavegar(item.ruta) },
                icon = {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = stringResource(id = item.etiquetaResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.etiquetaResId),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AmarilloAcento,
                    selectedTextColor = AmarilloAcento,
                    indicatorColor = ColorIndicadorActivo,
                    unselectedIconColor = Blanco.copy(alpha = 0.65f),
                    unselectedTextColor = Blanco.copy(alpha = 0.65f)
                )
            )
        }
    }
}

// Barra de navegación inferior flotante tipo cápsula para las pantallas de la administradora.
@Composable
fun BarraNavegacionAdmin(
    rutaActual: String?,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(elevation = 10.dp, shape = FormaCapsulaNavegacion)
            .clip(FormaCapsulaNavegacion),
        containerColor = ColorFondoBarraFlotante,
        contentColor = Blanco
    ) {
        ItemsAdministradora.forEach { item ->
            val seleccionado = rutaActual == item.ruta
            NavigationBarItem(
                selected = seleccionado,
                onClick = { onNavegar(item.ruta) },
                icon = {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = stringResource(id = item.etiquetaResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.etiquetaResId),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AmarilloAcento,
                    selectedTextColor = AmarilloAcento,
                    indicatorColor = ColorIndicadorActivo,
                    unselectedIconColor = Blanco.copy(alpha = 0.65f),
                    unselectedTextColor = Blanco.copy(alpha = 0.65f)
                )
            )
        }
    }
}

// Barra de navegación inferior flotante tipo cápsula para las pantallas del cliente.
@Composable
fun BarraNavegacionCliente(
    rutaActual: String?,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    mensajesNoLeidos: Int = 0
) {
    NavigationBar(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(elevation = 10.dp, shape = FormaCapsulaNavegacion)
            .clip(FormaCapsulaNavegacion),
        containerColor = ColorFondoBarraFlotante,
        contentColor = Blanco
    ) {
        ItemsCliente.forEach { item ->
            val seleccionado = rutaActual == item.ruta
            NavigationBarItem(
                selected = seleccionado,
                onClick = { onNavegar(item.ruta) },
                icon = {
                    if (item.ruta == Ruta.MensajesCliente.ruta && mensajesNoLeidos > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = AmarilloAcento,
                                    contentColor = ColorFondoBarraFlotante
                                ) {
                                    Text(text = if (mensajesNoLeidos > 99) "99+" else mensajesNoLeidos.toString())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icono,
                                contentDescription = stringResource(id = item.etiquetaResId)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icono,
                            contentDescription = stringResource(id = item.etiquetaResId)
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(id = item.etiquetaResId),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AmarilloAcento,
                    selectedTextColor = AmarilloAcento,
                    indicatorColor = ColorIndicadorActivo,
                    unselectedIconColor = Blanco.copy(alpha = 0.65f),
                    unselectedTextColor = Blanco.copy(alpha = 0.65f)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BarraNavegacionEmprendedoraPreview() {
    TextilCircularTheme {
        BarraNavegacionEmprendedora(
            rutaActual = Ruta.Inicio.ruta,
            onNavegar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BarraNavegacionAdminPreview() {
    TextilCircularTheme {
        BarraNavegacionAdmin(
            rutaActual = Ruta.PanelAdmin.ruta,
            onNavegar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BarraNavegacionClientePreview() {
    TextilCircularTheme {
        BarraNavegacionCliente(
            rutaActual = Ruta.Explorar.ruta,
            onNavegar = {},
            mensajesNoLeidos = 2
        )
    }
}
