package com.example.textilcircular

import com.example.textilcircular.data.repository.AdminRepositoryMemoria
import com.example.textilcircular.data.repository.EmprendedoraRepositoryMemoria
import com.example.textilcircular.data.repository.ProductoRepositoryMemoria
import com.example.textilcircular.ui.preview.DatosDemo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Pruebas unitarias para la visualización del perfil de emprendedora y detalle de productos desde la perspectiva del cliente.
class PerfilClienteMercadoTest {

    private lateinit var emprendedoraRepo: EmprendedoraRepositoryMemoria
    private lateinit var productoRepo: ProductoRepositoryMemoria
    private lateinit var adminRepo: AdminRepositoryMemoria

    @Before
    fun setUp() {
        emprendedoraRepo = EmprendedoraRepositoryMemoria()
        productoRepo = ProductoRepositoryMemoria()
        adminRepo = AdminRepositoryMemoria(emprendedoraRepo)
    }

    @Test
    fun perfilEmprendedora_cargaDatosYProductosCorrectos() = runTest {
        val emprendedoraId = 1
        val emp = emprendedoraRepo.obtenerPorId(emprendedoraId).first()
        assertNotNull(emp)
        assertEquals("Taller Hilos del Sur", emp?.nombreComercial)
        assertEquals("Valparaíso", emp?.region)
        assertEquals("Valparaíso", emp?.comuna)

        // El cliente puede ver el catálogo completo de creaciones de esta emprendedora
        val productos = productoRepo.obtenerPorEmprendedora(emprendedoraId).first()
        assertTrue(productos.isNotEmpty())
        assertEquals(3, productos.size)
        assertTrue(productos.all { it.emprendedoraId == emprendedoraId })
    }

    @Test
    fun contactoCliente_registraClicEnMetricasAdmin() = runTest {
        val metricasIniciales = adminRepo.obtenerMetricas().first()
        val clicsIniciales = metricasIniciales.totalClics

        // Simula la acción de pulsar "Escribirle" o "Preguntar por este producto"
        val region = "Valparaíso"
        val rubro = "Suprarreciclaje"
        adminRepo.registrarClicContacto(region, rubro)

        val metricasFinales = adminRepo.obtenerMetricas().first()
        assertEquals(clicsIniciales + 1, metricasFinales.totalClics)

        // Verifica que la región y rubro se contabilizaron
        val contactoRegion = metricasFinales.contactosPorRegion.find { it.first == region }
        val contactoRubro = metricasFinales.contactosPorRubro.find { it.first == rubro }
        assertNotNull(contactoRegion)
        assertNotNull(contactoRubro)
    }

    @Test
    fun detalleProducto_obtieneProductoYDatosDeEmprendedora() = runTest {
        val productoId = 101
        val producto = productoRepo.obtenerPorId(productoId).first()
        assertNotNull(producto)
        assertEquals("Bolso Upcycling Denim", producto?.nombre)

        // Conectamos con la emprendedora correspondiente
        val emp = emprendedoraRepo.obtenerPorId(producto!!.emprendedoraId).first()
        assertNotNull(emp)
        assertEquals("Taller Hilos del Sur", emp?.nombreComercial)
    }
}
