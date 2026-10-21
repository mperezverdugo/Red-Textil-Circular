package com.example.textilcircular.util.validacion

// Validador y formateador de RUT chileno usando el algoritmo de Módulo 11.
object ValidadorRut {

    fun limpiar(rut: String): String {
        return rut.replace(".", "")
            .replace("-", "")
            .trim()
            .uppercase()
    }

    fun calcularDigitoVerificador(cuerpoStr: String): Char {
        var suma = 0
        var multiplicador = 2
        for (i in cuerpoStr.length - 1 downTo 0) {
            suma += (cuerpoStr[i] - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = 11 - (suma % 11)
        return when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + resto)
        }
    }

    fun validar(rut: String): ResultadoValidacion {
        val limpio = limpiar(rut)
        if (limpio.isBlank()) {
            return ResultadoValidacion.error("El RUT no puede estar vacío")
        }

        if (limpio.length < 8 || limpio.length > 9) {
            return ResultadoValidacion.error("El RUT debe tener entre 7 y 8 dígitos más el dígito verificador")
        }

        val cuerpoStr = limpio.substring(0, limpio.length - 1)
        val dv = limpio.last()

        if (!cuerpoStr.all { it.isDigit() }) {
            return ResultadoValidacion.error("El cuerpo del RUT solo debe contener números")
        }

        val cuerpoNumero = cuerpoStr.toLongOrNull()
        if (cuerpoNumero == null || cuerpoNumero <= 0) {
            return ResultadoValidacion.error("El número de RUT no es válido")
        }

        if (dv !in '0'..'9' && dv != 'K') {
            return ResultadoValidacion.error("El dígito verificador debe ser un número o la letra K")
        }

        val dvEsperado = calcularDigitoVerificador(cuerpoStr)
        return if (dv == dvEsperado) {
            ResultadoValidacion.ok()
        } else {
            ResultadoValidacion.error("RUT inválido")
        }
    }

    fun formatear(rut: String): String {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return rut

        val cuerpo = limpio.substring(0, limpio.length - 1)
        val dv = limpio.last()

        val builder = StringBuilder()
        var contador = 0
        for (i in cuerpo.length - 1 downTo 0) {
            builder.append(cuerpo[i])
            contador++
            if (contador == 3 && i > 0) {
                builder.append('.')
                contador = 0
            }
        }
        return "${builder.reverse()}-$dv"
    }
}
