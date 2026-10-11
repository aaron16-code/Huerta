package cl.duoc.huertoescolar.ui.screens.problemas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.EstadoReporte
import cl.duoc.huertoescolar.data.Problema
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow

class ProblemasViewModel : ViewModel() {

    val problemas: StateFlow<List<Problema>> = RepositorioOperacion.problemas

    // Un comentario de seguimiento por reporte
    private val comentarios = mutableStateMapOf<Int, String>()

    var mensaje by mutableStateOf<String?>(null)
        private set
    var mensajeEsError by mutableStateOf(false)
        private set

    /** El docente ve todos los reportes; el estudiante, solo los suyos. Más recientes primero. */
    fun visibles(todos: List<Problema>, usuario: UsuarioPrueba): List<Problema> {
        val base = if (usuario.rol == RolUsuario.ADMINISTRADOR) todos
        else todos.filter { it.reportadoPorId == usuario.id }
        return base.sortedByDescending { it.fechaReporte }
    }

    fun comentarioDe(problemaId: Int): String = comentarios[problemaId] ?: ""

    fun cambiarComentario(problemaId: Int, texto: String) {
        comentarios[problemaId] = texto
    }

    /** Cambia el estado del reporte. Si no se escribe comentario nuevo, se conserva el anterior. */
    fun cambiarEstado(problema: Problema, nuevo: EstadoReporte, usuario: UsuarioPrueba) {
        val nuevoComentario = comentarioDe(problema.id).trim().ifEmpty { problema.comentarioDocente }
        val resultado = RepositorioOperacion.actualizarProblema(
            id = problema.id,
            rol = usuario.rol,
            nuevoEstado = nuevo,
            comentario = nuevoComentario
        )
        if (resultado.isSuccess) {
            mensaje = "Reporte actualizado: ${nuevo.etiqueta}."
            mensajeEsError = false
            comentarios.remove(problema.id)
        } else {
            mensaje = resultado.exceptionOrNull()?.message ?: "No se pudo actualizar el reporte."
            mensajeEsError = true
        }
    }
}