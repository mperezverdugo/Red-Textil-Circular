package com.example.textilcircular

import com.example.textilcircular.data.local.converter.ConversoresRoom
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.data.local.entity.ConversacionEntity
import com.example.textilcircular.data.local.entity.CorreoAutomatizacionEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MensajeEntity
import com.example.textilcircular.data.local.entity.MetricaEventoEntity
import com.example.textilcircular.data.local.entity.ProductoEntity
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import com.example.textilcircular.data.local.entity.SincronizacionEntity
import com.example.textilcircular.model.Cliente
import com.example.textilcircular.model.Conversacion
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.Mensaje
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.Rol
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.model.TipoEmisor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas unitarias para validar los conversores y la transformación de entidades de Room.
class RoomPersistenciaTest {

    private val conversores = ConversoresRoom()

    @Test
    fun conversores_transformanListasDeRubrosSinPerderDatos() {
        val rubrosOriginales = listOf(Rubro.SUPRARRECICLAJE, Rubro.REPARACION, Rubro.TALLERES)
        val textoGuardado = conversores.deListaRubros(rubrosOriginales)

        assertTrue(textoGuardado.contains("SUPRARRECICLAJE"))
        assertTrue(textoGuardado.contains("REPARACION"))
        assertTrue(textoGuardado.contains("TALLERES"))

        val rubrosRecuperados = conversores.aListaRubros(textoGuardado)
        assertEquals(rubrosOriginales, rubrosRecuperados)
    }

    @Test
    fun conversores_manejanListasVaciasONulas() {
        assertEquals("", conversores.deListaRubros(emptyList()))
        assertEquals("", conversores.deListaRubros(null))
        assertTrue(conversores.aListaRubros("").isEmpty())
        assertTrue(conversores.aListaRubros(null).isEmpty())

        assertEquals("", conversores.deListaTexto(emptyList()))
        assertTrue(conversores.aListaTexto("").isEmpty())
    }

    @Test
    fun conversores_transformanRutasDeFotosCorrectamente() {
        val rutasFotos = listOf(
            "/data/user/0/com.example.textilcircular/files/fotos/foto_1.jpg",
            "/data/user/0/com.example.textilcircular/files/fotos/foto_2.jpg"
        )
        val texto = conversores.deListaTexto(rutasFotos)
        val recuperadas = conversores.aListaTexto(texto)

        assertEquals(2, recuperadas.size)
        assertEquals(rutasFotos, recuperadas)
    }

    @Test
    fun conversores_transformanEnumsDeEstado() {
        assertEquals("CLIENTE", conversores.deTipoEmisor(TipoEmisor.CLIENTE))
        assertEquals(TipoEmisor.CLIENTE, conversores.aTipoEmisor("CLIENTE"))

        assertEquals("EMPRENDEDORA", conversores.deRol(Rol.EMPRENDEDORA))
        assertEquals(Rol.EMPRENDEDORA, conversores.aRol("EMPRENDEDORA"))

        assertEquals("PENDIENTE", conversores.deEstadoSincronizacion(EstadoSincronizacion.PENDIENTE))
        assertEquals(EstadoSincronizacion.PENDIENTE, conversores.aEstadoSincronizacion("PENDIENTE"))

        assertEquals("ENVIADO", conversores.deEstadoCorreo(EstadoCorreo.ENVIADO))
        assertEquals(EstadoCorreo.ENVIADO, conversores.aEstadoCorreo("ENVIADO"))

        assertEquals("POR_REVISAR", conversores.deEstadoFicha(EstadoFichaEmprendimiento.POR_REVISAR))
        assertEquals(EstadoFichaEmprendimiento.POR_REVISAR, conversores.aEstadoFicha("POR_REVISAR"))
    }

    @Test
    fun emprendedoraEntity_mapeaCorrectamenteHaciaModelo() {
        val modeloOriginal = Emprendedora(
            id = 5,
            rut = "18.111.222-3",
            nombre = "Artesana Prueba",
            nombreComercial = "Taller Creativo",
            region = "Valparaíso",
            comuna = "Quilpué",
            rubros = listOf(Rubro.SUPRARRECICLAJE),
            email = "artesana@correo.cl",
            instagram = "tallercreativo",
            activa = true
        )

        val entidad = EmprendedoraEntity.desdeModelo(
            emprendedora = modeloOriginal,
            estadoFicha = EstadoFichaEmprendimiento.ACTIVA,
            problemas = listOf("Falta foto de portada")
        )

        val modeloRecuperado = entidad.aModelo()
        assertEquals(modeloOriginal, modeloRecuperado)

        val fichaAdmin = entidad.aFichaAdmin()
        assertEquals(EstadoFichaEmprendimiento.ACTIVA, fichaAdmin.estadoFicha)
        assertEquals(1, fichaAdmin.problemasDetectados.size)
    }

