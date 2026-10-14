package com.example.textilcircular

import com.example.textilcircular.data.repository.MensajeRepositoryMemoria
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
    fun bandejas_filtranConversacionesCorrespondientesACadaRol() = runTest {
        val repo = MensajeRepositoryMemoria()

        val conversacionesEmprendedora1 = repo.obtenerConversaciones(1).first()
        assertTrue(conversacionesEmprendedora1.all { it.emprendedoraId == 1 })

        val conversacionesCliente1 = repo.obtenerConversacionesCliente(1).first()
        assertTrue(conversacionesCliente1.all { it.clienteId == 1 })
    }
}
