package com.example.textilcircular.ui.navigation

/**
 * Definición centralizada de todas las rutas de navegación de la aplicación mediante Sealed Class con Strings.
 * Organizada en 3 grafos: Autenticación, Emprendedora y Administradora.
 */
sealed class Ruta(val ruta: String) {

    // --- Grafo de Autenticación y Registro ---
    data object Bienvenida : Ruta("auth_bienvenida")
    data object Login : Ruta("auth_login")
    data object Registro : Ruta("auth_registro")
    data object RegistroExitoso : Ruta("auth_registro_exitoso")

    // --- Grafo de la Emprendedora (Pestañas principales con barra inferior) ---
    data object Inicio : Ruta("emp_inicio")
    data object Catalogo : Ruta("emp_catalogo")
    data object Mensajes : Ruta("emp_mensajes")
    data object Perfil : Ruta("emp_perfil")

    // --- Rutas secundarias de Emprendedora (sin barra inferior) ---
    data object ProductoForm : Ruta("emp_producto_form?productoId={productoId}") {
        fun crearRuta(productoId: Int? = null): String {
            return if (productoId != null) "emp_producto_form?productoId=$productoId" else "emp_producto_form"
        }
    }
    data object ProductoDetalle : Ruta("emp_producto_detalle/{productoId}") {
        fun crearRuta(productoId: Int): String = "emp_producto_detalle/$productoId"
    }
    data object VistaPreviaPublica : Ruta("emp_vista_previa")
    data object Conversacion : Ruta("emp_conversacion/{conversacionId}") {
        fun crearRuta(conversacionId: Int): String = "emp_conversacion/$conversacionId"
    }
    data object EditarPerfil : Ruta("emp_editar_perfil")
    data object Sincronizacion : Ruta("emp_sincronizacion")
    data object BajaCuenta : Ruta("emp_baja_cuenta")
    data object BajaExitosa : Ruta("emp_baja_exitosa")

    // --- Grafo de la Administradora (Pestañas principales con barra inferior) ---
    data object PanelAdmin : Ruta("admin_panel")
    data object EmprendimientosAdmin : Ruta("admin_emprendimientos")
    data object AutomatizacionesAdmin : Ruta("admin_automatizaciones")
    data object CuentaAdmin : Ruta("admin_cuenta")

    // --- Rutas secundarias de Administradora ---
    data object DetalleEmprendimientoAdmin : Ruta("admin_detalle_emp/{emprendimientoId}") {
        fun crearRuta(id: Int): String = "admin_detalle_emp/$id"
    }
    data object FormEmprendimientoAdmin : Ruta("admin_form_emp?emprendimientoId={emprendimientoId}") {
        fun crearRuta(id: Int? = null): String {
            return if (id != null) "admin_form_emp?emprendimientoId=$id" else "admin_form_emp"
        }
    }
}
