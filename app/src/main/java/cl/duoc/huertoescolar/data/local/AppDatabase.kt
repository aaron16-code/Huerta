package cl.duoc.huertoescolar.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        SectorEntity::class,
        CultivoEntity::class,
        TareaEntity::class,
        EvidenciaEntity::class,
        ValidacionEntity::class,
        ReporteProblemaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun huertoDao(): HuertoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huerto_escolar.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
