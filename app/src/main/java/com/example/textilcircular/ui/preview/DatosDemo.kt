package com.example.textilcircular.ui.preview

import com.example.textilcircular.model.Cliente
import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.FichaAdminEmprendimiento
import com.example.textilcircular.model.ItemSincronizacion
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.RegistroAutomatizacionCorreo
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.model.TipoEmisor

// Datos de prueba para previsualizar pantallas y simular la aplicación con datos en memoria.
object DatosDemo {

    // Regiones y comunas para los selectores desplegables y filtros de búsqueda.
    // TODO: en SQLite cargar la lista completa de comunas.
    val RegionesConComunas: Map<String, List<String>> = mapOf(
        "Arica y Parinacota" to listOf("Arica", "Camarones", "Putre", "General Lagos"),
        "Tarapacá" to listOf("Iquique", "Alto Hospicio", "Pozo Almonte", "Pica", "Huara"),
        "Antofagasta" to listOf("Antofagasta", "Mejillones", "Calama", "Tocopilla", "San Pedro de Atacama"),
        "Atacama" to listOf("Copiapó", "Caldera", "Vallenar", "Huasco", "Chañaral"),
        "Coquimbo" to listOf("La Serena", "Coquimbo", "Ovalle", "Illapel", "Vicuña"),
        "Valparaíso" to listOf("Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana", "Quillota", "San Antonio"),
        "Metropolitana de Santiago" to listOf("Santiago", "Providencia", "Ñuñoa", "Maipú", "La Florida", "Puente Alto"),
        "Libertador General Bernardo O'Higgins" to listOf("Rancagua", "Machalí", "San Fernando", "Rengo", "Pichilemu"),
        "Maule" to listOf("Talca", "Curicó", "Linares", "Constitución", "Cauquenes"),
        "Ñuble" to listOf("Chillán", "San Carlos", "Bulnes", "Quirihue", "Cobquecura"),
        "Biobío" to listOf("Concepción", "Talcahuano", "Los Ángeles", "Coronel", "San Pedro de la Paz"),
        "La Araucanía" to listOf("Temuco", "Padre Las Casas", "Villarrica", "Pucón", "Angol"),
        "Los Ríos" to listOf("Valdivia", "Corral", "La Unión", "Río Bueno", "Panguipulli"),
        "Los Lagos" to listOf("Puerto Montt", "Puerto Varas", "Castro", "Ancud", "Osorno"),
        "Aysén del General Carlos Ibáñez del Campo" to listOf("Coyhaique", "Aysén", "Chile Chico", "Cochrane"),
        "Magallanes y de la Antártica Chilena" to listOf("Punta Arenas", "Puerto Natales", "Porvenir", "Cabo de Hornos")
    )

    val ListaRegiones: List<String> = RegionesConComunas.keys.toList()

    // Clientes de ejemplo registrados en la plataforma.
    val ClientesDemo: List<Cliente> = listOf(
        Cliente(
            id = 1,
            nombre = "Francisca Tapia",
            email = "francisca.tapia@correo.cl",
            passwordHash = "a1be467f8434b5c97fe7337672a084fdb3b3aa1e5bf2c6e098699d8b3daf6599",
            sal = "sal_cliente_1",
            aceptaLeyDatos = true
        ),
        Cliente(
            id = 2,
            nombre = "Rodrigo San Martín",
            email = "rodrigo.sanmartin@correo.cl",
            passwordHash = "0fdaa6954edbb0afc6ef92ffbc706de8a58a337c4dec2f2b8c47194a2d58f46b",
            sal = "sal_cliente_2",
            aceptaLeyDatos = true
        )
    )

    val ClienteDemo: Cliente = ClientesDemo.first()

    // Emprendedora principal para la sesión activa de la app móvil.
    val EmprendedoraDemo = Emprendedora(
        id = 1,
        rut = "15.420.312-5",
        nombre = "Camila Andrea Morales Soto",
        nombreComercial = "Taller Hilos del Sur",
        region = "Valparaíso",
        comuna = "Valparaíso",
        rubros = listOf(Rubro.SUPRARRECICLAJE, Rubro.REPARACION),
        email = "camila.morales@textilcircular.cl",
        instagram = "tallerhilosdelsur",
        activa = true
    )

