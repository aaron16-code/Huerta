package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.ui.screens.tareas.DestinatarioTareaUi
import cl.duoc.huertoescolar.ui.screens.tareas.TareaFormUiState
import cl.duoc.huertoescolar.ui.screens.tareas.ValidadorFormularioTarea
import cl.duoc.huertoescolar.ui.screens.tareas.parsearFechaFinDelDia
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ValidadorFormularioTareaTest {

    private fun formularioValido() = TareaFormUiState(
        descripcion = "Regar tomates",
        instrucciones = "Evitar encharcar",
        sectorId = 1,
        fechaTexto = "17/11/2026",
        destinatario = DestinatarioTareaUi(
            etiqueta = "Sofía",
            usuarioId = 2,
            grupo = null
        )
    )

    @Test
    fun descripcionEInstruccionesExigenCincoCaracteresSinEspacios() {
        val invalido = formularioValido().copy(
            descripcion = " 1234 ",
            instrucciones = " 1234 "
        )

        val errores = ValidadorFormularioTarea.validar(
            invalido,
            exigirFechaActualOFutura = false
        )

        assertNotNull(errores.descripcion)
        assertNotNull(errores.instrucciones)
        assertFalse(errores.esValido)

        val valido = invalido.copy(
            descripcion = " 12345 ",
            instrucciones = " 12345 "
        )

        assertTrue(
            ValidadorFormularioTarea.validar(valido, false).esValido
        )
    }

    @Test
    fun faltaDeSectorODestinatarioImpideValidar() {
        val sinSector = ValidadorFormularioTarea.validar(
            formularioValido().copy(sectorId = null),
            false
        )

        assertEquals("Elige un sector", sinSector.sector)
        assertFalse(sinSector.esValido)

        val sinDestinatario = ValidadorFormularioTarea.validar(
            formularioValido().copy(destinatario = null),
            false
        )

        assertEquals(
            "Elige a quién se asigna la tarea",
            sinDestinatario.destinatario
        )
        assertFalse(sinDestinatario.esValido)
    }

    @Test
    fun distingueFechaVaciaDeFormatoInvalido() {
        val vacia = ValidadorFormularioTarea.validar(
            formularioValido().copy(fechaTexto = "   "),
            false
        )

        assertEquals("Indica la fecha programada", vacia.fecha)

        val incorrecta = ValidadorFormularioTarea.validar(
            formularioValido().copy(fechaTexto = "31/02/2026"),
            false
        )

        assertEquals(
            "Usa el formato dd/MM/aaaa (por ejemplo 25/10/2026)",
            incorrecta.fecha
        )
    }

    @Test
    fun fechaPasadaSePermiteAlEditarPeroNoAlCrear() {
        val entrada = formularioValido().copy(
            fechaTexto = "01/01/2000"
        )

        val creacion = ValidadorFormularioTarea.validar(
            entrada,
            exigirFechaActualOFutura = true
        )

        val edicion = ValidadorFormularioTarea.validar(
            entrada,
            exigirFechaActualOFutura = false
        )

        assertEquals(
            "La fecha no puede ser anterior a hoy",
            creacion.fecha
        )
        assertFalse(creacion.esValido)
        assertNull(edicion.fecha)
        assertTrue(edicion.esValido)
    }

    @Test
    fun fechaFuturaPermiteCrear() {
        val entrada = formularioValido().copy(
            fechaTexto = "01/01/2099"
        )

        assertTrue(
            ValidadorFormularioTarea.validar(
                entrada,
                exigirFechaActualOFutura = true
            ).esValido
        )
    }

    @Test
    fun fechaValidaTerminaAlFinalDelDia() {
        val fecha = parsearFechaFinDelDia(" 29/02/2024 ")
        assertNotNull(fecha)

        val calendario = Calendar.getInstance().apply {
            timeInMillis = fecha!!
        }

        assertEquals(2024, calendario.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, calendario.get(Calendar.MONTH))
        assertEquals(29, calendario.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, calendario.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendario.get(Calendar.MINUTE))
        assertEquals(59, calendario.get(Calendar.SECOND))
        assertEquals(0, calendario.get(Calendar.MILLISECOND))
    }

    @Test
    fun parserRechazaFormatosIncorrectosYFechasImposibles() {
        listOf(
            "",
            "texto",
            "hoy",
            "2026-11-17",
            "25-10-2026",
            "1/11/2026",
            "17/1/2026",
            "5/1/2026",
            "31/02/2026",
            "32/01/2026",
            "10/13/2026",
            "29/02/2025",
            "31/04/2026"
        ).forEach { entrada ->
            assertNull(
                "Debía rechazar: $entrada",
                parsearFechaFinDelDia(entrada)
            )
        }
    }
}