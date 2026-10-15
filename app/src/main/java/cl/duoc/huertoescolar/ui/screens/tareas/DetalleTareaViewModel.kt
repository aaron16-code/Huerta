package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow

class DetalleTareaViewModel : ViewModel() {

    val tareas: StateFlow<List<Tarea>> = RepositorioOperacion.tareas
    val tareaId: Int? = RepositorioOperacion.tareaSeleccionadaId

    var comentario by mutableStateOf("")
        private set
    var fotoUri by mutableStateOf<String?>(null)
        private set
    var mostrarConfirmacion by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var mensajeEsError by mutableStateOf(false)
        private set

    fun cambiarComentario(texto: String) {
        comentario = texto
    }

    /** Foto ficticia de prueba. Más adelante se reemplaza por la cámara o la galería. */
    fun adjuntarFotoDePrueba() {
        fotoUri = "foto_prueba_tarea_$tareaId"
        mensaje = null
    }

    fun pedirConfirmacion() {
        mostrarConfirmacion = true
    }

    fun cancelarConfirmacion() {
        mostrarConfirmacion = false
    }

    /** Solo el estudiante dueño de la tarea puede enviarla, y solo si está pendiente o rechazada. */
    fun puedeEnviar(tarea: Tarea, usuario: UsuarioPrueba): Boolean {
        val esSuya = tarea.responsableUsuarioId == usuario.id ||
                (tarea.responsableUsuarioId == null && tarea.grupoAsignado == usuario.grupo)
        val estadoValido = tarea.estado == EstadoTarea.PENDIENTE ||
                tarea.estado == EstadoTarea.EN_DESARROLLO ||
                tarea.estado == EstadoTarea.RECHAZADA
        return usuario.rol == RolUsuario.ESTUDIANTE && esSuya && estadoValido
    }

    fun enviar(usuario: UsuarioPrueba) {
        mostrarConfirmacion = false
        val id = tareaId ?: return
        val resultado = RepositorioOperacion.marcarRealizada(
            tareaId = id,
            usuarioId = usuario.id,
            grupo = usuario.grupo,
            comentario = comentario.trim(),
            fotoUri = fotoUri
        )
        if (resultado.isSuccess) {
            mensaje = "Tarea enviada. Quedó pendiente de validación."
            mensajeEsError = false
            comentario = ""
        } else {
            mensaje = resultado.exceptionOrNull()?.message ?: "No se pudo enviar la tarea."
            mensajeEsError = true
        }
    }
}