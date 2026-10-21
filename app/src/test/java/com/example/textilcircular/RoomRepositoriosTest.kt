package com.example.textilcircular

import com.example.textilcircular.data.local.dao.ClienteDao
import com.example.textilcircular.data.local.dao.EmprendedoraDao
import com.example.textilcircular.data.local.dao.MensajeDao
import com.example.textilcircular.data.local.dao.ProductoDao
import com.example.textilcircular.data.local.dao.SesionDao
import com.example.textilcircular.data.local.entity.ClienteEntity
import com.example.textilcircular.data.local.entity.ConversacionEntity
import com.example.textilcircular.data.local.entity.EmprendedoraEntity
import com.example.textilcircular.data.local.entity.MensajeEntity
import com.example.textilcircular.data.local.entity.ProductoEntity
import com.example.textilcircular.data.local.entity.SesionActivaEntity
import com.example.textilcircular.data.repository.ClienteRepositoryRoom
import com.example.textilcircular.data.repository.EmprendedoraRepositoryRoom
import com.example.textilcircular.data.repository.EstadoSesion
import com.example.textilcircular.data.repository.MensajeRepositoryRoom
import com.example.textilcircular.data.repository.ProductoRepositoryRoom
import com.example.textilcircular.data.repository.SesionRepositoryRoom
import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.EstadoFichaEmprendimiento
import com.example.textilcircular.model.Producto
import com.example.textilcircular.model.Rol
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.model.TipoEmisor
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas unitarias de los repositorios Room para validar el flujo completo sobre DAOs locales.
class RoomRepositoriosTest {

    // Simulación en memoria de EmprendedoraDao para pruebas unitarias rápidas
    class FakeEmprendedoraDao : EmprendedoraDao {
        private val listado = mutableListOf<EmprendedoraEntity>()
        private val _flow = MutableStateFlow<List<EmprendedoraEntity>>(emptyList())
        private var idContador = 1

        private fun notificar() {
            _flow.value = listado.toList()
        }

        override fun obtenerTodas(): Flow<List<EmprendedoraEntity>> = _flow.asStateFlow()

        override fun obtenerPorId(id: Int): Flow<EmprendedoraEntity?> {
            return _flow.map { lista -> lista.find { it.id == id } }
        }

        override suspend fun obtenerPorIdDirecto(id: Int): EmprendedoraEntity? {
            return listado.find { it.id == id }
        }

        override suspend fun obtenerPorRutDirecto(rut: String): EmprendedoraEntity? {
            return listado.find { it.rut.equals(rut.trim(), ignoreCase = true) }
        }

        override fun obtenerPorEmail(email: String): Flow<EmprendedoraEntity?> {
            return _flow.map { lista -> lista.find { it.email.equals(email.trim(), ignoreCase = true) } }
        }

        override suspend fun obtenerPorEmailDirecto(email: String): EmprendedoraEntity? {
            return listado.find { it.email.equals(email.trim(), ignoreCase = true) }
        }

        override fun obtenerMercado(busqueda: String, region: String?, comuna: String?): Flow<List<EmprendedoraEntity>> {
            return _flow.map { lista ->
                lista.filter { emp ->
                    emp.activa &&
                            (busqueda.isEmpty() || emp.nombreComercial.contains(busqueda, ignoreCase = true)) &&
                            (region.isNullOrEmpty() || emp.region.equals(region, ignoreCase = true)) &&
                            (comuna.isNullOrEmpty() || emp.comuna.equals(comuna, ignoreCase = true))
                }
            }
        }

        override suspend fun insertar(emprendedora: EmprendedoraEntity): Long {
            val asignada = if (emprendedora.id == 0) emprendedora.copy(id = idContador++) else emprendedora
            listado.removeAll { it.id == asignada.id }
            listado.add(asignada)
            notificar()
            return asignada.id.toLong()
        }

        override suspend fun insertarVarias(emprendedoras: List<EmprendedoraEntity>) {
            emprendedoras.forEach { insertar(it) }
        }

        override suspend fun actualizar(emprendedora: EmprendedoraEntity): Int {
            val index = listado.indexOfFirst { it.id == emprendedora.id }
            return if (index != -1) {
                listado[index] = emprendedora
                notificar()
                1
            } else 0
        }

