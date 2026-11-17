package cl.duoc.huertoescolar.ui.screens.tareas

data class DetalleTareaUiState(
    val comentario: String = "",
    val fotoUri: String? = null,
    val mostrarConfirmacion: Boolean = false,
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false
)