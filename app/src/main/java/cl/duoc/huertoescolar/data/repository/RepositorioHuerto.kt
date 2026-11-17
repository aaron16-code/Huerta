package cl.duoc.huertoescolar.data.repository

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import cl.duoc.huertoescolar.data.listaCultivosInicial
import cl.duoc.huertoescolar.data.listaSectoresInicial
import cl.duoc.huertoescolar.data.local.HuertoDao
import cl.duoc.huertoescolar.data.local.toEntity
import cl.duoc.huertoescolar.data.local.toModelo
import cl.duoc.huertoescolar.data.operacion.OperacionDatabase
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.domain.repository.CatalogoHuertoRepository
import cl.duoc.huertoescolar.domain.repository.CultivosRepository
import cl.duoc.huertoescolar.domain.repository.SectoresRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.plus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object RepositorioHuerto :
    CatalogoHuertoRepository,
    SectoresRepository,
    CultivosRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var huertoDao: HuertoDao? = null
    private var inicializado = false

    override var sectores by mutableStateOf(listaSectoresInicial())
        private set

    override var cultivos by mutableStateOf(listaCultivosInicial())
        private set

    override var sectorSeleccionado by mutableStateOf<Sector?>(null)
        private set

    override var cultivoSeleccionado by mutableStateOf<Cultivo?>(null)
        private set

    fun inicializar(context: Context) {
        if (inicializado) return
        inicializado = true

        val dao = OperacionDatabase.getInstance(context).huertoDao()
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

    override fun agregarSector(sector: Sector) {
        sectores = sectores + sector

        huertoDao?.let { dao ->
            scope.launch {
                dao.insertarSector(sector.toEntity())
            }
        }
    }

    override fun obtenerSiguienteId(): Int =
        (sectores.maxOfOrNull { it.id } ?: 0) + 1

    fun seleccionarSector(sector: Sector) {
        sectorSeleccionado = sector
    }

    override fun actualizarSector(sectorActualizado: Sector) {
        sectores = sectores.map { sector ->
            if (sector.id == sectorActualizado.id) {
                sectorActualizado
            } else {
                sector
            }
        }

        huertoDao?.let { dao ->
            scope.launch {
                dao.actualizarSector(sectorActualizado.toEntity())
            }
        }

        sectorSeleccionado = null
    }

    override fun agregarCultivo(cultivo: Cultivo) {
        cultivos = cultivos + cultivo

        huertoDao?.let { dao ->
            scope.launch {
                dao.insertarCultivo(cultivo.toEntity())
            }
        }
    }

    override fun obtenerSiguienteIdCultivo(): Int =
        (cultivos.maxOfOrNull { it.id } ?: 0) + 1

    fun seleccionarCultivo(cultivo: Cultivo) {
        cultivoSeleccionado = cultivo
    }

    override fun actualizarCultivo(cultivoActualizado: Cultivo) {
        cultivos = cultivos.map { cultivo ->
            if (cultivo.id == cultivoActualizado.id) {
                cultivoActualizado
            } else {
                cultivo
            }
        }

        huertoDao?.let { dao ->
            scope.launch {
                dao.actualizarCultivo(cultivoActualizado.toEntity())
            }
        }

        cultivoSeleccionado = null
    }
}