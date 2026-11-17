package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun CrearTareaScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CrearTareaViewModel = viewModel()
) {
    val state = viewModel.uiState

    PantallaHuerto(
        titulo = "Crear tarea",
        subtitulo = "Define la actividad y asígnala a un estudiante o grupo",
        onVolver = onVolver,
        modifier = modifier
    ) {
        when {
            usuario.rol != RolUsuario.ADMINISTRADOR ->
                TarjetaHuerto {
                    Text("Solo docentes o talleristas autorizados pueden crear tareas.")
                }

            state.guardada -> TarjetaHuerto {
                Text("Tarea creada.", style = MaterialTheme.typography.titleMedium)
                Text("Ya aparece en la lista del estudiante o grupo al que la asignaste.")
                BotonPrincipal(texto = "Volver a las tareas", onClick = onVolver)
                BotonSecundario(texto = "Crear otra tarea", onClick = viewModel::crearOtra)
            }

            else -> {
                FormularioTarea(
                    state = state,
                    destinatarios = viewModel.destinatarios,
                    sectores = viewModel.sectores,
                    cultivos = viewModel.cultivos,
                    onTipoChange = viewModel::elegirTipo,
                    onDescripcionChange = viewModel::cambiarDescripcion,
                    onInstruccionesChange = viewModel::cambiarInstrucciones,
                    onSectorChange = viewModel::elegirSector,
                    onCultivoChange = viewModel::elegirCultivo,
                    onFechaChange = viewModel::cambiarFecha,
                    onDestinatarioChange = viewModel::elegirDestinatario,
                    onFechaRapida = viewModel::elegirFechaRapida
                )
                BotonPrincipal(texto = "Crear tarea", onClick = viewModel::pedirConfirmacion)
                MensajeErrorFormulario(state.mensajeError)
            }
        }

        if (state.mostrarConfirmacion) {
            AlertDialog(
                onDismissRequest = viewModel::cancelarConfirmacion,
                title = { Text("¿Crear la tarea?") },
                text = {
                    Text("Se asignará a ${state.destinatario?.etiqueta ?: "el destinatario elegido"}.")
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.crear(usuario) }) { Text("Sí, crear") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::cancelarConfirmacion) { Text("Cancelar") }
                }
            )
        }
    }
}
