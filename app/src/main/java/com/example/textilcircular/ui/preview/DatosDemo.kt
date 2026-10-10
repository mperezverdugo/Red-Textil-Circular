package com.example.textilcircular.ui.preview

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

/**
 * Datos ficticios centralizados para demostración y previsualización (RNF-11).
 * Cumple con la Ley N.º 21.719 y la Ley N.º 19.628 de protección de datos personales.
 */
object DatosDemo {

    /**
     * Mapa con las 16 regiones oficiales de Chile y comunas de ejemplo.
     * TODO(Capa 3): La lista completa de las 346 comunas vendrá del dataset oficial de la Fundación.
     */
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

    /**
     * Emprendedora demo para la sesión activa de la aplicación móvil.
     */
    val EmprendedoraDemo = Emprendedora(
        id = 1,
        rut = "15.420.312-8",
        nombre = "Camila Andrea Morales Soto",
        nombreComercial = "Taller Hilos del Sur",
        region = "Valparaíso",
        comuna = "Valparaíso",
        rubros = listOf(Rubro.SUPRARRECICLAJE, Rubro.REPARACION),
        email = "camila.morales@textilcircular.cl",
        instagram = "tallerhilosdelsur",
        activa = true
    )

    /**
     * Lista de productos demo para el catálogo de la emprendedora (RF-13, RF-14, RF-18).
     */
    val ProductosDemo: List<Producto> = listOf(
        Producto(
            id = 101,
            emprendedoraId = 1,
            nombre = "Bolso Upcycling Denim",
            descripcionCorta = "Bolso resistente confeccionado a partir de 3 pantalones de jeans recuperados.",
            explicacionProceso = "Desarmamos jeans en desuso donados en la región, lavamos con bajo consumo de agua y unimos las piezas con doble costura reforzada.",
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
            descripcionCorta = "Chaqueta vintage de segunda mano con bordados a mano inspirados en la flora chilena.",
            explicacionProceso = "Restauración de botones originales y bordado con hilo de lana natural.",
            fotos = listOf("foto_1", "foto_2", "foto_3"),
            pendienteSincronizar = false
        )
    )

    /**
     * Conversaciones demo para la bandeja de mensajes (RF-19, RF-21, RF-22).
     */
    val ConversacionesDemo: List<Conversacion> = listOf(
        Conversacion(
            id = 201,
            emprendedoraId = 1,
            nombreCliente = "Francisca Tapia",
            ultimoMensaje = "¡Hola! ¿Hacen envíos a Viña del Mar o entregas presenciales?",
            fechaHora = "11:45",
            mensajesNoLeidos = 1,
            productoAsociado = ProductosDemo[0]
        ),
        Conversacion(
            id = 202,
            emprendedoraId = 1,
            nombreCliente = "Rodrigo San Martín",
            ultimoMensaje = "Muchas gracias Camila, me encantó el bolso.",
            fechaHora = "Ayer",
            mensajesNoLeidos = 0,
            productoAsociado = ProductosDemo[1]
        )
    )

    /**
     * Historial de mensajes de la conversación 201 (RF-20, RF-21, RF-22).
     */
    val MensajesConversacionDemo: List<Mensaje> = listOf(
        Mensaje(
            id = 1,
            conversacionId = 201,
            emisor = TipoEmisor.CLIENTE,
            contenido = "Quiero saber más sobre este producto: Bolso Upcycling Denim",
            fechaHora = "11:40"
        ),
        Mensaje(
            id = 2,
            conversacionId = 201,
            emisor = TipoEmisor.EMPRENDEDORA,
            contenido = "¡Hola Francisca! Claro que sí, es de mezclilla gruesa reforzada y tiene bolsillo interior.",
            fechaHora = "11:42"
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

    /**
     * Elementos locales pendientes de sincronización (RF-35).
     */
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

    /**
     * Lista de emprendimientos para el módulo de administración (RF-27, RF-29).
     */
    val EmprendimientosAdminDemo: List<FichaAdminEmprendimiento> = listOf(
        FichaAdminEmprendimiento(
            emprendedora = EmprendedoraDemo,
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemasDetectados = emptyList()
        ),
        FichaAdminEmprendimiento(
            emprendedora = Emprendedora(
                id = 2,
                rut = "17.892.441-2",
                nombre = "María Elena Sepúlveda",
                nombreComercial = "Renace Ropa Vintage",
                region = "Metropolitana de Santiago",
                comuna = "Providencia",
                rubros = listOf(Rubro.SEGUNDA_MANO),
                email = "maria.renace@correo.cl",
                instagram = "instagram.com/renace_vintage??",
                activa = true
            ),
            estadoFicha = EstadoFichaEmprendimiento.POR_REVISAR,
            problemasDetectados = listOf(
                "Enlace de Instagram contiene caracteres no válidos (RF-05).",
                "Falta agregar un segundo rubro o confirmar actividad."
            )
        ),
        FichaAdminEmprendimiento(
            emprendedora = Emprendedora(
                id = 3,
                rut = "14.331.092-K",
                nombre = "Rosa Inés Gómez",
                nombreComercial = "Costuras y Arreglos Rosa",
                region = "Biobío",
                comuna = "Concepción",
                rubros = listOf(Rubro.REPARACION, Rubro.TALLERES),
                email = "rosa.gomez@correo.cl",
                instagram = null,
                activa = false
            ),
            estadoFicha = EstadoFichaEmprendimiento.OCULTA,
            problemasDetectados = listOf(
                "Perfil pausado temporalmente por la emprendedora."
            )
        ),
        FichaAdminEmprendimiento(
            emprendedora = Emprendedora(
                id = 4,
                rut = "16.110.874-5",
                nombre = "Beatriz Fuentes",
                nombreComercial = "EcoTejidos Chiloé",
                region = "Los Lagos",
                comuna = "Castro",
                rubros = listOf(Rubro.SUPRARRECICLAJE),
                email = "beatriz@chiloe.cl",
                instagram = "ecotejidos_chiloe",
                activa = false
            ),
            estadoFicha = EstadoFichaEmprendimiento.DADA_DE_BAJA,
            problemasDetectados = listOf(
                "Emprendedora solicitó baja voluntaria del directorio (RF-11, RF-12)."
            )
        )
    )

    /**
     * Auditoría de correos automáticos para el módulo de administración (RF-30).
     */
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

    /**
     * Métricas agregadas para el panel de administración (RF-31, RF-32).
     */
    val TotalBusquedasMes: Int = 1420
    val TotalConsultasEnviadas: Int = 385
    val TotalClicsContacto: Int = 890
    val FichasPorRevisarContador: Int = 7

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
