package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.Cultivo
import cl.duoc.huertoescolar.EstadoCultivo
import cl.duoc.huertoescolar.RepositorioHuerto

class CultivoViewModel : ViewModel() {

    var nombre by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var sectorId by mutableStateOf(
        RepositorioHuerto.sectores.firstOrNull()?.id
    )
    var fechaSiembra by mutableStateOf("")
    var fechaCosecha by mutableStateOf("")
    var frecuenciaRiego by mutableStateOf("")
    var observaciones by mutableStateOf("")

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

    fun guardarCultivo(): Boolean {
        errorNombre = !validarNombreCultivo(nombre)
        errorDescripcion = !validarDescripcionCultivo(descripcion)
        errorSector = sectorId == null
        errorFechaSiembra = !validarFechaCultivo(fechaSiembra)
        errorFechaCosecha = !validarFechaCultivo(fechaCosecha)
        errorFrecuencia = !validarFrecuenciaRiego(frecuenciaRiego)

        if (
            errorNombre ||
            errorDescripcion ||
            errorSector ||
            errorFechaSiembra ||
            errorFechaCosecha ||
            errorFrecuencia
        ) {
            return false
        }

        val nuevoCultivo = Cultivo(
            id = RepositorioHuerto.obtenerSiguienteIdCultivo(),
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            sectorId = sectorId ?: return false,
            fechaSiembra = fechaSiembra.trim(),
            fechaCosechaEstimada = fechaCosecha.trim(),
            frecuenciaRiegoDias = frecuenciaRiego.toInt(),
            estado = EstadoCultivo.PLANIFICADO,
            observaciones = observaciones.trim()
        )

        RepositorioHuerto.agregarCultivo(nuevoCultivo)
        return true
    }
}