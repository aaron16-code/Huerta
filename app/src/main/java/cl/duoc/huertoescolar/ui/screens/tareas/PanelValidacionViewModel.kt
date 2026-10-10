package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow

class PanelValidacionViewModel : ViewModel() {

    val tareas: StateFlow<List<Tarea>> = RepositorioOperacion.tareas

    // Un comentario por tarea (el docente puede revisar varias a la vez)
    private val comentarios = mutableStateMapOf<Int, String>()

    var mensaje by mutableStateOf<String?>(null)
        private set
    var mensajeEsError by mutableStateOf(false)
        private set

    /** Solo las tareas que el estudiante envió y aún nadie ha revisado. */
    fun pendientes(todas: List<Tarea>): List<Tarea> =
        todas.filter { it.estado == EstadoTarea.PENDIENTE_VALIDACION }
            .sortedBy { it.fechaRealizacion ?: 0L }

    fun comentarioDe(tareaId: Int): String = comentarios[tareaId] ?: ""

    fun cambiarComentario(tareaId: Int, texto: String) {
        comentarios[tareaId] = texto
    }

    /** Confirma (aprobada = true) o rechaza (false). Las reglas del caso las aplica el repositorio. */
    fun resolver(tarea: Tarea, aprobada: Boolean, usuario: UsuarioPrueba) {
        val resultado = RepositorioOperacion.validarTarea(
            tareaId = tarea.id,
            validadorId = usuario.id,
            rol = usuario.rol,
            aprobada = aprobada,
            comentario = comentarioDe(tarea.id).trim()
        )
        if (resultado.isSuccess) {
            mensaje = if (aprobada) "Tarea validada." else "Tarea rechazada. El estudiante podrá repetirla."
            mensajeEsError = false
            comentarios.remove(tarea.id)
        } else {
            mensaje = resultado.exceptionOrNull()?.message ?: "No se pudo completar la acción."
            mensajeEsError = true
        }
    }
}