        override suspend fun cambiarVisibilidad(id: Int, activa: Boolean): Int {
            val index = listado.indexOfFirst { it.id == id }
            return if (index != -1) {
                val actual = listado[index]
                listado[index] = actual.copy(
                    activa = activa,
                    estadoFicha = if (activa) EstadoFichaEmprendimiento.ACTIVA else EstadoFichaEmprendimiento.OCULTA
                )
                notificar()
                1
            } else 0
        }

        override suspend fun cambiarEstadoFicha(id: Int, estado: EstadoFichaEmprendimiento, activa: Boolean): Int {
            val index = listado.indexOfFirst { it.id == id }
            return if (index != -1) {
                val actual = listado[index]
                listado[index] = actual.copy(estadoFicha = estado, activa = activa)
                notificar()
                1
            } else 0
        }

        override suspend fun eliminarPorId(id: Int): Int {
            val removed = listado.removeAll { it.id == id }
            if (removed) notificar()
            return if (removed) 1 else 0
        }
    }

    // Simulación en memoria de SesionDao
    class FakeSesionDao : SesionDao {
        private var sesion: SesionActivaEntity? = null
        private val _flow = MutableStateFlow<SesionActivaEntity?>(null)

        override fun obtenerSesion(): Flow<SesionActivaEntity?> = _flow.asStateFlow()

        override suspend fun obtenerSesionDirecta(): SesionActivaEntity? = sesion

        override suspend fun guardarSesion(sesion: SesionActivaEntity) {
            this.sesion = sesion
            _flow.value = sesion
        }

        override suspend fun cerrarSesion(): Int {
            val teniaSesion = sesion != null
            this.sesion = null
            _flow.value = null
            return if (teniaSesion) 1 else 0
        }
    }

    // Simulación en memoria de ClienteDao
    class FakeClienteDao : ClienteDao {
        private val listado = mutableListOf<ClienteEntity>()
        private val _flow = MutableStateFlow<List<ClienteEntity>>(emptyList())
        private var idContador = 1

        private fun notificar() {
            _flow.value = listado.toList()
        }

        override fun obtenerTodos(): Flow<List<ClienteEntity>> = _flow.asStateFlow()

        override fun obtenerPorEmail(email: String): Flow<ClienteEntity?> {
            return _flow.map { lista -> lista.find { it.email.equals(email.trim(), ignoreCase = true) } }
        }

        override suspend fun obtenerPorEmailDirecto(email: String): ClienteEntity? {
            return listado.find { it.email.equals(email.trim(), ignoreCase = true) }
        }

        override fun obtenerPorId(id: Int): Flow<ClienteEntity?> {
            return _flow.map { lista -> lista.find { it.id == id } }
        }

        override suspend fun obtenerPorIdDirecto(id: Int): ClienteEntity? {
            return listado.find { it.id == id }
        }

        override suspend fun insertar(cliente: ClienteEntity): Long {
            val asignado = if (cliente.id == 0) cliente.copy(id = idContador++) else cliente
            listado.removeAll { it.id == asignado.id }
            listado.add(asignado)
            notificar()
            return asignado.id.toLong()
        }

        override suspend fun insertarVarios(clientes: List<ClienteEntity>) {
            clientes.forEach { insertar(it) }
        }

        override suspend fun actualizar(cliente: ClienteEntity): Int {
            val idx = listado.indexOfFirst { it.id == cliente.id }
            return if (idx != -1) {
                listado[idx] = cliente
                notificar()
                1
            } else 0
        }
    }

    // Simulación en memoria de ProductoDao
    class FakeProductoDao : ProductoDao {
        private val productos = mutableListOf<ProductoEntity>()
        private val _flow = MutableStateFlow<List<ProductoEntity>>(emptyList())
        private var idContador = 100

        private fun notificar() {
            _flow.value = productos.toList()
        }

        override fun obtenerPorEmprendedora(emprendedoraId: Int): Flow<List<ProductoEntity>> {
            return _flow.map { lista -> lista.filter { it.emprendedoraId == emprendedoraId } }
        }

        override fun obtenerTodos(): Flow<List<ProductoEntity>> = _flow.asStateFlow()

        override fun obtenerPorId(id: Int): Flow<ProductoEntity?> {
            return _flow.map { lista -> lista.find { it.id == id } }
        }

        override suspend fun obtenerPorIdDirecto(id: Int): ProductoEntity? {
            return productos.find { it.id == id }
        }

