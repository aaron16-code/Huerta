package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.CultivoFormUiState
import cl.duoc.huertoescolar.ui.screens.cultivos.validarDescripcionCultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.validarFechaCultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.validarFormularioCultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.validarFrecuenciaRiego
import cl.duoc.huertoescolar.ui.screens.cultivos.validarNombreCultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.validarOrdenFechasCultivo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesCultivoTest {

    private fun formularioValido() = CultivoFormUiState(
        nombre = "Tomates",
        descripcion = "Cultivo de tomates",
        sectorId = 1,
        fechaSiembra = "17/11/2026",
        fechaCosecha = "17/02/2027",
        frecuenciaRiego = "2",
        observaciones = "Crecimiento normal",
        estado = EstadoCultivo.PLANIFICADO
    )

    @Test
    fun nombreRechazaVacioYEspacios() {
        assertFalse(validarNombreCultivo(""))
        assertFalse(validarNombreCultivo("   "))
        assertTrue(validarNombreCultivo("Tomates"))
    }

    @Test
    fun descripcionExigeAlMenosCincoCaracteres() {
        assertFalse(validarDescripcionCultivo(""))
        assertFalse(validarDescripcionCultivo("1234"))
        assertTrue(validarDescripcionCultivo("12345"))
        assertTrue(validarDescripcionCultivo("Cultivo de tomates"))
    }

    @Test
    fun aceptaFechasRealesYEspaciosExteriores() {
        assertTrue(validarFechaCultivo("18/10/2026"))
        assertTrue(validarFechaCultivo(" 18/10/2026 "))
        assertTrue(validarFechaCultivo("31/12/2026"))
    }

    @Test
    fun distingueAniosBisiestos() {
        assertTrue(validarFechaCultivo("29/02/2024"))
        assertFalse(validarFechaCultivo("29/02/2025"))
    }

    @Test
    fun rechazaFechasInexistentes() {
        listOf(
            "31/02/2026",
            "31/04/2026",
            "00/10/2026",
            "18/00/2026",
            "18/13/2026"
        ).forEach { fecha ->
            assertFalse(fecha, validarFechaCultivo(fecha))
        }
    }

    @Test
    fun rechazaFormatosIncorrectos() {
        listOf(
            "",
            "   ",
            "texto",
            "2026-10-18",
            "1/10/2026",
            "18/1/2026",
            "18/10/26",
            "18/10/2026extra"
        ).forEach { fecha ->
            assertFalse(fecha, validarFechaCultivo(fecha))
        }
    }

    @Test
    fun cosechaPuedeSerElMismoDiaOPosterior() {
        assertTrue(
            validarOrdenFechasCultivo("18/10/2026", "18/10/2026")
        )
        assertTrue(
            validarOrdenFechasCultivo("18/10/2026", "30/11/2026")
        )
        assertTrue(
            validarOrdenFechasCultivo("31/12/2026", "01/01/2027")
        )
    }

    @Test
    fun cosechaNoPuedeSerAnteriorALaSiembra() {
        assertFalse(
            validarOrdenFechasCultivo("18/10/2026", "17/10/2026")
        )
    }

    @Test
    fun ordenRechazaFechasInvalidasEnCualquieraDeLosCampos() {
        assertFalse(
            validarOrdenFechasCultivo("incorrecta", "18/10/2026")
        )
        assertFalse(
            validarOrdenFechasCultivo("18/10/2026", "incorrecta")
        )
        assertFalse(
            validarOrdenFechasCultivo("", "")
        )
    }

    @Test
    fun frecuenciaAceptaEnterosPositivos() {
        assertTrue(validarFrecuenciaRiego("1"))
        assertTrue(validarFrecuenciaRiego("2"))
        assertTrue(validarFrecuenciaRiego("30"))
    }

    @Test
    fun frecuenciaRechazaValoresInvalidos() {
        listOf(
            "",
            "   ",
            "abc",
            "0",
            "-1",
            "1.5",
            "2147483648"
        ).forEach { frecuencia ->
            assertFalse(frecuencia, validarFrecuenciaRiego(frecuencia))
        }
    }

    @Test
    fun formularioValidoNoTieneErrores() {
        val entrada = formularioValido()
        val resultado = validarFormularioCultivo(entrada)

        assertEquals(entrada, resultado)
    }

    @Test
    fun formularioVacioMarcaTodosLosErrores() {
        val resultado = validarFormularioCultivo(CultivoFormUiState())

        assertTrue(resultado.errorNombre)
        assertTrue(resultado.errorDescripcion)
        assertTrue(resultado.errorSector)
        assertTrue(resultado.errorFechaSiembra)
        assertTrue(resultado.errorFechaCosecha)
        assertTrue(resultado.errorFrecuencia)
    }

    @Test
    fun formularioMarcaCadaCampoInvalidoSinAlterarLosDemas() {
        val valido = formularioValido()

        val casos = listOf(
            valido.copy(nombre = "") to
                    valido.copy(nombre = "", errorNombre = true),

            valido.copy(descripcion = "1234") to
                    valido.copy(
                        descripcion = "1234",
                        errorDescripcion = true
                    ),

            valido.copy(sectorId = null) to
                    valido.copy(sectorId = null, errorSector = true),

            valido.copy(fechaCosecha = "incorrecta") to
                    valido.copy(
                        fechaCosecha = "incorrecta",
                        errorFechaCosecha = true
                    ),

            valido.copy(fechaCosecha = "16/11/2026") to
                    valido.copy(
                        fechaCosecha = "16/11/2026",
                        errorFechaCosecha = true
                    ),

            valido.copy(frecuenciaRiego = "0") to
                    valido.copy(
                        frecuenciaRiego = "0",
                        errorFrecuencia = true
                    )
        )

        casos.forEach { (entrada, esperado) ->
            assertEquals(esperado, validarFormularioCultivo(entrada))
        }
    }

    @Test
    fun siembraInvalidaTambienImpideValidarElOrdenDeCosecha() {
        val entrada = formularioValido().copy(
            fechaSiembra = "31/02/2026"
        )

        val resultado = validarFormularioCultivo(entrada)

        assertEquals(
            entrada.copy(
                errorFechaSiembra = true,
                errorFechaCosecha = true
            ),
            resultado
        )
    }

    @Test
    fun corregirFormularioLimpiaErroresYConservaDatos() {
        val valido = formularioValido()
        val entrada = valido.copy(
            errorNombre = true,
            errorDescripcion = true,
            errorSector = true,
            errorFechaSiembra = true,
            errorFechaCosecha = true,
            errorFrecuencia = true
        )

        val resultado = validarFormularioCultivo(entrada)

        assertEquals(valido, resultado)
        assertTrue(entrada.errorNombre)
        assertTrue(entrada.errorSector)
    }
}