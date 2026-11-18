package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.local.toEntity
import cl.duoc.huertoescolar.data.local.toModelo
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.Sector
import org.junit.Assert.*
import org.junit.Test

class MapeosHuertoTest {

    @Test
    fun sectorConservaCamposPersistidosParaTodosLosEstados() {
        EstadoSector.entries.forEach { estado ->
            val original = Sector(
                id = 7,
                nombre = "Bancal norte",
                descripcion = "Sector de hortalizas",
                ubicacion = "Zona norte",
                estado = estado,
                responsable = "Docente de prueba"
            )

            val entidad = original.toEntity()

            assertEquals(estado.name, entidad.estado)
            assertEquals(original, entidad.toModelo())
        }
    }

    @Test
    fun cultivoConservaTodosLosCamposAlConvertirIdaYVuelta() {
        EstadoCultivo.entries.forEach { estado ->
            val original = Cultivo(
                id = 12,
                nombre = "Tomates",
                descripcion = "Cultivo de prueba",
                sectorId = 7,
                fechaSiembra = "17/11/2026",
                fechaCosechaEstimada = "17/02/2027",
                frecuenciaRiegoDias = 3,
                estado = estado,
                observaciones = "Observaciones de prueba",
                fotoUri = "content://foto"
            )

            val entidad = original.toEntity()

            assertEquals(estado.name, entidad.estado)
            assertEquals(original, entidad.toModelo())
        }
    }

    @Test
    fun cultivoSinFotoNiObservacionesConservaEsosValores() {
        val original = Cultivo(
            id = 1,
            nombre = "Lechugas",
            descripcion = "Cultivo de lechugas",
            sectorId = 1,
            fechaSiembra = "17/11/2026",
            fechaCosechaEstimada = "17/12/2026",
            frecuenciaRiegoDias = 2,
            observaciones = "",
            fotoUri = null
        )

        val resultado = original.toEntity().toModelo()

        assertEquals(original, resultado)
        assertNull(resultado.fotoUri)
        assertEquals("", resultado.observaciones)
    }
}