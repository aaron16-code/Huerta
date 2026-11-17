package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.remote.HuertoApi
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.remote.SectorRequest
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.domain.model.EstadoSector
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class HuertoRemoteRepositoryTest {

    private val api = mockk<HuertoApi>()
    private val repository = HuertoRemoteRepository(api)

    @Test
    fun actualizarSectorConservaResponsableDelServidor() = runBlocking {
        val original = SectorDto(
            id = 8,
            nombre = "Bancal original",
            descripcion = "Descripción original",
            ubicacion = "Zona norte",
            estado = "DISPONIBLE",
            responsableId = 42
        )

        val solicitudEsperada = SectorRequest(
            nombre = "Bancal actualizado",
            descripcion = "Descripción actualizada",
            ubicacion = "Zona sur",
            estado = "EN_MANTENIMIENTO",
            responsableId = 42
        )

        val respuesta = original.copy(
            nombre = solicitudEsperada.nombre,
            descripcion = solicitudEsperada.descripcion,
            ubicacion = solicitudEsperada.ubicacion,
            estado = solicitudEsperada.estado
        )

        coEvery { api.obtenerSector(8) } returns original
        coEvery {
            api.actualizarSector(8, solicitudEsperada)
        } returns respuesta

        val resultado = repository.actualizarSector(
            id = 8,
            nombre = "Bancal actualizado",
            descripcion = "Descripción actualizada",
            ubicacion = "Zona sur",
            estado = EstadoSector.EN_MANTENIMIENTO
        )

        assertEquals(respuesta, resultado)
        assertEquals(42, resultado.responsableId)

        coVerify(exactly = 1) {
            api.actualizarSector(8, solicitudEsperada)
        }
    }

    @Test
    fun noActualizaSectorSiNoTieneResponsable() = runBlocking {
        coEvery { api.obtenerSector(8) } returns SectorDto(
            id = 8,
            nombre = "Bancal",
            descripcion = "Descripción",
            ubicacion = "Zona norte",
            estado = "DISPONIBLE",
            responsableId = null
        )

        try {
            repository.actualizarSector(
                id = 8,
                nombre = "Nuevo nombre",
                descripcion = "Descripción",
                ubicacion = "Zona sur",
                estado = EstadoSector.EN_USO
            )

            fail("Debía rechazar un sector sin responsable")
        } catch (e: IllegalArgumentException) {
            assertEquals(
                "El sector no tiene un responsable asignado.",
                e.message
            )
        }

        coVerify(exactly = 0) {
            api.actualizarSector(any(), any())
        }
    }

    @Test
    fun noEnviaActualizacionSiFallaLaConsultaDelSector() = runBlocking {
        val fallo = IOException("Sin conexión")

        coEvery { api.obtenerSector(8) } throws fallo

        try {
            repository.actualizarSector(
                id = 8,
                nombre = "Nuevo nombre",
                descripcion = "Descripción",
                ubicacion = "Zona sur",
                estado = EstadoSector.EN_USO
            )

            fail("Debía propagar el error de conexión")
        } catch (e: IOException) {
            assertSame(fallo, e)
        }

        coVerify(exactly = 0) {
            api.actualizarSector(any(), any())
        }
    }

    @Test
    fun propagaErrorCuandoElServidorRechazaLaActualizacion() = runBlocking {
        val fallo = IOException("Error del servidor")

        coEvery { api.obtenerSector(8) } returns SectorDto(
            id = 8,
            nombre = "Bancal",
            descripcion = "Descripción",
            ubicacion = "Zona norte",
            estado = "DISPONIBLE",
            responsableId = 42
        )

        coEvery {
            api.actualizarSector(any(), any())
        } throws fallo

        try {
            repository.actualizarSector(
                id = 8,
                nombre = "Nuevo nombre",
                descripcion = "Descripción",
                ubicacion = "Zona sur",
                estado = EstadoSector.EN_USO
            )

            fail("Debía propagar el error de actualización")
        } catch (e: IOException) {
            assertSame(fallo, e)
        }
    }
}