package com.example.textilcircular.util.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.example.textilcircular.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.math.roundToInt

// Utilidad para compresión y normalización de imágenes de la cámara y galería.
object CompresorImagen {

    const val MAX_DIMENSION_PX = 1280
    const val CALIDAD_COMPRESION_JPEG = 80

    suspend fun procesarYGuardarFoto(
        context: Context,
        origenUri: Uri,
        borrarOrigenSiEsArchivo: Boolean = false
    ): Resultado<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // 1. Obtener la rotación EXIF de la imagen
            var rotacionGrados = 0
            contentResolver.openInputStream(origenUri)?.use { stream ->
                rotacionGrados = obtenerRotacionExif(stream)
            }

            // 2. Leer dimensiones originales sin cargar todo el mapa de bits en memoria
            val opcionesMedidas = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(origenUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opcionesMedidas)
            }

            val anchoOriginal = opcionesMedidas.outWidth
            val altoOriginal = opcionesMedidas.outHeight
            if (anchoOriginal <= 0 || altoOriginal <= 0) {
                return@withContext Resultado.Error("No se pudieron leer las dimensiones de la imagen")
            }

            // 3. Calcular factor de submuestreo para optimizar consumo de RAM
            val ladoMayor = maxOf(anchoOriginal, altoOriginal)
            var sampleSize = 1
            while ((ladoMayor / sampleSize) > (MAX_DIMENSION_PX * 2)) {
                sampleSize *= 2
            }

            val opcionesDecodificacion = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            val bitmapDecodificado = contentResolver.openInputStream(origenUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opcionesDecodificacion)
            } ?: return@withContext Resultado.Error("No se pudo decodificar la imagen")

            // 4. Escalar si supera la dimensión máxima
            val anchoActual = bitmapDecodificado.width
            val altoActual = bitmapDecodificado.height
            val ladoMayorActual = maxOf(anchoActual, altoActual)

            val bitmapEscalado = if (ladoMayorActual > MAX_DIMENSION_PX) {
                val ratio = MAX_DIMENSION_PX.toFloat() / ladoMayorActual
                val nuevoAncho = (anchoActual * ratio).roundToInt()
                val nuevoAlto = (altoActual * ratio).roundToInt()
                Bitmap.createScaledBitmap(bitmapDecodificado, nuevoAncho, nuevoAlto, true)
            } else {
                bitmapDecodificado
            }

            // 5. Corregir rotación EXIF si es necesario
            val bitmapFinal = if (rotacionGrados != 0) {
                val matriz = Matrix().apply { postRotate(rotacionGrados.toFloat()) }
                Bitmap.createBitmap(
                    bitmapEscalado,
                    0,
                    0,
                    bitmapEscalado.width,
                    bitmapEscalado.height,
                    matriz,
                    true
                )
            } else {
                bitmapEscalado
            }

            // 6. Guardar en almacenamiento privado de la app
            val carpetaDestino = File(context.filesDir, "fotos").apply {
                if (!exists()) mkdirs()
            }
            val nombreArchivo = "foto_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val archivoDestino = File(carpetaDestino, nombreArchivo)

            FileOutputStream(archivoDestino).use { outStream ->
                bitmapFinal.compress(Bitmap.CompressFormat.JPEG, CALIDAD_COMPRESION_JPEG, outStream)
            }

            // Liberar memoria si se crearon bitmaps intermedios
            if (bitmapFinal !== bitmapEscalado && !bitmapEscalado.isRecycled) {
                bitmapEscalado.recycle()
            }
            if (bitmapEscalado !== bitmapDecodificado && !bitmapDecodificado.isRecycled) {
                bitmapDecodificado.recycle()
            }

            // 7. Borrar archivo temporal de origen si aplica
            if (borrarOrigenSiEsArchivo && origenUri.scheme == "file") {
                origenUri.path?.let { ruta ->
                    val f = File(ruta)
                    if (f.exists()) f.delete()
                }
            }

            Resultado.Exito(archivoDestino.absolutePath)
        } catch (e: Exception) {
            Resultado.Error("Error al procesar la imagen: ${e.localizedMessage ?: "desconocido"}")
        }
    }

    private fun obtenerRotacionExif(stream: InputStream): Int {
        return try {
            val exif = ExifInterface(stream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {
            0
        }
    }
}
