package cl.duoc.huertoescolar.ui.screens.problemas

fun validarFormularioProblema(
    estado: ReportarProblemaUiState
): ReportarProblemaUiState {
    return estado.copy(
        errorSector = if (estado.sectorId == null) {
            "Elige el sector donde viste el problema"
        } else {
            null
        },
        errorDescripcion = if (
            estado.descripcion.trim().length < 5
        ) {
            "Describe el problema con al menos 5 caracteres"
        } else {
            null
        }
    )
}