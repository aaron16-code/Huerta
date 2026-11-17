package cl.duoc.huertoescolar.ui.screens.tareas

data class PanelValidacionUiState(
    val comentarios: Map<Int, String> = emptyMap(),
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false
)