package com.example.textilcircular.model

// Modelos de datos para productos, mensajes, sincronización y administración.

data class Producto(
    val id: Int = 0,
    val emprendedoraId: Int = 0,
    val nombre: String,
    val descripcionCorta: String,
    val explicacionProceso: String? = null,
    val fotos: List<String> = emptyList(),
    val pendienteSincronizar: Boolean = false
)

enum class TipoEmisor {
    CLIENTE,
    EMPRENDEDORA
}

data class Mensaje(
    val id: Int = 0,
    val conversacionId: Int = 0,
    val emisor: TipoEmisor,
    val contenido: String,
    val fechaHora: String,
    val esLeido: Boolean = true,
    val productoId: Int? = null,
    val nombreProducto: String? = null
)

data class Conversacion(
    val id: Int = 0,
    val emprendedoraId: Int = 0,
    val clienteId: Int = 0,
    val nombreCliente: String,
    val nombreEmprendimiento: String = "",
    val ultimoMensaje: String,
    val fechaHora: String,
    val noLeidosEmprendedora: Int = 0,
    val noLeidosCliente: Int = 0,
    val productoAsociado: Producto? = null
) {
    // Mantiene compatibilidad con vistas de la emprendedora que leen mensajesNoLeidos.
    val mensajesNoLeidos: Int
        get() = noLeidosEmprendedora
}

enum class EstadoSincronizacion {
    PENDIENTE,
    SINCRONIZANDO,
    SINCRONIZADO,
    ERROR
}

data class ItemSincronizacion(
    val id: Int = 0,
    val tipoElemento: String,
    val detalle: String,
    val estado: EstadoSincronizacion = EstadoSincronizacion.PENDIENTE
)

enum class EstadoCorreo {
    ENVIADO,
    PENDIENTE,
    FALLIDO
}

data class RegistroAutomatizacionCorreo(
    val id: Int = 0,
    val destinatario: String,
    val emprendimiento: String,
    val fechaEnvio: String,
    val estado: EstadoCorreo
)

enum class EstadoFichaEmprendimiento {
    ACTIVA,
    OCULTA,
    POR_REVISAR,
    DADA_DE_BAJA
}

data class FichaAdminEmprendimiento(
    val emprendedora: Emprendedora,
    val estadoFicha: EstadoFichaEmprendimiento,
    val problemasDetectados: List<String> = emptyList()
)
