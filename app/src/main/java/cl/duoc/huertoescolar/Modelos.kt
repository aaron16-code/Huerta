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

enum class EstadoCultivo {
    PLANIFICADO,
    SEMBRADO,
    EN_CRECIMIENTO,
    LISTO_PARA_COSECHAR,
    COSECHADO,
    CON_PROBLEMAS,
    FINALIZADO
}

data class Cultivo(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val sectorId: Int,
    val fechaSiembra: String,
    val fechaCosechaEstimada: String,
    val frecuenciaRiegoDias: Int,
    val estado: EstadoCultivo = EstadoCultivo.PLANIFICADO,
    val observaciones: String = "",
    val fotoUri: String? = null
)
