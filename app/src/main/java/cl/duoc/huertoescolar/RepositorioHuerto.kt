package cl.duoc.huertoescolar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object RepositorioHuerto {

    var sectores by mutableStateOf(
        listOf(
            Sector(
                id = 1,
                nombre = "Bancal 1",
                descripcion = "Sector de hortalizas",
                ubicacion = "Zona norte",
                estado = EstadoSector.EN_USO,
                responsable = "Docente de prueba"
            )
        )
    )
        private set

    fun agregarSector(sector: Sector) {
        sectores = sectores + sector
    }

    fun obtenerSiguienteId(): Int {
        return (sectores.maxOfOrNull { it.id } ?: 0) + 1
    }

    var sectorSeleccionado by mutableStateOf<Sector?>(null)
        private set

    fun seleccionarSector(sector: Sector) {
        sectorSeleccionado = sector
    }

    fun actualizarSector(sectorActualizado: Sector) {
        sectores = sectores.map { sector ->
            if (sector.id == sectorActualizado.id) {
                sectorActualizado
            } else {
                sector
            }
        }

        sectorSeleccionado = null
    }


}