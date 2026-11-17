package cl.duoc.huertoescolar.ui.screens.cultivos

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.remote.CultivoRequest
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.Sector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CultivoViewModel(
    private val repository: HuertoRemoteRepository = HuertoRemoteRepository()
) : ViewModel() {

    var sectores by mutableStateOf<List<Sector>>(emptyList())
        private set

    var uiState by mutableStateOf(CultivoFormUiState())
        private set

    var cargandoSectores by mutableStateOf(false)
        private set

    var errorSectores by mutableStateOf<String?>(null)
        private set

    var guardando by mutableStateOf(false)
        private set

    var cultivoCreado by mutableStateOf(false)
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

    init {
        cargarSectores()
    }

    fun cargarSectores() {
        if (cargandoSectores) return

        cargandoSectores = true
        errorSectores = null

        viewModelScope.launch {
            try {
                sectores = repository.obtenerSectores().map { dto ->
                    Sector(
                        id = dto.id,
                        nombre = dto.nombre,
                        descripcion = dto.descripcion,
                        ubicacion = dto.ubicacion,
                        estado = EstadoSector.valueOf(dto.estado),
                        responsable = dto.responsableId?.let {
                            "Usuario #$it"
                        } ?: "Sin responsable"
                    )
                }

                val seleccionValida = sectores.any {
                    it.id == uiState.sectorId
                }

                if (!seleccionValida) {
                    uiState = uiState.copy(
                        sectorId = sectores.firstOrNull()?.id
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HuertoAPI", "Error al cargar sectores", e)
                errorSectores = "No se pudieron cargar los sectores."
            } finally {
                cargandoSectores = false
            }
        }
    }

    fun guardarCultivo() {
        if (guardando || cultivoCreado || cargandoSectores) return
        if (errorSectores != null) return

        val validado = validarFormularioCultivo(uiState)
        uiState = validado

        if (
            validado.errorNombre ||
            validado.errorDescripcion ||
            validado.errorSector ||
            validado.errorFechaSiembra ||
            validado.errorFechaCosecha ||
            validado.errorFrecuencia
        ) {
            return
        }

        val idSector = validado.sectorId
        if (idSector == null || sectores.none { it.id == idSector }) {
            uiState = uiState.copy(errorSector = true)
            return
        }

        val frecuencia = validado.frecuenciaRiego.trim().toIntOrNull()
        if (frecuencia == null || frecuencia <= 0) {
            uiState = uiState.copy(errorFrecuencia = true)
            return
        }

        val solicitud = CultivoRequest(
            nombre = validado.nombre.trim(),
            descripcion = validado.descripcion.trim(),
            sectorId = idSector,
            fechaSiembra = validado.fechaSiembra.trim(),
            fechaCosechaEstimada = validado.fechaCosecha.trim(),
            frecuenciaRiegoDias = frecuencia,
            estado = EstadoCultivo.PLANIFICADO.name,
            observaciones = validado.observaciones.trim()
        )

        guardando = true
        errorGuardado = null

        viewModelScope.launch {
            try {
                repository.crearCultivo(solicitud)
                cultivoCreado = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HuertoAPI", "Error al crear cultivo", e)
                errorGuardado =
                    "No se pudo confirmar el guardado. Actualiza la lista antes de volver a intentarlo."
            } finally {
                guardando = false
            }
        }
    }
}