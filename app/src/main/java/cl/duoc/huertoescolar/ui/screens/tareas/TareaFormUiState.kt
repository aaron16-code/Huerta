package cl.duoc.huertoescolar.ui.screens.tareas

import cl.duoc.huertoescolar.domain.model.TipoTarea

/** Opción de asignación presentada al docente. */
data class DestinatarioTareaUi(
    val etiqueta: String,
    val usuarioId: Int?,
    val grupo: String?
)

/** Estado completo y sin lógica del formulario de tareas. */
data class TareaFormUiState(
    val tipo: TipoTarea = TipoTarea.RIEGO,
    val descripcion: String = "",
    val instrucciones: String = "",
    val sectorId: Int? = null,
    val cultivoId: Int? = null,
    val fechaTexto: String = "",
    val destinatario: DestinatarioTareaUi? = null,
    val errorDescripcion: String? = null,
    val errorInstrucciones: String? = null,
    val errorSector: String? = null,
    val errorFecha: String? = null,
    val errorDestinatario: String? = null,
    val mensajeError: String? = null,
    val mostrarConfirmacion: Boolean = false,
    val guardada: Boolean = false,
    val tareaDisponible: Boolean = true
)
