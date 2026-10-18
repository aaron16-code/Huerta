package cl.duoc.huertoescolar

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cl.duoc.huertoescolar.data.local.AppDatabase
import cl.duoc.huertoescolar.data.local.CultivoEntity
import cl.duoc.huertoescolar.data.local.HuertoDao
import cl.duoc.huertoescolar.data.local.SectorEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object RepositorioHuerto {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var huertoDao: HuertoDao? = null
    private var inicializado = false

    var sectores by mutableStateOf(listaSectoresInicial())
        private set

    var cultivos by mutableStateOf(listaCultivosInicial())
        private set

    var sectorSeleccionado by mutableStateOf<Sector?>(null)
        private set

    var cultivoSeleccionado by mutableStateOf<Cultivo?>(null)
        private set

    fun inicializar(context: Context) {
        if (inicializado) return
        inicializado = true

        val dao = AppDatabase.getInstance(context).huertoDao()
        huertoDao = dao

        scope.launch {
            if (dao.contarSectores() == 0) {
                dao.insertarSector(listaSectoresInicial().first().toEntity())
            }
            if (dao.contarCultivos() == 0) {
                dao.insertarCultivo(listaCultivosInicial().first().toEntity())
            }
        }

        scope.launch {
            dao.observarSectores().collectLatest { entidades ->
                withContext(Dispatchers.Main) {
                    sectores = entidades.map { it.toModelo() }
                }
            }
        }

        scope.launch {
            dao.observarCultivos().collectLatest { entidades ->
                withContext(Dispatchers.Main) {
                    cultivos = entidades.map { it.toModelo() }
                }
            }
        }
    }

    fun agregarSector(sector: Sector) {
        sectores = sectores + sector
        huertoDao?.let { dao ->
            scope.launch { dao.insertarSector(sector.toEntity()) }
        }
    }

    fun obtenerSiguienteId(): Int = (sectores.maxOfOrNull { it.id } ?: 0) + 1

    fun seleccionarSector(sector: Sector) {
        sectorSeleccionado = sector
    }

    fun actualizarSector(sectorActualizado: Sector) {
        sectores = sectores.map { sector ->
            if (sector.id == sectorActualizado.id) sectorActualizado else sector
        }
        huertoDao?.let { dao ->
            scope.launch { dao.actualizarSector(sectorActualizado.toEntity()) }
        }
        sectorSeleccionado = null
    }

    fun agregarCultivo(cultivo: Cultivo) {
        cultivos = cultivos + cultivo
        huertoDao?.let { dao ->
            scope.launch { dao.insertarCultivo(cultivo.toEntity()) }
        }
    }

    fun obtenerSiguienteIdCultivo(): Int = (cultivos.maxOfOrNull { it.id } ?: 0) + 1

    fun seleccionarCultivo(cultivo: Cultivo) {
        cultivoSeleccionado = cultivo
    }

    fun actualizarCultivo(cultivoActualizado: Cultivo) {
        cultivos = cultivos.map { cultivo ->
            if (cultivo.id == cultivoActualizado.id) cultivoActualizado else cultivo
        }
        huertoDao?.let { dao ->
            scope.launch { dao.actualizarCultivo(cultivoActualizado.toEntity()) }
        }
        cultivoSeleccionado = null
    }

    private fun listaSectoresInicial() = listOf(
        Sector(
            id = 1,
            nombre = "Bancal 1",
            descripcion = "Sector de hortalizas",
            ubicacion = "Zona norte",
            estado = EstadoSector.EN_USO,
            responsable = "Docente de prueba"
        )
    )

    private fun listaCultivosInicial() = listOf(
        Cultivo(
            id = 1,
            nombre = "Lechugas",
            descripcion = "Cultivo de prueba",
            sectorId = 1,
            fechaSiembra = "01/10/2026",
            fechaCosechaEstimada = "30/11/2026",
            frecuenciaRiegoDias = 2,
            estado = EstadoCultivo.EN_CRECIMIENTO,
            observaciones = "Crecimiento normal"
        )
    )
}

private fun Sector.toEntity() = SectorEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = estado.name
)

private fun SectorEntity.toModelo() = Sector(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = EstadoSector.valueOf(estado),
    responsable = "Docente de prueba"
)

private fun Cultivo.toEntity() = CultivoEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    sectorId = sectorId,
    fechaSiembra = fechaSiembra,
    fechaCosechaEstimada = fechaCosechaEstimada,
    frecuenciaRiegoDias = frecuenciaRiegoDias,
    estado = estado.name,
    observaciones = observaciones,
    fotoUri = fotoUri
)

private fun CultivoEntity.toModelo() = Cultivo(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    sectorId = sectorId,
    fechaSiembra = fechaSiembra,
    fechaCosechaEstimada = fechaCosechaEstimada,
    frecuenciaRiegoDias = frecuenciaRiegoDias,
    estado = EstadoCultivo.valueOf(estado),
    observaciones = observaciones,
    fotoUri = fotoUri
)
