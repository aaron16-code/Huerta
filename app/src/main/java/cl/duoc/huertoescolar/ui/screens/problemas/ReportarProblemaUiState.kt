package cl.duoc.huertoescolar.ui.screens.problemas

import cl.duoc.huertoescolar.domain.model.TipoProblema

data class ReportarProblemaUiState(
    val sectorId: Int? = null,
    val cultivoId: Int? = null,
    val tipo: TipoProblema = TipoProblema.FALTA_AGUA,
    val descripcion: String = "",
    val fotoUri: String? = null,
    val mostrarConfirmacion: Boolean = false,
    val enviado: Boolean = false,
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false,
    val errorSector: String? = null,
    val errorDescripcion: String? = null
)