package cl.duoc.huertoescolar

import android.util.Log
import cl.duoc.huertoescolar.data.remote.CultivoDto
import cl.duoc.huertoescolar.data.remote.CultivoRequest
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.EditarCultivoViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class EditarCultivoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: HuertoRemoteRepository

    private val original = Cultivo(
        id = 42,
        nombre = "Tomates",
        descripcion = "Cultivo original",
        sectorId = 7,
        fechaSiembra = "17/11/2026",
        fechaCosechaEstimada = "17/02/2027",
        frecuenciaRiegoDias = 2,
        estado = EstadoCultivo.PLANIFICADO,
        observaciones = "Observación original",
        fotoUri = "content://foto-original"
    )

    private val sector = SectorDto(
        id = 7,
        nombre = "Bancal 7",
        descripcion = "Sector remoto",
        ubicacion = "Zona norte",
        estado = "EN_USO",
        responsableId = 1
    )

    private val respuesta = CultivoDto(
        id = 42,
        nombre = "Tomates editados",
        descripcion = "Descripción actualizada",
        sectorId = 7,
        fechaSiembra = "18/11/2026",
        fechaCosechaEstimada = "18/02/2027",
        frecuenciaRiegoDias = 3,
        estado = "SEMBRADO",
        observaciones = "Nueva observación",
        fotoUri = "content://foto-original"
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()

        mockkObject(RepositorioHuerto)
        every {
            RepositorioHuerto.cultivoSeleccionado
        } returns original

        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector)

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

    private fun completar(vm: EditarCultivoViewModel) {
        vm.nombre = "  Tomates editados  "
        vm.descripcion = "  Descripción actualizada  "
        vm.sectorId = 7
        vm.fechaSiembra = "18/11/2026"
        vm.fechaCosecha = "18/02/2027"
        vm.frecuenciaRiego = "3"
        vm.observaciones = "  Nueva observación  "
        vm.estado = EstadoCultivo.SEMBRADO
    }

    @Test
    fun cargaFormularioOriginalYSectoresRemotos() = runTest {
        val vm = EditarCultivoViewModel(repository)
        advanceUntilIdle()

        assertEquals(original.nombre, vm.nombre)
        assertEquals(original.descripcion, vm.descripcion)
        assertEquals(original.sectorId, vm.sectorId)
        assertEquals(original.fechaSiembra, vm.fechaSiembra)
        assertEquals(original.fechaCosechaEstimada, vm.fechaCosecha)
        assertEquals("2", vm.frecuenciaRiego)
        assertEquals(original.observaciones, vm.observaciones)
        assertEquals(original.estado, vm.estado)
        assertEquals("Bancal 7", vm.sectores.single().nombre)
        assertFalse(vm.cargandoSectores)
        assertNull(vm.errorSectores)
    }

    @Test
    fun sinCultivoSeleccionadoNoActualiza() = runTest {
        every {
            RepositorioHuerto.cultivoSeleccionado
        } returns null

        val vm = EditarCultivoViewModel(repository)
        advanceUntilIdle()

        vm.guardarCambios()
        advanceUntilIdle()

        assertNotNull(vm.errorGuardado)
        assertFalse(vm.cultivoActualizado)

        coVerify(exactly = 0) {
            repository.actualizarCultivo(any(), any())
        }
    }

    @Test
    fun formularioInvalidoNoEnviaActualizacion() = runTest {
        val vm = EditarCultivoViewModel(repository)
        advanceUntilIdle()

        vm.nombre = ""
        vm.descripcion = "1234"
        vm.sectorId = null
        vm.fechaSiembra = "incorrecta"
        vm.fechaCosecha = "incorrecta"
        vm.frecuenciaRiego = "0"

        vm.guardarCambios()
        advanceUntilIdle()

        assertTrue(vm.errorNombre)
        assertTrue(vm.errorDescripcion)
        assertTrue(vm.errorSector)
        assertTrue(vm.errorFechaSiembra)
        assertTrue(vm.errorFechaCosecha)
        assertTrue(vm.errorFrecuencia)

        coVerify(exactly = 0) {
            repository.actualizarCultivo(any(), any())
        }
    }

    @Test
    fun rechazaSectorQueNoExisteEnElCatalogo() = runTest {
        val vm = EditarCultivoViewModel(repository)
        advanceUntilIdle()

        vm.sectorId = 999
        vm.guardarCambios()
        advanceUntilIdle()

        assertTrue(vm.errorSector)
        assertFalse(vm.guardando)

        coVerify(exactly = 0) {
            repository.actualizarCultivo(any(), any())
        }
    }

    @Test
    fun actualizaElIdOriginalYConservaLaFoto() = runTest {
        val solicitud = slot<CultivoRequest>()

        coEvery {
            repository.actualizarCultivo(42, capture(solicitud))
        } returns respuesta

        val vm = EditarCultivoViewModel(repository)
        advanceUntilIdle()
        completar(vm)

        vm.guardarCambios()
        advanceUntilIdle()

        assertEquals(
            CultivoRequest(
                nombre = "Tomates editados",
                descripcion = "Descripción actualizada",
                sectorId = 7,
                fechaSiembra = "18/11/2026",
                fechaCosechaEstimada = "18/02/2027",
                frecuenciaRiegoDias = 3,
                estado = "SEMBRADO",
                observaciones = "Nueva observación",
                fotoUri = "content://foto-original"
            ),
            solicitud.captured
        )
        assertTrue(vm.cultivoActualizado)
        assertFalse(vm.guardando)
        assertNull(vm.errorGuardado)

        vm.guardarCambios()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.actualizarCultivo(any(), any())
        }
    }

    @Test
    fun errorDeCargaImpideGuardarYPermiteRecargar() = runTest {
        coEvery {
            repository.obtenerSectores()
        } throws IOException("Sin conexión")

        val vm = EditarCultivoViewModel(repository)
        advanceUntilIdle()

        assertNotNull(vm.errorSectores)
        vm.guardarCambios()

        coVerify(exactly = 0) {
            repository.actualizarCultivo(any(), any())
        }

        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector)

        vm.cargarSectores()
        advanceUntilIdle()

        assertNull(vm.errorSectores)
        assertFalse(vm.cargandoSectores)
        assertEquals(1, vm.sectores.size)
    }

    @Test
    fun falloAlGuardarConservaEdicionYPermiteReintentar() = runTest {
        coEvery {
            repository.actualizarCultivo(any(), any())
        } throws IOException("Servidor no disponible")

        val vm = EditarCultivoViewModel(repository)
        advanceUntilIdle()
        completar(vm)

        vm.guardarCambios()
        advanceUntilIdle()

        assertFalse(vm.cultivoActualizado)
        assertFalse(vm.guardando)
        assertNotNull(vm.errorGuardado)
        assertEquals("  Tomates editados  ", vm.nombre)
        assertEquals("  Nueva observación  ", vm.observaciones)
        assertEquals(EstadoCultivo.SEMBRADO, vm.estado)

        coEvery {
            repository.actualizarCultivo(any(), any())
        } returns respuesta

        vm.guardarCambios()
        advanceUntilIdle()

        assertTrue(vm.cultivoActualizado)
        assertNull(vm.errorGuardado)
    }
}