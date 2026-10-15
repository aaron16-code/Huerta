package cl.duoc.huertoescolar.ui.screens.historial

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.EventoHistorial
import cl.duoc.huertoescolar.data.Problema
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.TipoEvento
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow

enum class FiltroHistorial(val etiqueta: String) {
    TODOS("Todos"),
    TAREAS("Tareas"),
    VALIDACIONES("Validaciones"),
    PROBLEMAS("Problemas")
}

class HistorialViewModel : ViewModel() {

    val tareas: StateFlow<List<Tarea>> = RepositorioOperacion.tareas
    val problemas: StateFlow<List<Problema>> = RepositorioOperacion.problemas

    var filtro by mutableStateOf(FiltroHistorial.TODOS)
        private set

    fun cambiarFiltro(nuevo: FiltroHistorial) {
        filtro = nuevo
    }

    /**
     * Arma el historial en orden cronológico (lo más reciente primero).
     * El docente ve todo; el estudiante, solo lo suyo.
     */
    fun eventos(
        todasTareas: List<Tarea>,
        todosProblemas: List<Problema>,
        usuario: UsuarioPrueba,
        nombreSector: (Int) -> String
    ): List<EventoHistorial> {
        val esAdmin = usuario.rol == RolUsuario.ADMINISTRADOR
        val misTareas = if (esAdmin) todasTareas else todasTareas.filter {
            it.responsableUsuarioId == usuario.id ||
                    (it.responsableUsuarioId == null && it.grupoAsignado == usuario.grupo)
        }
        val misProblemas = if (esAdmin) todosProblemas
        else todosProblemas.filter { it.reportadoPorId == usuario.id }

        val lista = mutableListOf<EventoHistorial>()

        misTareas.forEach { tarea ->
            val detalleTarea = "${tarea.tipo.etiqueta} · ${nombreSector(tarea.sectorId)}"
            tarea.fechaRealizacion?.let {
                lista += EventoHistorial(
                    fecha = it,
                    tipo = TipoEvento.TAREA,
                    titulo = "Tarea realizada: $detalleTarea",
                    detalle = tarea.comentarioEstudiante,
                    fotoUri = tarea.fotoUri
                )
            }
            tarea.fechaValidacion?.let {
                val resultado = if (tarea.estado == EstadoTarea.VALIDADA) "validada" else "rechazada"
                lista += EventoHistorial(
                    fecha = it,
                    tipo = TipoEvento.VALIDACION,
                    titulo = "Tarea $resultado: $detalleTarea",
                    detalle = tarea.comentarioDocente
                )
            }
        }

        misProblemas.forEach { problema ->
            lista += EventoHistorial(
                fecha = problema.fechaReporte,
                tipo = TipoEvento.PROBLEMA,
                titulo = "Problema reportado: ${problema.tipo.etiqueta} · ${nombreSector(problema.sectorId)}",
                detalle = problema.descripcion,
                fotoUri = problema.fotoUri
            )
        }

        return lista
            .filter { cumpleFiltro(it.tipo) }
            .sortedByDescending { it.fecha }
    }

    private fun cumpleFiltro(tipo: TipoEvento): Boolean = when (filtro) {
        FiltroHistorial.TODOS -> true
        FiltroHistorial.TAREAS -> tipo == TipoEvento.TAREA
        FiltroHistorial.VALIDACIONES -> tipo == TipoEvento.VALIDACION
        FiltroHistorial.PROBLEMAS -> tipo == TipoEvento.PROBLEMA
    }
}