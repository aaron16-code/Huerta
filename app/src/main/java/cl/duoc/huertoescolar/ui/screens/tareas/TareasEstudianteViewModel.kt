package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import kotlinx.coroutines.flow.StateFlow

enum class FiltroTareas(val etiqueta: String) {
    TODAS("Todas"),
    PENDIENTES("Pendientes"),
    ATRASADAS("Atrasadas"),
    POR_VALIDAR("Por validar"),
    VALIDADAS("Validadas"),
    RECHAZADAS("Rechazadas")
}

class TareasEstudianteViewModel : ViewModel() {

    val tareas: StateFlow<List<Tarea>> = RepositorioOperacion.tareas

    var filtro by mutableStateOf(FiltroTareas.TODAS)
        private set

    fun cambiarFiltro(nuevo: FiltroTareas) {
        filtro = nuevo
    }

    /** Tareas del estudiante (suyas o de su grupo), filtradas y ordenadas por fecha programada. */
    fun misTareas(
        todas: List<Tarea>,
        usuarioId: Int,
        grupo: String,
        ahora: Long = System.currentTimeMillis()
    ): List<Tarea> =
        todas
            .filter {
                it.responsableUsuarioId == usuarioId ||
                        (it.responsableUsuarioId == null && it.grupoAsignado == grupo)
            }
            .filter { cumpleFiltro(it, filtro, ahora) }
            .sortedBy { it.fechaProgramada }

    private fun cumpleFiltro(t: Tarea, f: FiltroTareas, ahora: Long): Boolean = when (f) {
        FiltroTareas.TODAS -> true
        FiltroTareas.PENDIENTES -> t.estadoVisible(ahora).let {
            it == EstadoTarea.PENDIENTE || it == EstadoTarea.EN_DESARROLLO
        }
        FiltroTareas.ATRASADAS -> t.estaAtrasada(ahora)
        FiltroTareas.POR_VALIDAR -> t.estado == EstadoTarea.PENDIENTE_VALIDACION
        FiltroTareas.VALIDADAS -> t.estado == EstadoTarea.VALIDADA
        FiltroTareas.RECHAZADAS -> t.estado == EstadoTarea.RECHAZADA
    }
}