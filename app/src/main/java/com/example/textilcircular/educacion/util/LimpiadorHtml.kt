package com.example.textilcircular.educacion.util

// Utilidad pura para sanitizar y remover etiquetas o entidades HTML de los extractos devueltos por Wikipedia.
object LimpiadorHtml {

    private val REGEX_TAGS_HTML = Regex("<[^>]*>")
    private val REGEX_ESPACIOS_MULTIPLIES = Regex("\\s+")

    fun limpiar(html: String?): String {
        if (html.isNullOrBlank()) return ""

        // 1. Eliminar etiquetas HTML como <p>, <b>, <span>, <i>, etc.
        var texto = REGEX_TAGS_HTML.replace(html, "")

        // 2. Reemplazar entidades HTML comunes
        texto = texto
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&aacute;", "á")
            .replace("&eacute;", "é")
            .replace("&iacute;", "í")
            .replace("&oacute;", "ó")
            .replace("&uacute;", "ú")
            .replace("&ntilde;", "ñ")
            .replace("&Aacute;", "Á")
            .replace("&Eacute;", "É")
            .replace("&Iacute;", "Í")
            .replace("&Oacute;", "Ó")
            .replace("&Uacute;", "Ú")
            .replace("&Ntilde;", "Ñ")

        // 3. Normalizar saltos de línea y espacios en blanco redundantes
        return REGEX_ESPACIOS_MULTIPLIES.replace(texto, " ").trim()
    }
}
