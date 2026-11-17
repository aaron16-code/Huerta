package cl.duoc.huertoescolar.domain.repository

import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.Tarea
import kotlinx.coroutines.flow.StateFlow

interface TareasRepository {

    val tareas: StateFlow<List<Tarea>>

    val tareaSeleccionadaId: Int?

    fun seleccionarTarea(id: Int)

    fun obtenerTarea(id: Int): Tarea?

    fun crearTarea(
        rol: RolUsuario,
        nueva: Tarea
    ): Result<Unit>

    fun editarTarea(
        rol: RolUsuario,
        editada: Tarea
    ): Result<Unit>

    fun marcarRealizada(
        tareaId: Int,
        usuarioId: Int,
        grupo: String,
        comentario: String,
        fotoUri: String?
    ): Result<Unit>

    fun validarTarea(
        tareaId: Int,
        validadorId: Int,
        rol: RolUsuario,
        aprobada: Boolean,
        comentario: String
    ): Result<Unit>
}