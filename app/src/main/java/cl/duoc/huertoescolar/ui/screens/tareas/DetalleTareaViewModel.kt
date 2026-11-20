package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.repository.confirmarOperacion
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow
import cl.duoc.huertoescolar.domain.repository.TareasRepository





class DetalleTareaViewModel(
    private val repository: TareasRepository = RepositorioOperacion
) : ViewModel() {

    val tareas: StateFlow<List<Tarea>> = repository.tareas
    val tareaId: Int? = repository.tareaSeleccionadaId

    var uiState by mutableStateOf(DetalleTareaUiState())
        private set

    val comentario: String
        get() = uiState.comentario

    val fotoUri: String?
        get() = uiState.fotoUri

    val mostrarConfirmacion: Boolean
        get() = uiState.mostrarConfirmacion

    val mensaje: String?
        get() = uiState.mensaje

    val mensajeEsError: Boolean
        get() = uiState.mensajeEsError

    fun cambiarComentario(texto: String) {
        uiState = uiState.copy(comentario = texto)
    }

    fun elegirFoto(uri: String) {
        uiState = uiState.copy(
            fotoUri = uri,
            mensaje = null
        )
    }

    fun quitarFoto() {
        uiState = uiState.copy(fotoUri = null)
    }

    fun pedirConfirmacion() {
        uiState = uiState.copy(mostrarConfirmacion = true)
    }

    fun cancelarConfirmacion() {
        uiState = uiState.copy(mostrarConfirmacion = false)
    }

    fun puedeEnviar(tarea: Tarea, usuario: UsuarioPrueba): Boolean {
        val esSuya = tarea.responsableUsuarioId == usuario.id ||
                (
                        tarea.responsableUsuarioId == null &&
                                tarea.grupoAsignado == usuario.grupo
                        )

        val estadoValido = tarea.estado == EstadoTarea.PENDIENTE ||
                tarea.estado == EstadoTarea.EN_DESARROLLO ||
                tarea.estado == EstadoTarea.RECHAZADA

        return usuario.rol == RolUsuario.ESTUDIANTE &&
                esSuya &&
                estadoValido
    }

    fun enviar(usuario: UsuarioPrueba) {
        uiState = uiState.copy(mostrarConfirmacion = false)

        val id = tareaId ?: return

        confirmarOperacion(repository, accion = { repository.marcarRealizada(
            tareaId = id,
            usuarioId = usuario.id,
            grupo = usuario.grupo,
            comentario = uiState.comentario.trim(),
            fotoUri = uiState.fotoUri
        ) }) { resultado ->

        uiState = if (resultado.isSuccess) {
            uiState.copy(
                mensaje = "Tarea enviada. Quedó pendiente de validación.",
                mensajeEsError = false,
                comentario = ""
            )
        } else {
            uiState.copy(
                mensaje = resultado.exceptionOrNull()?.message
                    ?: "No se pudo enviar la tarea.",
                mensajeEsError = true
            )
        }
        }
    }
}