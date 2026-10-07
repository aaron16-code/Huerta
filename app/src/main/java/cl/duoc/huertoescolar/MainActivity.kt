package cl.duoc.huertoescolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HuertoEscolarTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Navegacion(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
