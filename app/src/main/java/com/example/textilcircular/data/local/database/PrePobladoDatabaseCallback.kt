package com.example.textilcircular.data.local.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.data.local.entity.ConversacionEntity
import com.example.textilcircular.data.local.entity.CorreoAutomatizacionEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MensajeEntity
import com.example.textilcircular.data.local.entity.MetricaEventoEntity
import com.example.textilcircular.data.local.entity.ProductoEntity
import com.example.textilcircular.data.local.entity.SincronizacionEntity
import com.example.textilcircular.ui.preview.DatosDemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Callback para insertar los datos semilla en la primera creación de la base de datos SQLite.
class PrePobladoDatabaseCallback(
    private val obtenerBaseDatos: () -> TextilCircularDatabase
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        ejecutarSembrado()
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        ejecutarSembrado()
    }

    private fun ejecutarSembrado() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val baseDatos = obtenerBaseDatos()
                sembrarSiEstaVacia(baseDatos)
                // Limpieza de cualquier elemento demo residual de sincronización en bases ya existentes
                baseDatos.sincronizacionDao().eliminarPorDetalleDemo("%Cojín Textil Parche%")
                baseDatos.sincronizacionDao().eliminarPorDetalleDemo("%@tallerhilosdelsur%")
                baseDatos.productoDao().limpiarPendienteProducto(102)
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        // Verifica si la tabla de emprendedoras está vacía y puebla todos los datos iniciales
        suspend fun sembrarSiEstaVacia(baseDatos: TextilCircularDatabase) {
            val primerEmp = baseDatos.emprendedoraDao().obtenerPorIdDirecto(1)
            if (primerEmp != null) return

            // 1. Clientes registrados de prueba
            val clientesEntidades = DatosDemo.ClientesDemo.map { ClienteEntity.desdeModelo(it) }
            baseDatos.clienteDao().insertarVarios(clientesEntidades)

            // 2. Emprendedoras con sus estados de moderación y auditoría
            val emprendedorasEntidades = DatosDemo.EmprendimientosAdminDemo.map { ficha ->
                EmprendedoraEntity.desdeModelo(
                    emprendedora = ficha.emprendedora,
                    estadoFicha = ficha.estadoFicha,
                    problemas = ficha.problemasDetectados
                )
            }
            baseDatos.emprendedoraDao().insertarVarias(emprendedorasEntidades)

            // 3. Catálogo de productos textiles
            val productosEntidades = DatosDemo.ProductosDemo.map { ProductoEntity.desdeModelo(it) }
            baseDatos.productoDao().insertarVarios(productosEntidades)

            // 4. Conversaciones y mensajes iniciales del chat
            val conversacionesEntidades = DatosDemo.ConversacionesDemo.map { ConversacionEntity.desdeModelo(it) }
            baseDatos.mensajeDao().insertarConversaciones(conversacionesEntidades)

            val mensajesEntidades = DatosDemo.MensajesConversacionDemo.map { MensajeEntity.desdeModelo(it) }
            baseDatos.mensajeDao().insertarMensajes(mensajesEntidades)

            // 5. Registros de automatizaciones de correo
            val correosEntidades = DatosDemo.AutomatizacionesCorreosDemo.map { CorreoAutomatizacionEntity.desdeModelo(it) }
            baseDatos.adminDao().insertarCorreos(correosEntidades)

            // 6. Cola inicial de sincronización
            // La cola de sincronización arranca limpia; los elementos pendientes deben generarse únicamente por cambios locales que realice la usuaria.

            // 7. Eventos de métricas iniciales para que el panel muestre estadísticas desde el inicio
            insertarMetricasIniciales(baseDatos)
        }

        // Inserta eventos base para que los gráficos y conteos del panel no arranquen en cero
        private suspend fun insertarMetricasIniciales(baseDatos: TextilCircularDatabase) {
            val ahora = System.currentTimeMillis()

            // Registramos eventos de contactos por región y rubro basados en los datos demo
            DatosDemo.ContactosPorRegionDemo.forEach { (region, cantidad) ->
                repeat(cantidad) {
                    baseDatos.adminDao().registrarEvento(
                        MetricaEventoEntity(
                            tipoEvento = "CLIC_CONTACTO",
                            region = region,
                            fechaRegistro = ahora
                        )
                    )
                }
            }

            DatosDemo.ContactosPorRubroDemo.forEach { (rubro, cantidad) ->
                repeat(cantidad) {
                    baseDatos.adminDao().registrarEvento(
                        MetricaEventoEntity(
                            tipoEvento = "CONSULTA_ENVIADA",
                            rubro = rubro,
                            fechaRegistro = ahora
                        )
                    )
                }
            }

            // Búsquedas generales
            repeat(DatosDemo.TotalBusquedasMes) {
                baseDatos.adminDao().registrarEvento(
                    MetricaEventoEntity(
                        tipoEvento = "BUSQUEDA",
                        termino = "textil",
                        fechaRegistro = ahora
                    )
                )
            }
        }
    }
}
