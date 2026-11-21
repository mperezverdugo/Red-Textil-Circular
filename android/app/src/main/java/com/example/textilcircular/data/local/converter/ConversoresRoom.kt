package com.example.textilcircular.data.local.converter

import androidx.room.TypeConverter
import com.example.textilcircular.model.EstadoCorreo
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.EstadoSincronizacion
import com.example.textilcircular.model.Rol
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.model.TipoEmisor

// Conversores para que Room pueda guardar listas y valores enumerados en columnas de texto.
class ConversoresRoom {

    private val separador = "|||"

    // Conversión de lista de rubros a texto y viceversa
    @TypeConverter
    fun deListaRubros(rubros: List<Rubro>?): String {
        if (rubros.isNullOrEmpty()) return ""
        return rubros.joinToString(separador) { it.name }
    }

    @TypeConverter
    fun aListaRubros(texto: String?): List<Rubro> {
        if (texto.isNullOrBlank()) return emptyList()
        return texto.split(separador)
            .filter { it.isNotBlank() }
            .mapNotNull { nombre ->
                try {
                    Rubro.valueOf(nombre)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
    }

    // Conversión de lista de textos (rutas de fotos o problemas detectados)
    @TypeConverter
    fun deListaTexto(lista: List<String>?): String {
        if (lista.isNullOrEmpty()) return ""
        return lista.joinToString(separador)
    }

    @TypeConverter
    fun aListaTexto(texto: String?): List<String> {
        if (texto.isNullOrBlank()) return emptyList()
        return texto.split(separador).filter { it.isNotBlank() }
    }

    // Conversiones de enums a texto para no depender de la posición numérica
    @TypeConverter
    fun deRol(rol: Rol?): String? = rol?.name

    @TypeConverter
    fun aRol(nombre: String?): Rol? {
        if (nombre.isNullOrBlank()) return null
        return try { Rol.valueOf(nombre) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun deRubro(rubro: Rubro?): String? = rubro?.name

    @TypeConverter
    fun aRubro(nombre: String?): Rubro? {
        if (nombre.isNullOrBlank()) return null
        return try { Rubro.valueOf(nombre) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun deTipoEmisor(emisor: TipoEmisor?): String? = emisor?.name

    @TypeConverter
    fun aTipoEmisor(nombre: String?): TipoEmisor? {
        if (nombre.isNullOrBlank()) return null
        return try { TipoEmisor.valueOf(nombre) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun deEstadoSincronizacion(estado: EstadoSincronizacion?): String? = estado?.name

    @TypeConverter
    fun aEstadoSincronizacion(nombre: String?): EstadoSincronizacion? {
        if (nombre.isNullOrBlank()) return null
        return try { EstadoSincronizacion.valueOf(nombre) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun deEstadoCorreo(estado: EstadoCorreo?): String? = estado?.name

    @TypeConverter
    fun aEstadoCorreo(nombre: String?): EstadoCorreo? {
        if (nombre.isNullOrBlank()) return null
        return try { EstadoCorreo.valueOf(nombre) } catch (_: Exception) { null }
    }

    @TypeConverter
    fun deEstadoFicha(estado: EstadoFichaEmprendimiento?): String? = estado?.name

    @TypeConverter
    fun aEstadoFicha(nombre: String?): EstadoFichaEmprendimiento? {
        if (nombre.isNullOrBlank()) return null
        return try { EstadoFichaEmprendimiento.valueOf(nombre) } catch (_: Exception) { null }
    }
}
