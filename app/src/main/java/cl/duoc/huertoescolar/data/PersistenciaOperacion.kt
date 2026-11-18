package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.domain.model.LecturaSensor
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.Tarea

/** Todo lo que se guarda del módulo de operación. */
data class DatosOperacion(
    val tareas: List<Tarea>,
    val problemas: List<Problema>,
    val lecturas: List<LecturaSensor>
)

/**
 * Lugar donde se guardan los datos del módulo de operación (Room o el microservicio por Retrofit).
 * El repositorio solo conoce esta interfaz. En las pruebas unitarias no hay persistencia
 * y todo sigue funcionando en memoria.
 */
interface PersistenciaOperacion {
    /** Devuelve lo guardado, o null si todavía no hay nada guardado. */
    suspend fun cargar(): DatosOperacion?

    /** Guarda los datos de ejemplo la primera vez que se abre la app. */
    suspend fun guardarSemilla(datos: DatosOperacion)

    /** Devuelve la tarea tal como quedó guardada (el servidor puede asignarle otro id). */
    suspend fun insertarTarea(tarea: Tarea): Tarea
    suspend fun actualizarTarea(tarea: Tarea)
    suspend fun eliminarTarea(tareaId: Int)
    /** Devuelve el problema tal como quedó guardado (el servidor puede asignarle otro id). */
    suspend fun insertarProblema(problema: Problema): Problema
    suspend fun actualizarProblema(problema: Problema)
}
