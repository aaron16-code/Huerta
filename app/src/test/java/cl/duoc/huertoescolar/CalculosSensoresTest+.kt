package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.domain.model.EstadoLectura
import cl.duoc.huertoescolar.domain.model.LecturaSensor
import cl.duoc.huertoescolar.ui.screens.sensores.agruparLecturasPorSector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculosSensoresTest {

    @Test
    fun sinLecturasDevuelveUnaListaVacia() {
        val resultado = agruparLecturasPorSector(emptyList())

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun separaLosSectoresYSeleccionaSuLecturaMasReciente() {
        val resultado = agruparLecturasPorSector(
            listOf(
                lectura(id = 1, sectorId = 2, fecha = 300L),
                lectura(id = 2, sectorId = 1, fecha = 100L),
                lectura(id = 3, sectorId = 1, fecha = 200L)
            )
        )

        assertEquals(listOf(1, 2), resultado.map { it.sectorId })
        assertEquals(3, resultado[0].ultima.id)
        assertEquals(listOf(2), resultado[0].anteriores.map { it.id })
        assertEquals(1, resultado[1].ultima.id)
        assertTrue(resultado[1].anteriores.isEmpty())
    }

    @Test
    fun conservaSoloLasTresLecturasAnterioresMasRecientes() {
        val resultado = agruparLecturasPorSector(
            listOf(
                lectura(id = 2, sectorId = 1, fecha = 200L),
                lectura(id = 5, sectorId = 1, fecha = 500L),
                lectura(id = 1, sectorId = 1, fecha = 100L),
                lectura(id = 4, sectorId = 1, fecha = 400L),
                lectura(id = 3, sectorId = 1, fecha = 300L)
            )
        ).single()

        assertEquals(5, resultado.ultima.id)
        assertEquals(
            listOf(4, 3, 2),
            resultado.anteriores.map { it.id }
        )
    }

    private fun lectura(
        id: Int,
        sectorId: Int,
        fecha: Long
    ): LecturaSensor {
        return LecturaSensor(
            id = id,
            sectorId = sectorId,
            fecha = fecha,
            humedadSuelo = 55,
            temperaturaAmbiente = 21.5,
            humedadAmbiente = 60,
            estado = EstadoLectura.NORMAL
        )
    }
}