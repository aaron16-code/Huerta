package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

/** Pantalla del docente o tallerista: confirmar o rechazar tareas enviadas por los estudiantes. */
@Composable
fun PanelValidacionScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: PanelValidacionViewModel = viewModel()
) {
    val todas by viewModel.tareas.collectAsState()
    val pendientes = viewModel.pendientes(todas)

    PantallaHuerto(
        titulo = "Validar tareas",
        subtitulo = "Revisa el trabajo enviado por los estudiantes",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (usuario.rol != RolUsuario.ADMINISTRADOR) {
            TarjetaHuerto { Text("Solo docentes o talleristas autorizados pueden validar tareas.") }
        } else {
            viewModel.mensaje?.let { texto ->
                Text(
                    text = texto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (viewModel.mensajeEsError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }

            if (pendientes.isEmpty()) {
                TarjetaHuerto { Text("No hay tareas pendientes de validación.") }
            } else {
                pendientes.forEach { tarea ->
                    TarjetaPendiente(
                        tarea = tarea,
                        sector = nombreSector(tarea.sectorId),
                        comentario = viewModel.comentarioDe(tarea.id),
                        onComentario = { viewModel.cambiarComentario(tarea.id, it) },
                        onConfirmar = { viewModel.resolver(tarea, true, usuario) },
                        onRechazar = { viewModel.resolver(tarea, false, usuario) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaPendiente(
    tarea: Tarea,
    sector: String,
    comentario: String,
    onComentario: (String) -> Unit,
    onConfirmar: () -> Unit,
    onRechazar: () -> Unit
) {
    TarjetaHuerto {
        Text(tarea.descripcion, style = MaterialTheme.typography.titleMedium)
        EtiquetaEstado(tarea.estado.etiqueta, colorEstadoContenedor(tarea.estado))
        Text("${tarea.tipo.etiqueta} · $sector")
        Text("Enviada por: estudiante ficticio n.° ${tarea.realizadaPorId}")
        tarea.fechaRealizacion?.let { Text("Realizada el ${formatearFecha(it)}") }
        Text(
            "Comentario del estudiante: " +
                    tarea.comentarioEstudiante.ifBlank { "sin comentario" }
        )
        Text("Foto adjunta: ${tarea.fotoUri ?: "sin foto"}")

        OutlinedTextField(
            value = comentario,
            onValueChange = onComentario,
            label = { Text("Comentario para el estudiante (obligatorio al rechazar)") },
            modifier = Modifier.fillMaxWidth()
        )
        BotonPrincipal(texto = "Confirmar tarea", onClick = onConfirmar)
        BotonSecundario(texto = "Rechazar tarea", onClick = onRechazar)
    }
}