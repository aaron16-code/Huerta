package cl.duoc.huertoescolar.ui.screens.historial

import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.EventoHistorial
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoEvento
import cl.duoc.huertoescolar.data.UsuarioPrueba

internal fun construirHistorial(
    tareas: List<Tarea>,
    problemas: List<Problema>,
    usuario: UsuarioPrueba,
    nombreSector: (Int) -> String,
    filtro: FiltroHistorial
): List<EventoHistorial> {
    val esAdmin = usuario.rol == RolUsuario.ADMINISTRADOR

    val tareasVisibles = if (esAdmin) {
        tareas
    } else {
        tareas.filter {
            it.responsableUsuarioId == usuario.id ||
                    (
                            it.responsableUsuarioId == null &&
                                    it.grupoAsignado == usuario.grupo
                            )
        }
    }

    val problemasVisibles = if (esAdmin) {
        problemas
    } else {
        problemas.filter { it.reportadoPorId == usuario.id }
    }

    val eventos = mutableListOf<EventoHistorial>()

    tareasVisibles.forEach { tarea ->
        val detalleTarea =
            "${tarea.tipo.etiqueta} · ${nombreSector(tarea.sectorId)}"

        tarea.fechaRealizacion?.let { fecha ->
            eventos += EventoHistorial(
                fecha = fecha,
                tipo = TipoEvento.TAREA,
                titulo = "Tarea realizada: $detalleTarea",
                detalle = tarea.comentarioEstudiante,
                fotoUri = tarea.fotoUri
            )
        }

        tarea.fechaValidacion?.let { fecha ->
            val resultado = if (tarea.estado == EstadoTarea.VALIDADA) {
                "validada"
            } else {
                "rechazada"
            }

            eventos += EventoHistorial(
                fecha = fecha,
                tipo = TipoEvento.VALIDACION,
                titulo = "Tarea $resultado: $detalleTarea",
                detalle = tarea.comentarioDocente
            )
        }
    }

    problemasVisibles.forEach { problema ->
        eventos += EventoHistorial(
            fecha = problema.fechaReporte,
            tipo = TipoEvento.PROBLEMA,
            titulo = "Problema reportado: ${problema.tipo.etiqueta}" +
                    " · ${nombreSector(problema.sectorId)}",
            detalle = problema.descripcion,
            fotoUri = problema.fotoUri
        )
    }

    return eventos
        .filter { evento ->
            when (filtro) {
                FiltroHistorial.TODOS -> true
                FiltroHistorial.TAREAS ->
                    evento.tipo == TipoEvento.TAREA
                FiltroHistorial.VALIDACIONES ->
                    evento.tipo == TipoEvento.VALIDACION
                FiltroHistorial.PROBLEMAS ->
                    evento.tipo == TipoEvento.PROBLEMA
            }
        }
        .sortedByDescending { it.fecha }
}