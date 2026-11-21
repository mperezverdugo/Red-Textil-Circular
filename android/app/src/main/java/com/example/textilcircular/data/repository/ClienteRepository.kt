package com.example.textilcircular.data.repository

import com.example.textilcircular.model.Cliente
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.seguridad.HashUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Propuesta para la tabla clientes en SQLite:
// CREATE TABLE clientes (
//     id INTEGER PRIMARY KEY AUTOINCREMENT,
//     nombre TEXT NOT NULL,
//     email TEXT NOT NULL UNIQUE,
//     password_hash TEXT NOT NULL,
//     sal TEXT NOT NULL,
//     acepta_ley_datos INTEGER NOT NULL DEFAULT 1
// );

// Contrato para gestionar el registro, consulta y autenticación de clientes.
interface ClienteRepository {
    fun obtenerTodos(): Flow<List<Cliente>>
    fun obtenerPorId(id: Int): Flow<Cliente?>
    fun obtenerPorEmail(email: String): Flow<Cliente?>
    suspend fun registrar(nombre: String, email: String, password: String, aceptaLeyDatos: Boolean): Resultado<Cliente>
    suspend fun autenticar(email: String, password: String): Resultado<Cliente>
    suspend fun actualizar(cliente: Cliente): Resultado<Unit>
}

// Implementación en memoria que simula la persistencia y autenticación de clientes.
class ClienteRepositoryMemoria : ClienteRepository {

    private val clientes = DatosDemo.ClientesDemo.toMutableList()
    private val _clientesFlow = MutableStateFlow<List<Cliente>>(clientes.toList())

    override fun obtenerTodos(): Flow<List<Cliente>> = _clientesFlow.asStateFlow()

    override fun obtenerPorId(id: Int): Flow<Cliente?> {
        return _clientesFlow.map { list -> list.firstOrNull { it.id == id } }
    }

    override fun obtenerPorEmail(email: String): Flow<Cliente?> {
        val correoNormalizado = email.trim().lowercase()
        return _clientesFlow.map { list -> list.firstOrNull { it.email.lowercase() == correoNormalizado } }
    }

    override suspend fun registrar(
        nombre: String,
        email: String,
        password: String,
        aceptaLeyDatos: Boolean
    ): Resultado<Cliente> {
        val correoNormalizado = email.trim().lowercase()
        val yaExiste = clientes.any { it.email.lowercase() == correoNormalizado }
        if (yaExiste) {
            return Resultado.Error("Ya existe una cuenta registrada con este correo")
        }

        val sal = HashUtil.generarSal()
        val passwordHash = HashUtil.generarHash(password, sal)
        val nuevoId = (clientes.maxOfOrNull { it.id } ?: 0) + 1

        val nuevoCliente = Cliente(
            id = nuevoId,
            nombre = nombre.trim(),
            email = correoNormalizado,
            passwordHash = passwordHash,
            sal = sal,
            aceptaLeyDatos = aceptaLeyDatos
        )

        clientes.add(nuevoCliente)
        _clientesFlow.value = clientes.toList()

        // TODO: guardar esto en SQLite más adelante
        // TODO: cuando haya servidor, sincronizar cliente registrado
        return Resultado.Exito(nuevoCliente)
    }

    override suspend fun autenticar(email: String, password: String): Resultado<Cliente> {
        val correoNormalizado = email.trim().lowercase()
        val cliente = clientes.firstOrNull { it.email.lowercase() == correoNormalizado }
            ?: return Resultado.Error("Correo o contraseña incorrectos")

        val coincide = HashUtil.verificarPassword(password, cliente.sal, cliente.passwordHash)
        return if (coincide) {
            Resultado.Exito(cliente)
        } else {
            Resultado.Error("Correo o contraseña incorrectos")
        }
    }

    override suspend fun actualizar(cliente: Cliente): Resultado<Unit> {
        val indice = clientes.indexOfFirst { it.id == cliente.id }
        return if (indice >= 0) {
            clientes[indice] = cliente
            _clientesFlow.value = clientes.toList()
            // TODO: guardar esto en SQLite más adelante
            Resultado.Exito(Unit)
        } else {
            Resultado.Error("Cliente no encontrado")
        }
    }
}
