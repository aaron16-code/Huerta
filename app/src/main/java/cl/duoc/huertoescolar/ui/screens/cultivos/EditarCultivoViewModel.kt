package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.EstadoCultivo
import cl.duoc.huertoescolar.RepositorioHuerto

class EditarCultivoViewModel : ViewModel() {
    private val cultivoOriginal = RepositorioHuerto.cultivoSeleccionado

    var nombre by mutableStateOf(cultivoOriginal?.nombre.orEmpty())
    var descripcion by mutableStateOf(cultivoOriginal?.descripcion.orEmpty())
    var sectorId by mutableStateOf(cultivoOriginal?.sectorId)
    var fechaSiembra by mutableStateOf(cultivoOriginal?.fechaSiembra.orEmpty())
    var fechaCosecha by mutableStateOf(cultivoOriginal?.fechaCosechaEstimada.orEmpty())
    var frecuenciaRiego by mutableStateOf(cultivoOriginal?.frecuenciaRiegoDias?.toString().orEmpty())
    var observaciones by mutableStateOf(cultivoOriginal?.observaciones.orEmpty())
    var estado by mutableStateOf(cultivoOriginal?.estado ?: EstadoCultivo.PLANIFICADO)

    var errorNombre by mutableStateOf(false)
        private set
    var errorDescripcion by mutableStateOf(false)
        private set
    var errorSector by mutableStateOf(false)
        private set
    var errorFechaSiembra by mutableStateOf(false)
        private set
    var errorFechaCosecha by mutableStateOf(false)
        private set
    var errorFrecuencia by mutableStateOf(false)
        private set

    fun guardarCambios(): Boolean {
        errorNombre = !validarNombreCultivo(nombre)
        errorDescripcion = !validarDescripcionCultivo(descripcion)
        errorSector = sectorId == null
        errorFechaSiembra = !validarFechaCultivo(fechaSiembra)
        errorFechaCosecha = !validarFechaCultivo(fechaCosecha)
        errorFrecuencia = !validarFrecuenciaRiego(frecuenciaRiego)

        if (errorNombre || errorDescripcion || errorSector || errorFechaSiembra ||
            errorFechaCosecha || errorFrecuencia
        ) return false

        val cultivo = cultivoOriginal ?: return false
        RepositorioHuerto.actualizarCultivo(
            cultivo.copy(
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                sectorId = sectorId ?: return false,
                fechaSiembra = fechaSiembra.trim(),
                fechaCosechaEstimada = fechaCosecha.trim(),
                frecuenciaRiegoDias = frecuenciaRiego.toInt(),
                observaciones = observaciones.trim(),
                estado = estado
            )
        )
        return true
    }
}
