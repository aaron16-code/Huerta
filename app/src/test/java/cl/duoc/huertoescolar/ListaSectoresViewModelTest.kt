package cl.duoc.huertoescolar

import android.util.Log
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.ui.screens.sectores.ListaSectoresViewModel
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
class ListaSectoresViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: HuertoRemoteRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()

        // Evita depender del Log de Android en pruebas locales.
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

    private fun sector(id: Int = 1) = SectorDto(
        id = id,
        nombre = "Bancal $id",
        descripcion = "Sector de hortalizas",
        ubicacion = "Zona norte",
        estado = "DISPONIBLE",
        responsableId = 1
    )

    @Test
    fun muestraCargaMientrasEsperaYPublicaLosSectores() = runTest {
        val respuesta = CompletableDeferred<List<SectorDto>>()
        val esperados = listOf(sector(1), sector(2))

        coEvery {
            repository.obtenerSectores()
        } coAnswers {
            respuesta.await()
        }

        val vm = ListaSectoresViewModel(repository)
        runCurrent()

        assertTrue(vm.uiState.value.cargando)
        assertTrue(vm.uiState.value.sectores.isEmpty())
        assertNull(vm.uiState.value.error)

        respuesta.complete(esperados)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
        assertEquals(esperados, vm.uiState.value.sectores)
        assertNull(vm.uiState.value.error)

        coVerify(exactly = 1) {
            repository.obtenerSectores()
        }
    }

    @Test
    fun listaVaciaEsUnResultadoExitoso() = runTest {
        coEvery {
            repository.obtenerSectores()
        } returns emptyList()

        val vm = ListaSectoresViewModel(repository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
        assertTrue(vm.uiState.value.sectores.isEmpty())
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun falloDeConexionMuestraErrorYTerminaLaCarga() = runTest {
        coEvery {
            repository.obtenerSectores()
        } throws IOException("Sin conexión")

        val vm = ListaSectoresViewModel(repository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
        assertTrue(vm.uiState.value.sectores.isEmpty())
        assertEquals(
            "No se pudieron cargar los sectores. Intenta nuevamente.",
            vm.uiState.value.error
        )
    }

    @Test
    fun reintentoExitosoLimpiaElErrorAnterior() = runTest {
        coEvery {
            repository.obtenerSectores()
        } throws IOException("Sin conexión")

        val vm = ListaSectoresViewModel(repository)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)

        val esperados = listOf(sector())
        coEvery {
            repository.obtenerSectores()
        } returns esperados

        vm.cargarSectores()

        assertTrue(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.error)

        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
        assertEquals(esperados, vm.uiState.value.sectores)
        assertNull(vm.uiState.value.error)

        coVerify(exactly = 2) {
            repository.obtenerSectores()
        }
    }

    @Test
    fun noDuplicaLaConsultaMientrasEstaCargando() = runTest {
        val respuesta = CompletableDeferred<List<SectorDto>>()

        coEvery {
            repository.obtenerSectores()
        } coAnswers {
            respuesta.await()
        }

        val vm = ListaSectoresViewModel(repository)
        runCurrent()

        vm.cargarSectores()
        vm.cargarSectores()
        runCurrent()

        coVerify(exactly = 1) {
            repository.obtenerSectores()
        }

        respuesta.complete(emptyList())
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun falloAlActualizarConservaLaListaAnterior() = runTest {
        val anteriores = listOf(sector())

        coEvery {
            repository.obtenerSectores()
        } returns anteriores

        val vm = ListaSectoresViewModel(repository)
        advanceUntilIdle()

        coEvery {
            repository.obtenerSectores()
        } throws IOException("Servidor no disponible")

        vm.cargarSectores()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
        assertEquals(anteriores, vm.uiState.value.sectores)
        assertNotNull(vm.uiState.value.error)
    }
}