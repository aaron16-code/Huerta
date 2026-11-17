package cl.duoc.huertoescolar.domain.repository

import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.TipoProblema
import kotlinx.coroutines.flow.StateFlow

interface ProblemasRepository {

    val problemas: StateFlow<List<Problema>>

    fun reportarProblema(
        sectorId: Int,
        cultivoId: Int?,
        tipo: TipoProblema,
        descripcion: String,
        reportanteId: Int,
        fotoUri: String?
    ): Result<Unit>

    fun actualizarProblema(
        id: Int,
        rol: RolUsuario,
        nuevoEstado: EstadoReporte,
        comentario: String
    ): Result<Unit>
}