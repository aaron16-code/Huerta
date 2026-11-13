package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.EstadoReporte
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.TipoProblema
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.UsuariosPrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reglas del caso sobre el repositorio en memoria.
 * El repositorio es único (object) y conserva su estado entre pruebas, por eso cada prueba
 * que modifica datos usa una tarea distinta (1, 2 y 5) y las demás solo comprueban rechazos.
 */
class ReglasOperacionTest {

    private val repo = RepositorioOperacion
    private val docente = UsuariosPrueba.docente
    private val estudiante = UsuariosPrueba.estudiante
    private val dia = 24L * 60 * 60 * 1000

    private fun tarea(id: Int): Tarea = repo.obtenerTarea(id)!!

    private fun mensaje(r: Result<Unit>): String = r.exceptionOrNull()?.message ?: ""

    private fun tareaNueva(
        descripcion: String = "Regar los tomates",
        responsable: Int? = 2,
        grupo: String? = null
    ) = Tarea(
        id = 0,
        tipo = TipoTarea.RIEGO,
        descripcion = descripcion,
        instrucciones = "Regar con regadera",
        sectorId = 1,
        cultivoId = 1,
        fechaCreacion = 0,
        fechaProgramada = System.currentTimeMillis() + dia,
        responsableUsuarioId = responsable,
        grupoAsignado = grupo,
        creadaPorId = docente.id
    )

    // ---------- El estudiante envía su tarea ----------

    @Test
    fun tarea_enviada_por_el_estudiante_queda_pendiente_de_validacion() {
        val r = repo.marcarRealizada(2, estudiante.id, estudiante.grupo, "Listo", null)
        assertTrue(r.isSuccess)
        assertEquals(EstadoTarea.PENDIENTE_VALIDACION, tarea(2).estado)
        assertEquals(estudiante.id, tarea(2).realizadaPorId)
    }

    @Test
    fun estudiante_no_puede_enviar_una_tarea_asignada_a_otro() {
        val r = repo.marcarRealizada(8, estudiante.id, estudiante.grupo, "", null)
        assertTrue(r.isFailure)
        assertEquals(EstadoTarea.PENDIENTE, tarea(8).estado)
    }

    @Test
    fun estudiante_solo_ve_sus_tareas_o_las_de_su_grupo() {
        val mias = repo.tareasDe(estudiante.id, estudiante.grupo)
        assertTrue(mias.isNotEmpty())
        assertTrue(mias.all {
            it.responsableUsuarioId == estudiante.id ||
                    (it.responsableUsuarioId == null && it.grupoAsignado == estudiante.grupo)
        })
        assertTrue(mias.none { it.id == 3 }) // la tarea 3 es de otro estudiante
    }

    // ---------- Validación ----------

    @Test
    fun el_estudiante_no_puede_validar() {
        val r = repo.validarTarea(3, estudiante.id, RolUsuario.ESTUDIANTE, true, "ok")
        assertTrue(r.isFailure)
        assertEquals(EstadoTarea.PENDIENTE_VALIDACION, tarea(3).estado)
    }

    @Test
    fun nadie_valida_su_propia_actividad() {
        // La tarea 3 fue realizada por el usuario 3: aunque sea administrador, no puede validarla.
        val r = repo.validarTarea(3, 3, RolUsuario.ADMINISTRADOR, true, "ok")
        assertTrue(r.isFailure)
        assertTrue(mensaje(r).contains("propia"))
        assertEquals(EstadoTarea.PENDIENTE_VALIDACION, tarea(3).estado)
    }

    @Test
    fun rechazar_exige_un_comentario() {
        val r = repo.validarTarea(3, docente.id, RolUsuario.ADMINISTRADOR, false, "   ")
        assertTrue(r.isFailure)
        assertTrue(mensaje(r).contains("comentario"))
        assertEquals(EstadoTarea.PENDIENTE_VALIDACION, tarea(3).estado)
    }

    @Test
    fun solo_se_validan_tareas_pendientes_de_validacion() {
        // La tarea 4 ya estaba validada
        val r = repo.validarTarea(4, docente.id, RolUsuario.ADMINISTRADOR, true, "ok")
        assertTrue(r.isFailure)
    }

