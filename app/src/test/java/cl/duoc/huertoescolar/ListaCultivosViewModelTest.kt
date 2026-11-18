package cl.duoc.huertoescolar

import android.util.Log
import cl.duoc.huertoescolar.data.remote.CultivoDto
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.ListaCultivosViewModel
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
class ListaCultivosViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: HuertoRemoteRepository

    private fun sector() = SectorDto(
        id = 7,
        nombre = "Bancal remoto",
        descripcion = "Sector de prueba",
        ubicacion = "Zona norte",
        estado = "DISPONIBLE",
        responsableId = 1
    )

    private fun cultivo() = CultivoDto(
        id = 12,
        nombre = "Tomates",
        descripcion = "Cultivo remoto",
        sectorId = 7,
        fechaSiembra = "17/11/2026",
        fechaCosechaEstimada = "17/02/2027",
        frecuenciaRiegoDias = 3,
        estado = "SEMBRADO",
        observaciones = "Sin problemas",
        fotoUri = "content://foto"
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

    @Test
    fun convierteTodosLosCamposYObtieneNombresDeSectores() = runTest {
        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector())

        coEvery {
            repository.obtenerCultivos()
        } returns listOf(cultivo())

        val vm = ListaCultivosViewModel(repository)
        advanceUntilIdle()

        val esperado = Cultivo(
            id = 12,
            nombre = "Tomates",
            descripcion = "Cultivo remoto",
            sectorId = 7,
            fechaSiembra = "17/11/2026",
            fechaCosechaEstimada = "17/02/2027",
            frecuenciaRiegoDias = 3,
            estado = EstadoCultivo.SEMBRADO,
            observaciones = "Sin problemas",
            fotoUri = "content://foto"
        )

        assertEquals(listOf(esperado), vm.uiState.value.cultivos)
        assertEquals(
            mapOf(7 to "Bancal remoto"),
            vm.uiState.value.nombresSectores
        )
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun respuestaVaciaEsExitoSinDatosInventados() = runTest {
        coEvery {
            repository.obtenerSectores()
        } returns emptyList()

        coEvery {
            repository.obtenerCultivos()
        } returns emptyList()

        val vm = ListaCultivosViewModel(repository)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.cultivos.isEmpty())
        assertTrue(vm.uiState.value.nombresSectores.isEmpty())
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun falloAlConsultarSectoresDetieneLaCarga() = runTest {
        coEvery {
            repository.obtenerSectores()
        } throws IOException("Sin conexión")

        val vm = ListaCultivosViewModel(repository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.cultivos.isEmpty())

        coVerify(exactly = 0) {
            repository.obtenerCultivos()
        }
    }

    @Test
    fun falloAlConsultarCultivosPermiteReintentar() = runTest {
        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector())

        coEvery {
            repository.obtenerCultivos()
        } throws IOException("Servidor caído")

        val vm = ListaCultivosViewModel(repository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
        assertEquals(
            "No se pudieron cargar los cultivos. Intenta nuevamente.",
            vm.uiState.value.error
        )

        coEvery {
            repository.obtenerCultivos()
        } returns listOf(cultivo().copy(fotoUri = null))

        vm.cargarCultivos()

        assertTrue(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.error)

        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.cultivos.size)
        assertNull(vm.uiState.value.cultivos.single().fotoUri)
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun noDuplicaConsultasDuranteLaCarga() = runTest {
        val respuesta = CompletableDeferred<List<CultivoDto>>()

        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector())

        coEvery {
            repository.obtenerCultivos()
        } coAnswers {
            respuesta.await()
        }

        val vm = ListaCultivosViewModel(repository)
        runCurrent()

        assertTrue(vm.uiState.value.cargando)

        vm.cargarCultivos()
        vm.cargarCultivos()
        runCurrent()

        coVerify(exactly = 1) {
            repository.obtenerSectores()
        }
        coVerify(exactly = 1) {
            repository.obtenerCultivos()
        }

        respuesta.complete(emptyList())
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun falloDeActualizacionConservaLosDatosAnteriores() = runTest {
        coEvery {
            repository.obtenerSectores()
        } returns listOf(sector())

        coEvery {
            repository.obtenerCultivos()
        } returns listOf(cultivo())

        val vm = ListaCultivosViewModel(repository)
        advanceUntilIdle()

        val anteriores = vm.uiState.value

        coEvery {
            repository.obtenerCultivos()
        } throws IOException("Sin conexión")

        vm.cargarCultivos()
        advanceUntilIdle()

        assertEquals(anteriores.cultivos, vm.uiState.value.cultivos)
        assertEquals(
            anteriores.nombresSectores,
            vm.uiState.value.nombresSectores
        )
        assertFalse(vm.uiState.value.cargando)
        assertNotNull(vm.uiState.value.error)
    }
}