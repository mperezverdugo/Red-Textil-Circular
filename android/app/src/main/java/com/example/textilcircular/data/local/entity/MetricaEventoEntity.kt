package com.example.textilcircular.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Entidad para registrar eventos de interacción (búsquedas, clics y consultas) para las métricas.
@Entity(
    tableName = "metricas_eventos",
    indices = [
        Index(value = ["tipo_evento"]),
        Index(value = ["region"]),
        Index(value = ["rubro"])
    ]
)
data class MetricaEventoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "tipo_evento")
    val tipoEvento: String,
    val termino: String? = null,
    val region: String = "",
    val rubro: String = "",
    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Long = System.currentTimeMillis()
)
