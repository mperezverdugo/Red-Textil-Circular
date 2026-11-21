package com.example.textilcircular.data.repository

import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.util.Resultado
import com.example.textilcircular.util.validacion.AuditorFichas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// Implementación del repositorio de emprendedoras conectada a SQLite a través de Room.
class EmprendedoraRepositoryRoom(
    private val emprendedoraDao: EmprendedoraDao
) : EmprendedoraRepository {

    // Retorna todas las emprendedoras registradas
    override fun obtenerTodas(): Flow<List<Emprendedora>> {
        return emprendedoraDao.obtenerTodas().map { lista ->
            lista.map { it.aModelo() }
        }
    }

    // Busca una emprendedora por su identificador
    override fun obtenerPorId(id: Int): Flow<Emprendedora?> {
        return emprendedoraDao.obtenerPorId(id).map { it?.aModelo() }
    }

    // Filtra las artesanas activas para la pantalla de mercado de clientes
    override fun obtenerMercado(
        textoBusqueda: String,
        region: String?,
        comuna: String?,
        rubro: Rubro?
    ): Flow<List<Emprendedora>> {
        val busquedaNormalizada = textoBusqueda.trim().lowercase()

        return emprendedoraDao.obtenerMercado(
            busqueda = busquedaNormalizada,
            region = region?.takeIf { it.isNotBlank() },
            comuna = comuna?.takeIf { it.isNotBlank() }
        ).map { lista ->
            // Si el cliente seleccionó un rubro, filtramos en memoria sobre la lista obtenida
            if (rubro == null) {
                lista.map { it.aModelo() }
            } else {
                lista.filter { it.rubros.contains(rubro) }.map { it.aModelo() }
            }
        }
    }

    // Guarda una nueva emprendedora o actualiza una existente
    override suspend fun guardar(emprendedora: Emprendedora): Resultado<Emprendedora> = withContext(Dispatchers.IO) {
        try {
            val correoLimpio = emprendedora.email.trim().lowercase()

            // Verificamos si el RUT ya está registrado por otra artesana
            val existenteRut = emprendedoraDao.obtenerPorRutDirecto(emprendedora.rut)
            if (existenteRut != null && (emprendedora.id <= 0 || existenteRut.id != emprendedora.id)) {
                return@withContext Resultado.Error("Ya existe una emprendedora registrada con este RUT")
            }

            // Verificamos si el correo ya está registrado por otra artesana
            val existenteEmail = emprendedoraDao.obtenerPorEmailDirecto(correoLimpio)
            if (existenteEmail != null && (emprendedora.id <= 0 || existenteEmail.id != emprendedora.id)) {
                return@withContext Resultado.Error("Ya existe una emprendedora registrada con este correo")
            }

            val problemas = AuditorFichas.auditar(emprendedora)

            // Las fichas nuevas registradas por la app inician en revisión antes de publicarse
            val estadoInicial = if (emprendedora.id > 0) {
                val actual = emprendedoraDao.obtenerPorIdDirecto(emprendedora.id)
                actual?.estadoFicha ?: EstadoFichaEmprendimiento.POR_REVISAR
            } else {
                EstadoFichaEmprendimiento.POR_REVISAR
            }

            val entidad = EmprendedoraEntity.desdeModelo(
                emprendedora = emprendedora.copy(
                    email = correoLimpio,
                    activa = (estadoInicial == EstadoFichaEmprendimiento.ACTIVA)
                ),
                estadoFicha = estadoInicial,
                problemas = problemas
            )
            val nuevoId = emprendedoraDao.insertar(entidad).toInt()
            val idFinal = if (emprendedora.id > 0) emprendedora.id else nuevoId

            // TODO: cuando haya servidor, enviar el registro de la emprendedora a la API REST
            Resultado.Exito(entidad.copy(id = idFinal).aModelo())
        } catch (e: Exception) {
            Resultado.Error("No se pudo guardar la emprendedora en la base de datos: ${e.message}")
        }
    }

    // Actualiza los datos de perfil de la emprendedora
    override suspend fun actualizar(emprendedora: Emprendedora): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val actual = emprendedoraDao.obtenerPorIdDirecto(emprendedora.id)
                ?: return@withContext Resultado.Error("No se encontró la emprendedora para actualizar")

            val problemas = AuditorFichas.auditar(emprendedora)
            val entidadActualizada = EmprendedoraEntity.desdeModelo(
                emprendedora = emprendedora,
                estadoFicha = actual.estadoFicha,
                problemas = problemas
            )
            emprendedoraDao.actualizar(entidadActualizada)

            // TODO: cuando haya servidor, sincronizar los cambios de la emprendedora con la API REST
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("Error al actualizar la emprendedora: ${e.message}")
        }
    }

    // Modifica si la artesana aparece visible en el mercado de clientes
    override suspend fun cambiarVisibilidad(id: Int, activa: Boolean): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            val filas = emprendedoraDao.cambiarVisibilidad(id, activa)
            if (filas > 0) {
                Resultado.Exito(Unit)
            } else {
                Resultado.Error("Emprendedora no encontrada")
            }
        } catch (e: Exception) {
            Resultado.Error("No se pudo cambiar la visibilidad: ${e.message}")
        }
    }

    // Registra la baja voluntaria de una artesana marcándola como inactiva en SQLite
    override suspend fun darDeBaja(id: Int): Resultado<Unit> = withContext(Dispatchers.IO) {
        try {
            emprendedoraDao.cambiarEstadoFicha(id, EstadoFichaEmprendimiento.DADA_DE_BAJA, activa = false)
            // TODO: cuando haya servidor, notificar la baja voluntaria a la API REST
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error("Error al registrar la baja: ${e.message}")
        }
    }
}
