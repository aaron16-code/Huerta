package cl.duoc.huertoescolar.domain.repository

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.Sector

/** Datos del huerto que necesita el formulario de tareas. */
interface CatalogoHuertoRepository {
    val sectores: List<Sector>
    val cultivos: List<Cultivo>
}