    @Test
    fun clienteEntity_guardaCredencialesSeguras() {
        val cliente = Cliente(
            id = 10,
            nombre = "Cliente Test",
            email = "cliente@correo.cl",
            passwordHash = "hash123",
            sal = "sal456",
            aceptaLeyDatos = true
        )

        val entidad = ClienteEntity.desdeModelo(cliente)
        assertEquals("hash123", entidad.passwordHash)
        assertEquals("sal456", entidad.sal)

        val recuperado = entidad.aModelo()
        assertEquals(cliente, recuperado)
    }

    @Test
    fun productoEntity_conservaRutasDeFotosYDetalles() {
        val producto = Producto(
            id = 101,
            emprendedoraId = 1,
            nombre = "Bolso Reciclado",
            descripcionCorta = "Bolso resistente hecho con jeans",
            explicacionProceso = "Lavado sustentable y costura reforzada",
            fotos = listOf("/ruta/foto1.jpg", "/ruta/foto2.jpg"),
            pendienteSincronizar = false
        )

        val entidad = ProductoEntity.desdeModelo(producto)
        val recuperado = entidad.aModelo()

        assertEquals(producto.id, recuperado.id)
        assertEquals(producto.emprendedoraId, recuperado.emprendedoraId)
        assertEquals(producto.nombre, recuperado.nombre)
        assertEquals(producto.fotos, recuperado.fotos)
        assertEquals(producto.explicacionProceso, recuperado.explicacionProceso)
    }

    @Test
    fun conversacionEntity_mantieneRelacionConProducto() {
        val prodAsociado = Producto(
            id = 20,
            nombre = "Cojín Decorativo",
            descripcionCorta = "Cojín de retazos"
        )
        val conversacion = Conversacion(
            id = 301,
            emprendedoraId = 2,
            clienteId = 1,
            nombreCliente = "Francisca Tapia",
            nombreEmprendimiento = "Renace Ropa Vintage",
            ultimoMensaje = "¿Está disponible?",
            fechaHora = "15:30",
            noLeidosEmprendedora = 1,
            noLeidosCliente = 0,
            productoAsociado = prodAsociado
        )

        val entidad = ConversacionEntity.desdeModelo(conversacion)
        assertEquals(20, entidad.productoId)

        val recuperada = entidad.aModelo(prodAsociado)
        assertEquals(conversacion, recuperada)
        assertNotNull(recuperada.productoAsociado)
    }

    @Test
    fun mensajeEntity_registraEmisorYProductoReferenciado() {
        val mensaje = Mensaje(
            id = 501,
            conversacionId = 301,
            emisor = TipoEmisor.CLIENTE,
            contenido = "Me interesa este cojín",
            fechaHora = "15:31",
            esLeido = true,
            productoId = 20,
            nombreProducto = "Cojín Decorativo"
        )

        val entidad = MensajeEntity.desdeModelo(mensaje)
        val recuperado = entidad.aModelo()

        assertEquals(mensaje, recuperado)
        assertEquals(TipoEmisor.CLIENTE, recuperado.emisor)
        assertEquals(20, recuperado.productoId)
    }

    @Test
    fun sincronizacionYCorreoEntity_transformanSinErrores() {
        val itemSync = SincronizacionEntity(
            id = 1,
            tipoElemento = "Producto",
            detalle = "Alta de bolso upcycling",
            estado = EstadoSincronizacion.PENDIENTE
        )
        val modelSync = itemSync.aModelo()
        assertEquals("Producto", modelSync.tipoElemento)
        assertEquals(EstadoSincronizacion.PENDIENTE, modelSync.estado)

        val correo = CorreoAutomatizacionEntity(
            id = 2,
            destinatario = "artesana@correo.cl",
            emprendimiento = "Taller Hilos",
            fechaEnvio = "Hoy",
            estado = EstadoCorreo.ENVIADO
        )
        val modelCorreo = correo.aModelo()
        assertEquals(EstadoCorreo.ENVIADO, modelCorreo.estado)
    }

    @Test
    fun sesionActivaEntity_almacenaRegistroUnico() {
        val sesion = SesionActivaEntity(
            id = 1,
            rol = Rol.EMPRENDEDORA,
            usuarioId = 5,
            correo = "artesana@correo.cl"
        )
        assertEquals(1, sesion.id)
        assertEquals(Rol.EMPRENDEDORA, sesion.rol)
        assertEquals(5, sesion.usuarioId)
    }

    @Test
    fun metricaEventoEntity_registraEventosCorrectamente() {
        val evento = MetricaEventoEntity(
            id = 1,
            tipoEvento = "CLIC_CONTACTO",
            termino = null,
            region = "Valparaíso",
            rubro = "Suprarreciclaje",
            fechaRegistro = 1000L
        )
        assertEquals("CLIC_CONTACTO", evento.tipoEvento)
        assertEquals("Valparaíso", evento.region)
        assertEquals("Suprarreciclaje", evento.rubro)
        assertNull(evento.termino)
    }
}
