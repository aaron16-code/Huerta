package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.repository.confirmarOperacion
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.repository.CatalogoHuertoRepository
import cl.duoc.huertoescolar.domain.repository.TareasRepository
import cl.duoc.huertoescolar.domain.model.TipoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba

class EditarTareaViewModel(
    private val repository: TareasRepository = RepositorioOperacion,
    private val catalogoRepository: CatalogoHuertoRepository = RepositorioHuerto
) : ViewModel() {

    private val tareaOriginal = repository.tareaSeleccionadaId?.let(repository::obtenerTarea)
    val destinatarios = destinatariosDisponibles()
    val sectores get() = catalogoRepository.sectores
    val cultivos get() = catalogoRepository.cultivos

    var uiState by mutableStateOf(
        tareaOriginal?.let { tarea ->
            TareaFormUiState(
                tipo = tarea.tipo,
                descripcion = tarea.descripcion,
                instrucciones = tarea.instrucciones,
                sectorId = tarea.sectorId,
                cultivoId = tarea.cultivoId,
                fechaTexto = formatearFecha(tarea.fechaProgramada),
                destinatario = destinatarios.find {
                    it.usuarioId == tarea.responsableUsuarioId &&
                            it.grupo == tarea.grupoAsignado
                }
            )
        } ?: TareaFormUiState(tareaDisponible = false)
    )
        private set

    fun elegirTipo(tipo: TipoTarea) = actualizar { copy(tipo = tipo) }
    fun cambiarDescripcion(texto: String) = actualizar {
        copy(descripcion = texto, errorDescripcion = null)
    }
    fun cambiarInstrucciones(texto: String) = actualizar {
        copy(instrucciones = texto, errorInstrucciones = null)
    }
    fun cambiarFecha(texto: String) = actualizar { copy(fechaTexto = texto, errorFecha = null) }
    fun elegirDestinatario(destinatario: DestinatarioTareaUi) = actualizar {
        copy(destinatario = destinatario, errorDestinatario = null)
    }
    fun elegirCultivo(id: Int?) = actualizar { copy(cultivoId = id) }

    fun elegirSector(id: Int) = actualizar {
        copy(
            sectorId = id,
            cultivoId = if (sectorId == id) cultivoId else null,
            errorSector = null
        )
    }

    fun guardar(usuario: UsuarioPrueba) {
        val original = tareaOriginal ?: return
        val errores = ValidadorFormularioTarea.validar(
            state = uiState,
            exigirFechaActualOFutura = false
        )
        uiState = uiState.conErrores(errores)
        if (!errores.esValido) return

        val state = uiState
        val sector = state.sectorId ?: return
        val fecha = parsearFechaFinDelDia(state.fechaTexto) ?: return
        val destino = state.destinatario ?: return
        confirmarOperacion(repository, accion = { repository.editarTarea(
            rol = usuario.rol,
            editada = original.copy(
                tipo = state.tipo,
                descripcion = state.descripcion.trim(),
                instrucciones = state.instrucciones.trim(),
                sectorId = sector,
                cultivoId = state.cultivoId,
                fechaProgramada = fecha,
                responsableUsuarioId = destino.usuarioId,
                grupoAsignado = destino.grupo
            )
        ) }) { resultado ->
        uiState = if (resultado.isSuccess) {
            uiState.copy(guardada = true, mensajeError = null)
        } else {
            uiState.copy(
                mensajeError = resultado.exceptionOrNull()?.message ?: "No se pudo actualizar la tarea."
            )
        }
        }
    }

    private fun actualizar(cambio: TareaFormUiState.() -> TareaFormUiState) {
        uiState = uiState.cambio()
    }
}