        override suspend fun insertar(producto: ProductoEntity): Long {
            val asignado = if (producto.id == 0) producto.copy(id = idContador++) else producto
            productos.removeAll { it.id == asignado.id }
            productos.add(asignado)
            notificar()
            return asignado.id.toLong()
        }

        override suspend fun insertarVarios(productos: List<ProductoEntity>) {
            productos.forEach { insertar(it) }
        }

        override suspend fun actualizar(producto: ProductoEntity): Int {
            val idx = productos.indexOfFirst { it.id == producto.id }
            return if (idx != -1) {
                productos[idx] = producto
                notificar()
                1
            } else 0
        }

        override suspend fun eliminarPorId(id: Int): Int {
            val removed = productos.removeAll { it.id == id }
            if (removed) notificar()
            return if (removed) 1 else 0
        }

        override suspend fun marcarTodosSincronizados(): Int {
            var actualizados = 0
            for (i in productos.indices) {
                if (productos[i].pendienteSincronizar) {
                    productos[i] = productos[i].copy(pendienteSincronizar = false)
                    actualizados++
                }
            }
            if (actualizados > 0) notificar()
            return actualizados
        }

        override suspend fun limpiarPendienteProducto(id: Int): Int {
            val idx = productos.indexOfFirst { it.id == id }
            return if (idx != -1) {
                productos[idx] = productos[idx].copy(pendienteSincronizar = false)
                notificar()
                1
            } else 0
        }
    }

    // Simulación en memoria de MensajeDao
    class FakeMensajeDao : MensajeDao {
        private val conversaciones = mutableListOf<ConversacionEntity>()
        private val mensajes = mutableListOf<MensajeEntity>()
        private val _convsFlow = MutableStateFlow<List<ConversacionEntity>>(emptyList())
        private val _mensajesFlow = MutableStateFlow<List<MensajeEntity>>(emptyList())
        private var idConvContador = 1
        private var idMensajeContador = 1

        private fun notificarConvs() { _convsFlow.value = conversaciones.toList() }
        private fun notificarMensajes() { _mensajesFlow.value = mensajes.toList() }

        override fun obtenerConversacionesEmprendedora(emprendedoraId: Int): Flow<List<ConversacionEntity>> {
            return _convsFlow.map { lista -> lista.filter { it.emprendedoraId == emprendedoraId } }
        }

        override fun obtenerConversacionesCliente(clienteId: Int): Flow<List<ConversacionEntity>> {
            return _convsFlow.map { lista -> lista.filter { it.clienteId == clienteId } }
        }

        override fun obtenerConversacionPorId(id: Int): Flow<ConversacionEntity?> {
            return _convsFlow.map { lista -> lista.find { it.id == id } }
        }

        override suspend fun obtenerConversacionPorIdDirecto(id: Int): ConversacionEntity? {
            return conversaciones.find { it.id == id }
        }

        override fun obtenerConversacionEntre(clienteId: Int, emprendedoraId: Int): Flow<ConversacionEntity?> {
            return _convsFlow.map { lista -> lista.find { it.clienteId == clienteId && it.emprendedoraId == emprendedoraId } }
        }

        override suspend fun obtenerConversacionEntreDirecto(clienteId: Int, emprendedoraId: Int): ConversacionEntity? {
            return conversaciones.find { it.clienteId == clienteId && it.emprendedoraId == emprendedoraId }
        }

        override fun obtenerMensajesPorConversacion(conversacionId: Int): Flow<List<MensajeEntity>> {
            return _mensajesFlow.map { lista -> lista.filter { it.conversacionId == conversacionId } }
        }

        override suspend fun insertarConversacion(conversacion: ConversacionEntity): Long {
            val asignada = if (conversacion.id == 0) conversacion.copy(id = idConvContador++) else conversacion
            conversaciones.removeAll { it.id == asignada.id }
            conversaciones.add(asignada)
            notificarConvs()
            return asignada.id.toLong()
        }

        override suspend fun insertarConversaciones(conversaciones: List<ConversacionEntity>) {
            conversaciones.forEach { insertarConversacion(it) }
        }

        override suspend fun actualizarConversacion(conversacion: ConversacionEntity): Int {
            val idx = conversaciones.indexOfFirst { it.id == conversacion.id }
            return if (idx != -1) {
                conversaciones[idx] = conversacion
                notificarConvs()
                1
            } else 0
        }

