package cl.duoc.huertoescolar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object RepositorioHuerto {


    var cultivos by mutableStateOf(
        listOf(
            Cultivo(
                id = 1,
                nombre = "Lechugas",
                descripcion = "Cultivo de prueba",
                sectorId = 1,
                fechaSiembra = "01/10/2026",
                fechaCosechaEstimada = "30/11/2026",
                frecuenciaRiegoDias = 2,
                estado = EstadoCultivo.EN_CRECIMIENTO,
                observaciones = "Crecimiento normal"
            )
        )
    )
        private set

    var cultivoSeleccionado by mutableStateOf<Cultivo?>(null)
        private set

    fun agregarCultivo(cultivo: Cultivo) {
        cultivos = cultivos + cultivo
    }

    fun obtenerSiguienteIdCultivo(): Int {
        return (cultivos.maxOfOrNull { it.id } ?: 0) + 1
    }

    fun seleccionarCultivo(cultivo: Cultivo) {
        cultivoSeleccionado = cultivo
    }

    fun actualizarCultivo(cultivoActualizado: Cultivo) {
        cultivos = cultivos.map { cultivo ->
            if (cultivo.id == cultivoActualizado.id) {
                cultivoActualizado
            } else {
                cultivo
            }
        }

        cultivoSeleccionado = null
    }
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