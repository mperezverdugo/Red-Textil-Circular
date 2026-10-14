package com.example.textilcircular.ui.screens.cliente

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.textilcircular.R
import com.example.textilcircular.ui.components.BarraSuperior

// Pantalla principal del mercado donde el cliente explora las emprendedoras.
@Composable
fun ExplorarScreen(
    onEmprendedoraClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(id = R.string.nav_cliente_explorar)
            )
        },
        modifier = modifier
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(id = R.string.nav_cliente_explorar))
        }
    }
}