        override suspend fun insertarMensaje(mensaje: MensajeEntity): Long {
            val asignado = if (mensaje.id == 0) mensaje.copy(id = idMensajeContador++) else mensaje
            mensajes.add(asignado)
            notificarMensajes()
            return asignado.id.toLong()
        }

        override suspend fun insertarMensajes(mensajes: List<MensajeEntity>) {
            mensajes.forEach { insertarMensaje(it) }
        }

        override suspend fun limpiarNoLeidosEmprendedora(conversacionId: Int): Int {
            val idx = conversaciones.indexOfFirst { it.id == conversacionId }
            return if (idx != -1) {
                conversaciones[idx] = conversaciones[idx].copy(noLeidosEmprendedora = 0)
                notificarConvs()
                1
            } else 0
        }

        override suspend fun limpiarNoLeidosCliente(conversacionId: Int): Int {
            val idx = conversaciones.indexOfFirst { it.id == conversacionId }
            return if (idx != -1) {
                conversaciones[idx] = conversaciones[idx].copy(noLeidosCliente = 0)
                notificarConvs()
                1
            } else 0
        }
    }

    @Test
    fun emprendedoraRepositoryRoom_nuevoRegistroQuedaInactivoYPorRevisar() = runTest {
        val fakeDao = FakeEmprendedoraDao()
        val repo = EmprendedoraRepositoryRoom(fakeDao)

        val nueva = Emprendedora(
            id = 0,
            rut = "12.345.678-5",
            nombre = "María Silva",
            nombreComercial = "Textiles Luna",
            region = "Metropolitana de Santiago",
            comuna = "Santiago",
            rubros = listOf(Rubro.SUPRARRECICLAJE),
            email = "maria@correo.cl",
            activa = true // El formulario puede pedir activación, pero la regla de moderación lo deja inactivo
        )

        val resultado = repo.guardar(nueva)
        assertTrue(resultado is Resultado.Exito)

        // Verificamos que en la base de datos local quedó inactiva y pendiente de revisión
        val guardadaEntity = fakeDao.obtenerPorEmailDirecto("maria@correo.cl")
        assertNotNull(guardadaEntity)
        assertFalse(guardadaEntity!!.activa)
        assertEquals(EstadoFichaEmprendimiento.POR_REVISAR, guardadaEntity.estadoFicha)
    }

    @Test
    fun emprendedoraRepositoryRoom_rechazaRutOCorreoDuplicados() = runTest {
        val fakeDao = FakeEmprendedoraDao()
        val repo = EmprendedoraRepositoryRoom(fakeDao)

        val emp1 = Emprendedora(
            id = 0,
            rut = "12.345.678-5",
            nombre = "Artesana Uno",
            nombreComercial = "Taller Uno",
            region = "Valparaíso",
            comuna = "Valparaíso",
            rubros = listOf(Rubro.REPARACION),
            email = "artesana1@correo.cl"
        )
        val res1 = repo.guardar(emp1)
        assertTrue(res1 is Resultado.Exito)

        // Intentar guardar otra con el mismo RUT
        val emp2MismoRut = emp1.copy(id = 0, email = "distinto@correo.cl")
        val resRut = repo.guardar(emp2MismoRut)
        assertTrue(resRut is Resultado.Error)

        // Intentar guardar otra con el mismo correo
        val emp3MismoEmail = emp1.copy(id = 0, rut = "11.111.111-1")
        val resEmail = repo.guardar(emp3MismoEmail)
        assertTrue(resEmail is Resultado.Error)
    }

    @Test
    fun sesionRepositoryRoom_loginConUsuarioInexistenteFallaSinUsarDemo() = runTest {
        val fakeEmpDao = FakeEmprendedoraDao()
        val fakeClienteDao = FakeClienteDao()
        val fakeSesionDao = FakeSesionDao()

        val empRepo = EmprendedoraRepositoryRoom(fakeEmpDao)
        val clienteRepo = ClienteRepositoryRoom(fakeClienteDao)
        val sesionRepo = SesionRepositoryRoom(
            sesionDao = fakeSesionDao,
            emprendedoraDao = fakeEmpDao,
            clienteDao = fakeClienteDao,
            emprendedoraRepository = empRepo,
            clienteRepository = clienteRepo,
            scope = this
        )

        // Intento de login con un correo que no existe en SQLite
        val resultado = sesionRepo.iniciarSesionEmprendedora("no_existe@correo.cl", "clave123")
        assertTrue(resultado is Resultado.Error)
        assertEquals(EstadoSesion.NoAutenticado, sesionRepo.estadoSesion.value)
    }

