package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoTarea
import cl.duoc.huertoescolar.domain.repository.TareasRepository
import cl.duoc.huertoescolar.ui.screens.tareas.DetalleTareaViewModel
import cl.duoc.huertoescolar.ui.screens.tareas.PanelValidacionViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TareasViewModelsTest {

    private fun repositorio(
        idSeleccionado: Int? = 1
    ): TareasRepository {
        return mockk {
            every { tareas } returns MutableStateFlow<List<Tarea>>(
                emptyList()
            )
            every { tareaSeleccionadaId } returns idSeleccionado
        }
    }

    private fun tarea(
        id: Int = 1,
        estado: EstadoTarea = EstadoTarea.PENDIENTE,
        responsable: Int? = 2,
        grupo: String? = null,
        realizada: Long? = null
    ): Tarea {
        return Tarea(
            id = id,
            tipo = TipoTarea.RIEGO,
            descripcion = "Regar el bancal",
            instrucciones = "Regar sin encharcar",
            sectorId = 1,
            cultivoId = null,
            fechaCreacion = 10L,
            fechaProgramada = 20L,
            responsableUsuarioId = responsable,
            grupoAsignado = grupo,
            creadaPorId = 1,
            estado = estado,
            fechaRealizacion = realizada
        )
    }

    @Test
    fun pendientesIncluyeSoloEntregasPorValidarYLasOrdena() {
        val vm = PanelValidacionViewModel(repositorio())

        val reciente = tarea(
            id = 1,
            estado = EstadoTarea.PENDIENTE_VALIDACION,
            realizada = 300L
        )

        val antigua = tarea(
            id = 2,
            estado = EstadoTarea.PENDIENTE_VALIDACION,
            realizada = 100L
        )

        val sinFecha = tarea(
            id = 3,
            estado = EstadoTarea.PENDIENTE_VALIDACION
        )

        val resultado = vm.pendientes(
            listOf(
                reciente,
                tarea(4, EstadoTarea.VALIDADA),
                antigua,
                sinFecha,
                tarea(5)
            )
        )

        assertEquals(
            listOf(sinFecha, antigua, reciente),
            resultado
        )
        assertTrue(vm.pendientes(emptyList()).isEmpty())
    }

    @Test
    fun aprobarEnviaDatosYLimpiaSoloElComentarioDeLaTarea() {
        val repo = repositorio()

        every {
            repo.validarTarea(
                1,
                1,
                RolUsuario.ADMINISTRADOR,
                true,
                "Buen trabajo"
            )
        } returns Result.success(Unit)

        val vm = PanelValidacionViewModel(repo)

        assertEquals("", vm.comentarioDe(1))

        vm.cambiarComentario(1, "  Buen trabajo  ")
        vm.cambiarComentario(2, "Otra entrega")

        vm.resolver(
            tarea(estado = EstadoTarea.PENDIENTE_VALIDACION),
            true,
            UsuariosPrueba.docente
        )

        verify(exactly = 1) {
            repo.validarTarea(
                1,
                1,
                RolUsuario.ADMINISTRADOR,
                true,
                "Buen trabajo"
            )
        }

        assertEquals("Tarea validada.", vm.mensaje)
        assertFalse(vm.mensajeEsError)
        assertEquals("", vm.comentarioDe(1))
        assertEquals("Otra entrega", vm.comentarioDe(2))
    }

    @Test
    fun rechazarMuestraQueElEstudiantePuedeRepetirLaEntrega() {
        val repo = repositorio()

        every {
            repo.validarTarea(
                1,
                1,
                RolUsuario.ADMINISTRADOR,
                false,
                "Falta evidencia"
            )
        } returns Result.success(Unit)

        val vm = PanelValidacionViewModel(repo)
        vm.cambiarComentario(1, "Falta evidencia")

        vm.resolver(
            tarea(estado = EstadoTarea.PENDIENTE_VALIDACION),
            false,
            UsuariosPrueba.docente
        )

        verify(exactly = 1) {
            repo.validarTarea(
                1,
                1,
                RolUsuario.ADMINISTRADOR,
                false,
                "Falta evidencia"
            )
        }

        assertEquals(
            "Tarea rechazada. El estudiante podrá repetirla.",
            vm.mensaje
        )
        assertFalse(vm.mensajeEsError)
        assertEquals("", vm.comentarioDe(1))
    }

    @Test
    fun falloDeValidacionConservaComentarioYMuestraLaCausa() {
        val repo = repositorio()

        every {
            repo.validarTarea(
                any(), any(), any(), any(), any()
            )
        } returns Result.failure(
            IllegalStateException("No autorizado")
        )

        val vm = PanelValidacionViewModel(repo)
        vm.cambiarComentario(1, "Conservar")

        vm.resolver(
            tarea(),
            true,
            UsuariosPrueba.docente
        )

        assertTrue(vm.mensajeEsError)
        assertEquals("No autorizado", vm.mensaje)
        assertEquals("Conservar", vm.comentarioDe(1))
    }

    @Test
    fun falloDeValidacionSinMensajeTieneTextoGenerico() {
        val repo = repositorio()

        every {
            repo.validarTarea(
                any(), any(), any(), any(), any()
            )
        } returns Result.failure(Exception())

        val vm = PanelValidacionViewModel(repo)

        vm.resolver(
            tarea(),
            true,
            UsuariosPrueba.docente
        )

        assertTrue(vm.mensajeEsError)
        assertEquals(
            "No se pudo completar la acción.",
            vm.mensaje
        )
    }

    @Test
    fun estudianteSoloPuedeEnviarEstadosPermitidosDeSuTarea() {
        val vm = DetalleTareaViewModel(repositorio())

        EstadoTarea.entries.forEach { estado ->
            val esperado = estado in setOf(
                EstadoTarea.PENDIENTE,
                EstadoTarea.EN_DESARROLLO,
                EstadoTarea.RECHAZADA
            )

            assertEquals(
                "Estado: $estado",
                esperado,
                vm.puedeEnviar(
                    tarea(estado = estado),
                    UsuariosPrueba.estudiante1
                )
            )
        }

        assertFalse(
            vm.puedeEnviar(
                tarea(),
                UsuariosPrueba.docente
            )
        )

        assertFalse(
            vm.puedeEnviar(
                tarea(responsable = 3),
                UsuariosPrueba.estudiante1
            )
        )
    }

    @Test
    fun asignacionIndividualTienePrioridadSobreElGrupo() {
        val vm = DetalleTareaViewModel(repositorio())
        val grupo = UsuariosPrueba.estudiante1.grupo

        assertTrue(
            vm.puedeEnviar(
                tarea(
                    responsable = null,
                    grupo = grupo
                ),
                UsuariosPrueba.estudiante1
            )
        )

        assertFalse(
            vm.puedeEnviar(
                tarea(
                    responsable = null,
                    grupo = UsuariosPrueba.estudiante3.grupo
                ),
                UsuariosPrueba.estudiante1
            )
        )

        assertFalse(
            vm.puedeEnviar(
                tarea(
                    responsable = 3,
                    grupo = grupo
                ),
                UsuariosPrueba.estudiante1
            )
        )
    }

    @Test
    fun detallePermiteCambiarComentarioFotoYConfirmacion() {
        val vm = DetalleTareaViewModel(repositorio())

        assertEquals(1, vm.tareaId)
        assertEquals("", vm.comentario)
        assertNull(vm.fotoUri)
        assertNull(vm.mensaje)
        assertFalse(vm.mensajeEsError)

        vm.cambiarComentario("Realizado")
        vm.elegirFoto("content://evidencia")
        vm.pedirConfirmacion()

        assertEquals("Realizado", vm.comentario)
        assertEquals("content://evidencia", vm.fotoUri)
        assertTrue(vm.mostrarConfirmacion)

        vm.quitarFoto()
        vm.cancelarConfirmacion()

        assertNull(vm.fotoUri)
        assertFalse(vm.mostrarConfirmacion)
    }

    @Test
    fun enviarTransmiteLaEvidenciaYLimpiaComentarioTrasExito() {
        val repo = repositorio()
        val estudiante = UsuariosPrueba.estudiante1

        every {
            repo.marcarRealizada(
                1,
                estudiante.id,
                estudiante.grupo,
                "Trabajo terminado",
                "content://evidencia"
            )
        } returns Result.success(Unit)

        val vm = DetalleTareaViewModel(repo)
        vm.cambiarComentario("  Trabajo terminado  ")
        vm.elegirFoto("content://evidencia")
        vm.pedirConfirmacion()

        vm.enviar(estudiante)

        verify(exactly = 1) {
            repo.marcarRealizada(
                1,
                estudiante.id,
                estudiante.grupo,
                "Trabajo terminado",
                "content://evidencia"
            )
        }

        assertFalse(vm.mostrarConfirmacion)
        assertFalse(vm.mensajeEsError)
        assertEquals("", vm.comentario)

        assertEquals(
            "Tarea enviada. Quedó pendiente de validación.",
            vm.mensaje
        )
    }

    @Test
    fun envioFallidoConservaComentarioYFoto() {
        val repo = repositorio()

        every {
            repo.marcarRealizada(
                any(), any(), any(), any(), any()
            )
        } returns Result.failure(
            IllegalStateException("Sin conexión")
        )

        val vm = DetalleTareaViewModel(repo)
        vm.cambiarComentario("No perder")
        vm.elegirFoto("content://foto")

        vm.enviar(UsuariosPrueba.estudiante1)

        assertTrue(vm.mensajeEsError)
        assertEquals("Sin conexión", vm.mensaje)
        assertEquals("No perder", vm.comentario)
        assertEquals("content://foto", vm.fotoUri)
    }

    @Test
    fun envioFallidoSinMensajeUtilizaTextoGenerico() {
        val repo = repositorio()

        every {
            repo.marcarRealizada(
                any(), any(), any(), any(), any()
            )
        } returns Result.failure(Exception())

        val vm = DetalleTareaViewModel(repo)
        vm.enviar(UsuariosPrueba.estudiante1)

        assertTrue(vm.mensajeEsError)
        assertEquals(
            "No se pudo enviar la tarea.",
            vm.mensaje
        )
    }

    @Test
    fun sinTareaSeleccionadaNoEnviaNada() {
        val repo = repositorio(idSeleccionado = null)
        val vm = DetalleTareaViewModel(repo)

        vm.pedirConfirmacion()
        vm.enviar(UsuariosPrueba.estudiante1)

        assertNull(vm.tareaId)
        assertFalse(vm.mostrarConfirmacion)

        verify(exactly = 0) {
            repo.marcarRealizada(
                any(), any(), any(), any(), any()
            )
        }
    }
}