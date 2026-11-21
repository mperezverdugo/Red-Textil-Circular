package com.example.textilcircular.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.textilcircular.data.local.converter.ConversoresRoom
import com.example.textilcircular.data.local.dao.AdminDao
import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.dao.MensajeDao
import com.example.textilcircular.data.local.dao.ProductoDao
import com.example.textilcircular.data.local.dao.SesionDao
import com.example.textilcircular.data.local.dao.SincronizacionDao
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.data.local.entity.ConversacionEntity
import com.example.textilcircular.data.local.entity.CorreoAutomatizacionEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MensajeEntity
import com.example.textilcircular.data.local.entity.MetricaEventoEntity
import com.example.textilcircular.data.local.entity.ProductoEntity
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import com.example.textilcircular.data.local.entity.SincronizacionEntity

// Base de datos central de Room que administra las tablas locales de SQLite.
@Database(
    entities = [
        EmprendedoraEntity::class,
        ClienteEntity::class,
        ProductoEntity::class,
        ConversacionEntity::class,
        MensajeEntity::class,
        MetricaEventoEntity::class,
        SincronizacionEntity::class,
        CorreoAutomatizacionEntity::class,
        SesionActivaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ConversoresRoom::class)
abstract class TextilCircularDatabase : RoomDatabase() {

    // Acceso a los DAOs
    abstract fun emprendedoraDao(): EmprendedoraDao
    abstract fun clienteDao(): ClienteDao
    abstract fun productoDao(): ProductoDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun adminDao(): AdminDao
    abstract fun sincronizacionDao(): SincronizacionDao
    abstract fun sesionDao(): SesionDao

    companion object {
        @Volatile
        private var INSTANCIA: TextilCircularDatabase? = null

        // Retorna la instancia única de la base de datos (patrón Singleton)
        fun obtenerInstancia(context: Context): TextilCircularDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    TextilCircularDatabase::class.java,
                    "textil_circular.db"
                )
                    .addCallback(PrePobladoDatabaseCallback { obtenerInstancia(context) })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCIA = instancia
                instancia
            }
        }
    }
}
