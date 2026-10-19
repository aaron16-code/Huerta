package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

/** Detalle de una tarea para el estudiante: instrucciones, estado y envío a validación. */
@Composable
fun DetalleTareaScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: DetalleTareaViewModel = viewModel()
) {
    val todas by viewModel.tareas.collectAsState()
    val tarea = todas.find { it.id == viewModel.tareaId }

    PantallaHuerto(
        titulo = "Detalle de la tarea",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (tarea == null) {
            TarjetaHuerto { Text("No se encontró la tarea.") }
        } else {
            val estado = tarea.estadoVisible()

            TarjetaHuerto(modifier = Modifier.animateContentSize()) {
                Text(tarea.descripcion, style = MaterialTheme.typography.titleLarge)
                EtiquetaEstado(estado.etiqueta, colorEstadoContenedor(estado))
                Text("Tipo: ${tarea.tipo.etiqueta}")
                Text("Sector: ${nombreSector(tarea.sectorId)}")
                Text("Fecha programada: ${formatearFecha(tarea.fechaProgramada)}")
                Text("Instrucciones: ${tarea.instrucciones}")
                tarea.fotoUri?.let { FotoEvidencia(it) }
            }

            if (tarea.comentarioDocente.isNotBlank()) {
                TarjetaHuerto {
                    Text("Comentario del docente", style = MaterialTheme.typography.titleMedium)
                    Text(tarea.comentarioDocente)
                }
            }

            if (viewModel.puedeEnviar(tarea, usuario)) {
                TarjetaHuerto {
                    Text("Registra tu avance", style = MaterialTheme.typography.titleMedium)
                    CampoValidado(
                        valor = viewModel.comentario,
                        alCambiar = viewModel::cambiarComentario,
                        etiqueta = "Comentario (opcional)",
                        icono = Icons.Filled.Edit,
                        error = null
                    )
                }
                SelectorFotoGaleria(
                    fotoUri = viewModel.fotoUri,
                    onFotoSeleccionada = viewModel::elegirFoto,
                    onQuitarFoto = viewModel::quitarFoto
                )
                BotonPrincipal(
                    texto = "Marcar como realizada",
                    onClick = viewModel::pedirConfirmacion
                )
            } else if (tarea.estado == EstadoTarea.PENDIENTE_VALIDACION) {
                TarjetaHuerto {
                    Text("Enviada. Esperando que un docente la valide.")
                    if (tarea.comentarioEstudiante.isNotBlank()) {
                        Text("Tu comentario: ${tarea.comentarioEstudiante}")
                    }
                }
            } else if (tarea.estado == EstadoTarea.VALIDADA) {
                TarjetaHuerto { Text("Tarea validada. ¡Buen trabajo!") }
            }

            MensajeAnimado(viewModel.mensaje, esError = viewModel.mensajeEsError)

            if (viewModel.mostrarConfirmacion) {
                AlertDialog(
                    onDismissRequest = viewModel::cancelarConfirmacion,
                    title = { Text("¿Enviar a validación?") },
                    text = { Text("Un docente revisará tu trabajo antes de darlo por terminado.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.enviar(usuario) }) { Text("Sí, enviar") }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::cancelarConfirmacion) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

/** Mensaje que aparece y desaparece con una animación suave. */
@Composable
private fun MensajeAnimado(texto: String?, esError: Boolean) {
    var ultimo by remember { mutableStateOf("") }
    LaunchedEffect(texto) {
        if (texto != null) ultimo = texto
    }
    AnimatedVisibility(
        visible = texto != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = texto ?: ultimo,
            style = MaterialTheme.typography.bodyLarge,
            color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}