    // Lista de emprendedoras activas en distintas regiones del país.
    val EmprendedorasActivasDemo: List<Emprendedora> = listOf(
        EmprendedoraDemo,
        Emprendedora(
            id = 2,
            rut = "17.892.441-2",
            nombre = "María Elena Sepúlveda",
            nombreComercial = "Renace Ropa Vintage",
            region = "Metropolitana de Santiago",
            comuna = "Providencia",
            rubros = listOf(Rubro.SEGUNDA_MANO),
            email = "maria.renace@correo.cl",
            instagram = "renace_vintage",
            activa = true
        ),
        Emprendedora(
            id = 3,
            rut = "16.425.881-3",
            nombre = "Daniela Paz Rojas Castillo",
            nombreComercial = "Sol Textil Arica",
            region = "Arica y Parinacota",
            comuna = "Arica",
            rubros = listOf(Rubro.SUPRARRECICLAJE),
            email = "daniela.sol@correo.cl",
            instagram = "soltextilarica",
            activa = true
        ),
        Emprendedora(
            id = 4,
            rut = "18.302.119-4",
            nombre = "Claudia Jimena Silva Ortiz",
            nombreComercial = "Recrea Telas Antofagasta",
            region = "Antofagasta",
            comuna = "Antofagasta",
            rubros = listOf(Rubro.SUPRARRECICLAJE, Rubro.TALLERES),
            email = "claudia.recrea@correo.cl",
            instagram = "recreatelas",
            activa = true
        ),
        Emprendedora(
            id = 5,
            rut = "19.123.456-7",
            nombre = "Valentina Alejandra Herrera Díaz",
            nombreComercial = "Arriendo Consciente Coquimbo",
            region = "Coquimbo",
            comuna = "La Serena",
            rubros = listOf(Rubro.ARRIENDO, Rubro.SEGUNDA_MANO),
            email = "valentina.coquimbo@correo.cl",
            instagram = "arriendoconsciente",
            activa = true
        ),
        Emprendedora(
            id = 6,
            rut = "14.567.890-1",
            nombre = "Loreto Paz Carrasco Morales",
            nombreComercial = "Puntada Limpia Maule",
            region = "Maule",
            comuna = "Talca",
            rubros = listOf(Rubro.REPARACION),
            email = "loreto.maule@correo.cl",
            instagram = "puntadalimpia",
            activa = true
        ),
        Emprendedora(
            id = 7,
            rut = "13.234.567-8",
            nombre = "Carmen Gloria Benítez Soto",
            nombreComercial = "Sur Circular Araucanía",
            region = "La Araucanía",
            comuna = "Temuco",
            rubros = listOf(Rubro.SUPRARRECICLAJE, Rubro.SEGUNDA_MANO),
            email = "carmen.araucania@correo.cl",
            instagram = "surcircular",
            activa = true
        ),
        Emprendedora(
            id = 8,
            rut = "12.345.678-9",
            nombre = "Lucía Ignacia Navarro Pérez",
            nombreComercial = "Lanas Magallánicas",
            region = "Magallanes y de la Antártica Chilena",
            comuna = "Punta Arenas",
            rubros = listOf(Rubro.TALLERES, Rubro.REPARACION),
            email = "lucia.patagonia@correo.cl",
            instagram = "lanasmagallanicas",
            activa = true
        )
    )

    // Emprendedora oculta por decisión administrativa (no debe figurar en el mercado).
    val EmprendedoraOcultaDemo = Emprendedora(
        id = 9,
        rut = "14.331.092-K",
        nombre = "Rosa Inés Gómez",
        nombreComercial = "Costuras y Arreglos Rosa",
        region = "Biobío",
        comuna = "Concepción",
        rubros = listOf(Rubro.REPARACION, Rubro.TALLERES),
        email = "rosa.gomez@correo.cl",
        instagram = null,
        activa = false
    )

    // Emprendedora que solicitó baja voluntaria (tampoco debe figurar en el mercado).
    val EmprendedoraBajaDemo = Emprendedora(
        id = 10,
        rut = "16.110.874-5",
        nombre = "Beatriz Fuentes",
        nombreComercial = "EcoTejidos Chiloé",
        region = "Los Lagos",
        comuna = "Castro",
        rubros = listOf(Rubro.SUPRARRECICLAJE),
        email = "beatriz@chiloe.cl",
        instagram = "ecotejidos_chiloe",
        activa = false
    )

