package com.example.textilcircular

import com.example.textilcircular.data.repository.AdminRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.MensajeRepositoryMemoria
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas unitarias para la mensajería interna, no leídos por rol y métricas de contacto.
class ConversacionesTest {

    @Test
    fun conversacion_mantieneUnaSolaPorParejaClienteEmprendedora() = runTest {
        val repo = MensajeRepositoryMemoria()
        val clienteId = 1
        val emprendedoraId = 1

        // Iniciar conversación por primera vez con esa pareja
        val res1 = repo.iniciarOObtenerConversacion(
            clienteId = clienteId,
            emprendedoraId = emprendedoraId,
            nombreCliente = "Francisca Tapia",
            nombreEmprendimiento = "Taller Hilos del Sur",
            primerMensaje = "Hola, me interesa tu trabajo",
            emisor = TipoEmisor.CLIENTE
        )
        assertTrue(res1 is Resultado.Exito)
        val convId1 = (res1 as Resultado.Exito).datos.id

        // Volver a iniciar conversación entre la misma pareja
        val res2 = repo.iniciarOObtenerConversacion(
            clienteId = clienteId,
            emprendedoraId = emprendedoraId,
            nombreCliente = "Francisca Tapia",
            nombreEmprendimiento = "Taller Hilos del Sur",
            primerMensaje = "Otra consulta",
            emisor = TipoEmisor.CLIENTE
        )
        assertTrue(res2 is Resultado.Exito)
        val convId2 = (res2 as Resultado.Exito).datos.id

        // Debe reutilizar el mismo id de conversación sin crear una duplicada
        assertEquals(convId1, convId2)
    }

    @Test
    fun mensajes_lleganACadaLado_clienteYEmprendedora() = runTest {
        val repo = MensajeRepositoryMemoria()
        val clienteId = 10
        val emprendedoraId = 20

        val inicio = repo.iniciarOObtenerConversacion(
            clienteId = clienteId,
            emprendedoraId = emprendedoraId,
            nombreCliente = "Ana López",
            nombreEmprendimiento = "Creaciones Renacer",
            primerMensaje = "",
            emisor = TipoEmisor.CLIENTE
        )
        val convId = (inicio as Resultado.Exito).datos.id

        // Mensaje enviado por el cliente
        val resCliente = repo.enviarMensaje(
            conversacionId = convId,
            contenido = "¿Hacen entregas en Santiago?",
            emisor = TipoEmisor.CLIENTE
        )
        assertTrue(resCliente is Resultado.Exito)

        // Respuesta enviada por la artesana
        val resEmp = repo.enviarMensaje(
            conversacionId = convId,
            contenido = "¡Hola Ana! Sí, entregamos en metro Los Leones.",
            emisor = TipoEmisor.EMPRENDEDORA
        )
        assertTrue(resEmp is Resultado.Exito)

        val listaMensajes = repo.obtenerMensajes(convId).first()
        assertEquals(2, listaMensajes.size)
        assertEquals(TipoEmisor.CLIENTE, listaMensajes[0].emisor)
        assertEquals("¿Hacen entregas en Santiago?", listaMensajes[0].contenido)
        assertEquals(TipoEmisor.EMPRENDEDORA, listaMensajes[1].emisor)
        assertEquals("¡Hola Ana! Sí, entregamos en metro Los Leones.", listaMensajes[1].contenido)
    }

    @Test
    fun conteoNoLeidos_separaContadoresPorLado() = runTest {
        val repo = MensajeRepositoryMemoria()
        val clienteId = 5
        val emprendedoraId = 8

        val inicio = repo.iniciarOObtenerConversacion(
            clienteId = clienteId,
            emprendedoraId = emprendedoraId,
            nombreCliente = "Cliente Nuevo",
            nombreEmprendimiento = "Lanas Magallánicas",
            primerMensaje = "Hola!",
            emisor = TipoEmisor.CLIENTE
        )
        val convId = (inicio as Resultado.Exito).datos.id

        // Como escribió el cliente, la emprendedora tiene 1 no leído y el cliente 0
        val conv1 = repo.obtenerConversacionPorId(convId).first()
        assertNotNull(conv1)
        assertEquals(1, conv1?.noLeidosEmprendedora)
        assertEquals(0, conv1?.noLeidosCliente)

        // El cliente envía otro mensaje
        repo.enviarMensaje(convId, "¿Tienes disponibilidad?", TipoEmisor.CLIENTE)
        val conv2 = repo.obtenerConversacionPorId(convId).first()
        assertEquals(2, conv2?.noLeidosEmprendedora)
        assertEquals(0, conv2?.noLeidosCliente)

        // La emprendedora lee los mensajes
        repo.marcarComoLeidoEmprendedora(convId)
        val convLeida = repo.obtenerConversacionPorId(convId).first()
        assertEquals(0, convLeida?.noLeidosEmprendedora)
        assertEquals(0, convLeida?.noLeidosCliente)

        // La emprendedora responde
        repo.enviarMensaje(convId, "¡Hola! Sí, tengo disponibilidad.", TipoEmisor.EMPRENDEDORA)
        val convRespondida = repo.obtenerConversacionPorId(convId).first()
        assertEquals(0, convRespondida?.noLeidosEmprendedora)
        assertEquals(1, convRespondida?.noLeidosCliente)

        // El cliente lee la respuesta
        repo.marcarComoLeidoCliente(convId)
        val convFinal = repo.obtenerConversacionPorId(convId).first()
        assertEquals(0, convFinal?.noLeidosEmprendedora)
        assertEquals(0, convFinal?.noLeidosCliente)
    }

