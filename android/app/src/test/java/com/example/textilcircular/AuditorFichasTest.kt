package com.example.textilcircular

import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.ui.preview.DatosDemo
import com.example.textilcircular.util.validacion.AuditorFichas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuditorFichasTest {

    @Test
    fun auditorFichas_casoCaptura_retornaExactamenteCuatroProblemas() {
        val fichaConProblemas = Emprendedora(
            id = 99,
            rut = "2145215211",
            nombre = "fasfafs",
            nombreComercial = "asdasda",
            region = "Libertador General Bernardo O'Higgins",
            comuna = "Rengo",
            rubros = emptyList(),
            email = "asdad",
            instagram = null,
            activa = true
        )

        val problemas = AuditorFichas.auditar(fichaConProblemas)

        assertEquals(4, problemas.size)
        assertTrue(problemas.any { it.contains("RUT", ignoreCase = true) })
        assertTrue(problemas.any { it.contains("titular", ignoreCase = true) || it.contains("palabra", ignoreCase = true) })
        assertTrue(problemas.any { it.contains("correo", ignoreCase = true) })
        assertTrue(problemas.any { it.contains("rubro", ignoreCase = true) })
    }

    @Test
    fun auditorFichas_emprendedoraDemo_sinProblemas() {
        val problemas = AuditorFichas.auditar(DatosDemo.EmprendedoraDemo)
        assertTrue("La ficha demo debería ser 100% válida", problemas.isEmpty())
    }

    @Test
    fun auditorFichas_soloRutInvalido() {
        val ficha = DatosDemo.EmprendedoraDemo.copy(rut = "11.111.111-0")
        val problemas = AuditorFichas.auditar(ficha)

        assertEquals(1, problemas.size)
        assertTrue(problemas[0].contains("RUT", ignoreCase = true))
    }

    @Test
    fun auditorFichas_soloTitularUnaPalabra() {
        val ficha = DatosDemo.EmprendedoraDemo.copy(nombre = "Camila")
        val problemas = AuditorFichas.auditar(ficha)

        assertEquals(1, problemas.size)
        assertTrue(problemas[0].contains("titular", ignoreCase = true) || problemas[0].contains("palabras", ignoreCase = true))
    }

    @Test
    fun auditorFichas_soloEmailInvalido() {
        val ficha = DatosDemo.EmprendedoraDemo.copy(email = "correo_invalido_sin_formato")
        val problemas = AuditorFichas.auditar(ficha)

        assertEquals(1, problemas.size)
        assertTrue(problemas[0].contains("correo", ignoreCase = true))
    }

    @Test
    fun auditorFichas_soloSinRubros() {
        val ficha = DatosDemo.EmprendedoraDemo.copy(rubros = emptyList())
        val problemas = AuditorFichas.auditar(ficha)

        assertEquals(1, problemas.size)
        assertTrue(problemas[0].contains("rubros", ignoreCase = true))
    }

    @Test
    fun auditorFichas_soloComunaNoCorrespondeARegion() {
        // Valparaíso no tiene comuna Rengo (Rengo pertenece a O'Higgins)
        val ficha = DatosDemo.EmprendedoraDemo.copy(
            region = "Valparaíso",
            comuna = "Rengo"
        )
        val problemas = AuditorFichas.auditar(ficha)

        assertEquals(1, problemas.size)
        assertTrue(problemas[0].contains("comuna", ignoreCase = true))
    }
}
