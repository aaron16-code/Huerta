package cl.duoc.huertoescolar.data.local

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val usuario: String,
    val clave: String,
    val rol: String,
    val grupo: String = ""
)

@Entity(
    tableName = "sectores",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("responsableId")]
)
data class SectorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: String,
    val responsableId: Int? = null
)

@Entity(
    tableName = "cultivos",
    foreignKeys = [
        ForeignKey(
            entity = SectorEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sectorId")]
)
data class CultivoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val sectorId: Int,
    val fechaSiembra: String,
    val fechaCosechaEstimada: String,
    val frecuenciaRiegoDias: Int,
    val estado: String,
    val observaciones: String = "",
    val fotoUri: String? = null
)

@Entity(
    tableName = "tareas",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = SectorEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CultivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cultivoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("responsableId"), Index("sectorId"), Index("cultivoId")]
)
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val fechaProgramada: String,
    val estado: String,
    val responsableId: Int,
    val sectorId: Int,
    val cultivoId: Int? = null,
    val comentarioEntrega: String = ""
)

@Entity(
    tableName = "evidencias",
    foreignKeys = [
        ForeignKey(
            entity = TareaEntity::class,
            parentColumns = ["id"],
            childColumns = ["tareaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tareaId")]
)
data class EvidenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tareaId: Int,
    val fotoUri: String,
    val comentario: String,
    val fechaRegistro: String
)

@Entity(
    tableName = "validaciones",
    foreignKeys = [
        ForeignKey(
            entity = TareaEntity::class,
            parentColumns = ["id"],
            childColumns = ["tareaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["validadorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("tareaId"), Index("validadorId")]
)
data class ValidacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tareaId: Int,
    val validadorId: Int,
    val resultado: String,
    val comentario: String,
    val fechaValidacion: String
)

@Entity(
    tableName = "reportes_problema",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportadoPorId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = SectorEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CultivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cultivoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("reportadoPorId"), Index("sectorId"), Index("cultivoId")]
)
data class ReporteProblemaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val estado: String,
    val fechaReporte: String,
    val reportadoPorId: Int,
    val sectorId: Int,
    val cultivoId: Int? = null,
    val fotoUri: String? = null
)
