package com.example.textilcircular.educacion.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.textilcircular.educacion.model.ArticuloEducativo
import com.example.textilcircular.ui.theme.GrisFondo
import com.example.textilcircular.ui.theme.GrisTextoSecundario
import com.example.textilcircular.ui.theme.NegroTexto
import com.example.textilcircular.ui.theme.RojoError
import com.example.textilcircular.ui.theme.RojoErrorFondo
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaClaro
import com.example.textilcircular.ui.theme.TurquesaFundacion
import com.example.textilcircular.ui.theme.TurquesaOscuro

// Componente visual autónomo en Jetpack Compose que consume la API externa de Wikipedia.
@Composable
fun SeccionEducacionWikipedia(
    modifier: Modifier = Modifier,
    viewModel: EducacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GrisFondo)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Encabezado pedagógico alineado a la identidad de Red Textil Circular
        Text(
            text = "Aprende sobre Textil Circular",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TurquesaOscuro
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Información educativa en vivo extraída de Wikipedia en español para fomentar la economía circular y el reciclaje textil.",
            style = MaterialTheme.typography.bodyMedium,
            color = GrisTextoSecundario
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (val estado = uiState) {
            is EducacionUiState.Cargando -> {
                EstadoCargandoEducacion()
            }
            is EducacionUiState.Error -> {
                EstadoErrorEducacion(
                    mensaje = estado.mensaje,
                    onReintentar = { viewModel.reintentar() }
                )
            }
            is EducacionUiState.Exito -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.articulos, key = { it.clave }) { articulo ->
                        TarjetaArticuloEducativo(articulo = articulo)
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// Tarjeta individual para cada artículo temático de Wikipedia
@Composable
fun TarjetaArticuloEducativo(
    articulo: ArticuloEducativo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chip temático
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TurquesaClaro)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = articulo.etiquetaTematica,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TurquesaOscuro
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Título del artículo
            Text(
                text = articulo.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NegroTexto
            )

            // Imagen opcional desde Wikimedia vía Coil
            if (!articulo.imagenUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = articulo.imagenUrl,
                    contentDescription = articulo.titulo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TurquesaClaro.copy(alpha = 0.3f)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Extracto depurado y sanitizado
            Text(
                text = articulo.extracto,
                style = MaterialTheme.typography.bodyMedium,
                color = GrisTextoSecundario,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Crédito obligatorio a la fuente
            Text(
                text = "Fuente: ${articulo.fuente}",
                style = MaterialTheme.typography.labelSmall,
                color = GrisTextoSecundario.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Botón para abrir el artículo original en el navegador del dispositivo
            OutlinedButton(
                onClick = { abrirEnlaceEnNavegador(context, articulo.urlOriginal) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TurquesaFundacion)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Leer artículo completo en Wikipedia",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Vista de carga reactiva
@Composable
private fun EstadoCargandoEducacion() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = TurquesaFundacion)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Conectando con la API de Wikipedia...",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisTextoSecundario
            )
        }
    }
}

// Vista de error con botón de reintento
@Composable
private fun EstadoErrorEducacion(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RojoErrorFondo)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = RojoError,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No se pudo obtener el contenido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RojoError
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = NegroTexto
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onReintentar,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TurquesaFundacion)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Reintentar conexión")
            }
        }
    }
}

private fun abrirEnlaceEnNavegador(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No se pudo abrir el navegador web", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun TarjetaArticuloEducativoPreview() {
    TextilCircularTheme {
        TarjetaArticuloEducativo(
            articulo = ArticuloEducativo(
                clave = "Reciclaje_textil",
                titulo = "Reciclaje textil",
                extracto = "El reciclaje textil es el método de reutilización o reprocesamiento de ropa usada, material fibroso y restos de ropa del proceso de fabricación.",
                imagenUrl = null,
                urlOriginal = "https://es.wikipedia.org/wiki/Reciclaje_textil",
                etiquetaTematica = "Reciclaje y Gestión de Residuos"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
