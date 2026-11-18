package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoTarea
import cl.duoc.huertoescolar.ui.screens.tareas.FiltroTareas
import cl.duoc.huertoescolar.ui.screens.tareas.TareasEstudianteViewModel
import org.junit.Assert.*
import org.junit.Test

class TareasEstudianteViewModelTest {

    private val ahora = 1_800_000_000_000L

    private fun tarea(
        id: Int,
        estado: EstadoTarea = EstadoTarea.PENDIENTE,
        fecha: Long = ahora + 1000,
        responsable: Int? = 2,
        grupo: String? = null
    ) = Tarea(
        id = id,
        tipo = TipoTarea.RIEGO,
        descripcion = "Actividad $id",
        instrucciones = "Regar",
        sectorId = 1,
        cultivoId = null,
        fechaCreacion = ahora - 1000,
        fechaProgramada = fecha,
        responsableUsuarioId = responsable,
        grupoAsignado = grupo,
        creadaPorId = 1,
        estado = estado
    )

    @Test
    fun muestraSoloAsignacionesPropiasODeSuGrupoOrdenadasPorFecha() {
        val vm = TareasEstudianteViewModel()

        val propia = tarea(1, fecha = ahora + 2000)
        val grupal = tarea(
            2,
            fecha = ahora + 1000,
            responsable = null,
            grupo = "Grupo A"
        )

        val resultado = vm.misTareas(
            todas = listOf(
                propia,
                tarea(3, responsable = 3, grupo = "Grupo A"),
                grupal,
                tarea(4, responsable = null, grupo = "Grupo B"),
                tarea(5, responsable = null)
            ),
            usuarioId = 2,
            grupo = "Grupo A",
            ahora = ahora
        )

        assertEquals(FiltroTareas.TODAS, vm.filtro)
        assertEquals(listOf(grupal, propia), resultado)
    }

    @Test
    fun cadaFiltroDevuelveLosEstadosCorrespondientes() {
        val vm = TareasEstudianteViewModel()

        val todas = listOf(
            tarea(1),
            tarea(2, EstadoTarea.EN_DESARROLLO),
            tarea(3, fecha = ahora - 1),
            tarea(4, EstadoTarea.PENDIENTE_VALIDACION),
            tarea(5, EstadoTarea.VALIDADA),
            tarea(6, EstadoTarea.RECHAZADA),
            tarea(
                7,
                EstadoTarea.EN_DESARROLLO,
                fecha = ahora - 2
            )
        )

        val casos = listOf(
            FiltroTareas.TODAS to setOf(1, 2, 3, 4, 5, 6, 7),
            FiltroTareas.PENDIENTES to setOf(1, 2),
            FiltroTareas.ATRASADAS to setOf(3, 7),
            FiltroTareas.POR_VALIDAR to setOf(4),
            FiltroTareas.VALIDADAS to setOf(5),
            FiltroTareas.RECHAZADAS to setOf(6)
        )

        casos.forEach { (filtro, esperados) ->
            vm.cambiarFiltro(filtro)

            val resultado = vm.misTareas(
                todas, 2, "Grupo A", ahora
            )

            assertEquals(
                "Filtro $filtro",
                esperados,
                resultado.map { it.id }.toSet()
            )
        }
    }

    @Test
    fun filtroNoPermiteVerTareasDeOtroEstudiante() {
        val vm = TareasEstudianteViewModel()
        vm.cambiarFiltro(FiltroTareas.VALIDADAS)

        val resultado = vm.misTareas(
            listOf(
                tarea(1, EstadoTarea.VALIDADA, responsable = 3),
                tarea(2, EstadoTarea.PENDIENTE, responsable = 2)
            ),
            2,
            "Grupo A",
            ahora
        )

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun listaVaciaNoProduceResultados() {
        val vm = TareasEstudianteViewModel()

        assertTrue(
            vm.misTareas(emptyList(), 2, "Grupo A", ahora).isEmpty()
        )
    }
}