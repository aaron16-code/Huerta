package cl.duoc.huertoescolar.ui.screens.sectores

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListaSectoresUiState(
    val cargando: Boolean = false,
    val sectores: List<SectorDto> = emptyList(),
    val error: String? = null
)

class ListaSectoresViewModel(
    private val repository: HuertoRemoteRepository = HuertoRemoteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaSectoresUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarSectores()
    }

    fun cargarSectores() {
        if (_uiState.value.cargando) return

        _uiState.value = _uiState.value.copy(
            cargando = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val sectores = repository.obtenerSectores()

                _uiState.value = ListaSectoresUiState(
                    sectores = sectores
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    "HuertoAPI",
                    "Error al obtener sectores",
                    e
                )

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudieron cargar los sectores. Intenta nuevamente."
                )
            }
        }
    }
}