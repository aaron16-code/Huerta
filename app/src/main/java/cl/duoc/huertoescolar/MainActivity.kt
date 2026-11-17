package cl.duoc.huertoescolar

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.navigation.Navegacion
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme

class MainActivity : ComponentActivity() {

    private val solicitarPermisoRedLocal = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        // Si se concede, la consulta puede repetirse desde "Reintentar".
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RepositorioHuerto.inicializar(applicationContext)
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

        comprobarPermisoRedLocal()
    }

    private fun comprobarPermisoRedLocal() {
        if (Build.VERSION.SDK_INT < 37) return

        val permiso = "android.permission.ACCESS_LOCAL_NETWORK"

        if (
            ContextCompat.checkSelfPermission(this, permiso) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisoRedLocal.launch(permiso)
        }
    }
}