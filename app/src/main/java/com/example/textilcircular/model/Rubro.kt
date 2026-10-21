package com.example.textilcircular.model

// Rubros textiles que puede elegir una emprendedora.
// TODO: consultar con la Fundación cuáles son los 2 rubros que faltan para completar los 7.
enum class Rubro(val nombre: String) {
    SUPRARRECICLAJE("Suprarreciclaje"),
    REPARACION("Reparación"),
    SEGUNDA_MANO("Segunda mano"),
    ARRIENDO("Arriendo"),
    TALLERES("Talleres")
}
