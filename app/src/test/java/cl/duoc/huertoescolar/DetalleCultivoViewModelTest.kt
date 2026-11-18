package cl.duoc.huertoescolar

import android.util.Log
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.DetalleCultivoViewModel
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
class DetalleCultivoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: HuertoRemoteRepository

    private val cultivo = Cultivo(
        id = 12,
        nombre = "Tomates",
        descripcion = "Cultivo de tomates",
        sectorId = 7,
        fechaSiembra = "17/11/2026",
        fechaCosechaEstimada = "17/02/2027",
        frecuenciaRiegoDias = 3
    )

    private val sector = SectorDto(
        id = 7,
        nombre = "Bancal del servidor",
        descripcion = "Sector remoto",
        ubicacion = "Zona norte",
        estado = "DISPONIBLE",
        responsableId = 1
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()

        mockkObject(RepositorioHuerto)
        every {
            RepositorioHuerto.cultivoSeleccionado
        } returns cultivo

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

    @Test
    fun consultaElSectorDelCultivoSeleccionado() = runTest {
        coEvery {
            repository.obtenerSector(7)
        } returns sector

        val vm = DetalleCultivoViewModel(repository)
        advanceUntilIdle()

        assertEquals(cultivo, vm.cultivo)
        assertEquals("Bancal del servidor", vm.nombreSector)
        assertFalse(vm.cargandoSector)
        assertNull(vm.errorSector)

        coVerify(exactly = 1) {
            repository.obtenerSector(7)
        }
    }

    @Test
    fun sinCultivoSeleccionadoNoConsultaElServidor() = runTest {
        every {
            RepositorioHuerto.cultivoSeleccionado
        } returns null

        val vm = DetalleCultivoViewModel(repository)
        advanceUntilIdle()

        assertNull(vm.cultivo)
        assertNull(vm.nombreSector)
        assertFalse(vm.cargandoSector)

        coVerify(exactly = 0) {
            repository.obtenerSector(any())
        }
    }

    @Test
    fun falloDeConsultaPermiteReintentar() = runTest {
        coEvery {
            repository.obtenerSector(7)
        } throws IOException("Sin conexión")

        val vm = DetalleCultivoViewModel(repository)
        advanceUntilIdle()

        assertFalse(vm.cargandoSector)
        assertNull(vm.nombreSector)
        assertEquals(
            "No se pudo consultar el nombre del sector.",
            vm.errorSector
        )

        coEvery {
            repository.obtenerSector(7)
        } returns sector

        vm.cargarSector()

        assertTrue(vm.cargandoSector)
        assertNull(vm.errorSector)

        advanceUntilIdle()

        assertEquals(sector.nombre, vm.nombreSector)
        assertFalse(vm.cargandoSector)
        assertNull(vm.errorSector)
    }

    @Test
    fun evitaConsultasDuplicadasMientrasEspera() = runTest {
        val pendiente = CompletableDeferred<SectorDto>()

        coEvery {
            repository.obtenerSector(7)
        } coAnswers {
            pendiente.await()
        }

        val vm = DetalleCultivoViewModel(repository)
        runCurrent()

        assertTrue(vm.cargandoSector)

        vm.cargarSector()
        vm.cargarSector()
        runCurrent()

        coVerify(exactly = 1) {
            repository.obtenerSector(7)
        }

        pendiente.complete(sector)
        advanceUntilIdle()

        assertFalse(vm.cargandoSector)
        assertEquals(sector.nombre, vm.nombreSector)
    }
}