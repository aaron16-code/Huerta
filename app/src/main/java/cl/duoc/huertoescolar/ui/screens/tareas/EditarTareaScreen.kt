package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun EditarTareaScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditarTareaViewModel = viewModel()
) {
    val state = viewModel.uiState

    PantallaHuerto(
        titulo = "Editar tarea",
        subtitulo = "Modifica la actividad, fecha o asignación",
        onVolver = onVolver,
        modifier = modifier
    ) {
        when {
            usuario.rol != RolUsuario.ADMINISTRADOR ->
                TarjetaHuerto {
                    Text("Solo docentes o talleristas autorizados pueden editar tareas.")
                }

            !state.tareaDisponible -> TarjetaHuerto {
                Text("No se encontró la tarea seleccionada.")
                BotonPrincipal(texto = "Volver a las tareas", onClick = onVolver)
            }

            state.guardada -> TarjetaHuerto {
                Text("Tarea actualizada", style = MaterialTheme.typography.titleMedium)
                Text("Los cambios fueron guardados correctamente.")
                BotonPrincipal(texto = "Volver a las tareas", onClick = onVolver)
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
                    onDestinatarioChange = viewModel::elegirDestinatario
                )
                BotonPrincipal(
                    texto = "Guardar cambios",
                    onClick = { viewModel.guardar(usuario) }
                )
                MensajeErrorFormulario(state.mensajeError)
            }
        }
    }
}