    @Test
    fun marcarComoLeido_limpiaSoloNoLeidosPropios() = runTest {
        val repo = MensajeRepositoryMemoria()
        val convId = 201 // Conversación existente en datos demo

        // Simulamos que la artesana recibe un mensaje y el cliente también
        repo.enviarMensaje(convId, "Mensaje para artesana", TipoEmisor.CLIENTE)
        repo.enviarMensaje(convId, "Mensaje para cliente", TipoEmisor.EMPRENDEDORA)

        val convAntes = repo.obtenerConversacionPorId(convId).first()
        assertTrue((convAntes?.noLeidosEmprendedora ?: 0) > 0)
        assertTrue((convAntes?.noLeidosCliente ?: 0) > 0)

        // Si la artesana abre la conversación, solo sus no leídos bajan a 0
        repo.marcarComoLeidoEmprendedora(convId)
        val convTrasArtesana = repo.obtenerConversacionPorId(convId).first()
        assertEquals(0, convTrasArtesana?.noLeidosEmprendedora)
        assertTrue((convTrasArtesana?.noLeidosCliente ?: 0) > 0)

        // Si el cliente abre la conversación, sus no leídos bajan a 0
        repo.marcarComoLeidoCliente(convId)
        val convTrasCliente = repo.obtenerConversacionPorId(convId).first()
        assertEquals(0, convTrasCliente?.noLeidosEmprendedora)
        assertEquals(0, convTrasCliente?.noLeidosCliente)
    }

    @Test
    fun mensaje_puedeLlevarProductoReferenciado() = runTest {
        val repo = MensajeRepositoryMemoria()
        val convId = 201

        val res = repo.enviarMensaje(
            conversacionId = convId,
            contenido = "Quiero saber más sobre este producto: Bolso Upcycling Denim",
            emisor = TipoEmisor.CLIENTE,
            productoId = 101,
            nombreProducto = "Bolso Upcycling Denim"
        )

        assertTrue(res is Resultado.Exito)
        val msg = (res as Resultado.Exito).datos
        assertEquals(101, msg.productoId)
        assertEquals("Bolso Upcycling Denim", msg.nombreProducto)

        // Verificar que persiste en la lista de mensajes de la conversación
        val mensajes = repo.obtenerMensajes(convId).first()
        val guardado = mensajes.find { it.id == msg.id }
        assertNotNull(guardado)
        assertEquals(101, guardado?.productoId)
        assertEquals("Bolso Upcycling Denim", guardado?.nombreProducto)
    }

    @Test
    fun primerMensaje_incrementaMetricaConsultasAdmin() = runTest {
        val empRepo = EmprendedoraRepositoryMemoria()
        val adminRepo = AdminRepositoryMemoria(empRepo)

        val metricasAntes = adminRepo.obtenerMetricas().first()
        val totalConsultasAntes = metricasAntes.totalConsultas

        // Cuando el cliente envía su primer mensaje, se registra la consulta
        adminRepo.registrarConsultaEnviada(
            region = "Valparaíso",
            rubro = "Upcycling"
        )

        val metricasDespues = adminRepo.obtenerMetricas().first()
        assertEquals(totalConsultasAntes + 1, metricasDespues.totalConsultas)

        // La región y el rubro deben registrar el nuevo contacto
        val cuentaRegion = metricasDespues.contactosPorRegion.find { it.first == "Valparaíso" }?.second ?: 0
        assertTrue(cuentaRegion > 0)
        val cuentaRubro = metricasDespues.contactosPorRubro.find { it.first == "Upcycling" }?.second ?: 0
        assertTrue(cuentaRubro > 0)
    }

    @Test
    fun bandejas_filtranConversacionesCorrespondientesACadaRol() = runTest {
        val repo = MensajeRepositoryMemoria()

        val conversacionesEmprendedora1 = repo.obtenerConversaciones(1).first()
        assertTrue(conversacionesEmprendedora1.all { it.emprendedoraId == 1 })

        val conversacionesCliente1 = repo.obtenerConversacionesCliente(1).first()
        assertTrue(conversacionesCliente1.all { it.clienteId == 1 })
    }
}
