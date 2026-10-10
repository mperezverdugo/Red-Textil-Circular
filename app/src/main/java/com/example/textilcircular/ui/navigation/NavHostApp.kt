package com.example.textilcircular.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.textilcircular.model.Producto
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.textilcircular.ui.components.BannerSinConexion
import com.example.textilcircular.ui.screens.auth.BienvenidaScreen
import com.example.textilcircular.ui.screens.auth.LoginScreen
import com.example.textilcircular.ui.screens.auth.RegistroExitosoScreen
import com.example.textilcircular.ui.screens.auth.RegistroGuiadoScreen
import com.example.textilcircular.ui.screens.catalog.CatalogoScreen
import com.example.textilcircular.ui.screens.catalog.ProductoDetalleScreen
import com.example.textilcircular.ui.screens.catalog.ProductoFormScreen
import com.example.textilcircular.ui.screens.catalog.VistaPreviaPublicaScreen
import com.example.textilcircular.ui.screens.home.InicioScreen
import com.example.textilcircular.ui.screens.chat.ConversacionScreen
import com.example.textilcircular.ui.screens.chat.MensajesScreen
import com.example.textilcircular.ui.screens.profile.BajaCuentaScreen
import com.example.textilcircular.ui.screens.profile.BajaExitosaScreen
import com.example.textilcircular.ui.screens.profile.EditarPerfilScreen
import com.example.textilcircular.ui.screens.profile.PerfilScreen
import com.example.textilcircular.ui.screens.profile.SincronizacionScreen
import com.example.textilcircular.ui.screens.admin.AutomatizacionesAdminScreen
import com.example.textilcircular.ui.screens.admin.CuentaAdminScreen
import com.example.textilcircular.ui.screens.admin.DetalleEmprendimientoAdminScreen
import com.example.textilcircular.ui.screens.admin.EmprendimientosAdminScreen
import com.example.textilcircular.ui.screens.admin.FormEmprendimientoAdminScreen
import com.example.textilcircular.ui.screens.admin.PanelAdminScreen
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.ui.preview.DatosDemo

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

    // Lista interactiva de productos para la demostración de la Capa 1 (RNF-11)
    val productosDemoEstado = remember {
        mutableStateListOf<Producto>().apply { addAll(DatosDemo.ProductosDemo) }
    }

    var emprendedoraEstado by remember { mutableStateOf(DatosDemo.EmprendedoraDemo) }
    var simularListasVacias by remember { mutableStateOf(false) }
    val mensajesChatEstado = remember {
        mutableStateListOf<Mensaje>().apply { addAll(DatosDemo.MensajesConversacionDemo) }
    }
    val fichasAdminEstado = remember {
        mutableStateListOf<FichaAdminEmprendimiento>().apply { addAll(DatosDemo.EmprendimientosAdminDemo) }
    }

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
                        InicioScreen(
                            emprendedora = emprendedoraEstado,
                            onAgregarProductoClick = { navController.navigate(Ruta.ProductoForm.crearRuta()) },
                            onVerMensajesClick = { navController.navigate(Ruta.Mensajes.ruta) }
                        )
                    }

                    composable(Ruta.Catalogo.ruta) {
                        CatalogoScreen(
                            productos = if (simularListasVacias) emptyList() else productosDemoEstado,
                            onProductoClick = { id -> navController.navigate(Ruta.ProductoDetalle.crearRuta(id)) },
                            onAgregarProductoClick = { navController.navigate(Ruta.ProductoForm.crearRuta()) }
                        )
                    }

                    composable(
                        route = Ruta.ProductoForm.ruta,
                        arguments = listOf(navArgument("productoId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) { backStackEntry ->
                        val prodIdStr = backStackEntry.arguments?.getString("productoId")
                        val prodId = prodIdStr?.toIntOrNull()
                        val productoExistente = if (prodId != null) productosDemoEstado.find { it.id == prodId } else null

                        ProductoFormScreen(
                            productoExistente = productoExistente,
                            onGuardarClick = { productoGuardado ->
                                if (prodId != null) {
                                    val idx = productosDemoEstado.indexOfFirst { it.id == prodId }
                                    if (idx >= 0) productosDemoEstado[idx] = productoGuardado
                                } else {
                                    val nuevoId = (productosDemoEstado.maxOfOrNull { it.id } ?: 100) + 1
                                    productosDemoEstado.add(productoGuardado.copy(id = nuevoId))
                                }
                                navController.popBackStack()
                            },
                            onEliminarClick = { idAEliminar ->
                                productosDemoEstado.removeAll { it.id == idAEliminar }
                                navController.popBackStack()
                            },
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Ruta.ProductoDetalle.ruta,
                        arguments = listOf(navArgument("productoId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val prodId = backStackEntry.arguments?.getInt("productoId") ?: 0
                        val producto = productosDemoEstado.find { it.id == prodId } ?: DatosDemo.ProductosDemo.first()

                        ProductoDetalleScreen(
                            producto = producto,
                            onEditarClick = { id -> navController.navigate(Ruta.ProductoForm.crearRuta(id)) },
                            onEliminarClick = { idAEliminar ->
                                productosDemoEstado.removeAll { it.id == idAEliminar }
                                navController.popBackStack()
                            },
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.VistaPreviaPublica.ruta) {
                        VistaPreviaPublicaScreen(
                            emprendedora = emprendedoraEstado,
                            productos = if (simularListasVacias) emptyList() else productosDemoEstado,
                            onProductoClick = { id -> navController.navigate(Ruta.ProductoDetalle.crearRuta(id)) },
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.Mensajes.ruta) {
                        MensajesScreen(
                            conversaciones = if (simularListasVacias) emptyList() else DatosDemo.ConversacionesDemo,
                            onConversacionClick = { id -> navController.navigate(Ruta.Conversacion.crearRuta(id)) }
                        )
                    }

                    composable(
                        route = Ruta.Conversacion.ruta,
                        arguments = listOf(navArgument("conversacionId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val conversacionId = backStackEntry.arguments?.getInt("conversacionId") ?: 201
                        val conversacion = DatosDemo.ConversacionesDemo.find { it.id == conversacionId } ?: DatosDemo.ConversacionesDemo.first()

                        ConversacionScreen(
                            conversacion = conversacion,
                            mensajes = mensajesChatEstado,
                            onEnviarMensaje = { nuevoTexto ->
                                mensajesChatEstado.add(
                                    Mensaje(
                                        id = mensajesChatEstado.size + 1,
                                        conversacionId = conversacionId,
                                        emisor = TipoEmisor.EMPRENDEDORA,
                                        contenido = nuevoTexto,
                                        fechaHora = "Ahora"
                                    )
                                )
                            },
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.Perfil.ruta) {
                        PerfilScreen(
                            emprendedora = emprendedoraEstado,
                            modoSinConexion = modoSinConexionDemo,
                            onModoSinConexionCambiado = { modoSinConexionDemo = it },
                            simularListasVacias = simularListasVacias,
                            onSimularListasVaciasCambiado = { simularListasVacias = it },
                            onEditarPerfilClick = { navController.navigate(Ruta.EditarPerfil.ruta) },
                            onVistaPreviaClick = { navController.navigate(Ruta.VistaPreviaPublica.ruta) },
                            onSincronizacionClick = { navController.navigate(Ruta.Sincronizacion.ruta) },
                            onBajaClick = { navController.navigate(Ruta.BajaCuenta.ruta) },
                            onCerrarSesionClick = {
                                navController.navigate(Ruta.Bienvenida.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Ruta.EditarPerfil.ruta) {
                        EditarPerfilScreen(
                            emprendedora = emprendedoraEstado,
                            onGuardarCambios = { nueva ->
                                emprendedoraEstado = nueva
                                navController.popBackStack()
                            },
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.Sincronizacion.ruta) {
                        SincronizacionScreen(
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.BajaCuenta.ruta) {
                        BajaCuentaScreen(
                            onConfirmarBaja = {
                                emprendedoraEstado = emprendedoraEstado.copy(activa = false)
                                navController.navigate(Ruta.BajaExitosa.ruta) {
                                    popUpTo(Ruta.Inicio.ruta) { inclusive = true }
                                }
                            },
                            onCancelarBaja = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.BajaExitosa.ruta) {
                        BajaExitosaScreen(
                            onVolverInicioClick = {
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
                        PanelAdminScreen(
                            onVerFichasPorRevisarClick = { navController.navigate(Ruta.EmprendimientosAdmin.ruta) }
                        )
                    }

                    composable(Ruta.EmprendimientosAdmin.ruta) {
                        EmprendimientosAdminScreen(
                            fichas = fichasAdminEstado,
                            onEmprendimientoClick = { id -> navController.navigate(Ruta.DetalleEmprendimientoAdmin.crearRuta(id)) },
                            onAgregarEmprendimientoClick = { navController.navigate(Ruta.FormEmprendimientoAdmin.crearRuta()) }
                        )
                    }

                    composable(
                        route = Ruta.DetalleEmprendimientoAdmin.ruta,
                        arguments = listOf(navArgument("emprendimientoId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val empId = backStackEntry.arguments?.getInt("emprendimientoId") ?: 1
                        val ficha = fichasAdminEstado.find { it.emprendedora.id == empId } ?: fichasAdminEstado.first()

                        DetalleEmprendimientoAdminScreen(
                            ficha = ficha,
                            onEditarClick = { id -> navController.navigate(Ruta.FormEmprendimientoAdmin.crearRuta(id)) },
                            onCambiarVisibilidadClick = { id ->
                                val idx = fichasAdminEstado.indexOfFirst { it.emprendedora.id == id }
                                if (idx >= 0) {
                                    val actual = fichasAdminEstado[idx]
                                    val nuevoEstado = if (actual.estadoFicha == EstadoFichaEmprendimiento.ACTIVA) {
                                        EstadoFichaEmprendimiento.OCULTA
                                    } else {
                                        EstadoFichaEmprendimiento.ACTIVA
                                    }
                                    fichasAdminEstado[idx] = actual.copy(estadoFicha = nuevoEstado)
                                }
                            },
                            onEliminarClick = { idAEliminar ->
                                fichasAdminEstado.removeAll { it.emprendedora.id == idAEliminar }
                                navController.popBackStack()
                            },
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Ruta.FormEmprendimientoAdmin.ruta,
                        arguments = listOf(navArgument("emprendimientoId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) { backStackEntry ->
                        val empIdStr = backStackEntry.arguments?.getString("emprendimientoId")
                        val empId = empIdStr?.toIntOrNull()
                        val empExistente = if (empId != null) fichasAdminEstado.find { it.emprendedora.id == empId }?.emprendedora else null

                        FormEmprendimientoAdminScreen(
                            emprendedoraExistente = empExistente,
                            onGuardarClick = { empGuardada ->
                                if (empId != null) {
                                    val idx = fichasAdminEstado.indexOfFirst { it.emprendedora.id == empId }
                                    if (idx >= 0) {
                                        fichasAdminEstado[idx] = fichasAdminEstado[idx].copy(emprendedora = empGuardada)
                                    }
                                } else {
                                    val nuevoId = (fichasAdminEstado.maxOfOrNull { it.emprendedora.id } ?: 10) + 1
                                    fichasAdminEstado.add(
                                        FichaAdminEmprendimiento(
                                            emprendedora = empGuardada.copy(id = nuevoId),
                                            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
                                            problemasDetectados = emptyList()
                                        )
                                    )
                                }
                                navController.popBackStack()
                            },
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.AutomatizacionesAdmin.ruta) {
                        AutomatizacionesAdminScreen()
                    }

                    composable(Ruta.CuentaAdmin.ruta) {
                        CuentaAdminScreen(
                            onCerrarSesionClick = {
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
