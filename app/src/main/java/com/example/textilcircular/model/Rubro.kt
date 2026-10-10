package com.example.textilcircular.model

/**
 * Enum que representa los rubros de economía circular textil reconocidos por la Fundación.
 *
 * Requisito RF-04: La emprendedora puede elegir hasta 2 rubros entre las categorías disponibles.
 * TODO(confirmar con la Fundación): En los requisitos se mencionan 7 categorías de economía circular textil,
 * pero en las reuniones solo se detallaron 5 (Suprarreciclaje, Reparación, Segunda mano, Arriendo y Talleres).
 * Faltan 2 de las 7 categorías por confirmar oficialmente.
 */
enum class Rubro(val nombre: String) {
    SUPRARRECICLAJE("Suprarreciclaje"),
    REPARACION("Reparación"),
    SEGUNDA_MANO("Segunda mano"),
    ARRIENDO("Arriendo"),
    TALLERES("Talleres")
}
