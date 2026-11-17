package cl.duoc.huertoescolar.ui.screens.sectores

import cl.duoc.huertoescolar.domain.model.EstadoSector

data class SectorFormUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: EstadoSector = EstadoSector.DISPONIBLE,
    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorUbicacion: Boolean = false
)