package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pantalla "Mis tareas" del estudiante. */
@Composable
fun ListaTareasScreen(
    usuario: UsuarioPrueba,
    onTareaClick: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: TareasEstudianteViewModel = viewModel()
) {
    val todas by viewModel.tareas.collectAsState()
    val lista = viewModel.misTareas(todas, usuario.id, usuario.grupo)

    PantallaHuerto(
        titulo = "Mis tareas",
        subtitulo = "Revisa las instrucciones y registra tu avance",
        onVolver = onVolver,
        modifier = modifier
    ) {
        ListaTareasContenido(
            tareas = lista,
            filtro = viewModel.filtro,
            onFiltro = viewModel::cambiarFiltro,
            onTareaClick = onTareaClick,
            nombreSector = nombreSector
        )
    }
}

@Composable
fun ListaTareasContenido(
    tareas: List<Tarea>,
    filtro: FiltroTareas,
    onFiltro: (FiltroTareas) -> Unit,
    onTareaClick: (Int) -> Unit,
    nombreSector: (Int) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroTareas.entries.forEach { f ->
                FilterChip(
                    selected = filtro == f,
                    onClick = { onFiltro(f) },
                    label = { Text(f.etiqueta) }
                )
            }
        }

        if (tareas.isEmpty()) {
            TarjetaHuerto {
                Text("No hay tareas en esta categoría.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            tareas.forEach { tarea ->
                TarjetaTarea(
                    tarea = tarea,
                    sector = nombreSector(tarea.sectorId),
                    onVerDetalle = { onTareaClick(tarea.id) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaTarea(tarea: Tarea, sector: String, onVerDetalle: () -> Unit) {
    val estado = tarea.estadoVisible()
    TarjetaHuerto {
        Text(tarea.descripcion, style = MaterialTheme.typography.titleMedium)
        EtiquetaEstado(estado.etiqueta, colorEstadoContenedor(estado))
        Text("${tarea.tipo.etiqueta} · $sector", style = MaterialTheme.typography.bodyLarge)
        Text("Fecha programada: ${formatearFecha(tarea.fechaProgramada)}", style = MaterialTheme.typography.bodyLarge)
        BotonSecundario(texto = "Ver detalle", onClick = onVerDetalle)
    }
}

/** Color de fondo de la etiqueta de estado (compartido con las otras pantallas de tareas). */
@Composable
internal fun colorEstadoContenedor(estado: EstadoTarea): Color = when (estado) {
    EstadoTarea.ATRASADA, EstadoTarea.RECHAZADA -> MaterialTheme.colorScheme.errorContainer
    EstadoTarea.VALIDADA -> MaterialTheme.colorScheme.primaryContainer
    EstadoTarea.PENDIENTE_VALIDACION -> MaterialTheme.colorScheme.tertiaryContainer
    else -> MaterialTheme.colorScheme.secondaryContainer
}

internal fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(Date(millis))

// Vista previa con datos ficticios: no necesita emulador.
@Preview(showBackground = true)
@Composable
private fun ListaTareasPreview() {
    HuertoEscolarTheme {
        PantallaHuerto(titulo = "Mis tareas", subtitulo = "Vista previa", onVolver = {}) {
            ListaTareasContenido(
                tareas = RepositorioOperacion.tareasDe(2, "Taller Huerto A"),
                filtro = FiltroTareas.TODAS,
                onFiltro = {},
                onTareaClick = {},
                nombreSector = { "Sector $it" }
            )
        }
    }
}