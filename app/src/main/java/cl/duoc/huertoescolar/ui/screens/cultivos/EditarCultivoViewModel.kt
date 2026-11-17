package cl.duoc.huertoescolar.ui.screens.cultivos

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.remote.CultivoRequest
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.Sector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class EditarCultivoViewModel(
    private val repository: HuertoRemoteRepository = HuertoRemoteRepository()
) : ViewModel() {

    private val cultivoOriginal = RepositorioHuerto.cultivoSeleccionado

    var sectores by mutableStateOf<List<Sector>>(emptyList())
        private set

    var cargandoSectores by mutableStateOf(false)
        private set

    var errorSectores by mutableStateOf<String?>(null)
        private set

    var guardando by mutableStateOf(false)
        private set

    var cultivoActualizado by mutableStateOf(false)
        private set

    var errorGuardado by mutableStateOf<String?>(
        if (cultivoOriginal == null) {
            "No hay un cultivo seleccionado. Vuelve a la lista."
        } else {
            null
        }
    )
        private set

    var uiState by mutableStateOf(
        CultivoFormUiState(
            nombre = cultivoOriginal?.nombre.orEmpty(),
            descripcion = cultivoOriginal?.descripcion.orEmpty(),
            sectorId = cultivoOriginal?.sectorId,
            fechaSiembra = cultivoOriginal?.fechaSiembra.orEmpty(),
            fechaCosecha = cultivoOriginal?.fechaCosechaEstimada.orEmpty(),
            frecuenciaRiego = cultivoOriginal?.frecuenciaRiegoDias
                ?.toString().orEmpty(),
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

    fun guardarCambios() {
        if (guardando || cultivoActualizado || cargandoSectores) return
        if (errorSectores != null) return

        val cultivo = cultivoOriginal ?: return
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
            estado = validado.estado.name,
            observaciones = validado.observaciones.trim(),
            fotoUri = cultivo.fotoUri
        )

        guardando = true
        errorGuardado = null

        viewModelScope.launch {
            try {
                repository.actualizarCultivo(cultivo.id, solicitud)
                cultivoActualizado = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HuertoAPI", "Error al editar cultivo", e)
                errorGuardado =
                    "No se pudo confirmar el guardado. Vuelve a la lista y actualízala para comprobar los cambios."
            } finally {
                guardando = false
            }
        }
    }
}