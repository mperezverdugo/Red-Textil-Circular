package com.example.textilcircular.util

/**
 * Constantes y reglas de negocio del sistema Textil Circular.
 */
object Constantes {
    /**
     * Límite máximo de rubros permitidos por emprendedora (Requisito RF-04).
     */
    const val MAX_RUBROS = 2

    /**
     * Límite provisorio de fotografías permitidas por producto (Requisito RF-15).
     * TODO(confirmar con la Fundación): Definir el límite máximo final de fotos por producto.
     */
    const val MAX_FOTOS_POR_PRODUCTO = 5

    /**
     * Cantidad de pasos en el formulario de registro guiado (RF-01).
     */
    const val TOTAL_PASOS_REGISTRO = 4
}
