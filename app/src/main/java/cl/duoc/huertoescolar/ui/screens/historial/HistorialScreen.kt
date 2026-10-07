package cl.duoc.huertoescolar.ui.screens.historial

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun HistorialScreen(
    onVolver: () -> Unit
) {
    PantallaHuerto(
        titulo = "Historial",
        subtitulo = "Registro reciente de actividades del huerto",
        onVolver = onVolver
    ) {
        TarjetaHuerto {
            Text(
                text = "Cultivo actualizado",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = "Hoy")

            Text("Se actualizó el estado del cultivo Lechugas.")
            Text("Responsable: Docente de prueba")
        }

        TarjetaHuerto {
            Text(
                text = "Tarea enviada",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = "Ayer")

            Text("La tarea Regar las lechugas fue enviada a validación.")
            Text("Responsable: Estudiante de prueba")
        }
    }
}