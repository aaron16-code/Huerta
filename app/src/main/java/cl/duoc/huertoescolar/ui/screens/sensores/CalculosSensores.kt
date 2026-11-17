package cl.duoc.huertoescolar.ui.screens.sensores

import cl.duoc.huertoescolar.domain.model.LecturaSensor

internal fun agruparLecturasPorSector(
    lecturas: List<LecturaSensor>
): List<LecturasDeSector> {
    return lecturas
        .groupBy { it.sectorId }
        .toSortedMap()
        .map { (sectorId, lecturasSector) ->
            val ordenadas = lecturasSector.sortedByDescending {
                it.fecha
            }

            LecturasDeSector(
                sectorId = sectorId,
                ultima = ordenadas.first(),
                anteriores = ordenadas.drop(1).take(3)
            )
        }
}