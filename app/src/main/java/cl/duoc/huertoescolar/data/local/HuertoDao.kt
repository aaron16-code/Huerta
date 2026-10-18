package cl.duoc.huertoescolar.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HuertoDao {
    @Insert
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun observarUsuarios(): Flow<List<UsuarioEntity>>

    @Insert
    suspend fun insertarSector(sector: SectorEntity): Long

    @Update
    suspend fun actualizarSector(sector: SectorEntity)

    @Delete
    suspend fun eliminarSector(sector: SectorEntity)

    @Query("SELECT * FROM sectores ORDER BY nombre")
    fun observarSectores(): Flow<List<SectorEntity>>

    @Query("SELECT COUNT(*) FROM sectores")
    suspend fun contarSectores(): Int

    @Insert
    suspend fun insertarCultivo(cultivo: CultivoEntity): Long

    @Update
    suspend fun actualizarCultivo(cultivo: CultivoEntity)

    @Delete
    suspend fun eliminarCultivo(cultivo: CultivoEntity)

    @Query("SELECT * FROM cultivos ORDER BY nombre")
    fun observarCultivos(): Flow<List<CultivoEntity>>

    @Query("SELECT COUNT(*) FROM cultivos")
    suspend fun contarCultivos(): Int

    @Query("SELECT * FROM cultivos WHERE sectorId = :sectorId ORDER BY nombre")
    fun observarCultivosDelSector(sectorId: Int): Flow<List<CultivoEntity>>

    @Insert
    suspend fun insertarTarea(tarea: TareaEntity): Long

    @Update
    suspend fun actualizarTarea(tarea: TareaEntity)

    @Query("SELECT * FROM tareas ORDER BY fechaProgramada")
    fun observarTareas(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE responsableId = :usuarioId ORDER BY fechaProgramada")
    fun observarTareasDelUsuario(usuarioId: Int): Flow<List<TareaEntity>>

    @Insert
    suspend fun insertarEvidencia(evidencia: EvidenciaEntity): Long

    @Query("SELECT * FROM evidencias WHERE tareaId = :tareaId ORDER BY id")
    fun observarEvidencias(tareaId: Int): Flow<List<EvidenciaEntity>>

    @Insert
    suspend fun insertarValidacion(validacion: ValidacionEntity): Long

    @Query("SELECT * FROM validaciones WHERE tareaId = :tareaId ORDER BY id")
    fun observarValidaciones(tareaId: Int): Flow<List<ValidacionEntity>>

    @Insert
    suspend fun insertarReporte(reporte: ReporteProblemaEntity): Long

    @Update
    suspend fun actualizarReporte(reporte: ReporteProblemaEntity)

    @Query("SELECT * FROM reportes_problema ORDER BY fechaReporte DESC")
    fun observarReportes(): Flow<List<ReporteProblemaEntity>>
}
