package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.TipoProblema
import cl.duoc.huertoescolar.domain.repository.ProblemasRepository
import cl.duoc.huertoescolar.ui.screens.problemas.ProblemasViewModel
import cl.duoc.huertoescolar.ui.screens.problemas.ReportarProblemaViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProblemasViewModelsTest {

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
    fun actualizarEnviaComentarioRecortadoYLimpiaSoloElReporteActual() {
        every {
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

        verify(exactly = 1) {
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
    fun comentarioVacioConservaLaRespuestaDocenteAnterior() {
        every {
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

        verify(exactly = 1) {
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
    fun falloAlActualizarConservaComentarioYMuestraError() {
        every {
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

        assertEquals("Sin conexión", vm.mensaje)
        assertTrue(vm.mensajeEsError)
        assertEquals(
            "Conservar este texto",
            vm.comentarioDe(1)
        )
    }

    @Test
    fun falloSinMensajeUtilizaMensajeGenerico() {
        every {
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
    fun enviarSinSectorNoLlamaAlRepositorio() {
        val vm = ReportarProblemaViewModel(repository)

        vm.enviar(UsuariosPrueba.estudiante1)

        assertFalse(vm.enviado)

        verify(exactly = 0) {
            repository.reportarProblema(
                any(), any(), any(), any(), any(), any()
            )
        }
    }

    @Test
    fun envioExitosoTransmiteDatosYMarcaElFormularioEnviado() {
        every {
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

        verify(exactly = 1) {
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
    fun falloAlEnviarMantieneFormularioYNoMarcaEnviado() {
        every {
            repository.reportarProblema(
                any(), any(), any(), any(), any(), any()
            )
        } returns Result.failure(
            IllegalStateException("Servidor caído")
        )

        val vm = formularioPreparado()

        vm.enviar(UsuariosPrueba.estudiante1)

        assertFalse(vm.enviado)
        assertTrue(vm.mensajeEsError)
        assertEquals("Servidor caído", vm.mensaje)
        assertEquals("  Suelo seco  ", vm.descripcion)
    }

    @Test
    fun falloDeEnvioSinMensajeUtilizaTextoGenerico() {
        every {
            repository.reportarProblema(
                any(), any(), any(), any(), any(), any()
            )
        } returns Result.failure(Exception())

        val vm = formularioPreparado()

        vm.enviar(UsuariosPrueba.estudiante1)

        assertFalse(vm.enviado)
        assertTrue(vm.mensajeEsError)
        assertEquals(
            "No se pudo enviar el reporte.",
            vm.mensaje
        )
    }
}