package com.example.textilcircular.educacion.remote

import com.google.gson.annotations.SerializedName

// Respuesta simplificada y tipada del endpoint /page/summary/{title} de Wikipedia en español.
data class WikipediaSummaryDto(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("displaytitle")
    val displayTitle: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("extract")
    val extract: String? = null,

    @SerializedName("extract_html")
    val extractHtml: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: WikipediaThumbnailDto? = null,

    @SerializedName("originalimage")
    val originalImage: WikipediaThumbnailDto? = null,

    @SerializedName("content_urls")
    val contentUrls: WikipediaContentUrlsDto? = null
)

data class WikipediaThumbnailDto(
    @SerializedName("source")
    val source: String? = null,

    @SerializedName("width")
    val width: Int? = null,

    @SerializedName("height")
    val height: Int? = null
)

data class WikipediaContentUrlsDto(
    @SerializedName("desktop")
    val desktop: WikipediaPageUrlDto? = null,

    @SerializedName("mobile")
    val mobile: WikipediaPageUrlDto? = null
)

data class WikipediaPageUrlDto(
    @SerializedName("page")
    val page: String? = null
)
