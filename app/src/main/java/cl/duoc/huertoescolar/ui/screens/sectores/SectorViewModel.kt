package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.EstadoSector
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.Sector

class SectorViewModel : ViewModel() {

    var nombre by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var ubicacion by mutableStateOf("")

    var errorNombre by mutableStateOf(false)
        private set

    var errorDescripcion by mutableStateOf(false)
        private set

    var errorUbicacion by mutableStateOf(false)
        private set

    fun validarFormulario(): Boolean {
        errorNombre = !validarNombreSector(nombre)
        errorDescripcion = !validarDescripcionSector(descripcion)
        errorUbicacion = !validarUbicacionSector(ubicacion)

        return !errorNombre &&
                !errorDescripcion &&
                !errorUbicacion
    }

    fun guardarSector(): Boolean {
        if (!validarFormulario()) {
            return false
        }

        val nuevoSector = Sector(
            id = RepositorioHuerto.obtenerSiguienteId(),
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            ubicacion = ubicacion.trim(),
            estado = EstadoSector.DISPONIBLE,
            responsable = "Docente de prueba"
        )

        RepositorioHuerto.agregarSector(nuevoSector)
        return true
    }
}