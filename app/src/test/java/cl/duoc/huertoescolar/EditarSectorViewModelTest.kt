package cl.duoc.huertoescolar

import android.util.Log
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.ui.screens.sectores.EditarSectorViewModel
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class EditarSectorViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: HuertoRemoteRepository

    private val original = Sector(
        id = 12,
        nombre = "Bancal original",
        descripcion = "Sector de hortalizas",
        ubicacion = "Zona norte",
        estado = EstadoSector.EN_USO,
        responsable = "Usuario #8"
    )

    private val actualizado = SectorDto(
        id = 12,
        nombre = "Bancal editado",
        descripcion = "Descripción actualizada",
        ubicacion = "Zona sur",
        estado = "EN_MANTENIMIENTO",
        responsableId = 8
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()

        mockkObject(RepositorioHuerto)
        every {
            RepositorioHuerto.sectorSeleccionado
        } returns original

        mockkStatic(Log::class)
        every {
            Log.e(any<String>(), any<String>(), any<Throwable>())
        } returns 0
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
        unmockkObject(RepositorioHuerto)
        unmockkStatic(Log::class)
    }

    private fun completar(vm: EditarSectorViewModel) {
        vm.nombre = "  Bancal editado  "
        vm.descripcion = "  Descripción actualizada  "
        vm.ubicacion = "  Zona sur  "
        vm.estado = EstadoSector.EN_MANTENIMIENTO
    }

    @Test
    fun formularioComienzaConLosDatosDelSectorSeleccionado() {
        val vm = EditarSectorViewModel(repository)

        assertEquals(original.nombre, vm.nombre)
        assertEquals(original.descripcion, vm.descripcion)
        assertEquals(original.ubicacion, vm.ubicacion)
        assertEquals(original.estado, vm.estado)
        assertFalse(vm.guardando)
        assertFalse(vm.sectorActualizado)
        assertNull(vm.errorGuardado)
    }

    @Test
    fun sinSeleccionMuestraErrorYNoActualiza() = runTest {
        every {
            RepositorioHuerto.sectorSeleccionado
        } returns null

        val vm = EditarSectorViewModel(repository)
        vm.guardarCambios()
        advanceUntilIdle()

        assertEquals("", vm.nombre)
        assertEquals(EstadoSector.DISPONIBLE, vm.estado)
        assertNotNull(vm.errorGuardado)
        assertFalse(vm.sectorActualizado)

        coVerify(exactly = 0) {
            repository.actualizarSector(
                any(), any(), any(), any(), any()
            )
        }
    }

    @Test
    fun camposInvalidosImpidenActualizar() = runTest {
        val vm = EditarSectorViewModel(repository)
        vm.nombre = ""
        vm.descripcion = "1234"
        vm.ubicacion = "   "

        vm.guardarCambios()
        advanceUntilIdle()

        assertTrue(vm.errorNombre)
        assertTrue(vm.errorDescripcion)
        assertTrue(vm.errorUbicacion)
        assertFalse(vm.guardando)

        coVerify(exactly = 0) {
            repository.actualizarSector(
                any(), any(), any(), any(), any()
            )
        }
    }

    @Test
    fun actualizaElIdOriginalConDatosRecortadosYEsperaConfirmacion() =
        runTest {
            val pendiente = CompletableDeferred<SectorDto>()

            coEvery {
                repository.actualizarSector(
                    12,
                    "Bancal editado",
                    "Descripción actualizada",
                    "Zona sur",
                    EstadoSector.EN_MANTENIMIENTO
                )
            } coAnswers {
                pendiente.await()
            }

            val vm = EditarSectorViewModel(repository)
            completar(vm)

            vm.guardarCambios()
            runCurrent()

            assertTrue(vm.guardando)
            assertFalse(vm.sectorActualizado)

            vm.guardarCambios()
            runCurrent()

            coVerify(exactly = 1) {
                repository.actualizarSector(
                    12,
                    "Bancal editado",
                    "Descripción actualizada",
                    "Zona sur",
                    EstadoSector.EN_MANTENIMIENTO
                )
            }

            pendiente.complete(actualizado)
            advanceUntilIdle()

            assertFalse(vm.guardando)
            assertTrue(vm.sectorActualizado)
            assertNull(vm.errorGuardado)

            vm.guardarCambios()
            advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.actualizarSector(
                    any(), any(), any(), any(), any()
                )
            }
        }

    @Test
    fun falloConservaEdicionYPermiteReintentar() = runTest {
        coEvery {
            repository.actualizarSector(
                any(), any(), any(), any(), any()
            )
        } throws IOException("Sin conexión")

        val vm = EditarSectorViewModel(repository)
        completar(vm)

        vm.guardarCambios()
        advanceUntilIdle()

        assertFalse(vm.guardando)
        assertFalse(vm.sectorActualizado)
        assertNotNull(vm.errorGuardado)
        assertEquals("  Bancal editado  ", vm.nombre)
        assertEquals("  Descripción actualizada  ", vm.descripcion)
        assertEquals("  Zona sur  ", vm.ubicacion)
        assertEquals(EstadoSector.EN_MANTENIMIENTO, vm.estado)

        coEvery {
            repository.actualizarSector(
                any(), any(), any(), any(), any()
            )
        } returns actualizado

        vm.guardarCambios()
        advanceUntilIdle()

        assertTrue(vm.sectorActualizado)
        assertFalse(vm.guardando)
        assertNull(vm.errorGuardado)
    }
}