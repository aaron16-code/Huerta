package cl.duoc.huertoescolar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun TareasScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    tareasViewModel: TareasViewModel = viewModel()
) {
    val tarea = tareasViewModel.tarea
    val estadoTexto = when (tarea.estado) {
        EstadoTarea.PENDIENTE -> "Pendiente"
        EstadoTarea.PENDIENTE_VALIDACION -> "Pendiente de validación"
        EstadoTarea.VALIDADA -> "Validada"
        EstadoTarea.RECHAZADA -> "Rechazada"
    }

    PantallaHuerto(
        titulo = "Mis tareas",
        subtitulo = "Revisa las instrucciones y registra tu avance",
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            Text(tarea.titulo, style = MaterialTheme.typography.titleLarge)
            EtiquetaEstado(estadoTexto)
            Text("Sector: ${tarea.sector}")
            Text("Responsable: ${tarea.responsable}")
            Text("Instrucciones: ${tarea.instrucciones}")

            BotonPrincipal(
                texto = "Enviar a validación",
                onClick = { tareasViewModel.enviarAValidacion() },
                enabled = tarea.estado == EstadoTarea.PENDIENTE
            )
        }
    }
}