    @Test
    fun el_docente_valida_una_tarea_enviada() {
        assertTrue(repo.marcarRealizada(1, estudiante.id, estudiante.grupo, "Regué la lechuga", null).isSuccess)

        val r = repo.validarTarea(1, docente.id, RolUsuario.ADMINISTRADOR, true, "Bien")

        assertTrue(r.isSuccess)
        assertEquals(EstadoTarea.VALIDADA, tarea(1).estado)
        assertEquals(docente.id, tarea(1).validadorId)
        assertEquals("Bien", tarea(1).comentarioDocente)
        assertNotNull(tarea(1).fechaValidacion)
    }

    @Test
    fun tarea_de_grupo_rechazada_se_puede_repetir() {
        // La tarea 5 está asignada al grupo "Taller Huerto A"
        assertTrue(repo.marcarRealizada(5, estudiante.id, estudiante.grupo, "Hecho", null).isSuccess)

        val rechazo = repo.validarTarea(5, docente.id, RolUsuario.ADMINISTRADOR, false, "Falta regar el borde")
        assertTrue(rechazo.isSuccess)
        assertEquals(EstadoTarea.RECHAZADA, tarea(5).estado)

        val reenvio = repo.marcarRealizada(5, estudiante.id, estudiante.grupo, "Ya lo regué", null)
        assertTrue(reenvio.isSuccess)
        assertEquals(EstadoTarea.PENDIENTE_VALIDACION, tarea(5).estado)
        assertNull(tarea(5).fechaValidacion)
    }

    // ---------- Crear tareas ----------

    @Test
    fun solo_el_docente_puede_crear_tareas() {
        val antes = repo.tareas.value.size

        val rechazada = repo.crearTarea(RolUsuario.ESTUDIANTE, tareaNueva())
        assertTrue(rechazada.isFailure)
        assertEquals(antes, repo.tareas.value.size)

        val aceptada = repo.crearTarea(RolUsuario.ADMINISTRADOR, tareaNueva())
        assertTrue(aceptada.isSuccess)
        assertEquals(antes + 1, repo.tareas.value.size)
    }

    @Test
    fun una_tarea_necesita_descripcion_y_responsable_o_grupo() {
        assertTrue(repo.crearTarea(RolUsuario.ADMINISTRADOR, tareaNueva(descripcion = "  ")).isFailure)
        assertTrue(repo.crearTarea(RolUsuario.ADMINISTRADOR, tareaNueva(responsable = null, grupo = null)).isFailure)
    }

    // ---------- Problemas ----------

    @Test
    fun reportar_un_problema_exige_descripcion() {
        val r = repo.reportarProblema(1, 1, TipoProblema.FALTA_AGUA, "   ", estudiante.id, null)
        assertTrue(r.isFailure)
    }

    @Test
    fun problema_reportado_queda_informado() {
        val r = repo.reportarProblema(1, 1, TipoProblema.DANO_PLANTA, "Hojas amarillas", estudiante.id, null)
        assertTrue(r.isSuccess)

        val ultimo = repo.problemas.value.maxByOrNull { it.id }!!
        assertEquals(EstadoReporte.INFORMADO, ultimo.estado)
        assertEquals(estudiante.id, ultimo.reportadoPorId)
    }

    @Test
    fun solo_el_docente_da_seguimiento_a_los_problemas() {
        repo.reportarProblema(1, null, TipoProblema.OTRO, "Se rompió una manguera", estudiante.id, null)
        val id = repo.problemas.value.maxOf { it.id }

        assertTrue(repo.actualizarProblema(id, RolUsuario.ESTUDIANTE, EstadoReporte.CERRADO, "x").isFailure)

        val r = repo.actualizarProblema(id, RolUsuario.ADMINISTRADOR, EstadoReporte.CERRADO, "Resuelto")
        assertTrue(r.isSuccess)

        val problema = repo.problemas.value.first { it.id == id }
        assertEquals(EstadoReporte.CERRADO, problema.estado)
        assertEquals("Resuelto", problema.comentarioDocente)
        assertNotNull(problema.fechaCierre)
    }

    // ---------- Historial ----------

    @Test
    fun el_historial_esta_en_orden_cronologico_inverso() {
        val fechas = repo.historial().map { it.fecha }
        assertFalse(fechas.isEmpty())
        assertEquals(fechas.sortedDescending(), fechas)
    }
}