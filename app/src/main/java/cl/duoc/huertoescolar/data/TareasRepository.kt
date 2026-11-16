package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.RolUsuario
import kotlinx.coroutines.flow.StateFlow

/** Contrato usado por la presentación para trabajar con tareas. */
interface TareasRepository {
    val tareas: StateFlow<List<Tarea>>
    val tareaSeleccionadaId: Int?

    fun seleccionarTarea(id: Int)
    fun obtenerTarea(id: Int): Tarea?
    fun crearTarea(rol: RolUsuario, nueva: Tarea): Result<Unit>
    fun editarTarea(rol: RolUsuario, editada: Tarea): Result<Unit>
}
