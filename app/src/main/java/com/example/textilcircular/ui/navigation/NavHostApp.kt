package com.example.textilcircular.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.textilcircular.ui.components.BannerSinConexion
import com.example.textilcircular.ui.screens.PantallaProvisoria
import com.example.textilcircular.ui.screens.auth.BienvenidaScreen
import com.example.textilcircular.ui.screens.auth.LoginScreen
import com.example.textilcircular.ui.screens.auth.RegistroExitosoScreen
import com.example.textilcircular.ui.screens.auth.RegistroGuiadoScreen

/**
 * Componente principal de navegación y andamiaje (Scaffold) de Textil Circular.
 * Controla la visualización condicional de las barras inferiores y la navegación entre grafos.
 */
@Composable
fun NavHostApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    // Estado de simulación offline para demostración (Capa 1)
    var modoSinConexionDemo by remember { mutableStateOf(false) }

    // Detección de rol y pestañas activas
    val esPestanaEmprendedora = rutaActual in listOf(
        Ruta.Inicio.ruta,
        Ruta.Catalogo.ruta,
        Ruta.Mensajes.ruta,
        Ruta.Perfil.ruta
    )

    val esPestanaAdmin = rutaActual in listOf(
        Ruta.PanelAdmin.ruta,
        Ruta.EmprendimientosAdmin.ruta,
        Ruta.AutomatizacionesAdmin.ruta,
        Ruta.CuentaAdmin.ruta
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            when {
                esPestanaEmprendedora -> {
                    BarraNavegacionEmprendedora(
                        rutaActual = rutaActual,
                        onNavegar = { rutaDestino ->
                            navController.navigate(rutaDestino) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                esPestanaAdmin -> {
                    BarraNavegacionAdmin(
                        rutaActual = rutaActual,
                        onNavegar = { rutaDestino ->
                            navController.navigate(rutaDestino) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
        ) {
            // Banner de advertencia de conectividad transversal (RNF-02)
            BannerSinConexion(visible = modoSinConexionDemo)

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = Ruta.Bienvenida.ruta
                ) {
                    // ==========================================
                    // 1. GRAFO DE AUTENTICACIÓN Y ACCESO
                    // ==========================================
                    composable(Ruta.Bienvenida.ruta) {
                        BienvenidaScreen(
                            onCrearCuentaClick = { navController.navigate(Ruta.Registro.ruta) },
                            onIniciarSesionClick = { navController.navigate(Ruta.Login.ruta) }
                        )
                    }

                    composable(Ruta.Login.ruta) {
                        LoginScreen(
                            onNavegarAtras = { navController.popBackStack() },
                            onIngresarClick = { _, _ ->
                                // TODO(Capa 2): Autenticación con ViewModel y obtención de token de sesión
                                navController.navigate(Ruta.Inicio.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onDemoEmprendedoraClick = {
                                navController.navigate(Ruta.Inicio.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onDemoAdminClick = {
                                navController.navigate(Ruta.PanelAdmin.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Ruta.Registro.ruta) {
                        RegistroGuiadoScreen(
                            onNavegarAtras = { navController.popBackStack() },
                            onRegistroCompletado = {
                                navController.navigate(Ruta.RegistroExitoso.ruta)
                            }
                        )
                    }

                    composable(Ruta.RegistroExitoso.ruta) {
                        RegistroExitosoScreen(
                            onIrACuentaClick = {
                                navController.navigate(Ruta.Inicio.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    // ==========================================
                    // 2. GRAFO DE LA EMPRENDEDORA
                    // ==========================================
                    composable(Ruta.Inicio.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "5. Inicio Emprendedora",
                            descripcion = "Resumen del perfil, avance y accesos directos.",
                            textoAccion = "Ver mi catálogo",
                            onAccion = { navController.navigate(Ruta.Catalogo.ruta) }
                        )
                    }

                    composable(Ruta.Catalogo.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "6. Catálogo de Creaciones (RF-13, RF-18)",
                            descripcion = "Cuadrícula de productos y servicios con estados.",
                            textoAccion = "Agregar nuevo producto",
                            onAccion = { navController.navigate(Ruta.ProductoForm.crearRuta()) }
                        )
                    }

                    composable(
                        route = Ruta.ProductoForm.ruta,
                        arguments = listOf(navArgument("productoId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) {
                        PantallaProvisoria(
                            nombrePantalla = "7. Crear / Editar Producto (RF-13 a RF-17)",
                            descripcion = "Carga de fotos, descripción y explicación del proceso.",
                            textoAccion = "Volver al catálogo",
                            onAccion = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Ruta.ProductoDetalle.ruta,
                        arguments = listOf(navArgument("productoId") { type = NavType.IntType })
                    ) {
                        PantallaProvisoria(
                            nombrePantalla = "8. Detalle del Producto",
                            descripcion = "Carrusel de fotos y ficha completa.",
                            textoAccion = "Volver",
                            onAccion = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.VistaPreviaPublica.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "9. Vista Previa Pública (RF-18)",
                            descripcion = "Así verán tus clientes tu perfil en el Directorio Web.",
                            textoAccion = "Volver a mi perfil",
                            onAccion = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.Mensajes.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "10. Bandeja de Mensajes (RF-19, RF-22)",
                            descripcion = "Conversaciones con clientes interesados de la web.",
                            textoAccion = "Abrir conversación de ejemplo",
                            onAccion = { navController.navigate(Ruta.Conversacion.crearRuta(201)) }
                        )
                    }

                    composable(
                        route = Ruta.Conversacion.ruta,
                        arguments = listOf(navArgument("conversacionId") { type = NavType.IntType })
                    ) {
                        PantallaProvisoria(
                            nombrePantalla = "11. Conversación Interna (RF-20 a RF-22)",
                            descripcion = "Chat en tiempo real dentro de la plataforma.",
                            textoAccion = "Volver a mensajes",
                            onAccion = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.Perfil.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "12. Mi Perfil",
                            descripcion = "Datos de contacto, opciones de demostración y ajustes.",
                            textoAccion = "Cerrar sesión",
                            onAccion = {
                                navController.navigate(Ruta.Bienvenida.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Ruta.EditarPerfil.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "13. Editar Perfil (RF-09)",
                            descripcion = "Actualización de datos y comuna.",
                            textoAccion = "Guardar",
                            onAccion = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.Sincronizacion.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "14. Estado de Sincronización (RF-35)",
                            descripcion = "Control de cambios pendientes de subir.",
                            textoAccion = "Volver",
                            onAccion = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.BajaCuenta.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "15. Darme de Baja (RF-11)",
                            descripcion = "Confirmación informada de retiro del directorio.",
                            textoAccion = "Confirmar baja",
                            onAccion = {
                                navController.navigate(Ruta.BajaExitosa.ruta) {
                                    popUpTo(Ruta.Inicio.ruta) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Ruta.BajaExitosa.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "15b. Perfil Dado de Baja (RF-12)",
                            descripcion = "Tu perfil ya no está disponible en el directorio.",
                            textoAccion = "Ir a bienvenida",
                            onAccion = {
                                navController.navigate(Ruta.Bienvenida.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    // ==========================================
                    // 3. GRAFO DE LA ADMINISTRADORA
                    // ==========================================
                    composable(Ruta.PanelAdmin.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "A1. Panel Administrativo (RF-31, RF-32)",
                            descripcion = "Métricas territoriales e interacciones.",
                            textoAccion = "Ver lista de emprendimientos",
                            onAccion = { navController.navigate(Ruta.EmprendimientosAdmin.ruta) }
                        )
                    }

                    composable(Ruta.EmprendimientosAdmin.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "A2. Directorio de Emprendimientos (RF-27)",
                            descripcion = "Búsqueda y filtros por región, comuna y rubro.",
                            textoAccion = "Agregar nuevo",
                            onAccion = { navController.navigate(Ruta.FormEmprendimientoAdmin.crearRuta()) }
                        )
                    }

                    composable(
                        route = Ruta.DetalleEmprendimientoAdmin.ruta,
                        arguments = listOf(navArgument("emprendimientoId") { type = NavType.IntType })
                    ) {
                        PantallaProvisoria(
                            nombrePantalla = "A3. Detalle Emprendimiento (RF-28, RF-29)",
                            descripcion = "Revisión de consistencia y acciones de moderación.",
                            textoAccion = "Volver",
                            onAccion = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Ruta.FormEmprendimientoAdmin.ruta,
                        arguments = listOf(navArgument("emprendimientoId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) {
                        PantallaProvisoria(
                            nombrePantalla = "A4. Formulario de Emprendimiento (RF-28)",
                            descripcion = "Creación o edición manual de fichas.",
                            textoAccion = "Guardar",
                            onAccion = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.AutomatizacionesAdmin.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "A5. Auditoría de Automatizaciones (RF-30)",
                            descripcion = "Supervisión de correos de bienvenida automáticos.",
                            textoAccion = "Volver al panel",
                            onAccion = { navController.navigate(Ruta.PanelAdmin.ruta) }
                        )
                    }

                    composable(Ruta.CuentaAdmin.ruta) {
                        PantallaProvisoria(
                            nombrePantalla = "A6. Cuenta de Administradora",
                            descripcion = "Datos de sesión y control del sistema.",
                            textoAccion = "Cerrar sesión de Administradora",
                            onAccion = {
                                navController.navigate(Ruta.Bienvenida.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
