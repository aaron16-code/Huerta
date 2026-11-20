package cl.duoc.huertoescolar
import cl.duoc.huertoescolar.data.remote.UsuarioRemoto
import cl.duoc.huertoescolar.domain.model.RolUsuario
import org.junit.Assert.assertEquals
import org.junit.Test
class UsuarioRemotoTest {
    @Test fun conservaIdYGrupoDelServidor() {
        val cuenta = UsuarioRemoto(42, "Estudiante ficticio", RolUsuario.ESTUDIANTE, "Grupo A").aModelo()
        assertEquals(42, cuenta.id)
        assertEquals("Grupo A", cuenta.grupo)
        assertEquals(RolUsuario.ESTUDIANTE, cuenta.rol)
    }
    @Test fun administradorSinGrupo() {
        val cuenta = UsuarioRemoto(1, "Docente ficticio", RolUsuario.ADMINISTRADOR, null).aModelo()
        assertEquals("", cuenta.grupo)
    }
}
