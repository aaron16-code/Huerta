package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.DatosOperacion
import cl.duoc.huertoescolar.data.PersistenciaOperacion
import cl.duoc.huertoescolar.data.repository.RepositorioOperacionImpl
import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.TipoProblema
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RepositorioOperacionPersistenciaTest {

    private fun persistenciaVacia() = mockk<PersistenciaOperacion>().also {
        coEvery { it.cargar() } returns DatosOperacion(
            tareas = emptyList(),
            problemas = emptyList(),
            lecturas = emptyList()
        )
    }

    private suspend fun reportar(repo: RepositorioOperacionImpl) =
        repo.reportarProblema(
            sectorId = 1,
            cultivoId = null,
            tipo = TipoProblema.FALTA_AGUA,
            descripcion = "El sector necesita riego",
            reportanteId = 2,
            fotoUri = "content://evidencia"
        )

    @Test
    fun falloDelServidorNoDebeInformarReporteGuardado() = runTest {
        val persistencia = persistenciaVacia()
        val fallo = IOException("Servidor no disponible")
        coEvery { persistencia.insertarProblema(any()) } throws fallo
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        val resultado = reportar(repo)
        runCurrent()

        coVerify(exactly = 1) { persistencia.insertarProblema(any()) }
        assertTrue(
            "El repositorio informó éxito aunque falló el guardado",
            resultado.isFailure
        )
        assertSame(fallo, resultado.exceptionOrNull())
        assertTrue("No debe publicar un reporte que no se guardó", repo.problemas.value.isEmpty())
    }

    @Test
    fun esperaAlServidorYPublicaElIdentificadorDefinitivo() = runTest {
        val persistencia = persistenciaVacia()
        val respuesta = CompletableDeferred<Problema>()
        var enviada: Problema? = null
        coEvery { persistencia.insertarProblema(any()) } coAnswers {
            enviada = firstArg()
            respuesta.await()
        }
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        val operacion = async { reportar(repo) }
        runCurrent()
        assertFalse(operacion.isCompleted)
        assertTrue(repo.problemas.value.isEmpty())

        val guardado = enviada!!.copy(id = 42)
        respuesta.complete(guardado)
        runCurrent()

        assertTrue(operacion.await().isSuccess)
        assertEquals(listOf(guardado), repo.problemas.value)
    }

    @Test
    fun reintentarTrasUnFalloNoDuplicaElReporte() = runTest {
        val persistencia = persistenciaVacia()
        var intentos = 0
        coEvery { persistencia.insertarProblema(any()) } coAnswers {
            intentos++
            if (intentos == 1) throw IOException("Sin conexión")
            firstArg<Problema>().copy(id = 42)
        }
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        assertTrue(reportar(repo).isFailure)
        assertTrue(repo.problemas.value.isEmpty())
        assertTrue(reportar(repo).isSuccess)
        assertEquals(listOf(42), repo.problemas.value.map { it.id })
        coVerify(exactly = 2) { persistencia.insertarProblema(any()) }
    }

    @Test
    fun falloDeCargaInicialNoPermiteSimularUnGuardadoEnMemoria() = runTest {
        val persistencia = persistenciaVacia()
        val fallo = IOException("No se pudo cargar el servidor")
        coEvery { persistencia.cargar() } throws fallo
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        val antes = repo.problemas.value
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        val resultado = reportar(repo)

        assertTrue(resultado.isFailure)
        assertSame(fallo, resultado.exceptionOrNull())
        assertEquals(antes, repo.problemas.value)
        coVerify(exactly = 0) { persistencia.insertarProblema(any()) }
    }

    @Test
    fun cancelarEnvioNoPublicaUnReporteNiOcultaLaCancelacion() = runTest {
        val persistencia = persistenciaVacia()
        val cancelacion = CancellationException("Envío cancelado")
        coEvery { persistencia.insertarProblema(any()) } throws cancelacion
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        try {
            reportar(repo)
            fail("La cancelación debe propagarse")
        } catch (e: CancellationException) {
            assertSame(cancelacion, e)
        }
        assertTrue(repo.problemas.value.isEmpty())
    }

    private fun reporteExistente() = Problema(
        id = 42,
        sectorId = 1,
        cultivoId = null,
        tipo = TipoProblema.FALTA_AGUA,
        descripcion = "Suelo seco",
        fechaReporte = 100L,
        reportadoPorId = 2,
        estado = EstadoReporte.INFORMADO,
        comentarioDocente = "Respuesta anterior"
    )

    private fun persistenciaConReporte(original: Problema) = persistenciaVacia().also {
        coEvery { it.cargar() } returns DatosOperacion(
            tareas = emptyList(), problemas = listOf(original), lecturas = emptyList()
        )
    }

    @Test
    fun falloAlActualizarNoCambiaEstadoComentarioNiFechaDeCierre() = runTest {
        val original = reporteExistente()
        val persistencia = persistenciaConReporte(original)
        val fallo = IOException("Servidor no disponible")
        coEvery { persistencia.actualizarProblema(any()) } throws fallo
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        val resultado = repo.actualizarProblema(
            42, RolUsuario.ADMINISTRADOR, EstadoReporte.CERRADO, "Riego solucionado"
        )
        runCurrent()

        coVerify(exactly = 1) { persistencia.actualizarProblema(any()) }
        assertTrue("No debe informar éxito si el servidor rechaza la actualización", resultado.isFailure)
        assertSame(fallo, resultado.exceptionOrNull())
        assertEquals(listOf(original), repo.problemas.value)
    }

    @Test
    fun actualizarEsperaAlServidorAntesDePublicarElCambio() = runTest {
        val original = reporteExistente()
        val persistencia = persistenciaConReporte(original)
        val respuesta = CompletableDeferred<Unit>()
        var enviado: Problema? = null
        coEvery { persistencia.actualizarProblema(any()) } coAnswers {
            enviado = firstArg()
            respuesta.await()
        }
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        val operacion = async {
            repo.actualizarProblema(
                42, RolUsuario.ADMINISTRADOR, EstadoReporte.CERRADO, "  Riego solucionado  "
            )
        }
        runCurrent()
        assertFalse(operacion.isCompleted)
        assertEquals(listOf(original), repo.problemas.value)
        assertEquals(EstadoReporte.CERRADO, enviado!!.estado)
        assertEquals("Riego solucionado", enviado!!.comentarioDocente)
        assertNotNull(enviado!!.fechaCierre)

        respuesta.complete(Unit)
        runCurrent()
        assertTrue(operacion.await().isSuccess)
        assertEquals(listOf(enviado!!), repo.problemas.value)
    }

    @Test
    fun actualizarPermiteReintentarSinDuplicarElReporte() = runTest {
        val original = reporteExistente()
        val persistencia = persistenciaConReporte(original)
        var intentos = 0
        coEvery { persistencia.actualizarProblema(any()) } coAnswers {
            intentos++
            if (intentos == 1) throw IOException("Sin conexión")
            Unit
        }
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        assertTrue(repo.actualizarProblema(
            42, RolUsuario.ADMINISTRADOR, EstadoReporte.EN_REVISION, "Revisaremos el riego"
        ).isFailure)
        assertEquals(listOf(original), repo.problemas.value)

        assertTrue(repo.actualizarProblema(
            42, RolUsuario.ADMINISTRADOR, EstadoReporte.EN_REVISION, "Revisaremos el riego"
        ).isSuccess)
        assertEquals(1, repo.problemas.value.size)
        val actualizado = repo.problemas.value.single()
        assertEquals(42, actualizado.id)
        assertEquals(EstadoReporte.EN_REVISION, actualizado.estado)
        assertEquals("Revisaremos el riego", actualizado.comentarioDocente)
        assertNull(actualizado.fechaCierre)
        coVerify(exactly = 2) { persistencia.actualizarProblema(any()) }
    }

    @Test
    fun reporteInexistenteNoDevuelveExitoNiLlamaAlServidor() = runTest {
        val persistencia = persistenciaVacia()
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        val resultado = repo.actualizarProblema(
            999, RolUsuario.ADMINISTRADOR, EstadoReporte.CERRADO, "Resuelto"
        )

        assertTrue(resultado.isFailure)
        assertEquals("El reporte no existe", resultado.exceptionOrNull()?.message)
        coVerify(exactly = 0) { persistencia.actualizarProblema(any()) }
    }

    @Test
    fun cancelarActualizacionConservaElReporteAnterior() = runTest {
        val original = reporteExistente()
        val persistencia = persistenciaConReporte(original)
        val cancelacion = CancellationException("Actualización cancelada")
        coEvery { persistencia.actualizarProblema(any()) } throws cancelacion
        val repo = RepositorioOperacionImpl(alcance = backgroundScope)
        repo.iniciarPersistencia(persistencia)
        runCurrent()

        try {
            repo.actualizarProblema(
                42, RolUsuario.ADMINISTRADOR, EstadoReporte.CERRADO, "Resuelto"
            )
            fail("La cancelación debe propagarse")
        } catch (e: CancellationException) {
            assertSame(cancelacion, e)
        }
        assertEquals(listOf(original), repo.problemas.value)
    }
}