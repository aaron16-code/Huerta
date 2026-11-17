package cl.duoc.huertoescolar.domain.repository

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.Sector

interface CultivosRepository {

    val sectores: List<Sector>

    val cultivoSeleccionado: Cultivo?

    fun obtenerSiguienteIdCultivo(): Int

    fun agregarCultivo(cultivo: Cultivo)

    fun actualizarCultivo(cultivoActualizado: Cultivo)
}