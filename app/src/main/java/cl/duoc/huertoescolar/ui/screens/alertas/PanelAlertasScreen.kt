package cl.duoc.huertoescolar.ui.screens.alertas

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

/** Alertas y recordatorios. Son solo informativos: la app no activa ningún dispositivo. */
@Composable
fun PanelAlertasScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: AlertasViewModel = viewModel()
) {
    val tareas by viewModel.tareas.collectAsState()
    val problemas by viewModel.problemas.collectAsState()
    val lecturas by viewModel.lecturas.collectAsState()
    val secciones = viewModel.secciones(tareas, problemas, lecturas, usuario, nombreSector)

    PantallaHuerto(
        titulo = "Alertas",
        subtitulo = "Recordatorios informativos. La app no activa ningún dispositivo.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        secciones.forEach { seccion ->
            TarjetaHuerto {
                Text(seccion.titulo, style = MaterialTheme.typography.titleMedium)
                if (seccion.lineas.isEmpty()) {
                    Text(
                        text = seccion.textoVacio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    EtiquetaEstado(
                        texto = if (seccion.lineas.size == 1) "1 aviso" else "${seccion.lineas.size} avisos",
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    seccion.lineas.forEach { linea ->
                        Text("• $linea", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}