package com.example.textilcircular.ui.navigation

import androidx.activity.compose.BackHandler
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
import kotlinx.coroutines.launch
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textilcircular.ui.screens.profile.BajaCuentaScreen
import com.example.textilcircular.ui.screens.profile.BajaExitosaScreen
import com.example.textilcircular.ui.screens.profile.EditarPerfilScreen
import com.example.textilcircular.ui.screens.profile.PerfilScreen
import com.example.textilcircular.ui.screens.profile.PerfilViewModel
import com.example.textilcircular.ui.screens.profile.SincronizacionScreen
import com.example.textilcircular.ui.viewmodel.AppViewModelProvider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.textilcircular.TextilCircularApp
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.ui.screens.cliente.ExplorarScreen
import com.example.textilcircular.ui.screens.cliente.RegistroClienteScreen

// Navegación principal de la app y estructura con barras inferiores.
@Composable
fun NavHostApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val perfilViewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val perfilUiState by perfilViewModel.uiState.collectAsStateWithLifecycle()

    val productosDemoEstado = remember {
        mutableStateListOf<Producto>().apply { addAll(DatosDemo.ProductosDemo) }
    }

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

    val contexto = LocalContext.current
    val appContainer = (contexto.applicationContext as TextilCircularApp).contenedor
    val estadoSesion by appContainer.sesionRepository.estadoSesion.collectAsStateWithLifecycle()

    // Rutas protegidas por rol para evitar accesos cruzados
    val rutasAdmin = listOf(
        Ruta.PanelAdmin.ruta,
        Ruta.EmprendimientosAdmin.ruta,
        Ruta.AutomatizacionesAdmin.ruta,
        Ruta.CuentaAdmin.ruta,
        "admin_"
    )

    val rutasEmprendedora = listOf(
        Ruta.Inicio.ruta,
        Ruta.Catalogo.ruta,
        Ruta.Mensajes.ruta,
        Ruta.Perfil.ruta,
        "emp_"
    )

    val rutasCliente = listOf(
        Ruta.Explorar.ruta,
        Ruta.MensajesCliente.ruta,
        Ruta.PerfilCliente.ruta,
        "cliente_"
    )

    // Protección de rutas: redirige a cada usuario a su inicio correspondiente si intenta acceder a otra área
    LaunchedEffect(rutaActual, estadoSesion) {
        val ruta = rutaActual ?: return@LaunchedEffect
        when (estadoSesion) {
            is EstadoSesion.AutenticadoCliente -> {
                val esProhibida = rutasAdmin.any { ruta.startsWith(it) } || rutasEmprendedora.any { ruta.startsWith(it) }
                if (esProhibida) {
                    navController.navigate(Ruta.Explorar.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is EstadoSesion.AutenticadoEmprendedora -> {
                val esProhibida = rutasAdmin.any { ruta.startsWith(it) } || rutasCliente.any { ruta.startsWith(it) }
                if (esProhibida) {
                    navController.navigate(Ruta.Inicio.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is EstadoSesion.AutenticadoAdmin -> {
                val esProhibida = rutasEmprendedora.any { ruta.startsWith(it) } || rutasCliente.any { ruta.startsWith(it) }
                if (esProhibida) {
                    navController.navigate(Ruta.PanelAdmin.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is EstadoSesion.NoAutenticado -> {
                val esProtegida = rutasAdmin.any { ruta.startsWith(it) } ||
                        rutasEmprendedora.any { ruta.startsWith(it) } ||
                        (rutasCliente.any { ruta.startsWith(it) } && ruta != Ruta.RegistroCliente.ruta)
                if (esProtegida) {
                    navController.navigate(Ruta.Bienvenida.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            when {
                esPestanaEmprendedora -> {
                    BarraNavegacionEmprendedora(
                        rutaActual = rutaActual,
                        onNavegar = { rutaDestino ->
                            navController.navigate(rutaDestino) {
                                popUpTo(Ruta.Inicio.ruta) {
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
                                popUpTo(Ruta.PanelAdmin.ruta) {
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
            // Aviso visual cuando no hay conexión a internet
            BannerSinConexion(visible = perfilUiState.modoSinConexion)

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = Ruta.Bienvenida.ruta
                ) {
                    // Rutas de inicio y registro
                    composable(Ruta.Bienvenida.ruta) {
                        BienvenidaScreen(
                            onSoyEmprendedoraClick = { navController.navigate(Ruta.Registro.ruta) },
                            onSoyClienteClick = { navController.navigate(Ruta.RegistroCliente.ruta) },
                            onIniciarSesionClick = { navController.navigate(Ruta.Login.ruta) }
                        )
                    }

                    composable(Ruta.Login.ruta) {
                        LoginScreen(
                            onNavegarAtras = { navController.popBackStack() },
                            onLoginExitosoEmprendedora = {
                                navController.navigate(Ruta.Inicio.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onLoginExitosoAdmin = {
                                navController.navigate(Ruta.PanelAdmin.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onLoginExitosoCliente = {
                                navController.navigate(Ruta.Explorar.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Ruta.RegistroCliente.ruta) {
                        RegistroClienteScreen(
                            onNavegarAtras = { navController.popBackStack() },
                            onRegistroCompletado = {
                                navController.navigate(Ruta.Explorar.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Ruta.Explorar.ruta) {
                        ExplorarScreen(
                            onEmprendedoraClick = { id ->
                                navController.navigate(Ruta.PerfilEmprendedoraCliente.crearRuta(id))
                            }
                        )
                    }

                    composable(
                        route = Ruta.PerfilEmprendedoraCliente.ruta,
                        arguments = listOf(navArgument("emprendedoraId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val empId = backStackEntry.arguments?.getInt("emprendedoraId") ?: 1
                        val empFlow = remember(empId) { appContainer.emprendedoraRepository.obtenerPorId(empId) }
                        val emp by empFlow.collectAsStateWithLifecycle(initialValue = null)

                        val prodsFlow = remember(empId) { appContainer.productoRepository.obtenerPorEmprendedora(empId) }
                        val prods by prodsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

                        val emprendedoraActual = emp ?: DatosDemo.EmprendedorasActivasDemo.find { it.id == empId } ?: DatosDemo.EmprendedoraDemo

                        VistaPreviaPublicaScreen(
                            emprendedora = emprendedoraActual,
                            productos = prods,
                            onProductoClick = { prodId ->
                                navController.navigate(Ruta.ProductoDetalleCliente.crearRuta(prodId))
                            },
                            onNavegarAtras = { navController.popBackStack() },
                            esCliente = true,
                            onEscribirClick = {
                                appContainer.adminRepository.registrarClicContacto(
                                    region = emprendedoraActual.region,
                                    rubro = emprendedoraActual.rubros.firstOrNull()?.nombre ?: ""
                                )
                                // TODO: en el Bloque 5 abrimos la conversación real
                                navController.navigate(Ruta.MensajesCliente.ruta)
                            }
                        )
                    }

                    composable(
                        route = Ruta.ProductoDetalleCliente.ruta,
                        arguments = listOf(navArgument("productoId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val prodId = backStackEntry.arguments?.getInt("productoId") ?: 101
                        val prodFlow = remember(prodId) { appContainer.productoRepository.obtenerPorId(prodId) }
                        val prod by prodFlow.collectAsStateWithLifecycle(initialValue = null)
                        val productoActual = prod ?: DatosDemo.ProductosDemo.find { it.id == prodId } ?: DatosDemo.ProductosDemo.first()

                        ProductoDetalleScreen(
                            producto = productoActual,
                            onNavegarAtras = { navController.popBackStack() },
                            esCliente = true,
                            onPreguntarClick = {
                                val emp = DatosDemo.EmprendedorasActivasDemo.find { it.id == productoActual.emprendedoraId }
                                appContainer.adminRepository.registrarClicContacto(
                                    region = emp?.region ?: "",
                                    rubro = emp?.rubros?.firstOrNull()?.nombre ?: ""
                                )
                                // TODO: en el Bloque 5 abrimos la conversación real con el producto sugerido
                                navController.navigate(Ruta.MensajesCliente.ruta)
                            }
                        )
                    }

                    composable(Ruta.Registro.ruta) {
                        RegistroGuiadoScreen(
                            onNavegarAtras = { navController.popBackStack() },
                            onRegistroCompletado = {
                                navController.navigate(Ruta.RegistroExitoso.ruta) {
                                    popUpTo(Ruta.Registro.ruta) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Ruta.RegistroExitoso.ruta) {
                        BackHandler {
                            navController.navigate(Ruta.Inicio.ruta) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        RegistroExitosoScreen(
                            onIrACuentaClick = {
                                navController.navigate(Ruta.Inicio.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    // Rutas de la emprendedora
                    composable(Ruta.Inicio.ruta) {
                        InicioScreen(
                            emprendedora = perfilUiState.emprendedora,
                            onAgregarProductoClick = { navController.navigate(Ruta.ProductoForm.crearRuta()) },
                            onVerMensajesClick = { navController.navigate(Ruta.Mensajes.ruta) }
                        )
                    }

                    composable(Ruta.Catalogo.ruta) {
                        CatalogoScreen(
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

                        ProductoFormScreen(
                            productoId = prodId,
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Ruta.ProductoDetalle.ruta,
                        arguments = listOf(navArgument("productoId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val prodId = backStackEntry.arguments?.getInt("productoId") ?: 0
                        val contexto = androidx.compose.ui.platform.LocalContext.current
                        val appContainer = (contexto.applicationContext as com.example.textilcircular.TextilCircularApp).contenedor
                        val productoPorId by androidx.compose.runtime.produceState<Producto?>(initialValue = null, prodId) {
                            appContainer.productoRepository.obtenerPorId(prodId).collect {
                                value = it
                            }
                        }
                        val productoFinal = productoPorId ?: DatosDemo.ProductosDemo.first()
                        val scope = androidx.compose.runtime.rememberCoroutineScope()

                        ProductoDetalleScreen(
                            producto = productoFinal,
                            onEditarClick = { id -> navController.navigate(Ruta.ProductoForm.crearRuta(id)) },
                            onEliminarClick = { idAEliminar ->
                                scope.launch {
                                    appContainer.productoRepository.eliminar(idAEliminar)
                                    navController.popBackStack()
                                }
                            },
                            onNavegarAtras = { navController.popBackStack() }
                        )
                    }

                    composable(Ruta.VistaPreviaPublica.ruta) {
                        val contexto = androidx.compose.ui.platform.LocalContext.current
                        val appContainer = (contexto.applicationContext as com.example.textilcircular.TextilCircularApp).contenedor
                        val empId = perfilUiState.emprendedora?.id ?: 1
                        val productosEmprendedora by androidx.compose.runtime.produceState<List<Producto>>(initialValue = emptyList(), empId) {
                            appContainer.productoRepository.obtenerPorEmprendedora(empId).collect {
                                value = it
                            }
                        }

                        VistaPreviaPublicaScreen(
                            emprendedora = perfilUiState.emprendedora,
                            productos = if (simularListasVacias) emptyList() else productosEmprendedora,
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

                        val retrocederConversacion = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Ruta.Mensajes.ruta) {
                                    popUpTo(Ruta.Inicio.ruta)
                                }
                            }
                        }

                        BackHandler {
                            retrocederConversacion()
                        }

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
                            onNavegarAtras = { retrocederConversacion() }
                        )
                    }

                    composable(Ruta.Perfil.ruta) {
                        PerfilScreen(
                            onEditarPerfilClick = { navController.navigate(Ruta.EditarPerfil.ruta) },
                            onVistaPreviaClick = { navController.navigate(Ruta.VistaPreviaPublica.ruta) },
                            onSincronizacionClick = { navController.navigate(Ruta.Sincronizacion.ruta) },
                            onBajaClick = { navController.navigate(Ruta.BajaCuenta.ruta) },
                            onCerrarSesionClick = {
                                navController.navigate(Ruta.Bienvenida.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            viewModel = perfilViewModel
                        )
                    }

                    composable(Ruta.EditarPerfil.ruta) {
                        EditarPerfilScreen(
                            onGuardarExitoso = { navController.popBackStack() },
                            onNavegarAtras = { navController.popBackStack() },
                            viewModel = perfilViewModel
                        )
                    }

                    composable(Ruta.Sincronizacion.ruta) {
                        SincronizacionScreen(
                            onNavegarAtras = { navController.popBackStack() },
                            viewModel = perfilViewModel
                        )
                    }

                    composable(Ruta.BajaCuenta.ruta) {
                        BajaCuentaScreen(
                            onConfirmarBaja = {
                                navController.navigate(Ruta.BajaExitosa.ruta) {
                                    popUpTo(Ruta.Inicio.ruta) { inclusive = true }
                                }
                            },
                            onCancelarBaja = { navController.popBackStack() },
                            viewModel = perfilViewModel
                        )
                    }

                    composable(Ruta.BajaExitosa.ruta) {
                        BackHandler {
                            navController.navigate(Ruta.Bienvenida.ruta) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        BajaExitosaScreen(
                            onVolverInicioClick = {
                                navController.navigate(Ruta.Bienvenida.ruta) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    // Rutas de la administración
                    composable(Ruta.PanelAdmin.ruta) {
                        val totalPorRevisar = fichasAdminEstado.count { it.estadoFicha == EstadoFichaEmprendimiento.POR_REVISAR }
                        PanelAdminScreen(
                            onVerFichasPorRevisarClick = { navController.navigate(Ruta.EmprendimientosAdmin.ruta) },
                            fichasPorRevisar = totalPorRevisar
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
                                val problemas = com.example.textilcircular.util.validacion.AuditorFichas.auditar(empGuardada)
                                val nuevoEstado = if (problemas.isNotEmpty()) {
                                    EstadoFichaEmprendimiento.POR_REVISAR
                                } else {
                                    EstadoFichaEmprendimiento.ACTIVA
                                }

                                if (empId != null) {
                                    val idx = fichasAdminEstado.indexOfFirst { it.emprendedora.id == empId }
                                    if (idx >= 0) {
                                        fichasAdminEstado[idx] = fichasAdminEstado[idx].copy(
                                            emprendedora = empGuardada,
                                            estadoFicha = nuevoEstado,
                                            problemasDetectados = problemas
                                        )
                                    }
                                } else {
                                    val nuevoId = (fichasAdminEstado.maxOfOrNull { it.emprendedora.id } ?: 10) + 1
                                    fichasAdminEstado.add(
                                        FichaAdminEmprendimiento(
                                            emprendedora = empGuardada.copy(id = nuevoId),
                                            estadoFicha = nuevoEstado,
                                            problemasDetectados = problemas
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
