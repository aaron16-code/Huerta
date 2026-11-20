package cl.duoc.huertoescolar.data.remote
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.domain.model.RolUsuario

data class UsuarioRemoto(val id: Int, val nombre: String, val rol: RolUsuario, val grupo: String?) {
    fun aModelo() = UsuarioPrueba(id, nombre, rol, grupo.orEmpty())
}
