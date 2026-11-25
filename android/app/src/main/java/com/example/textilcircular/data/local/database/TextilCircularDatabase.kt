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
    version = 4,
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

        val MIGRACION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sesion_activa ADD COLUMN token TEXT")
            }
        }

        val MIGRACION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE emprendedoras ADD COLUMN usuario_id INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE emprendedoras ADD COLUMN motivo_revision TEXT")

                // Recrear productos sin FK cruzada hacia emprendedoras.id (usa usuarioId de ms-mercado)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS productos_nueva (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        emprendedora_id INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        descripcion_corta TEXT NOT NULL,
                        explicacion_proceso TEXT,
                        fotos TEXT NOT NULL,
                        pendiente_sincronizar INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO productos_nueva (id, emprendedora_id, nombre, descripcion_corta, explicacion_proceso, fotos, pendiente_sincronizar)
                    SELECT id, emprendedora_id, nombre, descripcion_corta, explicacion_proceso, fotos, pendiente_sincronizar FROM productos
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE productos")
                db.execSQL("ALTER TABLE productos_nueva RENAME TO productos")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_productos_emprendedora_id ON productos (emprendedora_id)")

                // Respaldar mensajes, recrear conversaciones sin FK cruzadas, y restaurar mensajes
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mensajes_backup (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conversacion_id INTEGER NOT NULL,
                        emisor TEXT NOT NULL,
                        contenido TEXT NOT NULL,
                        fecha_hora TEXT NOT NULL,
                        es_leido INTEGER NOT NULL,
                        producto_id INTEGER,
                        nombre_producto TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO mensajes_backup (id, conversacion_id, emisor, contenido, fecha_hora, es_leido, producto_id, nombre_producto)
                    SELECT id, conversacion_id, emisor, contenido, fecha_hora, es_leido, producto_id, nombre_producto FROM mensajes
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE mensajes")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS conversaciones_nueva (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cliente_id INTEGER NOT NULL,
                        emprendedora_id INTEGER NOT NULL,
                        producto_id INTEGER,
                        nombre_cliente TEXT NOT NULL,
                        nombre_emprendimiento TEXT NOT NULL,
                        ultimo_mensaje TEXT NOT NULL,
                        fecha_hora TEXT NOT NULL,
                        no_leidos_cliente INTEGER NOT NULL,
                        no_leidos_emprendedora INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO conversaciones_nueva (id, cliente_id, emprendedora_id, producto_id, nombre_cliente, nombre_emprendimiento, ultimo_mensaje, fecha_hora, no_leidos_cliente, no_leidos_emprendedora)
                    SELECT id, cliente_id, emprendedora_id, producto_id, nombre_cliente, nombre_emprendimiento, ultimo_mensaje, fecha_hora, no_leidos_cliente, no_leidos_emprendedora FROM conversaciones
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE conversaciones")
                db.execSQL("ALTER TABLE conversaciones_nueva RENAME TO conversaciones")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_conversaciones_cliente_id_emprendedora_id ON conversaciones (cliente_id, emprendedora_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_conversaciones_emprendedora_id ON conversaciones (emprendedora_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_conversaciones_producto_id ON conversaciones (producto_id)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mensajes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conversacion_id INTEGER NOT NULL,
                        emisor TEXT NOT NULL,
                        contenido TEXT NOT NULL,
                        fecha_hora TEXT NOT NULL,
                        es_leido INTEGER NOT NULL,
                        producto_id INTEGER,
                        nombre_producto TEXT,
                        FOREIGN KEY(conversacion_id) REFERENCES conversaciones(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO mensajes (id, conversacion_id, emisor, contenido, fecha_hora, es_leido, producto_id, nombre_producto)
                    SELECT id, conversacion_id, emisor, contenido, fecha_hora, es_leido, producto_id, nombre_producto FROM mensajes_backup
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE mensajes_backup")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mensajes_conversacion_id ON mensajes (conversacion_id)")
            }
        }

        val MIGRACION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sesion_activa ADD COLUMN emprendedora_id INTEGER")
            }
        }

        // Retorna la instancia única de la base de datos (patrón Singleton)
        fun obtenerInstancia(context: Context): TextilCircularDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    TextilCircularDatabase::class.java,
                    "textil_circular.db"
                )
                    .addCallback(PrePobladoDatabaseCallback { obtenerInstancia(context) })
                    .addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCIA = instancia
                instancia
            }
        }
    }
}
