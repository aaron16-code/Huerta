package cl.duoc.huertoescolar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import cl.duoc.huertoescolar.domain.model.RolUsuario

data class UsuarioPrueba(
    val id: Int,
    val nombre: String,
    val rol: RolUsuario,
    val grupo: String
)

object UsuariosPrueba {
    val docente = UsuarioPrueba(1, "Profesora Laura", RolUsuario.ADMINISTRADOR, "")
    val estudiante1 = UsuarioPrueba(2, "Sofía Martínez", RolUsuario.ESTUDIANTE, "Taller Huerto A")
    val estudiante2 = UsuarioPrueba(3, "Tomás González", RolUsuario.ESTUDIANTE, "Taller Huerto A")
    val estudiante3 = UsuarioPrueba(4, "Valentina Rojas", RolUsuario.ESTUDIANTE, "Taller Huerto B")

    private val cuentasPrueba = listOf(docente, estudiante1, estudiante2, estudiante3)
    var cuentasRemotas: List<UsuarioPrueba>? by androidx.compose.runtime.mutableStateOf(null)
        private set
    val todos get() = cuentasRemotas ?: cuentasPrueba
    val estudiantes get() = todos.filter { it.rol == RolUsuario.ESTUDIANTE }
    fun actualizarCuentas(cuentas: List<UsuarioPrueba>) { cuentasRemotas = cuentas }


    // Se conserva este nombre para las pruebas y datos creados anteriormente.
    val estudiante = estudiante1

    fun porRol(rol: RolUsuario): UsuarioPrueba =
        if (rol == RolUsuario.ADMINISTRADOR) docente else estudiante
}
