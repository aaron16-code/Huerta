package cl.duoc.huertoescolar.ui.screens.problemas

data class ProblemasUiState(
    val comentarios: Map<Int, String> = emptyMap(),
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false
)