package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.repository.SectoresRepository

class EditarSectorViewModel(
    private val repository: SectoresRepository = RepositorioHuerto
) : ViewModel() {

    private val sectorOriginal = repository.sectorSeleccionado

    var uiState by mutableStateOf(
        SectorFormUiState(
            nombre = sectorOriginal?.nombre.orEmpty(),
            descripcion = sectorOriginal?.descripcion.orEmpty(),
            ubicacion = sectorOriginal?.ubicacion.orEmpty(),
            estado = sectorOriginal?.estado ?: EstadoSector.DISPONIBLE
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

    var ubicacion: String
        get() = uiState.ubicacion
        set(value) {
            uiState = uiState.copy(ubicacion = value)
        }

    var estado: EstadoSector
        get() = uiState.estado
        set(value) {
            uiState = uiState.copy(estado = value)
        }

    val errorNombre: Boolean
        get() = uiState.errorNombre

    val errorDescripcion: Boolean
        get() = uiState.errorDescripcion

    val errorUbicacion: Boolean
        get() = uiState.errorUbicacion

    fun guardarCambios(): Boolean {
        val estadoValidado = validarFormularioSector(uiState)
        uiState = estadoValidado

        if (
            estadoValidado.errorNombre ||
            estadoValidado.errorDescripcion ||
            estadoValidado.errorUbicacion
        ) {
            return false
        }

        val sector = sectorOriginal ?: return false

        repository.actualizarSector(
            sector.copy(
                nombre = estadoValidado.nombre.trim(),
                descripcion = estadoValidado.descripcion.trim(),
                ubicacion = estadoValidado.ubicacion.trim(),
                estado = estadoValidado.estado
            )
        )

        return true
    }
}