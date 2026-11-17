package cl.duoc.huertoescolar.domain.repository

import cl.duoc.huertoescolar.domain.model.Sector


interface SectoresRepository {

    val sectorSeleccionado: Sector?

    fun obtenerSiguienteId(): Int

    fun agregarSector(sector: Sector)

    fun actualizarSector(sectorActualizado: Sector)
}