package cl.duoc.huertoescolar

import android.util.Log
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.remote.SectorRequest
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.ui.screens.sectores.SectorViewModel
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
class SectorViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: HuertoRemoteRepository

    private val respuesta = SectorDto(
        id = 42,
        nombre = "Bancal nuevo",
        descripcion = "Sector de tomates",
        ubicacion = "Zona sur",
        estado = "DISPONIBLE",
        responsableId = 1
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()

        mockkStatic(Log::class)
        every {
            Log.e(any<String>(), any<String>(), any<Throwable>())
        } returns 0
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun formularioValido(): SectorViewModel {
        return SectorViewModel(repository).apply {
            nombre = "  Bancal nuevo  "
            descripcion = "  Sector de tomates  "
            ubicacion = "  Zona sur  "
        }
    }

    @Test
    fun formularioVacioMarcaErroresYNoEnviaSolicitud() = runTest {
        val vm = SectorViewModel(repository)

        vm.guardarSector()
        advanceUntilIdle()

        assertTrue(vm.errorNombre)
        assertTrue(vm.errorDescripcion)
        assertTrue(vm.errorUbicacion)
        assertFalse(vm.guardando)
        assertFalse(vm.sectorCreado)

        coVerify(exactly = 0) {
            repository.crearSector(any())
        }
    }

    @Test
    fun corregirCamposLimpiaLosErroresDeValidacion() {
        val vm = SectorViewModel(repository)

        assertFalse(vm.validarFormulario())

        vm.nombre = "Bancal nuevo"
        vm.descripcion = "Sector de tomates"
        vm.ubicacion = "Zona sur"

        assertTrue(vm.validarFormulario())
        assertFalse(vm.errorNombre)
        assertFalse(vm.errorDescripcion)
        assertFalse(vm.errorUbicacion)
        assertEquals("Bancal nuevo", vm.nombre)
        assertEquals("Sector de tomates", vm.descripcion)
        assertEquals("Zona sur", vm.ubicacion)
    }

    @Test
    fun guardaDatosRecortadosYConfirmaSoloTrasLaRespuesta() = runTest {
        val pendiente = CompletableDeferred<SectorDto>()
        val solicitud = slot<SectorRequest>()

        coEvery {
            repository.crearSector(capture(solicitud))
        } coAnswers {
            pendiente.await()
        }

        val vm = formularioValido()
        vm.guardarSector()
        runCurrent()

        assertTrue(vm.guardando)
        assertFalse(vm.sectorCreado)
        assertNull(vm.errorGuardado)

        assertEquals(
            SectorRequest(
                nombre = "Bancal nuevo",
                descripcion = "Sector de tomates",
                ubicacion = "Zona sur",
                estado = "DISPONIBLE",
                responsableId = 1
            ),
            solicitud.captured
        )

        pendiente.complete(respuesta)
        advanceUntilIdle()

        assertFalse(vm.guardando)
        assertTrue(vm.sectorCreado)
        assertNull(vm.errorGuardado)
    }

    @Test
    fun noDuplicaGuardadoMientrasEsperaNiDespuesDelExito() = runTest {
        val pendiente = CompletableDeferred<SectorDto>()

        coEvery {
            repository.crearSector(any())
        } coAnswers {
            pendiente.await()
        }

        val vm = formularioValido()

        vm.guardarSector()
        vm.guardarSector()
        runCurrent()

        coVerify(exactly = 1) {
            repository.crearSector(any())
        }

        pendiente.complete(respuesta)
        advanceUntilIdle()

        vm.guardarSector()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.crearSector(any())
        }
    }

    @Test
    fun falloDeGuardadoConservaElFormularioYNoConfirmaExito() = runTest {
        coEvery {
            repository.crearSector(any())
        } throws IOException("Sin conexión")

        val vm = formularioValido()
        vm.guardarSector()
        advanceUntilIdle()

        assertFalse(vm.guardando)
        assertFalse(vm.sectorCreado)
        assertNotNull(vm.errorGuardado)
        assertEquals("  Bancal nuevo  ", vm.nombre)
        assertEquals("  Sector de tomates  ", vm.descripcion)
        assertEquals("  Zona sur  ", vm.ubicacion)
    }

    @Test
    fun permiteReintentarDespuesDeUnFallo() = runTest {
        coEvery {
            repository.crearSector(any())
        } throws IOException("Sin conexión")

        val vm = formularioValido()
        vm.guardarSector()
        advanceUntilIdle()

        assertNotNull(vm.errorGuardado)

        coEvery {
            repository.crearSector(any())
        } returns respuesta

        vm.guardarSector()

        assertTrue(vm.guardando)
        assertNull(vm.errorGuardado)

        advanceUntilIdle()

        assertTrue(vm.sectorCreado)
        assertFalse(vm.guardando)
        assertNull(vm.errorGuardado)

        coVerify(exactly = 2) {
            repository.crearSector(any())
        }
    }
}