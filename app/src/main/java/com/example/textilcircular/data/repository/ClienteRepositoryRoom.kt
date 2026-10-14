package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.model.Cliente
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.seguridad.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// Implementación del repositorio de clientes respaldada en la base de datos local SQLite.
class ClienteRepositoryRoom(
    private val clienteDao: ClienteDao
) : ClienteRepository {

    // Retorna todos los clientes registrados ordenados alfabéticamente
    override fun obtenerTodos(): Flow<List<Cliente>> {
        return clienteDao.obtenerTodos().map { lista ->
            lista.map { it.aModelo() }
        }
    }

    // Busca un cliente por id de manera reactiva
    override fun obtenerPorId(id: Int): Flow<Cliente?> {
        return clienteDao.obtenerPorId(id).map { it?.aModelo() }
    }

    // Busca un cliente por su correo electrónico
    override fun obtenerPorEmail(email: String): Flow<Cliente?> {
        return clienteDao.obtenerPorEmail(email.trim().lowercase()).map { it?.aModelo() }
    }

    // Registra un nuevo cliente cifrando la contraseña con sal única antes de guardarla en SQLite
    override suspend fun registrar(
        nombre: String,
        email: String,
        password: String,
        aceptaLeyDatos: Boolean
    ): Resultado<Cliente> = withContext(Dispatchers.IO) {
        val correoNormalizado = email.trim().lowercase()

        // Verificamos si ya existe una cuenta con este correo
        val existente = clienteDao.obtenerPorEmailDirecto(correoNormalizado)
        if (existente != null) {
            return@withContext Resultado.Error("Ya existe una cuenta registrada con este correo")
        }

        // Generamos sal aleatoria y el hash seguro de la contraseña
        val sal = HashUtil.generarSal()
        val passwordHash = HashUtil.generarHash(password, sal)

        val entidad = ClienteEntity(
            id = 0,
            nombre = nombre.trim(),
            email = correoNormalizado,
            passwordHash = passwordHash,
            sal = sal,
            aceptaLeyDatos = aceptaLeyDatos
        )

        try {
            val nuevoId = clienteDao.insertar(entidad).toInt()
            val clienteGuardado = entidad.copy(id = nuevoId).aModelo()

            // TODO: cuando haya servidor, sincronizar cliente registrado con la API REST
            Resultado.Exito(clienteGuardado)
        } catch (e: Exception) {
            Resultado.Error("Error al guardar el cliente en la base de datos: ${e.message}")
        }
    }

    // Valida las credenciales comparando la contraseña ingresada con el hash y sal almacenados
    override suspend fun autenticar(email: String, password: String): Resultado<Cliente> = withContext(Dispatchers.IO) {
        val correoNormalizado = email.trim().lowercase()
        val clienteEntity = clienteDao.obtenerPorEmailDirecto(correoNormalizado)
            ?: return@withContext Resultado.Error("Correo o contraseña incorrectos")

        val coincide = HashUtil.verificarPassword(password, clienteEntity.sal, clienteEntity.passwordHash)
        if (coincide) {
            Resultado.Exito(clienteEntity.aModelo())
        } else {
            Resultado.Error("Correo o contraseña incorrectos")
        }
    }

    // Actualiza los datos del cliente en SQLite
    override suspend fun actualizar(cliente: Cliente): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val existente = clienteDao.obtenerPorIdDirecto(cliente.id)
                ?: return@withContext Resultado.Error("Cliente no encontrado")

            val entidadActualizada = ClienteEntity.desdeModelo(cliente).copy(
                passwordHash = existente.passwordHash,
                sal = existente.sal
            )
            clienteDao.actualizar(entidadActualizada)

            // TODO: cuando haya servidor, sincronizar actualización de cliente con la API REST
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("No se pudo actualizar el cliente: ${e.message}")
        }
    }
}
