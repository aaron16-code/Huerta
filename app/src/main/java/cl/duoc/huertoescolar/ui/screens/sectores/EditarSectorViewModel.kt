package cl.duoc.huertoescolar.ui.screens.sectores

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.EstadoSector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class EditarSectorViewModel(
    private val repository: HuertoRemoteRepository = HuertoRemoteRepository()
) : ViewModel() {

    private val sectorOriginal = RepositorioHuerto.sectorSeleccionado

    var uiState by mutableStateOf(
        SectorFormUiState(
            nombre = sectorOriginal?.nombre.orEmpty(),
            descripcion = sectorOriginal?.descripcion.orEmpty(),
            ubicacion = sectorOriginal?.ubicacion.orEmpty(),
            estado = sectorOriginal?.estado ?: EstadoSector.DISPONIBLE
        )
    )
        private set

    var guardando by mutableStateOf(false)
        private set

    var sectorActualizado by mutableStateOf(false)
        private set

    var errorGuardado by mutableStateOf<String?>(
        if (sectorOriginal == null) {
            "No hay un sector seleccionado. Vuelve a la lista."
        } else {
            null
        }
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

    fun guardarCambios() {
        if (guardando || sectorActualizado) return

        val sector = sectorOriginal ?: return
        val estadoValidado = validarFormularioSector(uiState)
        uiState = estadoValidado

        if (
            estadoValidado.errorNombre ||
            estadoValidado.errorDescripcion ||
            estadoValidado.errorUbicacion
        ) {
            return
        }

        guardando = true
        errorGuardado = null

        viewModelScope.launch {
            try {
                repository.actualizarSector(
                    id = sector.id,
                    nombre = estadoValidado.nombre.trim(),
                    descripcion = estadoValidado.descripcion.trim(),
                    ubicacion = estadoValidado.ubicacion.trim(),
                    estado = estadoValidado.estado
                )

                sectorActualizado = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HuertoAPI", "Error al editar sector", e)
                errorGuardado =
                    "No se pudo confirmar el guardado. Vuelve a la lista y actualízala para comprobar los cambios."
            } finally {
                guardando = false
            }
        }
    }
}