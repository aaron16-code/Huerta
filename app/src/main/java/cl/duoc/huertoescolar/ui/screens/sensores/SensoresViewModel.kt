package cl.duoc.huertoescolar.ui.screens.sensores

import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.LecturaSensor
import cl.duoc.huertoescolar.data.RepositorioOperacion
import kotlinx.coroutines.flow.StateFlow

/** Lecturas de un sector: la más reciente y hasta tres anteriores. */
data class LecturasDeSector(
    val sectorId: Int,
    val ultima: LecturaSensor,
    val anteriores: List<LecturaSensor>
)

class SensoresViewModel : ViewModel() {

    // Todas las lecturas son simuladas: no hay conexión con dispositivos físicos.
    val lecturas: StateFlow<List<LecturaSensor>> = RepositorioOperacion.lecturas

    fun agrupar(todas: List<LecturaSensor>): List<LecturasDeSector> =
        todas.groupBy { it.sectorId }
            .toSortedMap()
            .map { (sectorId, lista) ->
                val ordenadas = lista.sortedByDescending { it.fecha }
                LecturasDeSector(
                    sectorId = sectorId,
                    ultima = ordenadas.first(),
                    anteriores = ordenadas.drop(1).take(3)
                )
            }
}