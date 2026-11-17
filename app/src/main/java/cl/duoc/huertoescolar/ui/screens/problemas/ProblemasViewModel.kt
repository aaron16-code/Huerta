package cl.duoc.huertoescolar.ui.screens.problemas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow
import cl.duoc.huertoescolar.domain.repository.ProblemasRepository




class ProblemasViewModel(
    private val repository: ProblemasRepository = RepositorioOperacion
) : ViewModel() {

    val problemas: StateFlow<List<Problema>> = repository.problemas

    var uiState by mutableStateOf(ProblemasUiState())
        private set

    val mensaje: String?
        get() = uiState.mensaje

    val mensajeEsError: Boolean
        get() = uiState.mensajeEsError

    fun visibles(
        todos: List<Problema>,
        usuario: UsuarioPrueba
    ): List<Problema> {
        val visibles = if (usuario.rol == RolUsuario.ADMINISTRADOR) {
            todos
        } else {
            todos.filter { it.reportadoPorId == usuario.id }
        }

        return visibles.sortedByDescending { it.fechaReporte }
    }

    fun comentarioDe(problemaId: Int): String {
        return uiState.comentarios[problemaId] ?: ""
    }

    fun cambiarComentario(problemaId: Int, texto: String) {
        uiState = uiState.copy(
            comentarios = uiState.comentarios + (problemaId to texto)
        )
    }

    fun cambiarEstado(
        problema: Problema,
        nuevo: EstadoReporte,
        usuario: UsuarioPrueba
    ) {
        val nuevoComentario = comentarioDe(problema.id)
            .trim()
            .ifEmpty { problema.comentarioDocente }

        val resultado = repository.actualizarProblema(
            id = problema.id,
            rol = usuario.rol,
            nuevoEstado = nuevo,
            comentario = nuevoComentario
        )

        uiState = if (resultado.isSuccess) {
            uiState.copy(
                comentarios = uiState.comentarios - problema.id,
                mensaje = "Reporte actualizado: ${nuevo.etiqueta}.",
                mensajeEsError = false
            )
        } else {
            uiState.copy(
                mensaje = resultado.exceptionOrNull()?.message
                    ?: "No se pudo actualizar el reporte.",
                mensajeEsError = true
            )
        }
    }
}