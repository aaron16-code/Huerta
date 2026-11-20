package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.repository.confirmarOperacion
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow
import cl.duoc.huertoescolar.domain.repository.TareasRepository

class PanelValidacionViewModel(
    private val repository: TareasRepository = RepositorioOperacion
) : ViewModel() {

    val tareas: StateFlow<List<Tarea>> = repository.tareas

    var uiState by mutableStateOf(PanelValidacionUiState())
        private set

    val mensaje: String?
        get() = uiState.mensaje

    val mensajeEsError: Boolean
        get() = uiState.mensajeEsError

    fun pendientes(todas: List<Tarea>): List<Tarea> {
        return todas
            .filter { it.estado == EstadoTarea.PENDIENTE_VALIDACION }
            .sortedBy { it.fechaRealizacion ?: 0L }
    }

    fun comentarioDe(tareaId: Int): String {
        return uiState.comentarios[tareaId] ?: ""
    }

    fun cambiarComentario(tareaId: Int, texto: String) {
        uiState = uiState.copy(
            comentarios = uiState.comentarios + (tareaId to texto)
        )
    }

    fun resolver(
        tarea: Tarea,
        aprobada: Boolean,
        usuario: UsuarioPrueba
    ) {
        confirmarOperacion(repository, accion = { repository.validarTarea(
            tareaId = tarea.id,
            validadorId = usuario.id,
            rol = usuario.rol,
            aprobada = aprobada,
            comentario = comentarioDe(tarea.id).trim()
        ) }) { resultado ->

        uiState = if (resultado.isSuccess) {
            uiState.copy(
                comentarios = uiState.comentarios - tarea.id,
                mensaje = if (aprobada) {
                    "Tarea validada."
                } else {
                    "Tarea rechazada. El estudiante podrá repetirla."
                },
                mensajeEsError = false
            )
        } else {
            uiState.copy(
                mensaje = resultado.exceptionOrNull()?.message
                    ?: "No se pudo completar la acción.",
                mensajeEsError = true
            )
        }
        }
    }
}