package cl.duoc.huertoescolar.ui.screens.sensores

import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.domain.model.LecturaSensor
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import kotlinx.coroutines.flow.StateFlow

data class LecturasDeSector(
    val sectorId: Int,
    val ultima: LecturaSensor,
    val anteriores: List<LecturaSensor>
)

class SensoresViewModel(
    val lecturas: StateFlow<List<LecturaSensor>> =
        RepositorioOperacion.lecturas
) : ViewModel() {

    fun agrupar(
        todas: List<LecturaSensor>
    ): List<LecturasDeSector> {
        return agruparLecturasPorSector(todas)
    }
}