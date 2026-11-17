package cl.duoc.huertoescolar.ui.screens.sectores

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.remote.SectorRequest
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.domain.model.EstadoSector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SectorViewModel(
    private val repository: HuertoRemoteRepository = HuertoRemoteRepository()
) : ViewModel() {

    var uiState by mutableStateOf(SectorFormUiState())
        private set

    var guardando by mutableStateOf(false)
        private set

    var sectorCreado by mutableStateOf(false)
        private set

    var errorGuardado by mutableStateOf<String?>(null)
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
        uiState = validarFormularioSector(uiState)

        return !uiState.errorNombre &&
                !uiState.errorDescripcion &&
                !uiState.errorUbicacion
    }

    fun guardarSector() {
        if (guardando || sectorCreado) return
        if (!validarFormulario()) return

        val solicitud = SectorRequest(
            nombre = uiState.nombre.trim(),
            descripcion = uiState.descripcion.trim(),
            ubicacion = uiState.ubicacion.trim(),
            estado = EstadoSector.DISPONIBLE.name,
            responsableId = 1
        )

        guardando = true
        errorGuardado = null

        viewModelScope.launch {
            try {
                repository.crearSector(solicitud)
                sectorCreado = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HuertoAPI", "Error al crear sector", e)
                errorGuardado =
                    "No se pudo confirmar el guardado. Actualiza la lista antes de volver a intentarlo."
            } finally {
                guardando = false
            }
        }
    }
}