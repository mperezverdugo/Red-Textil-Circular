package com.example.textilcircular

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.textilcircular.ui.navigation.NavHostApp
import com.example.textilcircular.ui.theme.TextilCircularTheme

// Pantalla principal que carga el tema y el navegador de la aplicación.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextilCircularTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.textilcircular.ui.theme.GrisFondo
                ) {
                    NavHostApp()
                }
            }
        }
    }
}
