package cl.duoc.huertoescolar

import android.util.Log
import cl.duoc.huertoescolar.data.remote.CultivoDto
import cl.duoc.huertoescolar.data.remote.CultivoRequest
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.ui.screens.cultivos.CultivoViewModel
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
class CultivoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: HuertoRemoteRepository

    private fun sector(id: Int = 7) = SectorDto(
        id = id,
        nombre = "Bancal $id",
        descripcion = "Sector remoto",
        ubicacion = "Zona norte",
        estado = "DISPONIBLE",
        responsableId = null
    )

    private fun respuesta() = CultivoDto(
        id = 42,
        nombre = "Tomates",
        descripcion = "Cultivo de tomates",
        sectorId = 7,
        fechaSiembra = "17/11/2026",
        fechaCosechaEstimada = "17/02/2027",
        frecuenciaRiegoDias = 3,
        estado = "PLANIFICADO",
        observaciones = "Observación",
        fotoUri = null
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()

        mockkStatic(Log::class)
        every {
            Log.e(any<String>(), any<String>(), any<Throwable>())
        } returns 0

        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector())
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun completar(vm: CultivoViewModel) {
        vm.nombre = "  Tomates  "
        vm.descripcion = "  Cultivo de tomates  "
        vm.fechaSiembra = "17/11/2026"
        vm.fechaCosecha = "17/02/2027"
        vm.frecuenciaRiego = "3"
        vm.observaciones = "  Observación  "
    }

    @Test
    fun cargaSectoresRemotosYSeleccionaElPrimero() = runTest {
        val vm = CultivoViewModel(repository)

        assertTrue(vm.cargandoSectores)
        advanceUntilIdle()

        assertFalse(vm.cargandoSectores)
        assertNull(vm.errorSectores)
        assertEquals(7, vm.sectorId)
        assertEquals("Bancal 7", vm.sectores.single().nombre)
        assertEquals(
            "Sin responsable",
            vm.sectores.single().responsable
        )
    }

    @Test
    fun respuestaSinSectoresMantieneSeleccionVacia() = runTest {
        coEvery {
            repository.obtenerSectores()
        } returns emptyList()

        val vm = CultivoViewModel(repository)
        advanceUntilIdle()

        assertTrue(vm.sectores.isEmpty())
        assertNull(vm.sectorId)
        assertNull(vm.errorSectores)
        assertFalse(vm.cargandoSectores)
    }

    @Test
    fun falloDeCargaPermiteReintentar() = runTest {
        coEvery {
            repository.obtenerSectores()
        } throws IOException("Sin conexión")

        val vm = CultivoViewModel(repository)
        advanceUntilIdle()

        assertNotNull(vm.errorSectores)
        assertFalse(vm.cargandoSectores)

        vm.guardarCultivo()
        coVerify(exactly = 0) {
            repository.crearCultivo(any())
        }

        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector())

        vm.cargarSectores()
        advanceUntilIdle()

        assertNull(vm.errorSectores)
        assertEquals(7, vm.sectorId)
    }

    @Test
    fun formularioInvalidoMarcaErroresYNoGuarda() = runTest {
        val vm = CultivoViewModel(repository)
        advanceUntilIdle()

        vm.sectorId = null
        vm.guardarCultivo()

        assertTrue(vm.errorNombre)
        assertTrue(vm.errorDescripcion)
        assertTrue(vm.errorSector)
        assertTrue(vm.errorFechaSiembra)
        assertTrue(vm.errorFechaCosecha)
        assertTrue(vm.errorFrecuencia)
        assertFalse(vm.cultivoCreado)

        coVerify(exactly = 0) {
            repository.crearCultivo(any())
        }
    }

    @Test
    fun rechazaUnSectorQueNoEstaEnElCatalogoRemoto() = runTest {
        val vm = CultivoViewModel(repository)
        advanceUntilIdle()
        completar(vm)

        vm.sectorId = 999
        vm.guardarCultivo()

        assertTrue(vm.errorSector)
        assertFalse(vm.guardando)

        coVerify(exactly = 0) {
            repository.crearCultivo(any())
        }
    }

    @Test
    fun guardaDatosCorrectosYEsperaLaRespuestaDelServidor() = runTest {
        val pendiente = CompletableDeferred<CultivoDto>()
        val solicitud = slot<CultivoRequest>()

        coEvery {
            repository.crearCultivo(capture(solicitud))
        } coAnswers {
            pendiente.await()
        }

        val vm = CultivoViewModel(repository)
        advanceUntilIdle()
        completar(vm)

        vm.guardarCultivo()
        runCurrent()

        assertTrue(vm.guardando)
        assertFalse(vm.cultivoCreado)

        assertEquals(
            CultivoRequest(
                nombre = "Tomates",
                descripcion = "Cultivo de tomates",
                sectorId = 7,
                fechaSiembra = "17/11/2026",
                fechaCosechaEstimada = "17/02/2027",
                frecuenciaRiegoDias = 3,
                estado = "PLANIFICADO",
                observaciones = "Observación",
                fotoUri = null
            ),
            solicitud.captured
        )

        vm.guardarCultivo()
        runCurrent()

        coVerify(exactly = 1) {
            repository.crearCultivo(any())
        }

        pendiente.complete(respuesta())
        advanceUntilIdle()

        assertTrue(vm.cultivoCreado)
        assertFalse(vm.guardando)
        assertNull(vm.errorGuardado)

        vm.guardarCultivo()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.crearCultivo(any())
        }
    }

    @Test
    fun falloDeGuardadoConservaDatosYPermiteReintentar() = runTest {
        coEvery {
            repository.crearCultivo(any())
        } throws IOException("Servidor no disponible")

        val vm = CultivoViewModel(repository)
        advanceUntilIdle()
        completar(vm)

        vm.guardarCultivo()
        advanceUntilIdle()

        assertFalse(vm.guardando)
        assertFalse(vm.cultivoCreado)
        assertNotNull(vm.errorGuardado)
        assertEquals("  Tomates  ", vm.nombre)
        assertEquals("  Cultivo de tomates  ", vm.descripcion)
        assertEquals("17/11/2026", vm.fechaSiembra)
        assertEquals("17/02/2027", vm.fechaCosecha)
        assertEquals("3", vm.frecuenciaRiego)
        assertEquals("  Observación  ", vm.observaciones)

        coEvery {
            repository.crearCultivo(any())
        } returns respuesta()

        vm.guardarCultivo()
        advanceUntilIdle()

        assertTrue(vm.cultivoCreado)
        assertNull(vm.errorGuardado)
    }

    @Test
    fun recargarConservaSeleccionValidaYReemplazaLaInexistente() = runTest {
        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector(7), sector(8))

        val vm = CultivoViewModel(repository)
        advanceUntilIdle()

        vm.sectorId = 8
        vm.cargarSectores()
        advanceUntilIdle()

        assertEquals(8, vm.sectorId)

        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector(7))

        vm.cargarSectores()
        advanceUntilIdle()

        assertEquals(7, vm.sectorId)
    }
}