    @Test
    fun sesionRepositoryRoom_sesionDeUsuarioEliminadoSeCierraAutomaticamente() = runTest {
        val fakeEmpDao = FakeEmprendedoraDao()
        val fakeClienteDao = FakeClienteDao()
        val fakeSesionDao = FakeSesionDao()

        // Guardamos una sesión activa en Room que apunta a un usuario que no existe en emprendedoras
        fakeSesionDao.guardarSesion(
            SesionActivaEntity(
                id = 1,
                rol = Rol.EMPRENDEDORA,
                usuarioId = 999,
                correo = "eliminada@correo.cl"
            )
        )

        val empRepo = EmprendedoraRepositoryRoom(fakeEmpDao)
        val clienteRepo = ClienteRepositoryRoom(fakeClienteDao)
        val sesionRepo = SesionRepositoryRoom(
            sesionDao = fakeSesionDao,
            emprendedoraDao = fakeEmpDao,
            clienteDao = fakeClienteDao,
            emprendedoraRepository = empRepo,
            clienteRepository = clienteRepo,
            scope = this
        )

        // Al restaurar sesión, detecta que la emprendedora ya no existe en la base de datos y limpia la sesión
        sesionRepo.restaurarSesionDesdeBaseDeDatos()
        assertEquals(EstadoSesion.NoAutenticado, sesionRepo.estadoSesion.value)
        assertTrue(fakeSesionDao.obtenerSesionDirecta() == null)
    }

    @Test
    fun productoRepositoryRoom_noMezclaProductosEntrePerfiles() = runTest {
        val fakeProdDao = FakeProductoDao()
        val repo = ProductoRepositoryRoom(fakeProdDao)

        // Producto de la emprendedora 1
        val prod1 = Producto(
            id = 0,
            emprendedoraId = 1,
            nombre = "Bolso Emprendedora 1",
            descripcionCorta = "Confeccionado por el perfil 1"
        )
        // Producto de la emprendedora 2
        val prod2 = Producto(
            id = 0,
            emprendedoraId = 2,
            nombre = "Bolso Emprendedora 2",
            descripcionCorta = "Confeccionado por el perfil 2"
        )

        repo.guardar(prod1)
        repo.guardar(prod2)

        val prodsEmp1 = repo.obtenerPorEmprendedora(1).first()
        val prodsEmp2 = repo.obtenerPorEmprendedora(2).first()

        assertEquals(1, prodsEmp1.size)
        assertEquals("Bolso Emprendedora 1", prodsEmp1.first().nombre)
        assertEquals(1, prodsEmp1.first().emprendedoraId)

        assertEquals(1, prodsEmp2.size)
        assertEquals("Bolso Emprendedora 2", prodsEmp2.first().nombre)
        assertEquals(2, prodsEmp2.first().emprendedoraId)
    }

    @Test
    fun mensajeRepositoryRoom_mantieneRelacionEntreClienteEmprendedoraYProducto() = runTest {
        val fakeMensajeDao = FakeMensajeDao()
        val fakeProdDao = FakeProductoDao()
        val repo = MensajeRepositoryRoom(fakeMensajeDao, fakeProdDao)

        val prod = Producto(
            id = 10,
            emprendedoraId = 2,
            nombre = "Falda Denim",
            descripcionCorta = "Falda reciclada"
        )

        // Inicio de conversación
        val res = repo.iniciarOObtenerConversacion(
            clienteId = 1,
            emprendedoraId = 2,
            nombreCliente = "Francisca Tapia",
            nombreEmprendimiento = "Renace Ropa Vintage",
            primerMensaje = "Hola, me interesa este producto",
            emisor = TipoEmisor.CLIENTE,
            productoAsociado = prod
        )
        assertTrue(res is Resultado.Exito)

        val convId = (res as Resultado.Exito).datos.id
        val mensajes = repo.obtenerMensajes(convId).first()
        assertEquals(1, mensajes.size)
        assertEquals("Hola, me interesa este producto", mensajes.first().contenido)
        assertEquals(10, mensajes.first().productoId)
        assertEquals("Falda Denim", mensajes.first().nombreProducto)
    }
}
