package com.example.textilcircular

import com.example.textilcircular.data.repository.ClienteRepositoryMemoria
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.seguridad.HashUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClienteRepositoryTest {

    @Test
    fun registrar_guardaClienteConHashYSalSegura() = runTest {
        val repo = ClienteRepositoryMemoria()
        val correo = "nuevo.cliente@correo.cl"
        val clave = "MiClaveSegura123"

        val resultado = repo.registrar(
            nombre = "Martín Valenzuela",
            email = correo,
            password = clave,
            aceptaLeyDatos = true
        )

        assertTrue(resultado is Resultado.Exito)
        val cliente = (resultado as Resultado.Exito).datos

        assertEquals("Martín Valenzuela", cliente.nombre)
        assertEquals(correo, cliente.email)
        assertTrue(cliente.sal.isNotBlank())
        assertTrue(cliente.passwordHash.isNotBlank())
        assertNotEquals(clave, cliente.passwordHash)

        // El hash guardado debe coincidir con la clave y la sal generada
        val coincide = HashUtil.verificarPassword(clave, cliente.sal, cliente.passwordHash)
        assertTrue(coincide)
    }

    @Test
    fun registrar_fallaSiElCorreoYaEstaRegistrado() = runTest {
        val repo = ClienteRepositoryMemoria()
        val correoExistente = "francisca.tapia@correo.cl"

        val resultado = repo.registrar(
            nombre = "Otra Francisca",
            email = correoExistente,
            password = "otraClave456",
            aceptaLeyDatos = true
        )

        assertTrue(resultado is Resultado.Error)
        assertEquals("Ya existe una cuenta registrada con este correo", (resultado as Resultado.Error).mensaje)
    }

    @Test
    fun autenticar_permiteAccesoConClaveCorrecta() = runTest {
        val repo = ClienteRepositoryMemoria()

        val resultado = repo.autenticar("francisca.tapia@correo.cl", "cliente123")

        assertTrue(resultado is Resultado.Exito)
        val cliente = (resultado as Resultado.Exito).datos
        assertEquals("Francisca Tapia", cliente.nombre)
    }

    @Test
    fun autenticar_rechazaAccesoConClaveIncorrecta() = runTest {
        val repo = ClienteRepositoryMemoria()

        val resultado = repo.autenticar("francisca.tapia@correo.cl", "claveErronea")

        assertTrue(resultado is Resultado.Error)
        assertEquals("Correo o contraseña incorrectos", (resultado as Resultado.Error).mensaje)
    }

    @Test
    fun autenticar_rechazaCorreoInexistente() = runTest {
        val repo = ClienteRepositoryMemoria()

        val resultado = repo.autenticar("no.existe@correo.cl", "cliente123")

        assertTrue(resultado is Resultado.Error)
        assertEquals("Correo o contraseña incorrectos", (resultado as Resultado.Error).mensaje)
    }

    @Test
    fun actualizar_modificaNombreDelCliente() = runTest {
        val repo = ClienteRepositoryMemoria()
        val clienteOriginal = repo.obtenerPorId(1).first()
        assertNotNull(clienteOriginal)

        val clienteModificado = clienteOriginal!!.copy(nombre = "Francisca Tapia Modificada")
        val resultado = repo.actualizar(clienteModificado)
        assertTrue(resultado is Resultado.Exito)

        val guardado = repo.obtenerPorId(1).first()
        assertEquals("Francisca Tapia Modificada", guardado?.nombre)
    }
}
