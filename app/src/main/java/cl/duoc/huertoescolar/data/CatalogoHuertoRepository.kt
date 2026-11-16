package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.Cultivo
import cl.duoc.huertoescolar.Sector

/** Datos del huerto que necesita el formulario de tareas. */
interface CatalogoHuertoRepository {
    val sectores: List<Sector>
    val cultivos: List<Cultivo>
}
