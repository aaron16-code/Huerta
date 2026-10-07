package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.EstadoSector

class EditarSectorViewModel : ViewModel() {



    private val sectorOriginal = RepositorioHuerto.sectorSeleccionado

    var estado by mutableStateOf(
        sectorOriginal?.estado ?: EstadoSector.DISPONIBLE
    )
    var nombre by mutableStateOf(sectorOriginal?.nombre.orEmpty())
    var descripcion by mutableStateOf(sectorOriginal?.descripcion.orEmpty())
    var ubicacion by mutableStateOf(sectorOriginal?.ubicacion.orEmpty())

    var errorNombre by mutableStateOf(false)
        private set

    var errorDescripcion by mutableStateOf(false)
        private set

    var errorUbicacion by mutableStateOf(false)
        private set

    fun guardarCambios(): Boolean {
        errorNombre = !validarNombreSector(nombre)
        errorDescripcion = !validarDescripcionSector(descripcion)
        errorUbicacion = !validarUbicacionSector(ubicacion)

        if (errorNombre || errorDescripcion || errorUbicacion) {
            return false
        }

        val sector = sectorOriginal ?: return false

        RepositorioHuerto.actualizarSector(
            sector.copy(
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                ubicacion = ubicacion.trim(),
                estado = estado
            )
        )

        return true
    }
}