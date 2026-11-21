package com.example.textilcircular

import com.example.textilcircular.data.repository.AdminRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.MensajeRepositoryMemoria
import com.example.textilcircular.data.repository.ProductoRepositoryMemoria
import com.example.textilcircular.data.repository.SesionRepositoryMemoria
import com.example.textilcircular.data.repository.SincronizacionRepositoryMemoria
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoriosTest {

    @Test
    fun sesionRepository_gestionaInicioYCierreDeSesion() = runTest {
        val emprendedoraRepo = EmprendedoraRepositoryMemoria()
        val sesionRepo = SesionRepositoryMemoria(emprendedoraRepo)

        assertEquals(EstadoSesion.NoAutenticado, sesionRepo.estadoSesion.value)

        val resLogin = sesionRepo.iniciarSesionDemoEmprendedora()
        assertTrue(resLogin is Resultado.Exito)
        assertTrue(sesionRepo.estadoSesion.value is EstadoSesion.AutenticadoEmprendedora)

        sesionRepo.cerrarSesion()
        assertEquals(EstadoSesion.NoAutenticado, sesionRepo.estadoSesion.value)

        val resAdmin = sesionRepo.iniciarSesionDemoAdmin()
        assertTrue(resAdmin is Resultado.Exito)
        assertEquals(EstadoSesion.AutenticadoAdmin, sesionRepo.estadoSesion.value)
    }

    @Test
    fun productoRepository_guardaYFiltraCorrectamente() = runTest {
        val repo = ProductoRepositoryMemoria()
        val productosIniciales = repo.obtenerPorEmprendedora(1).first()
        val cantidadInicial = productosIniciales.size

        val nuevo = Producto(
            id = 0,
            emprendedoraId = 1,
            nombre = "Bolso Test",
            descripcionCorta = "Descripción para pruebas unitarias",
            fotos = listOf("foto.jpg")
        )

        val resultado = repo.guardar(nuevo)
        assertTrue(resultado is Resultado.Exito)

        val productosActualizados = repo.obtenerPorEmprendedora(1).first()
        assertEquals(cantidadInicial + 1, productosActualizados.size)

        val productoId = (resultado as Resultado.Exito).datos.id
        val eliminado = repo.eliminar(productoId)
        assertTrue(eliminado is Resultado.Exito)

        val productosFinales = repo.obtenerPorEmprendedora(1).first()
        assertEquals(cantidadInicial, productosFinales.size)
    }

    @Test
    fun mensajeRepository_enviaMensajeYActualizaConversacion() = runTest {
        val repo = MensajeRepositoryMemoria()
        val conversacionId = 201

        val mensajesAntes = repo.obtenerMensajes(conversacionId).first().size
        val res = repo.enviarMensaje(conversacionId, "Mensaje de prueba", TipoEmisor.EMPRENDEDORA)
        assertTrue(res is Resultado.Exito)

        val mensajesDespues = repo.obtenerMensajes(conversacionId).first()
        assertEquals(mensajesAntes + 1, mensajesDespues.size)
        assertEquals("Mensaje de prueba", mensajesDespues.last().contenido)

        val convActualizada = repo.obtenerConversacionPorId(conversacionId).first()
        assertNotNull(convActualizada)
        assertEquals("Mensaje de prueba", convActualizada?.ultimoMensaje)
    }

    @Test
    fun sincronizacionRepository_sincronizaTodo() = runTest {
        val repo = SincronizacionRepositoryMemoria()
        repo.registrarPendiente(tipo = "Producto nuevo", detalle = "Cojín Textil")
        val pendientesIniciales = repo.obtenerPendientes().first()
        assertTrue(pendientesIniciales.isNotEmpty())

        val res = repo.sincronizar()
        assertTrue(res is Resultado.Exito)
        assertEquals(EstadoSincronizacion.SINCRONIZADO, repo.estadoSincronizacion.value)
        assertTrue(repo.obtenerPendientes().first().isEmpty())
    }

    @Test
    fun adminRepository_actualizaFichasYCorreos() = runTest {
        val emprendedoraRepo = EmprendedoraRepositoryMemoria()
        val adminRepo = AdminRepositoryMemoria(emprendedoraRepo)

        val resEstado = adminRepo.cambiarEstadoFicha(1, EstadoFichaEmprendimiento.OCULTA)
        assertTrue(resEstado is Resultado.Exito)

        val ficha = adminRepo.obtenerFichaPorId(1).first()
        assertNotNull(ficha)
        assertEquals(EstadoFichaEmprendimiento.OCULTA, ficha?.estadoFicha)

        val resCorreo = adminRepo.reenviarCorreo(2)
        assertTrue(resCorreo is Resultado.Exito)
        val correo = adminRepo.obtenerCorreos().first().firstOrNull { it.id == 2 }
        assertEquals(EstadoCorreo.ENVIADO, correo?.estado)
    }
}
