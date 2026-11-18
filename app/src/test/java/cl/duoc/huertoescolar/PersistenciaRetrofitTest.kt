package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.remote.ApiOperacion
import cl.duoc.huertoescolar.data.remote.PersistenciaRetrofit
import cl.duoc.huertoescolar.data.remote.ProblemaRemoto
import cl.duoc.huertoescolar.data.remote.TareaRemota
import cl.duoc.huertoescolar.data.remote.aModelo
import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.TipoProblema
import cl.duoc.huertoescolar.domain.model.TipoTarea
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class PersistenciaRetrofitTest {

    private val api = mockk<ApiOperacion>()
    private val persistencia = PersistenciaRetrofit(api)

    private fun tareaRemota() = TareaRemota(
        id = 42,
        tipo = TipoTarea.RIEGO,
        descripcion = "Regar tomates",
        instrucciones = "Evitar encharcar",
        sectorId = 1,
        fechaCreacion = 100L,
        fechaProgramada = 200L,
        responsableUsuarioId = 2,
        creadaPorId = 1,
        estado = EstadoTarea.PENDIENTE
    )

    private fun problemaRemoto() = ProblemaRemoto(
        id = 8,
        sectorId = 1,
        tipo = TipoProblema.FALTA_AGUA,
        descripcion = "Suelo seco",
        fechaReporte = 300L,
        reportadoPorId = 2,
        estado = EstadoReporte.INFORMADO
    )

    @Test
    fun cargarObtieneTareasYProblemasSinInventarSensores() = runBlocking {
        val tarea = tareaRemota()
        val problema = problemaRemoto()

        coEvery { api.listarTareas() } returns listOf(tarea)
        coEvery { api.listarProblemas() } returns listOf(problema)

        val resultado = persistencia.cargar()

        assertEquals(listOf(tarea.aModelo()), resultado.tareas)
        assertEquals(listOf(problema.aModelo()), resultado.problemas)
        assertTrue(resultado.lecturas.isEmpty())
    }

    @Test
    fun cargarAceptaListasVacias() = runBlocking {
        coEvery { api.listarTareas() } returns emptyList()
        coEvery { api.listarProblemas() } returns emptyList()

        val resultado = persistencia.cargar()

        assertTrue(resultado.tareas.isEmpty())
        assertTrue(resultado.problemas.isEmpty())
        assertTrue(resultado.lecturas.isEmpty())
    }

    @Test
    fun cargaFallidaNoDevuelveDatosParcialesComoExito() = runBlocking {
        coEvery {
            api.listarTareas()
        } returns listOf(tareaRemota())

        val fallo = IOException("No se pudieron cargar los problemas")
        coEvery { api.listarProblemas() } throws fallo

        try {
            persistencia.cargar()
            fail("La carga debía propagar el error")
        } catch (e: IOException) {
            assertSame(fallo, e)
        }
    }

    @Test
    fun insertarTareaDevuelveElIdAsignadoPorElServidor() = runBlocking {
        val enviada = slot<TareaRemota>()
        val respuesta = tareaRemota()
        val temporal = respuesta.aModelo().copy(id = 999)

        coEvery {
            api.crearTarea(capture(enviada))
        } returns respuesta

        val resultado = persistencia.insertarTarea(temporal)

        assertEquals(42, resultado.id)
        assertEquals(temporal.descripcion, enviada.captured.descripcion)
        assertEquals(temporal.sectorId, enviada.captured.sectorId)
        assertEquals(
            temporal.responsableUsuarioId,
            enviada.captured.responsableUsuarioId
        )
    }

    @Test
    fun insertarProblemaConservaCultivoOpcionalYUsaIdDelServidor() =
        runBlocking {
            val enviado = slot<ProblemaRemoto>()
            val respuesta = problemaRemoto()
            val temporal = respuesta.aModelo().copy(id = 999)

            coEvery {
                api.crearProblema(capture(enviado))
            } returns respuesta

            val resultado = persistencia.insertarProblema(temporal)

            assertEquals(8, resultado.id)
            assertNull(enviado.captured.cultivoId)
            assertEquals(2, enviado.captured.reportadoPorId)
            assertEquals("Suelo seco", enviado.captured.descripcion)
        }

    @Test
    fun actualizarTareaEnviaEstadoYEvidenciaConElIdCorrecto() = runBlocking {
        val enviada = slot<TareaRemota>()
        val tarea = tareaRemota().aModelo().copy(
            estado = EstadoTarea.PENDIENTE_VALIDACION,
            fechaRealizacion = 500L,
            realizadaPorId = 2,
            comentarioEstudiante = "Trabajo realizado",
            fotoUri = "content://foto"
        )

        coEvery {
            api.actualizarTarea(42, capture(enviada))
        } returns tareaRemota()

        persistencia.actualizarTarea(tarea)

        assertEquals(EstadoTarea.PENDIENTE_VALIDACION, enviada.captured.estado)
        assertEquals(500L, enviada.captured.fechaRealizacion)
        assertEquals(2, enviada.captured.realizadaPorId)
        assertEquals("Trabajo realizado", enviada.captured.comentarioEstudiante)
        assertEquals("content://foto", enviada.captured.fotoUri)

        coVerify(exactly = 1) {
            api.actualizarTarea(42, any())
        }
    }

    @Test
    fun actualizarProblemaEnviaCierreYComentario() = runBlocking {
        val enviado = slot<ProblemaRemoto>()
        val problema = problemaRemoto().aModelo().copy(
            estado = EstadoReporte.CERRADO,
            comentarioDocente = "Riego solucionado",
            fechaCierre = 900L
        )

        coEvery {
            api.actualizarProblema(8, capture(enviado))
        } returns problemaRemoto()

        persistencia.actualizarProblema(problema)

        assertEquals(EstadoReporte.CERRADO, enviado.captured.estado)
        assertEquals("Riego solucionado", enviado.captured.comentarioDocente)
        assertEquals(900L, enviado.captured.fechaCierre)
        assertEquals(2, enviado.captured.reportadoPorId)
    }

    @Test
    fun falloDeGuardadoSePropagaAlLlamador() = runBlocking {
        val fallo = IOException("Servidor no disponible")

        coEvery {
            api.crearProblema(any())
        } throws fallo

        try {
            persistencia.insertarProblema(problemaRemoto().aModelo())
            fail("El guardado debía propagar el error")
        } catch (e: IOException) {
            assertSame(fallo, e)
        }
    }

    @Test
    fun eliminarTareaUsaElIdSolicitado() = runBlocking {
        coEvery { api.eliminarTarea(42) } just Runs

        persistencia.eliminarTarea(42)

        coVerify(exactly = 1) {
            api.eliminarTarea(42)
        }
    }
}