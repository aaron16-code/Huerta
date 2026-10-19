package cl.duoc.huertoescolar.data.operacion

import android.content.Context
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.Update
import cl.duoc.huertoescolar.data.EstadoLectura
import cl.duoc.huertoescolar.data.EstadoReporte
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.LecturaSensor
import cl.duoc.huertoescolar.data.Problema
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.TipoProblema
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.local.CultivoEntity
import cl.duoc.huertoescolar.data.local.EvidenciaEntity
import cl.duoc.huertoescolar.data.local.HuertoDao
import cl.duoc.huertoescolar.data.local.ReporteProblemaEntity
import cl.duoc.huertoescolar.data.local.SectorEntity
import cl.duoc.huertoescolar.data.local.TareaEntity
import cl.duoc.huertoescolar.data.local.UsuarioEntity
import cl.duoc.huertoescolar.data.local.ValidacionEntity

// Una sola base Room para todos los módulos del huerto.
// Las tablas "op_..." conservan la estructura creada para tareas, problemas y lecturas.
// Las fechas de esas tablas se guardan como Long.

@Entity(tableName = "op_tareas")
data class TareaOpEntity(
    @PrimaryKey val id: Int,
    val tipo: String,
    val descripcion: String,
    val instrucciones: String,
    val sectorId: Int,
    val cultivoId: Int?,
    val fechaCreacion: Long,
    val fechaProgramada: Long,
    val responsableUsuarioId: Int?,
    val grupoAsignado: String?,
    val creadaPorId: Int,
    val estado: String,
    val fechaRealizacion: Long?,
    val realizadaPorId: Int?,
    val fotoUri: String?,
    val comentarioEstudiante: String,
    val comentarioDocente: String,
    val validadorId: Int?,
    val fechaValidacion: Long?
)

@Entity(tableName = "op_problemas")
data class ProblemaOpEntity(
    @PrimaryKey val id: Int,
    val sectorId: Int,
    val cultivoId: Int?,
    val tipo: String,
    val descripcion: String,
    val fechaReporte: Long,
    val reportadoPorId: Int,
    val fotoUri: String?,
    val estado: String,
    val comentarioDocente: String,
    val fechaCierre: Long?
)

@Entity(tableName = "op_lecturas")
data class LecturaOpEntity(
    @PrimaryKey val id: Int,
    val sectorId: Int,
    val fecha: Long,
    val humedadSuelo: Int,
    val temperaturaAmbiente: Double,
    val humedadAmbiente: Int,
    val estado: String,
    val origen: String
)

@Dao
interface OperacionDao {
    @Insert
    suspend fun insertarTarea(tarea: TareaOpEntity): Long

    @Update
    suspend fun actualizarTarea(tarea: TareaOpEntity)

    @Query("DELETE FROM op_tareas WHERE id = :tareaId")
    suspend fun eliminarTarea(tareaId: Int)

    @Query("SELECT * FROM op_tareas ORDER BY id")
    suspend fun leerTareas(): List<TareaOpEntity>

    @Query("SELECT COUNT(*) FROM op_tareas")
    suspend fun contarTareas(): Int

    @Insert
    suspend fun insertarProblema(problema: ProblemaOpEntity): Long

    @Update
    suspend fun actualizarProblema(problema: ProblemaOpEntity)

    @Query("SELECT * FROM op_problemas ORDER BY id")
    suspend fun leerProblemas(): List<ProblemaOpEntity>

    @Query("SELECT COUNT(*) FROM op_problemas")
    suspend fun contarProblemas(): Int

    @Insert
    suspend fun insertarLectura(lectura: LecturaOpEntity): Long

    @Query("SELECT * FROM op_lecturas ORDER BY id")
    suspend fun leerLecturas(): List<LecturaOpEntity>

    @Query("SELECT COUNT(*) FROM op_lecturas")
    suspend fun contarLecturas(): Int
}

@Database(
    entities = [
        UsuarioEntity::class,
        SectorEntity::class,
        CultivoEntity::class,
        TareaEntity::class,
        EvidenciaEntity::class,
        ValidacionEntity::class,
        ReporteProblemaEntity::class,
        TareaOpEntity::class,
        ProblemaOpEntity::class,
        LecturaOpEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OperacionDatabase : RoomDatabase() {
    abstract fun operacionDao(): OperacionDao
    abstract fun huertoDao(): HuertoDao

    companion object {
        @Volatile
        private var INSTANCE: OperacionDatabase? = null

        fun getInstance(context: Context): OperacionDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    OperacionDatabase::class.java,
                    "huerto_escolar_unificado.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

// ---------- Conversión entre el modelo de la app y las tablas ----------

fun Tarea.aEntidad() = TareaOpEntity(
    id = id, tipo = tipo.name, descripcion = descripcion, instrucciones = instrucciones,
    sectorId = sectorId, cultivoId = cultivoId, fechaCreacion = fechaCreacion,
    fechaProgramada = fechaProgramada, responsableUsuarioId = responsableUsuarioId,
    grupoAsignado = grupoAsignado, creadaPorId = creadaPorId, estado = estado.name,
    fechaRealizacion = fechaRealizacion, realizadaPorId = realizadaPorId, fotoUri = fotoUri,
    comentarioEstudiante = comentarioEstudiante, comentarioDocente = comentarioDocente,
    validadorId = validadorId, fechaValidacion = fechaValidacion
)

fun TareaOpEntity.aModelo() = Tarea(
    id = id, tipo = TipoTarea.valueOf(tipo), descripcion = descripcion, instrucciones = instrucciones,
    sectorId = sectorId, cultivoId = cultivoId, fechaCreacion = fechaCreacion,
    fechaProgramada = fechaProgramada, responsableUsuarioId = responsableUsuarioId,
    grupoAsignado = grupoAsignado, creadaPorId = creadaPorId, estado = EstadoTarea.valueOf(estado),
    fechaRealizacion = fechaRealizacion, realizadaPorId = realizadaPorId, fotoUri = fotoUri,
    comentarioEstudiante = comentarioEstudiante, comentarioDocente = comentarioDocente,
    validadorId = validadorId, fechaValidacion = fechaValidacion
)

fun Problema.aEntidad() = ProblemaOpEntity(
    id = id, sectorId = sectorId, cultivoId = cultivoId, tipo = tipo.name, descripcion = descripcion,
    fechaReporte = fechaReporte, reportadoPorId = reportadoPorId, fotoUri = fotoUri,
    estado = estado.name, comentarioDocente = comentarioDocente, fechaCierre = fechaCierre
)

fun ProblemaOpEntity.aModelo() = Problema(
    id = id, sectorId = sectorId, cultivoId = cultivoId, tipo = TipoProblema.valueOf(tipo),
    descripcion = descripcion, fechaReporte = fechaReporte, reportadoPorId = reportadoPorId,
    fotoUri = fotoUri, estado = EstadoReporte.valueOf(estado),
    comentarioDocente = comentarioDocente, fechaCierre = fechaCierre
)

fun LecturaSensor.aEntidad() = LecturaOpEntity(
    id = id, sectorId = sectorId, fecha = fecha, humedadSuelo = humedadSuelo,
    temperaturaAmbiente = temperaturaAmbiente, humedadAmbiente = humedadAmbiente,
    estado = estado.name, origen = origen
)

fun LecturaOpEntity.aModelo() = LecturaSensor(
    id = id, sectorId = sectorId, fecha = fecha, humedadSuelo = humedadSuelo,
    temperaturaAmbiente = temperaturaAmbiente, humedadAmbiente = humedadAmbiente,
    estado = EstadoLectura.valueOf(estado), origen = origen
)
