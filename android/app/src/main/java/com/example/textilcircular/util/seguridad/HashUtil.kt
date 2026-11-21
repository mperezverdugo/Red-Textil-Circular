package com.example.textilcircular.util.seguridad

import java.security.MessageDigest
import java.security.SecureRandom

// Herramienta para proteger contraseñas generando una sal aleatoria y un hash SHA-256.
object HashUtil {

    // Genera una sal aleatoria en formato hexadecimal para reforzar la seguridad de la clave.
    fun generarSal(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Calcula el hash seguro combinando la contraseña con su respectiva sal.
    fun generarHash(password: String, sal: String): String {
        val entrada = password + sal
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(entrada.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Comprueba si una contraseña ingresada coincide con el hash almacenado.
    fun verificarPassword(password: String, sal: String, hashEsperado: String): Boolean {
        val hashGenerado = generarHash(password, sal)
        return hashGenerado.equals(hashEsperado, ignoreCase = true)
    }
}
