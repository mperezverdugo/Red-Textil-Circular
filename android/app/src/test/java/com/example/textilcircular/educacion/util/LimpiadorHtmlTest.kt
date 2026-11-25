package com.example.textilcircular.educacion.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LimpiadorHtmlTest {

    @Test
    fun limpiar_textoConEtiquetasParrafoYNegrita_retornaTextoLimpio() {
        val entrada = "<p>El <b>reciclaje textil</b> es el método de reutilización.</p>"
        val esperado = "El reciclaje textil es el método de reutilización."
        val resultado = LimpiadorHtml.limpiar(entrada)
        assertEquals(esperado, resultado)
    }

    @Test
    fun limpiar_textoConEntidadesAcentuadasYComillas_decodificaCorrectamente() {
        val entrada = "La &quot;econom&iacute;a circular&quot; busca reducir la contaminaci&oacute;n."
        val esperado = "La \"economía circular\" busca reducir la contaminación."
        val resultado = LimpiadorHtml.limpiar(entrada)
        assertEquals(esperado, resultado)
    }

    @Test
    fun limpiar_textoNuloOVacio_retornaCadenaVacia() {
        assertEquals("", LimpiadorHtml.limpiar(null))
        assertEquals("", LimpiadorHtml.limpiar(""))
        assertEquals("", LimpiadorHtml.limpiar("   "))
    }

    @Test
    fun limpiar_textoConEspaciosYTabulacionesMultiples_normalizaEspacios() {
        val entrada = "   Texto    con      espacios \n  múltiples   "
        val esperado = "Texto con espacios múltiples"
        val resultado = LimpiadorHtml.limpiar(entrada)
        assertEquals(esperado, resultado)
    }

    @Test
    fun limpiar_extractoRealWikipediaConSpans_retornaTextoLegible() {
        val entrada = "<p>El <span class=\"mw-page-title-main\"><b>suprarreciclaje</b></span> o <i>upcycling</i> es clave.</p>"
        val esperado = "El suprarreciclaje o upcycling es clave."
        val resultado = LimpiadorHtml.limpiar(entrada)
        assertEquals(esperado, resultado)
    }
}
