package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.ui.screens.sectores.SectorFormUiState
import cl.duoc.huertoescolar.ui.screens.sectores.validarDescripcionSector
import cl.duoc.huertoescolar.ui.screens.sectores.validarFormularioSector
import cl.duoc.huertoescolar.ui.screens.sectores.validarNombreSector
import cl.duoc.huertoescolar.ui.screens.sectores.validarUbicacionSector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesSectorTest {

    @Test
    fun nombreVacioOConEspaciosEsInvalido() {
        assertFalse(validarNombreSector(""))
        assertFalse(validarNombreSector("   "))
        assertFalse(validarNombreSector("\t\n"))
    }

    @Test
    fun nombreConTextoEsValido() {
        assertTrue(validarNombreSector("Bancal 1"))
        assertTrue(validarNombreSector(" Bancal 1 "))
    }

    @Test
    fun descripcionConMenosDeCincoCaracteresEsInvalida() {
        assertFalse(validarDescripcionSector(""))
        assertFalse(validarDescripcionSector("1234"))
    }

    @Test
    fun descripcionDesdeCincoCaracteresEsValida() {
        assertTrue(validarDescripcionSector("12345"))
        assertTrue(validarDescripcionSector("Sector de hortalizas"))
    }

    @Test
    fun ubicacionVaciaOConEspaciosEsInvalida() {
        assertFalse(validarUbicacionSector(""))
        assertFalse(validarUbicacionSector("   "))
        assertFalse(validarUbicacionSector("\t\n"))
    }

    @Test
    fun ubicacionConTextoEsValida() {
        assertTrue(validarUbicacionSector("Zona norte"))
    }

    @Test
    fun formularioVacioMarcaTodosLosErrores() {
        val resultado = validarFormularioSector(SectorFormUiState())

        assertTrue(resultado.errorNombre)
        assertTrue(resultado.errorDescripcion)
        assertTrue(resultado.errorUbicacion)
    }

    @Test
    fun formularioValidoLimpiaErroresYConservaLosDatos() {
        val entrada = SectorFormUiState(
            nombre = "Bancal 2",
            descripcion = "Sector de tomates",
            ubicacion = "Zona sur",
            estado = EstadoSector.EN_USO,
            errorNombre = true,
            errorDescripcion = true,
            errorUbicacion = true
        )

        val resultado = validarFormularioSector(entrada)

        assertEquals(
            entrada.copy(
                errorNombre = false,
                errorDescripcion = false,
                errorUbicacion = false
            ),
            resultado
        )

        // La validación no modifica el estado original.
        assertTrue(entrada.errorNombre)
        assertTrue(entrada.errorDescripcion)
        assertTrue(entrada.errorUbicacion)
    }

    @Test
    fun formularioMarcaSoloElCampoIncorrecto() {
        val valido = SectorFormUiState(
            nombre = "Bancal 1",
            descripcion = "Hortalizas",
            ubicacion = "Zona norte"
        )

        val sinNombre = validarFormularioSector(valido.copy(nombre = ""))
        assertTrue(sinNombre.errorNombre)
        assertFalse(sinNombre.errorDescripcion)
        assertFalse(sinNombre.errorUbicacion)

        val sinDescripcion = validarFormularioSector(
            valido.copy(descripcion = "1234")
        )
        assertFalse(sinDescripcion.errorNombre)
        assertTrue(sinDescripcion.errorDescripcion)
        assertFalse(sinDescripcion.errorUbicacion)

        val sinUbicacion = validarFormularioSector(
            valido.copy(ubicacion = "")
        )
        assertFalse(sinUbicacion.errorNombre)
        assertFalse(sinUbicacion.errorDescripcion)
        assertTrue(sinUbicacion.errorUbicacion)
    }
}