    // Catálogo con creaciones textiles para las emprendedoras.
    val ProductosDemo: List<Producto> = listOf(
        Producto(
            id = 101,
            emprendedoraId = 1,
            nombre = "Bolso Upcycling Denim",
            descripcionCorta = "Bolso resistente confeccionado a partir de 3 pantalones de jeans recuperados.",
            explicacionProceso = "Desarmamos jeans en desuso, lavamos con bajo consumo y reforzamos uniones.",
            fotos = listOf("foto_1", "foto_2"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 102,
            emprendedoraId = 1,
            nombre = "Cojín Textil Parche",
            descripcionCorta = "Cojín decorativo relleno con retazos de algodón y lino recuperado.",
            explicacionProceso = "Aprovechamiento integral de descartes de sastrerías locales.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = true
        ),
        Producto(
            id = 103,
            emprendedoraId = 1,
            nombre = "Chaqueta Intervenida Bordada",
            descripcionCorta = "Chaqueta vintage de segunda mano con bordados a mano inspirados en flora local.",
            explicacionProceso = "Restauración de botones y bordado con hilo de lana natural.",
            fotos = listOf("foto_1", "foto_2", "foto_3"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 104,
            emprendedoraId = 2,
            nombre = "Vestido Vintage Años 90",
            descripcionCorta = "Vestido estampado floral en excelente estado de conservación.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 105,
            emprendedoraId = 2,
            nombre = "Camisa de Seda Rescatada",
            descripcionCorta = "Camisa clásica de seda natural con botones originales recuperados.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 106,
            emprendedoraId = 3,
            nombre = "Mochila Retazos del Desierto",
            descripcionCorta = "Mochila urbana fabricada con telas recuperadas y correas de seguridad reutilizadas.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 107,
            emprendedoraId = 3,
            nombre = "Estuche Mezclilla Arica",
            descripcionCorta = "Estuche multiuso reforzado con cierre metálico reciclado.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 108,
            emprendedoraId = 4,
            nombre = "Funda Cojín Minero",
            descripcionCorta = "Funda tejida con excedentes textiles industriales de la zona norte.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 109,
            emprendedoraId = 4,
            nombre = "Taller Costura Inicial",
            descripcionCorta = "Sesión práctica de reparación básica de prendas de vestir.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 110,
            emprendedoraId = 5,
            nombre = "Vestido Gala para Arriendo",
            descripcionCorta = "Prenda de fiesta disponible para arriendo por fin de semana.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 111,
            emprendedoraId = 5,
            nombre = "Tapado Fiesta Reutilizado",
            descripcionCorta = "Tapado elegante de segunda mano higienizado y listo para usar.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 112,
            emprendedoraId = 6,
            nombre = "Arreglo y Ajuste de Pantalones",
            descripcionCorta = "Servicio de basta, entalle y cambio de cierres para prolongar vida útil.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 113,
            emprendedoraId = 7,
            nombre = "Manta de Parches Temuco",
            descripcionCorta = "Manta abrigadora armada con retazos de polar y lana recuperada.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 114,
            emprendedoraId = 7,
            nombre = "Chaleco Lana Segunda Mano",
            descripcionCorta = "Chaleco artesanal tejido en lana chilota restaurado a mano.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 115,
            emprendedoraId = 8,
            nombre = "Gorro Lana Merino Magallanes",
            descripcionCorta = "Gorro térmico confeccionado con sobrantes de hilados patagónicos.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 116,
            emprendedoraId = 8,
            nombre = "Taller Hilado Circular",
            descripcionCorta = "Clase grupal para transformar retazos delgados en hilo utilizable.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 117,
            emprendedoraId = 9,
            nombre = "Costura Básica Rápida",
            descripcionCorta = "Servicio de reparación rápida en Concepción.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        ),
        Producto(
            id = 118,
            emprendedoraId = 10,
            nombre = "Cojín Chiloé Eco",
            descripcionCorta = "Cojín decorativo con motivos del sur de Chile.",
            fotos = listOf("foto_1"),
            pendienteSincronizar = false
        )
    )

    // Conversaciones activas entre clientes y emprendedoras.
    val ConversacionesDemo: List<Conversacion> = listOf(
        Conversacion(
            id = 201,
            emprendedoraId = 1,
            clienteId = 1,
            nombreCliente = "Francisca Tapia",
            nombreEmprendimiento = "Taller Hilos del Sur",
            ultimoMensaje = "¡Hola! ¿Hacen envíos a Viña del Mar o entregas presenciales?",
            fechaHora = "11:45",
            noLeidosEmprendedora = 1,
            noLeidosCliente = 0,
            productoAsociado = ProductosDemo[0]
        ),
        Conversacion(
            id = 202,
            emprendedoraId = 1,
            clienteId = 2,
            nombreCliente = "Rodrigo San Martín",
            nombreEmprendimiento = "Taller Hilos del Sur",
            ultimoMensaje = "Muchas gracias Camila, me encantó el bolso.",
            fechaHora = "Ayer",
            noLeidosEmprendedora = 0,
            noLeidosCliente = 0,
            productoAsociado = ProductosDemo[1]
        )
    )

    // Mensajes de la conversación 201.
    val MensajesConversacionDemo: List<Mensaje> = listOf(
        Mensaje(
            id = 1,
            conversacionId = 201,
            emisor = TipoEmisor.CLIENTE,
            contenido = "Quiero saber más sobre este producto: Bolso Upcycling Denim",
            fechaHora = "11:40",
            esLeido = true,
            productoId = 101,
            nombreProducto = "Bolso Upcycling Denim"
        ),
        Mensaje(
            id = 2,
            conversacionId = 201,
            emisor = TipoEmisor.EMPRENDEDORA,
            contenido = "¡Hola Francisca! Claro que sí, es de mezclilla gruesa reforzada y tiene bolsillo interior.",
            fechaHora = "11:42",
            esLeido = true
        ),
        Mensaje(
            id = 3,
            conversacionId = 201,
            emisor = TipoEmisor.CLIENTE,
            contenido = "¡Hola! ¿Hacen envíos a Viña del Mar o entregas presenciales?",
            fechaHora = "11:45",
            esLeido = false
        )
    )

    // Elementos pendientes de sincronización.
    val ElementosPendientesSyncDemo: List<ItemSincronizacion> = listOf(
        ItemSincronizacion(
            id = 1,
            tipoElemento = "Producto nuevo",
            detalle = "Cojín Textil Parche (con 1 fotografía)",
            estado = EstadoSincronizacion.PENDIENTE
        ),
        ItemSincronizacion(
            id = 2,
            tipoElemento = "Actualización de contacto",
            detalle = "Cambio de usuario de Instagram a @tallerhilosdelsur",
            estado = EstadoSincronizacion.PENDIENTE
        )
    )

    // Fichas visibles y moderadas para el panel de administración.
    val EmprendimientosAdminDemo: List<FichaAdminEmprendimiento> = listOf(
        FichaAdminEmprendimiento(
            emprendedora = EmprendedorasActivasDemo[0],
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedorasActivasDemo[1],
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedorasActivasDemo[2],
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedorasActivasDemo[3],
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedorasActivasDemo[4],
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedorasActivasDemo[5],
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedorasActivasDemo[6],
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedorasActivasDemo[7],
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedoraOcultaDemo,
            estadoFicha = EstadoFichaEmprendimiento.OCULTA,
            problemasDetectados = listOf(
                "Ficha ocultada manualmente por la administradora."
            )
        ),
        FichaAdminEmprendimiento(
            emprendedora = EmprendedoraBajaDemo,
            estadoFicha = EstadoFichaEmprendimiento.DADA_DE_BAJA,
            problemasDetectados = listOf(
                "Emprendedora solicitó baja voluntaria del directorio."
            )
        )
    )

    // Registro de correos automáticos emitidos por la plataforma.
    val AutomatizacionesCorreosDemo: List<RegistroAutomatizacionCorreo> = listOf(
        RegistroAutomatizacionCorreo(
            id = 1,
            destinatario = "camila.morales@textilcircular.cl",
            emprendimiento = "Taller Hilos del Sur",
            fechaEnvio = "Hoy, 09:15",
            estado = EstadoCorreo.ENVIADO
        ),
        RegistroAutomatizacionCorreo(
            id = 2,
            destinatario = "maria.renace@correo.cl",
            emprendimiento = "Renace Ropa Vintage",
            fechaEnvio = "Hoy, 10:20",
            estado = EstadoCorreo.FALLIDO
        ),
        RegistroAutomatizacionCorreo(
            id = 3,
            destinatario = "lucia.patagonia@correo.cl",
            emprendimiento = "Lanas Magallánicas",
            fechaEnvio = "Ayer, 18:00",
            estado = EstadoCorreo.ENVIADO
        ),
        RegistroAutomatizacionCorreo(
            id = 4,
            destinatario = "daniela.arica@correo.cl",
            emprendimiento = "Sol Textil Arica",
            fechaEnvio = "Hace 2 días",
            estado = EstadoCorreo.PENDIENTE
        )
    )

    // Métricas para el panel de administración.
    val TotalBusquedasMes: Int = 1420
    val TotalConsultasEnviadas: Int = 385
    val TotalClicsContacto: Int = 890
    val FichasPorRevisarContador: Int = 0

    val ContactosPorRegionDemo: List<Pair<String, Int>> = listOf(
        "Valparaíso" to 142,
        "Metropolitana" to 125,
        "Biobío" to 58,
        "Los Lagos" to 34,
        "Coquimbo" to 26
    )

    val ContactosPorRubroDemo: List<Pair<String, Int>> = listOf(
        "Suprarreciclaje" to 164,
        "Reparación" to 110,
        "Segunda mano" to 68,
        "Talleres" to 28,
        "Arriendo" to 15
    )
}
