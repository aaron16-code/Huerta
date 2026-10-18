package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.ui.screens.cultivos.validarFechaCultivo
import cl.duoc.huertoescolar.ui.screens.cultivos.validarOrdenFechasCultivo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesCultivoTest {
    @Test
    fun acepta_una_fecha_real_con_formato_correcto() {
        assertTrue(validarFechaCultivo("18/10/2026"))
    }

    @Test
    fun rechaza_una_fecha_inexistente() {
        assertFalse(validarFechaCultivo("31/02/2026"))
    }

    @Test
    fun la_cosecha_no_puede_ser_anterior_a_la_siembra() {
        assertFalse(validarOrdenFechasCultivo("18/10/2026", "17/10/2026"))
        assertTrue(validarOrdenFechasCultivo("18/10/2026", "30/11/2026"))
    }
}
