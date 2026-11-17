package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.domain.model.*
import cl.duoc.huertoescolar.ui.screens.historial.FiltroHistorial
import cl.duoc.huertoescolar.ui.screens.historial.construirHistorial
import org.junit.Assert.*
import org.junit.Test

class CalculosHistorialTest {

    private fun tarea(id: Int = 1) = Tarea(
        id = id,
        tipo = TipoTarea.RIEGO,
        descripcion = "Riego",
        instrucciones = "Regar",
        sectorId = 1,
        cultivoId = null,
        fechaCreacion = 10L,
        fechaProgramada = 20L,
        responsableUsuarioId = 2,
        creadaPorId = 1,
        estado = EstadoTarea.VALIDADA,
        fechaRealizacion = 100L,
        fotoUri = "foto-tarea",
        comentarioEstudiante = "Riego realizado",
        comentarioDocente = "Correcto",
        fechaValidacion = 300L
    )

    private fun problema(
        id: Int = 1,
        autor: Int = 2
    ) = Problema(
        id = id,
        sectorId = 1,
        cultivoId = null,
        tipo = TipoProblema.FALTA_AGUA,
        descripcion = "Suelo seco",
        fechaReporte = 200L,
        reportadoPorId = autor,
        fotoUri = "foto-problema"
    )

    private fun historial(
        tareas: List<Tarea> = emptyList(),
        problemas: List<Problema> = emptyList(),
        usuario: UsuarioPrueba = UsuariosPrueba.docente,
        filtro: FiltroHistorial = FiltroHistorial.TODOS
    ) = construirHistorial(
        tareas = tareas,
        problemas = problemas,
        usuario = usuario,
        nombreSector = { "Sector $it" },
        filtro = filtro
    )

    @Test
    fun entradasVaciasDevuelvenHistorialVacio() {
        assertTrue(historial().isEmpty())
        assertTrue(
            historial(usuario = UsuariosPrueba.estudiante1).isEmpty()
        )
    }

    @Test
    fun generaEventosCompletosYLosOrdenaDelMasRecienteAlMasAntiguo() {
        val resultado = historial(
            tareas = listOf(tarea()),
            problemas = listOf(problema())
        )

        assertEquals(
            listOf(
                EventoHistorial(
                    fecha = 300L,
                    tipo = TipoEvento.VALIDACION,
                    titulo = "Tarea validada: Riego · Sector 1",
                    detalle = "Correcto"
                ),
                EventoHistorial(
                    fecha = 200L,
                    tipo = TipoEvento.PROBLEMA,
                    titulo = "Problema reportado: Falta de agua · Sector 1",
                    detalle = "Suelo seco",
                    fotoUri = "foto-problema"
                ),
                EventoHistorial(
                    fecha = 100L,
                    tipo = TipoEvento.TAREA,
                    titulo = "Tarea realizada: Riego · Sector 1",
                    detalle = "Riego realizado",
                    fotoUri = "foto-tarea"
                )
            ),
            resultado
        )
    }

    @Test
    fun tareaSinFechasNoGeneraEventos() {
        val resultado = historial(
            tareas = listOf(
                tarea().copy(
                    fechaRealizacion = null,
                    fechaValidacion = null
                )
            )
        )

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun validacionRechazadaConservaComentarioDocente() {
        val resultado = historial(
            tareas = listOf(
                tarea().copy(
                    estado = EstadoTarea.RECHAZADA,
                    fechaRealizacion = null,
                    comentarioDocente = "Falta evidencia"
                )
            )
        )

        assertEquals(1, resultado.size)
        assertEquals(TipoEvento.VALIDACION, resultado.single().tipo)
        assertEquals(
            "Tarea rechazada: Riego · Sector 1",
            resultado.single().titulo
        )
        assertEquals("Falta evidencia", resultado.single().detalle)
    }

    @Test
    fun cadaFiltroDevuelveSoloElTipoSolicitado() {
        val tareas = listOf(tarea())
        val problemas = listOf(problema())

        val filtros = listOf(
            FiltroHistorial.TAREAS to TipoEvento.TAREA,
            FiltroHistorial.VALIDACIONES to TipoEvento.VALIDACION,
            FiltroHistorial.PROBLEMAS to TipoEvento.PROBLEMA
        )

        filtros.forEach { (filtro, tipo) ->
            val resultado = historial(
                tareas = tareas,
                problemas = problemas,
                filtro = filtro
            )

            assertEquals(1, resultado.size)
            assertEquals(tipo, resultado.single().tipo)
        }
    }

    @Test
    fun estudianteVeTareasPropiasYDeGrupoPeroSoloSusProblemas() {
        val propias = tarea(1).copy(
            fechaValidacion = null,
            comentarioEstudiante = "Propia"
        )

        val deGrupo = tarea(2).copy(
            responsableUsuarioId = null,
            grupoAsignado = UsuariosPrueba.estudiante1.grupo,
            fechaValidacion = null,
            comentarioEstudiante = "De grupo"
        )

        val ajena = tarea(3).copy(
            responsableUsuarioId = 3,
            grupoAsignado = UsuariosPrueba.estudiante1.grupo,
            fechaValidacion = null,
            comentarioEstudiante = "Ajena individual"
        )

        val otroGrupo = tarea(4).copy(
            responsableUsuarioId = null,
            grupoAsignado = UsuariosPrueba.estudiante3.grupo,
            fechaValidacion = null,
            comentarioEstudiante = "Otro grupo"
        )

        val tareas = listOf(propias, deGrupo, ajena, otroGrupo)
        val problemas = listOf(
            problema(1, autor = 2),
            problema(2, autor = 3)
        )

        val resultado = historial(
            tareas = tareas,
            problemas = problemas,
            usuario = UsuariosPrueba.estudiante1
        )

        assertEquals(3, resultado.size)

        assertEquals(
            setOf("Propia", "De grupo"),
            resultado.filter {
                it.tipo == TipoEvento.TAREA
            }.map { it.detalle }.toSet()
        )

        assertEquals(
            1,
            resultado.count { it.tipo == TipoEvento.PROBLEMA }
        )

        assertEquals(
            6,
            historial(tareas = tareas, problemas = problemas).size
        )
    }
}