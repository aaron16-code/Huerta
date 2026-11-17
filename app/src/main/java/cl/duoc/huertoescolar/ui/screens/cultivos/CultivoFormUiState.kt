package cl.duoc.huertoescolar.ui.screens.cultivos

import cl.duoc.huertoescolar.domain.model.EstadoCultivo

data class CultivoFormUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val sectorId: Int? = null,
    val fechaSiembra: String = "",
    val fechaCosecha: String = "",
    val frecuenciaRiego: String = "",
    val observaciones: String = "",
    val estado: EstadoCultivo = EstadoCultivo.PLANIFICADO,
    val errorNombre: Boolean = false,
    val errorDescripcion: Boolean = false,
    val errorSector: Boolean = false,
    val errorFechaSiembra: Boolean = false,
    val errorFechaCosecha: Boolean = false,
    val errorFrecuencia: Boolean = false
)