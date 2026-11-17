package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.repository.CultivosRepository

class EditarCultivoViewModel(
    private val repository: CultivosRepository = RepositorioHuerto
) : ViewModel() {

    private val cultivoOriginal = repository.cultivoSeleccionado

    val sectores: List<Sector>
        get() = repository.sectores

    var uiState by mutableStateOf(
        CultivoFormUiState(
            nombre = cultivoOriginal?.nombre.orEmpty(),
            descripcion = cultivoOriginal?.descripcion.orEmpty(),
            sectorId = cultivoOriginal?.sectorId,
            fechaSiembra = cultivoOriginal?.fechaSiembra.orEmpty(),
            fechaCosecha = cultivoOriginal?.fechaCosechaEstimada.orEmpty(),
            frecuenciaRiego = cultivoOriginal?.frecuenciaRiegoDias
                ?.toString()
                .orEmpty(),
            observaciones = cultivoOriginal?.observaciones.orEmpty(),
            estado = cultivoOriginal?.estado ?: EstadoCultivo.PLANIFICADO
        )
    )
        private set

    var nombre: String
        get() = uiState.nombre
        set(value) {
            uiState = uiState.copy(nombre = value)
        }

    var descripcion: String
        get() = uiState.descripcion
        set(value) {
            uiState = uiState.copy(descripcion = value)
        }

    var sectorId: Int?
        get() = uiState.sectorId
        set(value) {
            uiState = uiState.copy(sectorId = value)
        }

    var fechaSiembra: String
        get() = uiState.fechaSiembra
        set(value) {
            uiState = uiState.copy(fechaSiembra = value)
        }

    var fechaCosecha: String
        get() = uiState.fechaCosecha
        set(value) {
            uiState = uiState.copy(fechaCosecha = value)
        }

    var frecuenciaRiego: String
        get() = uiState.frecuenciaRiego
        set(value) {
            uiState = uiState.copy(frecuenciaRiego = value)
        }

    var observaciones: String
        get() = uiState.observaciones
        set(value) {
            uiState = uiState.copy(observaciones = value)
        }

    var estado: EstadoCultivo
        get() = uiState.estado
        set(value) {
            uiState = uiState.copy(estado = value)
        }

    val errorNombre: Boolean
        get() = uiState.errorNombre

    val errorDescripcion: Boolean
        get() = uiState.errorDescripcion

    val errorSector: Boolean
        get() = uiState.errorSector

    val errorFechaSiembra: Boolean
        get() = uiState.errorFechaSiembra

    val errorFechaCosecha: Boolean
        get() = uiState.errorFechaCosecha

    val errorFrecuencia: Boolean
        get() = uiState.errorFrecuencia

    fun guardarCambios(): Boolean {
        val estadoValidado = validarFormularioCultivo(uiState)
        uiState = estadoValidado

        if (
            estadoValidado.errorNombre ||
            estadoValidado.errorDescripcion ||
            estadoValidado.errorSector ||
            estadoValidado.errorFechaSiembra ||
            estadoValidado.errorFechaCosecha ||
            estadoValidado.errorFrecuencia
        ) {
            return false
        }

        val cultivo = cultivoOriginal ?: return false

        repository.actualizarCultivo(
            cultivo.copy(
                nombre = estadoValidado.nombre.trim(),
                descripcion = estadoValidado.descripcion.trim(),
                sectorId = estadoValidado.sectorId ?: return false,
                fechaSiembra = estadoValidado.fechaSiembra.trim(),
                fechaCosechaEstimada = estadoValidado.fechaCosecha.trim(),
                frecuenciaRiegoDias = estadoValidado.frecuenciaRiego.toInt(),
                observaciones = estadoValidado.observaciones.trim(),
                estado = estadoValidado.estado
            )
        )

        return true
    }
}