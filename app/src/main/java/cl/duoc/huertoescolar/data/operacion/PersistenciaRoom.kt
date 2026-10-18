package cl.duoc.huertoescolar.data.operacion

import android.content.Context
import cl.duoc.huertoescolar.data.DatosOperacion
import cl.duoc.huertoescolar.data.Problema
import cl.duoc.huertoescolar.data.PersistenciaOperacion
import cl.duoc.huertoescolar.data.Tarea

/** Guarda y carga los datos del módulo de operación usando Room. */
class PersistenciaRoom(contexto: Context) : PersistenciaOperacion {

    private val dao = OperacionDatabase.getInstance(contexto).operacionDao()

    override suspend fun cargar(): DatosOperacion? {
        val vacia = dao.contarTareas() == 0 && dao.contarProblemas() == 0 && dao.contarLecturas() == 0
        if (vacia) return null
        return DatosOperacion(
            tareas = dao.leerTareas().map { it.aModelo() },
            problemas = dao.leerProblemas().map { it.aModelo() },
            lecturas = dao.leerLecturas().map { it.aModelo() }
        )
    }

    override suspend fun guardarSemilla(datos: DatosOperacion) {
        datos.tareas.forEach { dao.insertarTarea(it.aEntidad()) }
        datos.problemas.forEach { dao.insertarProblema(it.aEntidad()) }
        datos.lecturas.forEach { dao.insertarLectura(it.aEntidad()) }
    }

    override suspend fun insertarTarea(tarea: Tarea) {
        dao.insertarTarea(tarea.aEntidad())
    }

    override suspend fun actualizarTarea(tarea: Tarea) {
        dao.actualizarTarea(tarea.aEntidad())
    }

    override suspend fun insertarProblema(problema: Problema) {
        dao.insertarProblema(problema.aEntidad())
    }

    override suspend fun actualizarProblema(problema: Problema) {
        dao.actualizarProblema(problema.aEntidad())
    }
}