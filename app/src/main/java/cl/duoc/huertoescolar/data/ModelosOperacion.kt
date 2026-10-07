package cl.duoc.huertoescolar.data

// Modelos del módulo de operación (Persona B).
// Las fechas son epoch millis (Long): funcionan con minSdk 24 sin librerías extra.

enum class TipoTarea(val etiqueta: String) {
    PREPARACION_SECTOR("Preparación del sector"),
    SIEMBRA("Siembra"),
    TRASPLANTE("Trasplante"),
    RIEGO("Riego"),
    LIMPIEZA("Limpieza"),
    RETIRO_MALEZAS("Retiro de malezas"),
    OBSERVACION("Observación del cultivo"),
    MANTENIMIENTO("Mantenimiento"),
    REVISION_SUELO("Revisión del suelo"),
    REGISTRO_CRECIMIENTO("Registro del crecimiento"),
    COSECHA("Cosecha"),
    OTRA("Otra actividad autorizada")
}

enum class EstadoTarea(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_DESARROLLO("En desarrollo"),
    REALIZADA("Realizada"),
    PENDIENTE_VALIDACION("Pendiente de validación"),
    VALIDADA("Validada"),
    RECHAZADA("Rechazada"),
    ATRASADA("Atrasada")
}

enum class TipoProblema(val etiqueta: String) {
    FALTA_AGUA("Falta de agua"),
    DANO_PLANTA("Daño en la planta"),
    PLAGA_SIMULADA("Plaga simulada"),
    DETERIORO_SECTOR("Deterioro del sector"),
    PROBLEMA_MANTENCION("Problema de mantenimiento"),
    OTRO("Otro problema observado")
}

enum class EstadoReporte(val etiqueta: String) {
    INFORMADO("Informado"),
    EN_REVISION("En revisión"),
    ATENDIDO("Atendido"),
    CERRADO("Cerrado")
}

enum class EstadoLectura(val etiqueta: String) {
    NORMAL("Normal"),
    BAJO("Bajo"),
    ALTO("Alto"),
    PENDIENTE_REVISION("Pendiente de revisión")
}

enum class TipoEvento { TAREA, VALIDACION, PROBLEMA }

data class Tarea(
    val id: Int,
    val tipo: TipoTarea,
    val descripcion: String,
    val instrucciones: String,
    val sectorId: Int,
    val cultivoId: Int?,
    val fechaCreacion: Long,
    val fechaProgramada: Long,
    val responsableUsuarioId: Int? = null,   // estudiante individual...
    val grupoAsignado: String? = null,       // ...o grupo (si no hay individual)
    val creadaPorId: Int,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val fechaRealizacion: Long? = null,
    val realizadaPorId: Int? = null,
    val fotoUri: String? = null,
    val comentarioEstudiante: String = "",
    val comentarioDocente: String = "",
    val validadorId: Int? = null,
    val fechaValidacion: Long? = null
) {
    /** Atrasada = pasó la fecha programada y nadie la ha enviado a validar. */
    fun estaAtrasada(ahora: Long = System.currentTimeMillis()): Boolean =
        fechaProgramada < ahora &&
                (estado == EstadoTarea.PENDIENTE || estado == EstadoTarea.EN_DESARROLLO)

    /** Estado que se muestra en pantalla (convierte en ATRASADA cuando corresponde). */
    fun estadoVisible(ahora: Long = System.currentTimeMillis()): EstadoTarea =
        if (estaAtrasada(ahora)) EstadoTarea.ATRASADA else estado
}

data class Problema(
    val id: Int,
    val sectorId: Int,
    val cultivoId: Int?,
    val tipo: TipoProblema,
    val descripcion: String,
    val fechaReporte: Long,
    val reportadoPorId: Int,
    val fotoUri: String? = null,
    val estado: EstadoReporte = EstadoReporte.INFORMADO,
    val comentarioDocente: String = "",
    val fechaCierre: Long? = null
)

data class LecturaSensor(
    val id: Int,
    val sectorId: Int,
    val fecha: Long,
    val humedadSuelo: Int,          // %
    val temperaturaAmbiente: Double, // °C
    val humedadAmbiente: Int,       // %
    val estado: EstadoLectura,
    val origen: String = "Simulación académica" // siempre visible en pantalla
)

data class EventoHistorial(
    val fecha: Long,
    val tipo: TipoEvento,
    val titulo: String,
    val detalle: String,
    val fotoUri: String? = null
)

/** Rangos de prueba (ajustables). Función pura: fácil de probar con test unitario. */
fun calcularEstadoLectura(humedadSuelo: Int, temperatura: Double, humedadAmbiente: Int): EstadoLectura = when {
    humedadSuelo !in 0..100 || humedadAmbiente !in 0..100 -> EstadoLectura.PENDIENTE_REVISION
    humedadSuelo < 30 || temperatura < 8.0 || humedadAmbiente < 30 -> EstadoLectura.BAJO
    humedadSuelo > 80 || temperatura > 30.0 || humedadAmbiente > 85 -> EstadoLectura.ALTO
    else -> EstadoLectura.NORMAL
}