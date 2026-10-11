package com.example.textilcircular

import com.example.textilcircular.model.Emprendedora
import com.example.textilcircular.model.Rubro
import com.example.textilcircular.util.validacion.AuditorFichas
import com.example.textilcircular.util.validacion.ValidadorChat
import com.example.textilcircular.util.validacion.ValidadorContacto
import com.example.textilcircular.util.validacion.ValidadorContrasena
import com.example.textilcircular.util.validacion.ValidadorProducto
import com.example.textilcircular.util.validacion.ValidadorRut
import com.example.textilcircular.util.validacion.ValidadorTexto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadoresTest {

    @Test
    fun validadorTexto_reconoceVaciosYLongitudes() {
        val resVacio = ValidadorTexto.validarNoVacio("   ", "Nombre")
        assertFalse(resVacio.esValido)

        val resOk = ValidadorTexto.validarNoVacio("Textil Circular", "Nombre")
        assertTrue(resOk.esValido)

        val resCorta = ValidadorTexto.validarLongitud("ab", 3, 10, "Texto")
        assertFalse(resCorta.esValido)

        val resLarga = ValidadorTexto.validarLongitud("0123456789a", 3, 10, "Texto")
        assertFalse(resLarga.esValido)
    }

    @Test
    fun validadorRut_validaRutCorrecto() {
        // RUT con puntos y guión
        val res1 = ValidadorRut.validar("12.345.678-5")
        assertTrue(res1.esValido)

        // RUT sin formato
        val res2 = ValidadorRut.validar("123456785")
        assertTrue(res2.esValido)

        // RUT con dígito verificador calculado
        val cuerpoTest = "11111111"
        val dv = ValidadorRut.calcularDigitoVerificador(cuerpoTest)
        val res3 = ValidadorRut.validar("$cuerpoTest-$dv")
        assertTrue(res3.esValido)
    }

    @Test
    fun validadorRut_rechazaRutInvalido() {
        // Dígito verificador erróneo
        val resDvMalo = ValidadorRut.validar("12.345.678-9")
        assertFalse(resDvMalo.esValido)

        // RUT con letras en el cuerpo
        val resLetras = ValidadorRut.validar("12A45678-5")
        assertFalse(resLetras.esValido)

        // RUT vacío o muy corto
        assertFalse(ValidadorRut.validar("").esValido)
        assertFalse(ValidadorRut.validar("123-4").esValido)
    }

    @Test
    fun validadorRut_formateaCorrectamente() {
        val formateado = ValidadorRut.formatear("123456785")
        assertEquals("12.345.678-5", formateado)
    }

    @Test
    fun validadorContacto_validaEmail() {
        assertTrue(ValidadorContacto.validarEmail("contacto@textilcircular.cl").esValido)
        assertTrue(ValidadorContacto.validarEmail("ana.perez@duocuc.cl").esValido)

        assertFalse(ValidadorContacto.validarEmail("").esValido)
        assertFalse(ValidadorContacto.validarEmail("correo-sin-arroba").esValido)
        assertFalse(ValidadorContacto.validarEmail("ana@dominio").esValido)
    }

    @Test
    fun validadorContacto_validaInstagram() {
        assertTrue(ValidadorContacto.validarInstagram("@taller_circular").esValido)
        assertTrue(ValidadorContacto.validarInstagram("telar.reciclado").esValido)
        assertTrue(ValidadorContacto.validarInstagram(null).esValido)

        assertFalse(ValidadorContacto.validarInstagram("cuenta con espacios").esValido)
    }

    @Test
    fun validadorContrasena_validaSeguridadYCoincidencia() {
        assertTrue(ValidadorContrasena.validar("123456").esValido)
        assertFalse(ValidadorContrasena.validar("12345").esValido)

        assertTrue(ValidadorContrasena.validarCoincidencia("segura123", "segura123").esValido)
        assertFalse(ValidadorContrasena.validarCoincidencia("segura123", "distinta").esValido)
    }

    @Test
    fun validadorProducto_compruebaLimites() {
        val resValido = ValidadorProducto.validarFormularioCompleto(
            nombre = "Estuche de Mezclilla",
            descripcion = "Confeccionado a mano reutilizando retazos de jeans",
            fotos = listOf("foto1.jpg")
        )
        assertTrue(resValido.esValido)

        val resNombreCorto = ValidadorProducto.validarNombre("Ab")
        assertFalse(resNombreCorto.esValido)

        val resDescCorta = ValidadorProducto.validarDescripcion("Muy corta")
        assertFalse(resDescCorta.esValido)

        val fotosExcesivas = listOf("1", "2", "3", "4", "5", "6")
        assertFalse(ValidadorProducto.validarFotos(fotosExcesivas).esValido)
    }

    @Test
    fun validadorChat_validaMensaje() {
        assertTrue(ValidadorChat.validarMensaje("Hola, ¿tienen stock disponible?").esValido)
        assertFalse(ValidadorChat.validarMensaje("   ").esValido)
    }

    @Test
    fun auditorFichas_detectaProblemas() {
        val emprendedoraValida = Emprendedora(
            id = 1,
            rut = "12.345.678-5",
            nombre = "Ana Morales",
            nombreComercial = "ReciclaTextil",
            region = "Metropolitana de Santiago",
            comuna = "Providencia",
            rubros = listOf(Rubro.SUPRARRECICLAJE),
            email = "contacto@reciclatextil.cl",
            instagram = "reciclatextil",
            activa = true
        )
        val problemasValida = AuditorFichas.auditar(emprendedoraValida)
        assertTrue(problemasValida.isEmpty())

        val emprendedoraConProblemas = Emprendedora(
            id = 2,
            rut = "1111-0", // RUT inválido
            nombre = "A",   // Nombre incompleto
            nombreComercial = "", // Vacío
            region = "",
            comuna = "",
            rubros = emptyList(), // Sin rubros
            email = "email_invalido",
            activa = false
        )
        val problemas = AuditorFichas.auditar(emprendedoraConProblemas)
        assertFalse(problemas.isEmpty())
        assertTrue(problemas.size >= 5)
    }
}
