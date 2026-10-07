package cl.duoc.huertoescolar.ui.screens.alertas

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun AlertasScreen(
    onVolver: () -> Unit
) {
    PantallaHuerto(
        titulo = "Alertas",
        subtitulo = "Avisos que requieren atención",
        onVolver = onVolver
    ) {
        TarjetaHuerto {
            Text(
                text = "Riego pendiente",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = "Importante")

            Text("Sector: Bancal 1")
            Text("Cultivo: Lechugas")
            Text("El cultivo necesita riego durante el día de hoy.")
        }

        TarjetaHuerto {
            Text(
                text = "Revisión de cultivo",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = "Informativa")

            Text("Se recomienda revisar el estado de las hojas.")
        }
    }
}