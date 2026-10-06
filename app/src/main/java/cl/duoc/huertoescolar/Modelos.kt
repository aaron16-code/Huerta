package cl.duoc.huertoescolar

enum class RolUsuario {
    ADMINISTRADOR,
    ESTUDIANTE
}

data class Usuario(
        val id: Int,
        val nombre: String,
        val rol: RolUsuario
        )

enum class EstadoSector {
    DISPONIBLE,
    EN_USO,
    EN_MANTENIMIENTO,
    FUERA_DE_USO
}

data class Sector(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: EstadoSector,
    val responsable: String
)