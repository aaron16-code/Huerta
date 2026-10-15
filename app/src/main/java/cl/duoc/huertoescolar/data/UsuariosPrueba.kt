package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.RolUsuario

/**
 * El login del proyecto solo entrega un rol (ADMINISTRADOR o ESTUDIANTE).
 * Para el MVP, cada rol equivale a un usuario ficticio de los datos de prueba.
 */
data class UsuarioPrueba(
    val id: Int,
    val nombre: String,
    val rol: RolUsuario,
    val grupo: String
)

object UsuariosPrueba {
    val docente = UsuarioPrueba(1, "Docente de prueba", RolUsuario.ADMINISTRADOR, "")
    val estudiante = UsuarioPrueba(2, "Estudiante de prueba", RolUsuario.ESTUDIANTE, "Taller Huerto A")

    fun porRol(rol: RolUsuario): UsuarioPrueba =
        if (rol == RolUsuario.ADMINISTRADOR) docente else estudiante
}