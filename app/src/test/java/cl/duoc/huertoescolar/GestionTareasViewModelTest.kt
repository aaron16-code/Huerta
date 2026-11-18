package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoTarea
import cl.duoc.huertoescolar.ui.screens.tareas.FiltroTareas
import cl.duoc.huertoescolar.ui.screens.tareas.GestionTareasViewModel
import cl.duoc.huertoescolar.ui.screens.tareas.formatearFecha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GestionTareasViewModelTest {

    private val ahora = 1_800_000_000_000L

    private fun tarea(
        id: Int,
        estado: EstadoTarea = EstadoTarea.PENDIENTE,
        fecha: Long = ahora + 1000,
        descripcion: String = "Actividad $id",
        responsable: Int? = 2,
        grupo: String? = null,
        tipo: TipoTarea = TipoTarea.RIEGO
    ) = Tarea(
        id = id,
        tipo = tipo,
        descripcion = descripcion,
        instrucciones = "Instrucciones",
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
    fun inicialmenteMuestraTodasOrdenadasPorFecha() {
        val vm = GestionTareasViewModel()
        val reciente = tarea(1, fecha = ahora + 3000)
        val antigua = tarea(2, fecha = ahora - 1000)
        val intermedia = tarea(3, fecha = ahora + 1000)

        assertEquals(FiltroTareas.TODAS, vm.filtro)
        assertEquals("", vm.busqueda)

        assertEquals(
            listOf(antigua, intermedia, reciente),
            vm.visibles(
                listOf(reciente, antigua, intermedia),
                ahora
            )
        )
        assertTrue(vm.visibles(emptyList(), ahora).isEmpty())
    }

    @Test
    fun filtroPendientesExcluyeAtrasadasYEntregadas() {
        val vm = GestionTareasViewModel()
        vm.cambiarFiltro(FiltroTareas.PENDIENTES)

        val pendiente = tarea(1)
        val enDesarrollo = tarea(2, EstadoTarea.EN_DESARROLLO)

        val resultado = vm.visibles(
            listOf(
                pendiente,
                enDesarrollo,
                tarea(3, fecha = ahora - 1),
                tarea(4, EstadoTarea.PENDIENTE_VALIDACION),
                tarea(5, EstadoTarea.VALIDADA)
            ),
            ahora
        )

        assertEquals(setOf(1, 2), resultado.map { it.id }.toSet())
    }

    @Test
    fun filtroAtrasadasRespetaFechaYEstado() {
        val vm = GestionTareasViewModel()
        vm.cambiarFiltro(FiltroTareas.ATRASADAS)

        val resultado = vm.visibles(
            listOf(
                tarea(1, fecha = ahora - 1),
                tarea(
                    2,
                    EstadoTarea.EN_DESARROLLO,
                    fecha = ahora - 2
                ),
                tarea(3, fecha = ahora),
                tarea(4, EstadoTarea.VALIDADA, fecha = ahora - 3),
                tarea(5, EstadoTarea.RECHAZADA, fecha = ahora - 4)
            ),
            ahora
        )

        assertEquals(listOf(2, 1), resultado.map { it.id })
    }

    @Test
    fun filtrosPorValidarValidadasYRechazadasSonIndependientes() {
        val vm = GestionTareasViewModel()

        val todas = listOf(
            tarea(1, EstadoTarea.PENDIENTE),
            tarea(2, EstadoTarea.PENDIENTE_VALIDACION),
            tarea(3, EstadoTarea.VALIDADA),
            tarea(4, EstadoTarea.RECHAZADA)
        )

        val casos = listOf(
            FiltroTareas.POR_VALIDAR to 2,
            FiltroTareas.VALIDADAS to 3,
            FiltroTareas.RECHAZADAS to 4
        )

        casos.forEach { (filtro, idEsperado) ->
            vm.cambiarFiltro(filtro)

            assertEquals(
                listOf(idEsperado),
                vm.visibles(todas, ahora).map { it.id }
            )
        }
    }

    @Test
    fun busquedaIgnoraMayusculasYEspaciosExteriores() {
        val vm = GestionTareasViewModel()
        val encontrada = tarea(
            1,
            descripcion = "Preparar tierra para tomates"
        )

        vm.cambiarBusqueda("  TOMATES  ")

        assertEquals("  TOMATES  ", vm.busqueda)
        assertEquals(
            listOf(encontrada),
            vm.visibles(
                listOf(encontrada, tarea(2)),
                ahora
            )
        )

        vm.cambiarBusqueda("   ")

        assertEquals(
            2,
            vm.visibles(listOf(encontrada, tarea(2)), ahora).size
        )
    }

    @Test
    fun buscaPorTipoResponsableGrupoYFecha() {
        val vm = GestionTareasViewModel()

        val individual = tarea(
            1,
            tipo = TipoTarea.COSECHA,
            responsable = UsuariosPrueba.estudiante1.id
        )

        val grupal = tarea(
            2,
            responsable = null,
            grupo = "Equipo Semillas"
        )

        val desconocida = tarea(
            3,
            responsable = 999
        )

        val todas = listOf(individual, grupal, desconocida)

        vm.cambiarBusqueda("Cosecha")
        assertEquals(listOf(individual), vm.visibles(todas, ahora))

        vm.cambiarBusqueda(UsuariosPrueba.estudiante1.nombre)
        assertEquals(listOf(individual), vm.visibles(todas, ahora))

        vm.cambiarBusqueda("equipo semillas")
        assertEquals(listOf(grupal), vm.visibles(todas, ahora))

        vm.cambiarBusqueda(formatearFecha(individual.fechaProgramada))
        assertEquals(3, vm.visibles(todas, ahora).size)

        vm.cambiarBusqueda("No existe esta actividad")
        assertTrue(vm.visibles(todas, ahora).isEmpty())
    }

    @Test
    fun busquedaYFiltroSeAplicanJuntos() {
        val vm = GestionTareasViewModel()

        val pendiente = tarea(
            1,
            descripcion = "Revisar tomates"
        )
        val validada = tarea(
            2,
            EstadoTarea.VALIDADA,
            descripcion = "Cosechar tomates"
        )

        vm.cambiarBusqueda("tomates")
        vm.cambiarFiltro(FiltroTareas.VALIDADAS)

        assertEquals(
            listOf(validada),
            vm.visibles(listOf(pendiente, validada), ahora)
        )
    }
}
