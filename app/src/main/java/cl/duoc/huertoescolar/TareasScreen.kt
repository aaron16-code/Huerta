package cl.duoc.huertoescolar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun TareasScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    tareasViewModel: TareasViewModel = viewModel()
) {
    val tarea = tareasViewModel.tarea

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = tarea.titulo,
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Sector: ${tarea.sector}")
        Text("Responsable: ${tarea.responsable}")
        Text("Instrucciones: ${tarea.instrucciones}")

        val estadoTexto = when (tarea.estado) {
            EstadoTarea.PENDIENTE -> "Pendiente"
            EstadoTarea.PENDIENTE_VALIDACION -> "Pendiente de validación"
            EstadoTarea.VALIDADA -> "Validada"
            EstadoTarea.RECHAZADA -> "Rechazada"
        }

        Text("Estado: $estadoTexto")

        Button(
            onClick = { tareasViewModel.enviarAValidacion() },
            enabled = tarea.estado == EstadoTarea.PENDIENTE
        ) {
            Text("Enviar a validación")
        }

        Button(onClick = onVolver) {
            Text("Volver")
        }
    }
}