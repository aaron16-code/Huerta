package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lista de tareas del estudiante. Recibe el usuario por parámetro (no depende de la navegación).
 * nombreSector: más adelante se conecta a RepositorioHuerto para mostrar el nombre real.
 */
@Composable
fun ListaTareasScreen(
    usuarioId: Int,
    grupo: String,
    onTareaClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: TareasEstudianteViewModel = viewModel()
) {
    val todas by viewModel.tareas.collectAsState()
    val lista = viewModel.misTareas(todas, usuarioId, grupo)

    ListaTareasContenido(
        tareas = lista,
        filtro = viewModel.filtro,
        onFiltro = viewModel::cambiarFiltro,
        onTareaClick = onTareaClick,
        nombreSector = nombreSector,
        modifier = modifier
    )
}

@Composable
fun ListaTareasContenido(
    tareas: List<Tarea>,
    filtro: FiltroTareas,
    onFiltro: (FiltroTareas) -> Unit,
    onTareaClick: (Int) -> Unit,
    nombreSector: (Int) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FiltroTareas.entries) { f ->
                FilterChip(
                    selected = filtro == f,
                    onClick = { onFiltro(f) },
                    label = { Text(f.etiqueta) }
                )
            }
        }

        if (tareas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay tareas en esta categoría.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tareas, key = { it.id }) { tarea ->
                    TarjetaTarea(tarea, nombreSector(tarea.sectorId), onClick = { onTareaClick(tarea.id) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaTarea(tarea: Tarea, sector: String, onClick: () -> Unit) {
    val estado = tarea.estadoVisible()
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(tarea.descripcion, style = MaterialTheme.typography.titleMedium)
            Text("${tarea.tipo.etiqueta} · $sector", style = MaterialTheme.typography.bodyMedium)
            Text("Fecha programada: ${formatearFecha(tarea.fechaProgramada)}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = estado.etiqueta,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colorEstado(estado)
            )
        }
    }
}

@Composable
private fun colorEstado(estado: EstadoTarea): Color = when (estado) {
    EstadoTarea.ATRASADA, EstadoTarea.RECHAZADA -> MaterialTheme.colorScheme.error
    EstadoTarea.VALIDADA -> MaterialTheme.colorScheme.primary
    EstadoTarea.PENDIENTE_VALIDACION -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.secondary
}

private fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(Date(millis))

// Vista previa con datos ficticios: no necesita emulador.
@Preview(showBackground = true)
@Composable
private fun ListaTareasPreview() {
    HuertoEscolarTheme {
        ListaTareasContenido(
            tareas = RepositorioOperacion.tareasDe(2, "Taller Huerto A"),
            filtro = FiltroTareas.TODAS,
            onFiltro = {},
            onTareaClick = {},
            nombreSector = { "Sector $it" }
        )
    }
}