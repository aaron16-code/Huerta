package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.RolUsuario

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

    val estudiantes = listOf(estudiante1, estudiante2, estudiante3)
    val todos = listOf(docente) + estudiantes

    // Se conserva este nombre para las pruebas y datos creados anteriormente.
    val estudiante = estudiante1

    fun porRol(rol: RolUsuario): UsuarioPrueba =
        if (rol == RolUsuario.ADMINISTRADOR) docente else estudiante
}
