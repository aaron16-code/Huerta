package cl.duoc.huertoescolar.ui.screens.historial

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.domain.model.EventoHistorial
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow

enum class FiltroHistorial(val etiqueta: String) {
    TODOS("Todos"),
    TAREAS("Tareas"),
    VALIDACIONES("Validaciones"),
    PROBLEMAS("Problemas")
}

class HistorialViewModel : ViewModel() {

    val tareas: StateFlow<List<Tarea>> =
        RepositorioOperacion.tareas

    val problemas: StateFlow<List<Problema>> =
        RepositorioOperacion.problemas

    var filtro by mutableStateOf(FiltroHistorial.TODOS)
        private set

    fun cambiarFiltro(nuevo: FiltroHistorial) {
        filtro = nuevo
    }

    fun eventos(
        todasTareas: List<Tarea>,
        todosProblemas: List<Problema>,
        usuario: UsuarioPrueba,
        nombreSector: (Int) -> String
    ): List<EventoHistorial> {
        return construirHistorial(
            tareas = todasTareas,
            problemas = todosProblemas,
            usuario = usuario,
            nombreSector = nombreSector,
            filtro = filtro
        )
    }
}