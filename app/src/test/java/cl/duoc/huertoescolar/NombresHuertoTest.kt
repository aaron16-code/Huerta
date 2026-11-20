package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.*
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.RolUsuario
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.every
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class NombresHuertoTest {

    @Before
    fun preparar() {
        mockkObject(RepositorioHuerto)

        val sector = listaSectoresInicial().first()
        val cultivo = listaCultivosInicial().first()

        every { RepositorioHuerto.sectores } returns listOf(
            sector,
            sector.copy(id = 999, nombre = "Sector prueba")
        )

        every { RepositorioHuerto.cultivos } returns listOf(
            cultivo,
            cultivo.copy(id = 999, nombre = "Cultivo prueba")
        )
    }

    @After
    fun limpiar() {
        unmockkObject(RepositorioHuerto)
    }

    @Test
    fun sectorExistente() {
        assertEquals(
            "Sector prueba",
            nombreDeSector(999)
        )
    }

    @Test
    fun sectorInexistente() {
        assertEquals(
            "Sector -1",
            nombreDeSector(-1)
        )
    }

    @Test
    fun sectoresVacios() {
        every { RepositorioHuerto.sectores } returns emptyList()
        assertEquals("Sector 99", nombreDeSector(99))
    }

    @Test
    fun cultivoExistente() {
        assertEquals(
            "Cultivo prueba",
            nombreDeCultivo(999)
        )
    }

    @Test
    fun cultivoInexistente() {
        assertEquals(
            "Cultivo -1",
            nombreDeCultivo(-1)
        )
    }

    @Test
    fun cultivoSinAsociar() {
        assertEquals(
            "Sin cultivo asociado",
            nombreDeCultivo(null)
        )
    }

    @Test
    fun cultivosVacios() {
        every { RepositorioHuerto.cultivos } returns emptyList()
        assertEquals("Cultivo 99", nombreDeCultivo(99))
    }

    @Test
    fun obtenerDocentePorRol() {
        assertEquals(
            UsuariosPrueba.docente,
            UsuariosPrueba.porRol(RolUsuario.ADMINISTRADOR)
        )
    }

    @Test
    fun obtenerEstudiantePorRol() {
        assertEquals(
            UsuariosPrueba.estudiante,
            UsuariosPrueba.porRol(RolUsuario.ESTUDIANTE)
        )
    }
}
