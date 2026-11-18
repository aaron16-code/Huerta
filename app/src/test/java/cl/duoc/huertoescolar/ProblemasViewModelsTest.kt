package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.TipoProblema
import cl.duoc.huertoescolar.domain.repository.ProblemasRepository
import cl.duoc.huertoescolar.ui.screens.problemas.ProblemasViewModel
import cl.duoc.huertoescolar.ui.screens.problemas.ReportarProblemaViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Before
import org.junit.After
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProblemasViewModelsTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun prepararCorrutinas() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun limpiarCorrutinas() {
        Dispatchers.resetMain()
    }


    private val repository = mockk<ProblemasRepository> {
        every { problemas } returns MutableStateFlow<List<Problema>>(
            emptyList()
        )
    }

    private fun problema(
        id: Int = 1,
        autor: Int = 2,
        fecha: Long = 100L
    ): Problema {
        return Problema(
            id = id,
            sectorId = 1,
            cultivoId = null,
            tipo = TipoProblema.FALTA_AGUA,
            descripcion = "Suelo seco",
            fechaReporte = fecha,
            reportadoPorId = autor,
            comentarioDocente = "Comentario anterior"
        )
    }

    private fun formularioPreparado(): ReportarProblemaViewModel {
        return ReportarProblemaViewModel(repository).apply {
            elegirSector(1)
            cambiarDescripcion("  Suelo seco  ")
        }
    }

    @Test
    fun docenteVeTodosLosReportesOrdenadosDelMasReciente() {
        val vm = ProblemasViewModel(repository)
        val primero = problema(1, autor = 2, fecha = 100L)
        val segundo = problema(2, autor = 3, fecha = 200L)

        assertEquals(
            listOf(segundo, primero),
            vm.visibles(
                listOf(primero, segundo),
                UsuariosPrueba.docente
            )
        )
    }

    @Test
    fun estudianteVeSoloSusReportes() {
        val vm = ProblemasViewModel(repository)
        val propio = problema(1, autor = 2)
        val ajeno = problema(2, autor = 3)

        assertEquals(
            listOf(propio),
            vm.visibles(
                listOf(ajeno, propio),
                UsuariosPrueba.estudiante1
            )
        )

        assertTrue(
            vm.visibles(
                listOf(ajeno),
                UsuariosPrueba.estudiante1
            ).isEmpty()
        )
    }

    @Test
    fun comentariosSeMantienenSeparadosPorReporte() {
        val vm = ProblemasViewModel(repository)

        assertEquals("", vm.comentarioDe(1))

        vm.cambiarComentario(1, "Primero")
        vm.cambiarComentario(2, "Segundo")

        assertEquals("Primero", vm.comentarioDe(1))
        assertEquals("Segundo", vm.comentarioDe(2))
    }

    @Test
    fun actualizarEnviaComentarioRecortadoYLimpiaSoloElReporteActual() = runTest {
        coEvery {
            repository.actualizarProblema(
                1,
                RolUsuario.ADMINISTRADOR,
                EstadoReporte.EN_REVISION,
                "Revisaremos el riego"
            )
        } returns Result.success(Unit)

        val vm = ProblemasViewModel(repository)
        vm.cambiarComentario(1, "  Revisaremos el riego  ")
        vm.cambiarComentario(2, "Otro comentario")

        vm.cambiarEstado(
            problema(),
            EstadoReporte.EN_REVISION,
            UsuariosPrueba.docente
        )
        runCurrent()

        coVerify(exactly = 1) {
            repository.actualizarProblema(
                1,
                RolUsuario.ADMINISTRADOR,
                EstadoReporte.EN_REVISION,
                "Revisaremos el riego"
            )
        }

        assertEquals("", vm.comentarioDe(1))
        assertEquals("Otro comentario", vm.comentarioDe(2))
        assertEquals(
            "Reporte actualizado: En revisión.",
            vm.mensaje
        )
        assertFalse(vm.mensajeEsError)
    }

    @Test
    fun comentarioVacioConservaLaRespuestaDocenteAnterior() = runTest {
        coEvery {
            repository.actualizarProblema(
                1,
                RolUsuario.ADMINISTRADOR,
                EstadoReporte.CERRADO,
                "Comentario anterior"
            )
        } returns Result.success(Unit)

        val vm = ProblemasViewModel(repository)
        vm.cambiarComentario(1, "   ")

        vm.cambiarEstado(
            problema(),
            EstadoReporte.CERRADO,
            UsuariosPrueba.docente
        )
        runCurrent()

        coVerify(exactly = 1) {
            repository.actualizarProblema(
                1,
                RolUsuario.ADMINISTRADOR,
                EstadoReporte.CERRADO,
                "Comentario anterior"
            )
        }

        assertFalse(vm.mensajeEsError)
    }

    @Test
    fun falloAlActualizarConservaComentarioYMuestraError() = runTest {
        coEvery {
            repository.actualizarProblema(
                any(), any(), any(), any()
            )
        } returns Result.failure(
            IllegalStateException("Sin conexión")
        )

        val vm = ProblemasViewModel(repository)
        vm.cambiarComentario(1, "Conservar este texto")

        vm.cambiarEstado(
            problema(),
            EstadoReporte.CERRADO,
            UsuariosPrueba.docente
        )
        runCurrent()

        assertEquals("Sin conexión", vm.mensaje)
        assertTrue(vm.mensajeEsError)
        assertEquals(
            "Conservar este texto",
            vm.comentarioDe(1)
        )
    }

    @Test
    fun falloSinMensajeUtilizaMensajeGenerico() = runTest {
        coEvery {
            repository.actualizarProblema(
                any(), any(), any(), any()
            )
        } returns Result.failure(Exception())

        val vm = ProblemasViewModel(repository)

        vm.cambiarEstado(
            problema(),
            EstadoReporte.EN_REVISION,
            UsuariosPrueba.docente
        )
        runCurrent()

        assertEquals(
            "No se pudo actualizar el reporte.",
            vm.mensaje
        )
        assertTrue(vm.mensajeEsError)
    }

    @Test
    fun formularioInvalidoNoAbreConfirmacion() {
        val vm = ReportarProblemaViewModel(repository)

        vm.pedirConfirmacion()

        assertNotNull(vm.errorSector)
        assertNotNull(vm.errorDescripcion)
        assertFalse(vm.mostrarConfirmacion)
        assertFalse(vm.enviado)

        vm.elegirSector(1)
        vm.cambiarDescripcion("1234")
        vm.pedirConfirmacion()

        assertNull(vm.errorSector)
        assertNotNull(vm.errorDescripcion)
        assertFalse(vm.mostrarConfirmacion)
    }

    @Test
    fun descripcionDeCincoCaracteresPermiteConfirmarYCancelar() {
        val vm = ReportarProblemaViewModel(repository)
        vm.elegirSector(1)
        vm.cambiarDescripcion("12345")

        vm.pedirConfirmacion()

        assertTrue(vm.mostrarConfirmacion)
        assertNull(vm.errorSector)
        assertNull(vm.errorDescripcion)

        vm.cancelarConfirmacion()

        assertFalse(vm.mostrarConfirmacion)
    }

    @Test
    fun cambiarSectorLimpiaElCultivoSeleccionado() {
        val vm = formularioPreparado()

        vm.elegirCultivo(7)
        assertEquals(7, vm.cultivoId)

        vm.elegirSector(2)

        assertEquals(2, vm.sectorId)
        assertNull(vm.cultivoId)
        assertNull(vm.errorSector)
        assertNull(vm.mensaje)
    }

    @Test
    fun permiteCambiarTipoYAgregarOQuitarFoto() {
        val vm = formularioPreparado()

        vm.elegirTipo(TipoProblema.DANO_PLANTA)
        vm.elegirFoto("content://foto")

        assertEquals(TipoProblema.DANO_PLANTA, vm.tipo)
        assertEquals("content://foto", vm.fotoUri)
        assertEquals("  Suelo seco  ", vm.descripcion)

        vm.quitarFoto()

        assertNull(vm.fotoUri)
    }

    @Test
    fun enviarSinSectorNoLlamaAlRepositorio() = runTest {
        val vm = ReportarProblemaViewModel(repository)

        vm.enviar(UsuariosPrueba.estudiante1)
        runCurrent()

        assertFalse(vm.enviado)

        coVerify(exactly = 0) {
            repository.reportarProblema(
                any(), any(), any(), any(), any(), any()
            )
        }
    }

    @Test
    fun envioExitosoTransmiteDatosYMarcaElFormularioEnviado() = runTest {
        coEvery {
            repository.reportarProblema(
                1,
                7,
                TipoProblema.DANO_PLANTA,
                "Suelo seco",
                2,
                "content://foto"
            )
        } returns Result.success(Unit)

        val vm = formularioPreparado()
        vm.elegirCultivo(7)
        vm.elegirTipo(TipoProblema.DANO_PLANTA)
        vm.elegirFoto("content://foto")
        vm.pedirConfirmacion()

        vm.enviar(UsuariosPrueba.estudiante1)
        runCurrent()

        coVerify(exactly = 1) {
            repository.reportarProblema(
                1,
                7,
                TipoProblema.DANO_PLANTA,
                "Suelo seco",
                2,
                "content://foto"
            )
        }

        assertTrue(vm.enviado)
        assertFalse(vm.mostrarConfirmacion)
        assertNull(vm.mensaje)
        assertFalse(vm.mensajeEsError)
    }

    @Test
    fun falloAlEnviarMantieneFormularioYNoMarcaEnviado() = runTest {
        coEvery {
            repository.reportarProblema(
                any(), any(), any(), any(), any(), any()
            )
        } returns Result.failure(
            IllegalStateException("Servidor caído")
        )

        val vm = formularioPreparado()

        vm.enviar(UsuariosPrueba.estudiante1)
        runCurrent()

        assertFalse(vm.enviado)
        assertTrue(vm.mensajeEsError)
        assertEquals("Servidor caído", vm.mensaje)
        assertEquals("  Suelo seco  ", vm.descripcion)
    }

    @Test
    fun falloDeEnvioSinMensajeUtilizaTextoGenerico() = runTest {
        coEvery {
            repository.reportarProblema(
                any(), any(), any(), any(), any(), any()
            )
        } returns Result.failure(Exception())

        val vm = formularioPreparado()

        vm.enviar(UsuariosPrueba.estudiante1)
        runCurrent()

        assertFalse(vm.enviado)
        assertTrue(vm.mensajeEsError)
        assertEquals(
            "No se pudo enviar el reporte.",
            vm.mensaje
        )
    }

    @Test
    fun esperaConfirmacionYEvitaEnviosDuplicados() = runTest {
        val respuesta = CompletableDeferred<Result<Unit>>()
        coEvery {
            repository.reportarProblema(any(), any(), any(), any(), any(), any())
        } coAnswers { respuesta.await() }
        val vm = formularioPreparado()

        vm.enviar(UsuariosPrueba.estudiante1)
        vm.enviar(UsuariosPrueba.estudiante1)
        runCurrent()

        assertTrue(vm.enviando)
        assertFalse(vm.enviado)
        coVerify(exactly = 1) {
            repository.reportarProblema(any(), any(), any(), any(), any(), any())
        }

        respuesta.complete(Result.success(Unit))
        runCurrent()
        assertFalse(vm.enviando)
        assertTrue(vm.enviado)

        vm.enviar(UsuariosPrueba.estudiante1)
        runCurrent()
        coVerify(exactly = 1) {
            repository.reportarProblema(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun falloConservaDatosYPermiteReintentar() = runTest {
        coEvery {
            repository.reportarProblema(any(), any(), any(), any(), any(), any())
        } returnsMany listOf(
            Result.failure(IllegalStateException("Sin conexión")),
            Result.success(Unit)
        )
        val vm = formularioPreparado()
        vm.elegirFoto("content://evidencia")
        vm.elegirCultivo(7)

        vm.enviar(UsuariosPrueba.estudiante1)
        runCurrent()
        assertFalse(vm.enviado)
        assertFalse(vm.enviando)
        assertTrue(vm.mensajeEsError)
        assertEquals("  Suelo seco  ", vm.descripcion)
        assertEquals("content://evidencia", vm.fotoUri)
        assertEquals(1, vm.sectorId)
        assertEquals(7, vm.cultivoId)

        vm.enviar(UsuariosPrueba.estudiante1)
        runCurrent()
        assertTrue(vm.enviado)
        assertFalse(vm.mensajeEsError)
        assertNull(vm.mensaje)
        coVerify(exactly = 2) {
            repository.reportarProblema(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun actualizarEsperaConfirmacionYEvitaDuplicadosDelMismoReporte() = runTest {
        val respuesta = CompletableDeferred<Result<Unit>>()
        coEvery {
            repository.actualizarProblema(any(), any(), any(), any())
        } coAnswers { respuesta.await() }
        val vm = ProblemasViewModel(repository)
        vm.cambiarComentario(1, "Revisar el riego")

        vm.cambiarEstado(problema(), EstadoReporte.EN_REVISION, UsuariosPrueba.docente)
        vm.cambiarEstado(problema(), EstadoReporte.CERRADO, UsuariosPrueba.docente)
        runCurrent()

        assertTrue(vm.estaActualizando(1))
        assertNull(vm.mensaje)
        assertEquals("Revisar el riego", vm.comentarioDe(1))
        coVerify(exactly = 1) {
            repository.actualizarProblema(1, RolUsuario.ADMINISTRADOR,
                EstadoReporte.EN_REVISION, "Revisar el riego")
        }
        coVerify(exactly = 0) {
            repository.actualizarProblema(1, any(), EstadoReporte.CERRADO, any())
        }

        respuesta.complete(Result.success(Unit))
        runCurrent()
        assertFalse(vm.estaActualizando(1))
        assertEquals("", vm.comentarioDe(1))
        assertEquals("Reporte actualizado: En revisión.", vm.mensaje)
    }

    @Test
    fun actualizarFallidoPermiteReintentarConElComentarioConservado() = runTest {
        coEvery {
            repository.actualizarProblema(any(), any(), any(), any())
        } returnsMany listOf(
            Result.failure(IllegalStateException("Sin conexión")),
            Result.success(Unit)
        )
        val vm = ProblemasViewModel(repository)
        vm.cambiarComentario(1, "Riego solucionado")
        vm.cambiarComentario(2, "Otro seguimiento")

        vm.cambiarEstado(problema(), EstadoReporte.CERRADO, UsuariosPrueba.docente)
        runCurrent()
        assertTrue(vm.mensajeEsError)
        assertFalse(vm.estaActualizando(1))
        assertEquals("Riego solucionado", vm.comentarioDe(1))

        vm.cambiarEstado(problema(), EstadoReporte.CERRADO, UsuariosPrueba.docente)
        runCurrent()
        assertFalse(vm.mensajeEsError)
        assertEquals("Reporte actualizado: Cerrado.", vm.mensaje)
        assertEquals("", vm.comentarioDe(1))
        assertEquals("Otro seguimiento", vm.comentarioDe(2))
        coVerify(exactly = 2) {
            repository.actualizarProblema(1, RolUsuario.ADMINISTRADOR,
                EstadoReporte.CERRADO, "Riego solucionado")
        }
    }

    @Test
    fun actualizarNoBorraTextoEscritoMientrasSeEsperaAlServidor() = runTest {
        val respuesta = CompletableDeferred<Result<Unit>>()
        coEvery {
            repository.actualizarProblema(any(), any(), any(), any())
        } coAnswers { respuesta.await() }
        val vm = ProblemasViewModel(repository)
        vm.cambiarComentario(1, "Comentario enviado")
        vm.cambiarEstado(problema(), EstadoReporte.EN_REVISION, UsuariosPrueba.docente)
        runCurrent()

        vm.cambiarComentario(1, "Nuevo comentario todavía sin enviar")
        respuesta.complete(Result.success(Unit))
        runCurrent()

        assertEquals("Nuevo comentario todavía sin enviar", vm.comentarioDe(1))
        assertFalse(vm.estaActualizando(1))
        coVerify(exactly = 1) {
            repository.actualizarProblema(1, RolUsuario.ADMINISTRADOR,
                EstadoReporte.EN_REVISION, "Comentario enviado")
        }
    }
}