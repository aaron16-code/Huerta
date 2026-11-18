package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.domain.model.*
import cl.duoc.huertoescolar.domain.repository.CatalogoHuertoRepository
import cl.duoc.huertoescolar.domain.repository.TareasRepository
import cl.duoc.huertoescolar.ui.screens.tareas.EditarTareaViewModel
import cl.duoc.huertoescolar.ui.screens.tareas.parsearFechaFinDelDia
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

class EditarTareaViewModelTest {

    private val original = Tarea(
        id = 42,
        tipo = TipoTarea.RIEGO,
        descripcion = "Regar tomates",
        instrucciones = "Evitar encharcar",
        sectorId = 1,
        cultivoId = 7,
        fechaCreacion = 100L,
        fechaProgramada = parsearFechaFinDelDia("17/11/2026")!!,
        responsableUsuarioId = 2,
        creadaPorId = 1,
        estado = EstadoTarea.VALIDADA,
        fechaRealizacion = 200L,
        realizadaPorId = 2,
        fotoUri = "content://evidencia",
        comentarioEstudiante = "Realizado",
        comentarioDocente = "Correcto",
        validadorId = 1,
        fechaValidacion = 300L
    )

    private val repo = mockk<TareasRepository> {
        every { tareaSeleccionadaId } returns 42
        every { obtenerTarea(42) } returns original
    }

    private val catalogo = mockk<CatalogoHuertoRepository> {
        every { sectores } returns emptyList()
        every { cultivos } returns emptyList()
    }

    @Test
    fun cargaLosDatosYElDestinatarioOriginal() {
        val vm = EditarTareaViewModel(repo, catalogo)

        assertTrue(vm.uiState.tareaDisponible)
        assertEquals(original.tipo, vm.uiState.tipo)
        assertEquals(original.descripcion, vm.uiState.descripcion)
        assertEquals(original.instrucciones, vm.uiState.instrucciones)
        assertEquals(1, vm.uiState.sectorId)
        assertEquals(7, vm.uiState.cultivoId)
        assertEquals("17/11/2026", vm.uiState.fechaTexto)
        assertEquals(2, vm.uiState.destinatario?.usuarioId)
        assertFalse(vm.uiState.guardada)
    }

    @Test
    fun sinSeleccionNoIntentaGuardar() {
        every { repo.tareaSeleccionadaId } returns null

        val vm = EditarTareaViewModel(repo, catalogo)
        vm.guardar(UsuariosPrueba.docente)

        assertFalse(vm.uiState.tareaDisponible)
        assertFalse(vm.uiState.guardada)

        verify(exactly = 0) {
            repo.editarTarea(any(), any())
        }
    }

    @Test
    fun seleccionDeTareaInexistenteNoPermiteGuardar() {
        every { repo.obtenerTarea(42) } returns null

        val vm = EditarTareaViewModel(repo, catalogo)
        vm.guardar(UsuariosPrueba.docente)

        assertFalse(vm.uiState.tareaDisponible)

        verify(exactly = 0) {
            repo.editarTarea(any(), any())
        }
    }

    @Test
    fun camposInvalidosGeneranErroresSinEnviar() {
        val vm = EditarTareaViewModel(repo, catalogo)
        vm.cambiarDescripcion("")
        vm.cambiarInstrucciones("1234")
        vm.cambiarFecha("incorrecta")

        vm.guardar(UsuariosPrueba.docente)

        assertNotNull(vm.uiState.errorDescripcion)
        assertNotNull(vm.uiState.errorInstrucciones)
        assertNotNull(vm.uiState.errorFecha)
        assertFalse(vm.uiState.guardada)

        verify(exactly = 0) {
            repo.editarTarea(any(), any())
        }

        vm.cambiarDescripcion("Descripción corregida")
        vm.cambiarInstrucciones("Instrucciones corregidas")
        vm.cambiarFecha("18/11/2026")

        assertNull(vm.uiState.errorDescripcion)
        assertNull(vm.uiState.errorInstrucciones)
        assertNull(vm.uiState.errorFecha)
    }

    @Test
    fun cambiarSectorConservaCultivoSoloSiElSectorEsElMismo() {
        val vm = EditarTareaViewModel(repo, catalogo)

        vm.elegirSector(1)
        assertEquals(7, vm.uiState.cultivoId)

        vm.elegirSector(2)
        assertEquals(2, vm.uiState.sectorId)
        assertNull(vm.uiState.cultivoId)

        vm.elegirCultivo(8)
        assertEquals(8, vm.uiState.cultivoId)
    }

    @Test
    fun guardarConservaEvidenciaEstadoYValidacion() {
        val enviada = slot<Tarea>()

        every {
            repo.editarTarea(
                RolUsuario.ADMINISTRADOR,
                capture(enviada)
            )
        } returns Result.success(Unit)

        val vm = EditarTareaViewModel(repo, catalogo)
        vm.elegirTipo(TipoTarea.LIMPIEZA)
        vm.cambiarDescripcion("  Limpiar el bancal  ")
        vm.cambiarInstrucciones("  Retirar residuos  ")
        vm.cambiarFecha("18/11/2026")
        vm.elegirDestinatario(
            vm.destinatarios.first { it.usuarioId == 3 }
        )

        vm.guardar(UsuariosPrueba.docente)

        assertEquals(
            original.copy(
                tipo = TipoTarea.LIMPIEZA,
                descripcion = "Limpiar el bancal",
                instrucciones = "Retirar residuos",
                fechaProgramada =
                    parsearFechaFinDelDia("18/11/2026")!!,
                responsableUsuarioId = 3,
                grupoAsignado = null
            ),
            enviada.captured
        )

        assertTrue(vm.uiState.guardada)
        assertNull(vm.uiState.mensajeError)
    }

    @Test
    fun permiteReasignarLaTareaAUnGrupo() {
        val enviada = slot<Tarea>()

        every {
            repo.editarTarea(any(), capture(enviada))
        } returns Result.success(Unit)

        val vm = EditarTareaViewModel(repo, catalogo)
        val grupo = vm.destinatarios.first { it.grupo != null }

        vm.elegirDestinatario(grupo)
        vm.guardar(UsuariosPrueba.docente)

        assertNull(enviada.captured.responsableUsuarioId)
        assertEquals(grupo.grupo, enviada.captured.grupoAsignado)
        assertTrue(vm.uiState.guardada)
    }

    @Test
    fun falloConservaLosCambiosSinMarcarGuardado() {
        every {
            repo.editarTarea(any(), any())
        } returns Result.failure(
            IllegalStateException("No se pudo conectar")
        )

        val vm = EditarTareaViewModel(repo, catalogo)
        vm.cambiarDescripcion("Descripción editada")
        vm.guardar(UsuariosPrueba.docente)

        assertFalse(vm.uiState.guardada)
        assertEquals(
            "No se pudo conectar",
            vm.uiState.mensajeError
        )
        assertEquals(
            "Descripción editada",
            vm.uiState.descripcion
        )
    }

    @Test
    fun falloSinMensajeMuestraErrorGenerico() {
        every {
            repo.editarTarea(any(), any())
        } returns Result.failure(Exception())

        val vm = EditarTareaViewModel(repo, catalogo)
        vm.guardar(UsuariosPrueba.docente)

        assertFalse(vm.uiState.guardada)
        assertEquals(
            "No se pudo actualizar la tarea.",
            vm.uiState.mensajeError
        )
    }
}