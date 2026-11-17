package cl.duoc.huertoescolar.ui.screens.sectores

fun validarNombreSector(nombre: String): Boolean {
    return nombre.isNotBlank()
}

fun validarDescripcionSector(descripcion: String): Boolean {
    return descripcion.length >= 5
}

fun validarUbicacionSector(ubicacion: String): Boolean {
    return ubicacion.isNotBlank()
}

fun validarFormularioSector(
    estado: SectorFormUiState
): SectorFormUiState {
    return estado.copy(
        errorNombre = !validarNombreSector(estado.nombre),
        errorDescripcion = !validarDescripcionSector(estado.descripcion),
        errorUbicacion = !validarUbicacionSector(estado.ubicacion)
    )
}