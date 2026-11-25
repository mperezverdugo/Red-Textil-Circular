package com.example.textilcircular.ui.screens.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.textilcircular.R
import com.example.textilcircular.ui.components.AvatarUsuario
import com.example.textilcircular.ui.components.BotonPrincipal
import com.example.textilcircular.ui.components.BotonSecundario
import com.example.textilcircular.ui.components.fondoTramaTextil
import com.example.textilcircular.ui.theme.AmarilloAcento
import com.example.textilcircular.ui.theme.FormaPildora
import com.example.textilcircular.ui.theme.TextilCircularTheme
import com.example.textilcircular.ui.theme.TurquesaFundacion

// Pantalla de bienvenida con diseño editorial de moda sustentable.
@Composable
fun BienvenidaScreen(
    onSoyEmprendedoraClick: () -> Unit,
    onSoyClienteClick: () -> Unit,
    onIniciarSesionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .fondoTramaTextil(alpha = 0.05f)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabecera editorial institucional
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            // Chip de categoría editorial estilo referencia
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, FormaPildora)
                    .border(1.dp, AmarilloAcento.copy(alpha = 0.4f), FormaPildora)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍃 Moda Circular · Upcycling",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Avatar vectorial personalizado (Opción B) con aros decorativos
            AvatarUsuario(
                drawableRes = R.drawable.ic_avatar_taller,
                tamano = 104.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.fundacion_nombre),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AmarilloAcento,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.bienvenida_titulo),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.bienvenida_bajada),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Acciones principales con jerarquía clara y botones en píldora
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BotonPrincipal(
                texto = stringResource(id = R.string.bienvenida_boton_soy_cliente),
                onClick = onSoyClienteClick
            )

            BotonSecundario(
                texto = stringResource(id = R.string.bienvenida_boton_soy_emprendedora),
                onClick = onSoyEmprendedoraClick
            )

            BotonSecundario(
                texto = stringResource(id = R.string.bienvenida_boton_iniciar_sesion),
                onClick = onIniciarSesionClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BienvenidaScreenPreview() {
    TextilCircularTheme {
        BienvenidaScreen(
            onSoyEmprendedoraClick = {},
            onSoyClienteClick = {},
            onIniciarSesionClick = {}
        )
    }
}
