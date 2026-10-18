package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.EstadoLectura
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.data.calcularEstadoLectura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelosOperacionTest {

    private val ahora = 1_000_000_000_000L
    private val dia = 24L * 60 * 60 * 1000

    private fun tarea(estado: EstadoTarea, fechaProgramada: Long) = Tarea(
        id = 1,
        tipo = TipoTarea.RIEGO,
        descripcion = "Regar la lechuga",
        instrucciones = "Regar con regadera",
        sectorId = 1,
        cultivoId = 1,
        fechaCreacion = ahora - 5 * dia,
        fechaProgramada = fechaProgramada,
        responsableUsuarioId = 2,
        creadaPorId = 1,
        estado = estado
    )

    // ---------- Estado de las lecturas simuladas ----------

    @Test
    fun lectura_con_valores_normales_es_normal() {
        assertEquals(EstadoLectura.NORMAL, calcularEstadoLectura(55, 21.5, 60))
    }

    @Test
    fun humedad_de_suelo_baja_es_bajo() {
        assertEquals(EstadoLectura.BAJO, calcularEstadoLectura(22, 24.0, 55))
    }

    @Test
    fun temperatura_alta_es_alto() {
        assertEquals(EstadoLectura.ALTO, calcularEstadoLectura(60, 33.5, 70))
    }

    @Test
    fun temperatura_muy_baja_es_bajo() {
        assertEquals(EstadoLectura.BAJO, calcularEstadoLectura(50, 5.0, 60))
    }

    @Test
    fun humedad_de_suelo_alta_es_alto() {
        assertEquals(EstadoLectura.ALTO, calcularEstadoLectura(90, 20.0, 60))
    }

    @Test
    fun porcentaje_fuera_de_0_a_100_queda_pendiente_de_revision() {
        assertEquals(EstadoLectura.PENDIENTE_REVISION, calcularEstadoLectura(105, 22.0, 50))
        assertEquals(EstadoLectura.PENDIENTE_REVISION, calcularEstadoLectura(50, 22.0, -1))
    }

    @Test
    fun los_limites_exactos_siguen_siendo_normales() {
        assertEquals(EstadoLectura.NORMAL, calcularEstadoLectura(30, 8.0, 30))
        assertEquals(EstadoLectura.NORMAL, calcularEstadoLectura(80, 30.0, 85))
    }

    // ---------- Tareas atrasadas ----------

    @Test
    fun tarea_pendiente_con_fecha_pasada_esta_atrasada() {
        val t = tarea(EstadoTarea.PENDIENTE, ahora - 2 * dia)
        assertTrue(t.estaAtrasada(ahora))
        assertEquals(EstadoTarea.ATRASADA, t.estadoVisible(ahora))
    }

    @Test
    fun tarea_en_desarrollo_con_fecha_pasada_esta_atrasada() {
        assertTrue(tarea(EstadoTarea.EN_DESARROLLO, ahora - dia).estaAtrasada(ahora))
    }

    @Test
    fun tarea_pendiente_con_fecha_futura_no_esta_atrasada() {
        val t = tarea(EstadoTarea.PENDIENTE, ahora + 2 * dia)
        assertFalse(t.estaAtrasada(ahora))
        assertEquals(EstadoTarea.PENDIENTE, t.estadoVisible(ahora))
    }

    @Test
    fun tarea_ya_enviada_o_validada_no_se_marca_atrasada() {
        assertFalse(tarea(EstadoTarea.PENDIENTE_VALIDACION, ahora - dia).estaAtrasada(ahora))
        assertFalse(tarea(EstadoTarea.VALIDADA, ahora - dia).estaAtrasada(ahora))
    }

    @Test
    fun tarea_rechazada_se_sigue_mostrando_como_rechazada() {
        val t = tarea(EstadoTarea.RECHAZADA, ahora - dia)
        assertFalse(t.estaAtrasada(ahora))
        assertEquals(EstadoTarea.RECHAZADA, t.estadoVisible(ahora))
    }

    // ---------- Usuarios de prueba ----------

    @Test
    fun cada_rol_se_traduce_a_su_usuario_ficticio() {
        assertEquals(RolUsuario.ADMINISTRADOR, UsuariosPrueba.porRol(RolUsuario.ADMINISTRADOR).rol)
        assertEquals(1, UsuariosPrueba.porRol(RolUsuario.ADMINISTRADOR).id)
        assertEquals(RolUsuario.ESTUDIANTE, UsuariosPrueba.porRol(RolUsuario.ESTUDIANTE).rol)
        assertEquals(2, UsuariosPrueba.porRol(RolUsuario.ESTUDIANTE).id)
    }
}