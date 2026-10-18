package cl.duoc.huertoescolar.ui.screens.tareas

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

/** Pantalla del docente o tallerista: ve todas las tareas y puede crear y asignar nuevas. */
@Composable
fun GestionTareasScreen(
    usuario: UsuarioPrueba,
    onCrearTarea: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: GestionTareasViewModel = viewModel()
) {
    val todas by viewModel.tareas.collectAsState()
    val lista = viewModel.visibles(todas)

    PantallaHuerto(
        titulo = "Tareas del huerto",
        subtitulo = "Crea, asigna y revisa las tareas de los estudiantes",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (usuario.rol != RolUsuario.ADMINISTRADOR) {
            TarjetaHuerto { Text("Solo docentes o talleristas autorizados pueden administrar tareas.") }
        } else {
            BotonPrincipal(texto = "Crear y asignar una tarea", onClick = onCrearTarea)

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroTareas.entries.forEach { f ->
                    FilterChip(
                        selected = viewModel.filtro == f,
                        onClick = { viewModel.cambiarFiltro(f) },
                        label = { Text(f.etiqueta) }
                    )
                }
            }

            if (lista.isEmpty()) {
                TarjetaHuerto { Text("No hay tareas en esta categoría.") }
            } else {
                lista.forEach { tarea ->
                    TarjetaTareaDocente(
                        tarea = tarea,
                        sector = nombreSector(tarea.sectorId),
                        onVerDetalle = { onVerDetalle(tarea.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaTareaDocente(tarea: Tarea, sector: String, onVerDetalle: () -> Unit) {
    val estado = tarea.estadoVisible()
    TarjetaHuerto {
        Text(tarea.descripcion, style = MaterialTheme.typography.titleMedium)
        EtiquetaEstado(estado.etiqueta, colorEstadoContenedor(estado))
        Text("${tarea.tipo.etiqueta} · $sector")
        Text("Asignada a: ${textoResponsable(tarea)}")
        Text("Fecha programada: ${formatearFecha(tarea.fechaProgramada)}")
        BotonSecundario(texto = "Ver detalle", onClick = onVerDetalle)
    }
}

/** Texto de a quién está asignada la tarea (estudiante individual o grupo). */
internal fun textoResponsable(tarea: Tarea): String = when {
    tarea.responsableUsuarioId != null -> "estudiante ficticio n.° ${tarea.responsableUsuarioId}"
    !tarea.grupoAsignado.isNullOrBlank() -> "grupo ${tarea.grupoAsignado}"
    else -> "sin asignar"
}