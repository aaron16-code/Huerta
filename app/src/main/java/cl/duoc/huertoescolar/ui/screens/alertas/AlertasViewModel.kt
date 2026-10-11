package cl.duoc.huertoescolar.ui.screens.alertas

import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.EstadoCultivo
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.EstadoLectura
import cl.duoc.huertoescolar.data.EstadoReporte
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.LecturaSensor
import cl.duoc.huertoescolar.data.Problema
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.screens.tareas.formatearFecha
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale

private const val DIA = 24L * 60 * 60 * 1000

/** Un grupo de avisos (por ejemplo "Riegos atrasados") con sus líneas de texto. */
data class SeccionAlerta(
    val titulo: String,
    val lineas: List<String>,
    val textoVacio: String
)

class AlertasViewModel : ViewModel() {

    val tareas: StateFlow<List<Tarea>> = RepositorioOperacion.tareas
    val problemas: StateFlow<List<Problema>> = RepositorioOperacion.problemas
    val lecturas: StateFlow<List<LecturaSensor>> = RepositorioOperacion.lecturas

    /**
     * Arma los avisos. Son solo informativos: no activan nada.
     * El estudiante ve los avisos de sus tareas; el docente ve todo, más validaciones, problemas y sensores.
     */
    fun secciones(
        todas: List<Tarea>,
        listaProblemas: List<Problema>,
        listaLecturas: List<LecturaSensor>,
        usuario: UsuarioPrueba,
        nombreSector: (Int) -> String,
        ahora: Long = System.currentTimeMillis()
    ): List<SeccionAlerta> {
        val esAdmin = usuario.rol == RolUsuario.ADMINISTRADOR
        val visibles = if (esAdmin) todas else todas.filter {
            it.responsableUsuarioId == usuario.id ||
                    (it.responsableUsuarioId == null && it.grupoAsignado == usuario.grupo)
        }

        fun linea(t: Tarea) =
            "${t.descripcion} · ${nombreSector(t.sectorId)} · ${formatearFecha(t.fechaProgramada)}"

        fun proximas(tipo: TipoTarea, dias: Int): List<String> = visibles
            .filter {
                it.tipo == tipo &&
                        it.fechaProgramada in ahora..(ahora + dias * DIA) &&
                        (it.estado == EstadoTarea.PENDIENTE || it.estado == EstadoTarea.EN_DESARROLLO)
            }
            .sortedBy { it.fechaProgramada }
            .map { linea(it) }

        val atrasadas = visibles.filter { it.estaAtrasada(ahora) }.sortedBy { it.fechaProgramada }

        // Cosechas estimadas que vienen de los cultivos registrados (próximos 60 días)
        val cosechasEstimadas = RepositorioHuerto.cultivos.mapNotNull { c ->
            val fecha = parsearFecha(c.fechaCosechaEstimada) ?: return@mapNotNull null
            val activo = c.estado != EstadoCultivo.COSECHADO && c.estado != EstadoCultivo.FINALIZADO
            if (activo && fecha in ahora..(ahora + 60 * DIA)) {
                "Cosecha estimada de ${c.nombre} · ${nombreSector(c.sectorId)} · ${c.fechaCosechaEstimada}"
            } else null
        }

        val secciones = mutableListOf(
            SeccionAlerta(
                "Próximos riegos (7 días)",
                proximas(TipoTarea.RIEGO, 7),
                "No hay riegos programados esta semana."
            ),
            SeccionAlerta(
                "Riegos atrasados",
                atrasadas.filter { it.tipo == TipoTarea.RIEGO }.map { linea(it) },
                "No hay riegos atrasados."
            ),
            SeccionAlerta(
                "Otras tareas atrasadas",
                atrasadas.filter { it.tipo != TipoTarea.RIEGO }.map { linea(it) },
                "No hay tareas atrasadas."
            ),
            SeccionAlerta(
                "Próximas cosechas",
                proximas(TipoTarea.COSECHA, 14) + cosechasEstimadas,
                "No hay cosechas próximas."
            ),
            SeccionAlerta(
                "Mantenimientos programados (30 días)",
                proximas(TipoTarea.MANTENIMIENTO, 30),
                "No hay mantenimientos programados."
            )
        )

        if (esAdmin) {
            secciones += SeccionAlerta(
                "Tareas pendientes de validación",
                todas.filter { it.estado == EstadoTarea.PENDIENTE_VALIDACION }
                    .map { "${it.descripcion} · ${nombreSector(it.sectorId)}" },
                "No hay tareas por validar."
            )
            secciones += SeccionAlerta(
                "Problemas pendientes de revisión",
                listaProblemas
                    .filter { it.estado == EstadoReporte.INFORMADO || it.estado == EstadoReporte.EN_REVISION }
                    .map { "${it.tipo.etiqueta} · ${nombreSector(it.sectorId)} · ${it.estado.etiqueta}" },
                "No hay problemas pendientes."
            )
            // Última lectura simulada de cada sector, solo si está fuera de rango
            secciones += SeccionAlerta(
                "Lecturas simuladas fuera de rango (dato de prueba)",
                listaLecturas.groupBy { it.sectorId }
                    .mapNotNull { (_, lecturasSector) -> lecturasSector.maxByOrNull { it.fecha } }
                    .filter { it.estado == EstadoLectura.BAJO || it.estado == EstadoLectura.ALTO }
                    .map {
                        "${nombreSector(it.sectorId)} · ${it.estado.etiqueta} · suelo ${it.humedadSuelo}%, " +
                                "${it.temperaturaAmbiente} °C, aire ${it.humedadAmbiente}%"
                    },
                "No hay lecturas simuladas fuera de rango."
            )
        }
        return secciones
    }

    private fun parsearFecha(texto: String): Long? = try {
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).parse(texto)?.time
    } catch (e: Exception) {
        null
    }
}