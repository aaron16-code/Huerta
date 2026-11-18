package cl.duoc.huertoescolar.data.remote

import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoProblema
import cl.duoc.huertoescolar.domain.model.TipoTarea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelosRemotosTest {

    private fun tarea() = Tarea(
        id = 3, tipo = TipoTarea.RIEGO, descripcion = "Regar la lechuga", instrucciones = "Con regadera",
        sectorId = 1, cultivoId = 1, fechaCreacion = 10L, fechaProgramada = 20L,
        responsableUsuarioId = 2, creadaPorId = 1, estado = EstadoTarea.VALIDADA,
        comentarioEstudiante = "Listo", comentarioDocente = "Bien", validadorId = 1, fechaValidacion = 30L
    )

    @Test
    fun tarea_ida_y_vuelta_no_pierde_datos() {
        val original = tarea()

        val vuelta = original.aRemota().aModelo()

        assertEquals(original, vuelta)
    }

    @Test
    fun tarea_remota_sin_estado_ni_comentarios_usa_valores_por_defecto() {
        val remota = TareaRemota(
            tipo = TipoTarea.SIEMBRA, descripcion = "Sembrar", instrucciones = "Surcos",
            sectorId = 1, fechaProgramada = 5L, creadaPorId = 1
        )

        val modelo = remota.aModelo()

        assertEquals(0, modelo.id)
        assertEquals(EstadoTarea.PENDIENTE, modelo.estado)
        assertEquals("", modelo.comentarioEstudiante)
        assertEquals("", modelo.comentarioDocente)
        assertNull(modelo.cultivoId)
    }

    @Test
    fun problema_ida_y_vuelta_no_pierde_datos() {
        val original = Problema(
            id = 2, sectorId = 2, cultivoId = null, tipo = TipoProblema.FALTA_AGUA,
            descripcion = "Tierra seca", fechaReporte = 99L, reportadoPorId = 3,
            estado = EstadoReporte.EN_REVISION, comentarioDocente = "Lo revisamos"
        )

        assertEquals(original, original.aRemoto().aModelo())
    }

    @Test
    fun problema_remoto_sin_estado_parte_informado() {
        val remoto = ProblemaRemoto(
            sectorId = 1, tipo = TipoProblema.OTRO, descripcion = "Algo raro", reportadoPorId = 2
        )

        val modelo = remoto.aModelo()

        assertEquals(EstadoReporte.INFORMADO, modelo.estado)
        assertEquals("", modelo.comentarioDocente)
    }
}
