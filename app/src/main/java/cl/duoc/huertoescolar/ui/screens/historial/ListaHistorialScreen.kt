package cl.duoc.huertoescolar.ui.screens.historial

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.EventoHistorial
import cl.duoc.huertoescolar.data.TipoEvento
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Historial cronológico de tareas realizadas, validaciones y problemas reportados. */
@Composable
fun ListaHistorialScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: HistorialViewModel = viewModel()
) {
    val tareas by viewModel.tareas.collectAsState()
    val problemas by viewModel.problemas.collectAsState()
    val eventos = viewModel.eventos(tareas, problemas, usuario, nombreSector)

    PantallaHuerto(
        titulo = "Historial",
        subtitulo = "Actividades, validaciones y problemas, de lo más reciente a lo más antiguo",
        onVolver = onVolver,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroHistorial.entries.forEach { f ->
                FilterChip(
                    selected = viewModel.filtro == f,
                    onClick = { viewModel.cambiarFiltro(f) },
                    label = { Text(f.etiqueta) }
                )
            }
        }

        if (eventos.isEmpty()) {
            TarjetaHuerto { Text("Todavía no hay registros en esta categoría.") }
        } else {
            eventos.forEach { evento -> TarjetaEvento(evento) }
        }
    }
}

@Composable
private fun TarjetaEvento(evento: EventoHistorial) {
    TarjetaHuerto {
        EtiquetaEstado(etiquetaTipo(evento.tipo), colorTipo(evento.tipo))
        Text(evento.titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            formatearFechaHora(evento.fecha),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (evento.detalle.isNotBlank()) {
            Text(evento.detalle, style = MaterialTheme.typography.bodyLarge)
        }
        evento.fotoUri?.let { Text("Foto adjunta: $it") }
    }
}

private fun etiquetaTipo(tipo: TipoEvento): String = when (tipo) {
    TipoEvento.TAREA -> "Tarea"
    TipoEvento.VALIDACION -> "Validación"
    TipoEvento.PROBLEMA -> "Problema"
}

@Composable
private fun colorTipo(tipo: TipoEvento): Color = when (tipo) {
    TipoEvento.TAREA -> MaterialTheme.colorScheme.secondaryContainer
    TipoEvento.VALIDACION -> MaterialTheme.colorScheme.primaryContainer
    TipoEvento.PROBLEMA -> MaterialTheme.colorScheme.tertiaryContainer
}

private fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))