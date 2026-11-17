package cl.duoc.huertoescolar.ui.screens.alertas

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.EstadoLectura
import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.LecturaSensor
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.screens.tareas.formatearFecha
import java.text.SimpleDateFormat
import java.util.Locale

private const val MILISEGUNDOS_DIA_ALERTA = 24L * 60 * 60 * 1000

internal fun construirAlertas(
    tareas: List<Tarea>,
    problemas: List<Problema>,
    lecturas: List<LecturaSensor>,
    cultivos: List<Cultivo>,
    usuario: UsuarioPrueba,
    nombreSector: (Int) -> String,
    ahora: Long = System.currentTimeMillis()
): List<SeccionAlerta> {
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

    fun linea(tarea: Tarea): String =
        "${tarea.descripcion} · ${nombreSector(tarea.sectorId)}" +
                " · ${formatearFecha(tarea.fechaProgramada)}"

    fun proximas(tipo: TipoTarea, dias: Int): List<String> {
        return tareasVisibles
            .filter {
                it.tipo == tipo &&
                        it.fechaProgramada in
                        ahora..(ahora + dias * MILISEGUNDOS_DIA_ALERTA) &&
                        (
                                it.estado == EstadoTarea.PENDIENTE ||
                                        it.estado == EstadoTarea.EN_DESARROLLO
                                )
            }
            .sortedBy { it.fechaProgramada }
            .map { linea(it) }
    }

    val atrasadas = tareasVisibles
        .filter { it.estaAtrasada(ahora) }
        .sortedBy { it.fechaProgramada }

    val cosechasEstimadas = cultivos.mapNotNull { cultivo ->
        val fecha = parsearFechaAlerta(cultivo.fechaCosechaEstimada)
            ?: return@mapNotNull null

        val activo = cultivo.estado != EstadoCultivo.COSECHADO &&
                cultivo.estado != EstadoCultivo.FINALIZADO

        if (
            activo &&
            fecha in ahora..(ahora + 60 * MILISEGUNDOS_DIA_ALERTA)
        ) {
            "Cosecha estimada de ${cultivo.nombre}" +
                    " · ${nombreSector(cultivo.sectorId)}" +
                    " · ${cultivo.fechaCosechaEstimada}"
        } else {
            null
        }
    }

    val secciones = mutableListOf(
        SeccionAlerta(
            titulo = "Próximos riegos (7 días)",
            lineas = proximas(TipoTarea.RIEGO, 7),
            textoVacio = "No hay riegos programados esta semana."
        ),
        SeccionAlerta(
            titulo = "Riegos atrasados",
            lineas = atrasadas
                .filter { it.tipo == TipoTarea.RIEGO }
                .map { linea(it) },
            textoVacio = "No hay riegos atrasados."
        ),
        SeccionAlerta(
            titulo = "Otras tareas atrasadas",
            lineas = atrasadas
                .filter { it.tipo != TipoTarea.RIEGO }
                .map { linea(it) },
            textoVacio = "No hay tareas atrasadas."
        ),
        SeccionAlerta(
            titulo = "Próximas cosechas",
            lineas = proximas(TipoTarea.COSECHA, 14) + cosechasEstimadas,
            textoVacio = "No hay cosechas próximas."
        ),
        SeccionAlerta(
            titulo = "Mantenimientos programados (30 días)",
            lineas = proximas(TipoTarea.MANTENIMIENTO, 30),
            textoVacio = "No hay mantenimientos programados."
        )
    )

    if (esAdmin) {
        secciones += SeccionAlerta(
            titulo = "Tareas pendientes de validación",
            lineas = tareas
                .filter { it.estado == EstadoTarea.PENDIENTE_VALIDACION }
                .map {
                    "${it.descripcion} · ${nombreSector(it.sectorId)}"
                },
            textoVacio = "No hay tareas por validar."
        )

        secciones += SeccionAlerta(
            titulo = "Problemas pendientes de revisión",
            lineas = problemas
                .filter {
                    it.estado == EstadoReporte.INFORMADO ||
                            it.estado == EstadoReporte.EN_REVISION
                }
                .map {
                    "${it.tipo.etiqueta} · ${nombreSector(it.sectorId)}" +
                            " · ${it.estado.etiqueta}"
                },
            textoVacio = "No hay problemas pendientes."
        )

        secciones += SeccionAlerta(
            titulo = "Lecturas simuladas fuera de rango (dato de prueba)",
            lineas = lecturas
                .groupBy { it.sectorId }
                .mapNotNull { (_, lecturasSector) ->
                    lecturasSector.maxByOrNull { it.fecha }
                }
                .filter {
                    it.estado == EstadoLectura.BAJO ||
                            it.estado == EstadoLectura.ALTO
                }
                .map {
                    "${nombreSector(it.sectorId)} · ${it.estado.etiqueta}" +
                            " · suelo ${it.humedadSuelo}%, " +
                            "${it.temperaturaAmbiente} °C, " +
                            "aire ${it.humedadAmbiente}%"
                },
            textoVacio = "No hay lecturas simuladas fuera de rango."
        )
    }

    return secciones
}

private fun parsearFechaAlerta(texto: String): Long? {
    return try {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale("es", "CL")
        ).parse(texto)?.time
    } catch (e: Exception) {
        null
    }
}