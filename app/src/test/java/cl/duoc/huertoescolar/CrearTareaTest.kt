package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.ui.screens.tareas.CrearTareaViewModel
import cl.duoc.huertoescolar.ui.screens.tareas.parsearFechaFinDelDia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class CrearTareaTest {

    private val docente = UsuariosPrueba.docente

    /** Deja el formulario completo y válido, salvo lo que cada prueba quiera cambiar. */
    private fun formularioCompleto(descripcion: String = "Regar los tomates"): CrearTareaViewModel {
        val vm = CrearTareaViewModel()
        vm.cambiarDescripcion(descripcion)
        vm.cambiarInstrucciones("Regar con regadera la base de cada planta")
        vm.elegirSector(1)
        vm.elegirFechaRapida(1)
        vm.elegirDestinatario(vm.destinatarios[0])
        return vm
    }

    // ---------- Fecha del formulario ----------

    @Test
    fun una_fecha_valida_se_convierte() {
        assertNotNull(parsearFechaFinDelDia("25/10/2026"))
    }

    @Test
    fun una_fecha_que_no_existe_se_rechaza() {
        assertNull(parsearFechaFinDelDia("31/02/2026"))
        assertNull(parsearFechaFinDelDia("32/01/2026"))
        assertNull(parsearFechaFinDelDia("10/13/2026"))
    }

    @Test
    fun un_formato_incorrecto_se_rechaza() {
        assertNull(parsearFechaFinDelDia(""))
        assertNull(parsearFechaFinDelDia("25-10-2026"))
        assertNull(parsearFechaFinDelDia("5/1/2026"))
        assertNull(parsearFechaFinDelDia("hoy"))
    }

    @Test
    fun la_fecha_queda_al_final_del_dia() {
        val millis = parsearFechaFinDelDia("25/10/2026")!!
        val calendario = Calendar.getInstance().apply { time = Date(millis) }
        assertEquals(23, calendario.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendario.get(Calendar.MINUTE))
    }

    // ---------- Validaciones del formulario ----------

    @Test
    fun un_formulario_vacio_no_pide_confirmacion_y_marca_los_errores() {
        val vm = CrearTareaViewModel()
        vm.pedirConfirmacion()

        assertFalse(vm.mostrarConfirmacion)
        assertNotNull(vm.errorDescripcion)
        assertNotNull(vm.errorInstrucciones)
        assertNotNull(vm.errorSector)
        assertNotNull(vm.errorFecha)
        assertNotNull(vm.errorDestinatario)
    }

    @Test
    fun una_fecha_pasada_se_rechaza() {
        val vm = formularioCompleto()
        vm.cambiarFecha("01/01/2020")
        vm.pedirConfirmacion()

        assertFalse(vm.mostrarConfirmacion)
        assertTrue(vm.errorFecha!!.contains("anterior"))
    }

    @Test
    fun una_descripcion_muy_corta_se_rechaza() {
        val vm = formularioCompleto(descripcion = "ab")
        vm.pedirConfirmacion()

        assertFalse(vm.mostrarConfirmacion)
        assertNotNull(vm.errorDescripcion)
    }

    @Test
    fun un_formulario_completo_pide_confirmacion_sin_errores() {
        val vm = formularioCompleto()
        vm.pedirConfirmacion()

        assertTrue(vm.mostrarConfirmacion)
        assertNull(vm.errorDescripcion)
        assertNull(vm.errorInstrucciones)
        assertNull(vm.errorSector)
        assertNull(vm.errorFecha)
        assertNull(vm.errorDestinatario)
    }

    @Test
    fun cambiar_de_sector_borra_el_cultivo_elegido() {
        val vm = CrearTareaViewModel()
        vm.elegirSector(1)
        vm.elegirCultivo(1)
        vm.elegirSector(2)

        assertNull(vm.cultivoId)
    }

    // ---------- Crear la tarea ----------

    @Test
    fun la_tarea_creada_queda_asignada_al_estudiante_elegido() {
        val vm = formularioCompleto(descripcion = "Tarea unitaria para el estudiante")
        vm.pedirConfirmacion()
        vm.crear(docente)

        assertTrue(vm.creada)
        val tarea = RepositorioOperacion.tareas.value.first { it.descripcion == "Tarea unitaria para el estudiante" }
        assertEquals(2, tarea.responsableUsuarioId)
        assertNull(tarea.grupoAsignado)
        assertEquals(docente.id, tarea.creadaPorId)
    }

    @Test
    fun la_tarea_creada_puede_asignarse_a_un_grupo() {
        val vm = formularioCompleto(descripcion = "Tarea unitaria para el grupo")
        vm.elegirDestinatario(vm.destinatarios.first { it.grupo != null })
        vm.pedirConfirmacion()
        vm.crear(docente)

        assertTrue(vm.creada)
        val tarea = RepositorioOperacion.tareas.value.first { it.descripcion == "Tarea unitaria para el grupo" }
        assertNull(tarea.responsableUsuarioId)
        assertEquals("Taller Huerto A", tarea.grupoAsignado)
    }
}