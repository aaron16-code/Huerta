package cl.duoc.huertoescolar.data.remoto

import cl.duoc.huertoescolar.data.DatosOperacion
import cl.duoc.huertoescolar.data.PersistenciaOperacion
import cl.duoc.huertoescolar.data.Problema
import cl.duoc.huertoescolar.data.Tarea

/**
 * Guarda y carga tareas y problemas en el microservicio (Spring Boot + MySQL) usando Retrofit.
 * Las lecturas de sensores son simuladas y no se guardan en el servidor.
 */
class PersistenciaRetrofit(
    private val api: ApiOperacion = ClienteApi.operacion
) : PersistenciaOperacion {

    override suspend fun cargar(): DatosOperacion {
        val tareas = api.listarTareas().map { it.aModelo() }
        val problemas = api.listarProblemas().map { it.aModelo() }
        return DatosOperacion(tareas, problemas, emptyList())
    }

    /** El servidor ya trae sus datos de ejemplo, así que no hay nada que sembrar desde la app. */
    override suspend fun guardarSemilla(datos: DatosOperacion) = Unit

    override suspend fun insertarTarea(tarea: Tarea): Tarea =
        api.crearTarea(tarea.aRemota()).aModelo()

    override suspend fun actualizarTarea(tarea: Tarea) {
        api.actualizarTarea(tarea.id, tarea.aRemota())
    }

    override suspend fun eliminarTarea(tareaId: Int) {
        api.eliminarTarea(tareaId)
    }

    override suspend fun insertarProblema(problema: Problema): Problema =
        api.crearProblema(problema.aRemoto()).aModelo()

    override suspend fun actualizarProblema(problema: Problema) {
        api.actualizarProblema(problema.id, problema.aRemoto())
    }
}
