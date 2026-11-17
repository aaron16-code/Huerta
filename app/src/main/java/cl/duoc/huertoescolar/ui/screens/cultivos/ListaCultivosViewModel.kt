package cl.duoc.huertoescolar.ui.screens.cultivos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListaCultivosUiState(
    val cargando: Boolean = false,
    val cultivos: List<Cultivo> = emptyList(),
    val nombresSectores: Map<Int, String> = emptyMap(),
    val error: String? = null
)

class ListaCultivosViewModel(
    private val repository: HuertoRemoteRepository = HuertoRemoteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaCultivosUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarCultivos()
    }

    fun cargarCultivos() {
        if (_uiState.value.cargando) return

        _uiState.value = _uiState.value.copy(
            cargando = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val sectores = repository.obtenerSectores()
                val cultivos = repository.obtenerCultivos().map { dto ->
                    Cultivo(
                        id = dto.id,
                        nombre = dto.nombre,
                        descripcion = dto.descripcion,
                        sectorId = dto.sectorId,
                        fechaSiembra = dto.fechaSiembra,
                        fechaCosechaEstimada = dto.fechaCosechaEstimada,
                        frecuenciaRiegoDias = dto.frecuenciaRiegoDias,
                        estado = EstadoCultivo.valueOf(dto.estado),
                        observaciones = dto.observaciones,
                        fotoUri = dto.fotoUri
                    )
                }

                _uiState.value = ListaCultivosUiState(
                    cultivos = cultivos,
                    nombresSectores = sectores.associate {
                        it.id to it.nombre
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HuertoAPI", "Error al obtener cultivos", e)

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudieron cargar los cultivos. Intenta nuevamente."
                )
            }
        }
    }
}