package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.operacion.aEntidad
import cl.duoc.huertoescolar.data.operacion.aModelo
import cl.duoc.huertoescolar.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class MapeosOperacionTest {

    private fun tarea() = Tarea(
        id = 42,
        tipo = TipoTarea.RIEGO,
        descripcion = "Regar tomates",
        instrucciones = "Evitar encharcar",
        sectorId = 7,
        cultivoId = 12,
        fechaCreacion = 100L,
        fechaProgramada = 200L,
        responsableUsuarioId = 2,
        grupoAsignado = null,
        creadaPorId = 1,
        estado = EstadoTarea.VALIDADA,
        fechaRealizacion = 300L,
        realizadaPorId = 2,
        fotoUri = "content://evidencia",
        comentarioEstudiante = "Realizada",
        comentarioDocente = "Correcto",
        validadorId = 1,
        fechaValidacion = 400L
    )

    @Test
    fun tareaConservaTodosLosCamposAlConvertirIdaYVuelta() {
        val original = tarea()
        val entidad = original.aEntidad()

        assertEquals("RIEGO", entidad.tipo)
        assertEquals("VALIDADA", entidad.estado)
        assertEquals(original, entidad.aModelo())
    }

    @Test
    fun tareaGrupalConservaAsignacionYCamposOpcionalesVacios() {
        val original = tarea().copy(
            cultivoId = null,
            responsableUsuarioId = null,
            grupoAsignado = "Taller Huerto A",
            estado = EstadoTarea.PENDIENTE,
            fechaRealizacion = null,
            realizadaPorId = null,
            fotoUri = null,
            comentarioEstudiante = "",
            comentarioDocente = "",
            validadorId = null,
            fechaValidacion = null
        )

        assertEquals(original, original.aEntidad().aModelo())
    }

    @Test
    fun conversionDeTareaReconoceTodosLosTiposYEstados() {
        TipoTarea.entries.forEach { tipo ->
            val original = tarea().copy(tipo = tipo)
            assertEquals(original, original.aEntidad().aModelo())
        }

        EstadoTarea.entries.forEach { estado ->
            val original = tarea().copy(estado = estado)
            assertEquals(original, original.aEntidad().aModelo())
        }
    }

    @Test
    fun problemaConservaRespuestaFotoYCierre() {
        val original = Problema(
            id = 8,
            sectorId = 7,
            cultivoId = 12,
            tipo = TipoProblema.FALTA_AGUA,
            descripcion = "Suelo seco",
            fechaReporte = 100L,
            reportadoPorId = 2,
            fotoUri = "content://problema",
            estado = EstadoReporte.CERRADO,
            comentarioDocente = "Riego solucionado",
            fechaCierre = 300L
        )

        val entidad = original.aEntidad()

        assertEquals("FALTA_AGUA", entidad.tipo)
        assertEquals("CERRADO", entidad.estado)
        assertEquals(original, entidad.aModelo())
    }

    @Test
    fun problemaSinCultivoNiFotoConservaValoresNulos() {
        val original = Problema(
            id = 8,
            sectorId = 7,
            cultivoId = null,
            tipo = TipoProblema.OTRO,
            descripcion = "Problema de prueba",
            fechaReporte = 100L,
            reportadoPorId = 2
        )

        assertEquals(original, original.aEntidad().aModelo())
    }

    @Test
    fun conversionDeProblemaReconoceTodosLosTiposYEstados() {
        val base = Problema(
            id = 1,
            sectorId = 1,
            cultivoId = null,
            tipo = TipoProblema.OTRO,
            descripcion = "Descripción",
            fechaReporte = 100L,
            reportadoPorId = 2
        )

        TipoProblema.entries.forEach { tipo ->
            val original = base.copy(tipo = tipo)
            assertEquals(original, original.aEntidad().aModelo())
        }

        EstadoReporte.entries.forEach { estado ->
            val original = base.copy(estado = estado)
            assertEquals(original, original.aEntidad().aModelo())
        }
    }

    @Test
    fun lecturaConservaMedicionesFechaYOrigen() {
        EstadoLectura.entries.forEach { estado ->
            val original = LecturaSensor(
                id = 3,
                sectorId = 7,
                fecha = 500L,
                humedadSuelo = 45,
                temperaturaAmbiente = 22.5,
                humedadAmbiente = 60,
                estado = estado,
                origen = "Simulación académica"
            )

            val entidad = original.aEntidad()

            assertEquals(estado.name, entidad.estado)
            assertEquals(original, entidad.aModelo())
        }
    }

    @Test
    fun estadoDesconocidoNoSeConvierteSilenciosamenteEnValido() {
        val entidad = tarea().aEntidad().copy(
            estado = "ESTADO_DESCONOCIDO"
        )

        try {
            entidad.aModelo()
            fail("Debía rechazar un estado desconocido")
        } catch (e: IllegalArgumentException) {
            assertNotNull(e.message)
        }
    }
}