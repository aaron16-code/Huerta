package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.repository.SectoresRepository

class SectorViewModel(
    private val repository: SectoresRepository = RepositorioHuerto
) : ViewModel() {

    var uiState by mutableStateOf(SectorFormUiState())
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

    val errorNombre: Boolean
        get() = uiState.errorNombre

    val errorDescripcion: Boolean
        get() = uiState.errorDescripcion

    val errorUbicacion: Boolean
        get() = uiState.errorUbicacion

    fun validarFormulario(): Boolean {
        val estadoValidado = validarFormularioSector(uiState)
        uiState = estadoValidado

        return !estadoValidado.errorNombre &&
                !estadoValidado.errorDescripcion &&
                !estadoValidado.errorUbicacion
    }

    fun guardarSector(): Boolean {
        if (!validarFormulario()) {
            return false
        }

        val nuevoSector = Sector(
            id = repository.obtenerSiguienteId(),
            nombre = uiState.nombre.trim(),
            descripcion = uiState.descripcion.trim(),
            ubicacion = uiState.ubicacion.trim(),
            estado = EstadoSector.DISPONIBLE,
            responsable = "Docente de prueba"
        )

        repository.agregarSector(nuevoSector)
        return true
    }
}