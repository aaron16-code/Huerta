package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun ValidarTareasScreen(
    onVolver: () -> Unit
) {
    PantallaHuerto(
        titulo = "Validar tareas",
        subtitulo = "Actividades enviadas por estudiantes",
        onVolver = onVolver
    ) {
        TarjetaHuerto {
            Text(
                text = "Regar las lechugas",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = "Pendiente de validación")

            Text("Sector: Bancal 1")
            Text("Estudiante: Estudiante de prueba")
            Text("Observación: Se realizó el riego solicitado.")

            BotonPrincipal(
                texto = "Aprobar tarea",
                onClick = { }
            )

            BotonSecundario(
                texto = "Rechazar tarea",
                onClick = { }
            )
        }
    }
}