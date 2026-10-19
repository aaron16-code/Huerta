package cl.duoc.huertoescolar.data

/** Todo lo que se guarda del módulo de operación. */
data class DatosOperacion(
    val tareas: List<Tarea>,
    val problemas: List<Problema>,
    val lecturas: List<LecturaSensor>
)

/**
 * Lugar donde se guardan los datos del módulo de operación (hoy: Room).
 * El repositorio solo conoce esta interfaz. En las pruebas unitarias no hay persistencia
 * y todo sigue funcionando en memoria.
 */
interface PersistenciaOperacion {
    /** Devuelve lo guardado, o null si todavía no hay nada guardado. */
    suspend fun cargar(): DatosOperacion?

    /** Guarda los datos de ejemplo la primera vez que se abre la app. */
    suspend fun guardarSemilla(datos: DatosOperacion)

    suspend fun insertarTarea(tarea: Tarea)
    suspend fun actualizarTarea(tarea: Tarea)
    suspend fun eliminarTarea(tareaId: Int)
    suspend fun insertarProblema(problema: Problema)
    suspend fun actualizarProblema(problema: Problema)
}
