package cl.duoc.huertoescolar

enum class EstadoTarea {
    PENDIENTE,
    PENDIENTE_VALIDACION,
    VALIDADA,
    RECHAZADA
}

data class Tarea(
    val id: Int,
    val titulo: String,
    val sector: String,
    val responsable: String,
    val instrucciones: String,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE
)