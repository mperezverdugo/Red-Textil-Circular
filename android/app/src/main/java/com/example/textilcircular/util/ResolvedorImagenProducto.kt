package com.example.textilcircular.util

import com.example.textilcircular.R
import com.example.textilcircular.model.Producto

/**
 * Resuelve imágenes visuales realistas del catálogo inicial de la Fundación Textil Circular
 * cuando no existe una foto local en disco. Asigna muestras textiles de alta fidelidad según
 * el rubro, nombre y perspectiva del producto.
 */
object ResolvedorImagenProducto {

    fun resolverDrawable(producto: Producto, indiceFoto: Int = 0): Int {
        val nombre = producto.nombre.lowercase()

        return when {
            // Bolso Upcycling Denim y accesorios afines
            nombre.contains("denim") || (nombre.contains("bolso") && nombre.contains("upcycling")) -> {
                when (indiceFoto) {
                    0 -> R.drawable.img_muestra_bolso_denim
                    1 -> R.drawable.img_muestra_bolso
                    else -> R.drawable.img_muestra_textil
                }
            }
            // Cojines, tapices y mantas de hogar
            nombre.contains("cojín") || nombre.contains("cojin") || nombre.contains("parche") || nombre.contains("manta") -> {
                when (indiceFoto) {
                    0 -> R.drawable.img_muestra_cojin
                    1 -> R.drawable.img_muestra_hogar
                    else -> R.drawable.img_muestra_textil
                }
            }
            // Chaquetas intervenidas y prendas bordadas
            nombre.contains("chaqueta") || nombre.contains("bordad") -> {
                when (indiceFoto) {
                    0 -> R.drawable.img_muestra_chaqueta
                    1 -> R.drawable.img_muestra_vestuario
                    else -> R.drawable.img_muestra_textil
                }
            }
            // Vestidos y prendas elegantes
            nombre.contains("vestido") || nombre.contains("seda") || nombre.contains("camisa") || nombre.contains("tapado") -> {
                when (indiceFoto) {
                    0 -> R.drawable.img_muestra_vestido
                    1 -> R.drawable.img_muestra_vestuario
                    else -> R.drawable.img_muestra_textil
                }
            }
            // Mochilas y estuches urbanos reciclados
            nombre.contains("mochila") || nombre.contains("estuche") -> {
                when (indiceFoto) {
                    0 -> R.drawable.img_muestra_mochila
                    else -> R.drawable.img_muestra_bolso_denim
                }
            }
            // Bolsos genéricos / totes
            nombre.contains("bolso") || nombre.contains("tote") -> {
                R.drawable.img_muestra_bolso_denim
            }
            // Vestuario / pantalones
            nombre.contains("pantalon") || nombre.contains("jeans") || nombre.contains("chaleco") || nombre.contains("gorro") -> {
                R.drawable.img_muestra_vestuario
            }
            // Hogar / fundas
            nombre.contains("hogar") || nombre.contains("funda") || nombre.contains("tapiz") -> {
                R.drawable.img_muestra_cojin
            }
            // Fallback determinístico por ID y por índice de foto
            else -> when ((Math.abs(producto.id) + indiceFoto) % 5) {
                0 -> R.drawable.img_muestra_bolso_denim
                1 -> R.drawable.img_muestra_chaqueta
                2 -> R.drawable.img_muestra_cojin
                3 -> R.drawable.img_muestra_vestido
                else -> R.drawable.img_muestra_mochila
            }
        }
    }

    fun resolverDrawablePorEmprendedora(emprendedoraId: Int): Int {
        return when (Math.abs(emprendedoraId) % 5) {
            0 -> R.drawable.img_muestra_bolso_denim
            1 -> R.drawable.img_muestra_chaqueta
            2 -> R.drawable.img_muestra_cojin
            3 -> R.drawable.img_muestra_vestido
            else -> R.drawable.img_muestra_mochila
        }
    }
}
