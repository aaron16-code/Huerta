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
import androidx.compose.runtime.setValue

object RepositorioHuerto :
    CatalogoHuertoRepository,
    SectoresRepository,
    CultivosRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var huertoDao: HuertoDao? = null
    private var inicializado = false
    val modoAplicacion get() = inicializado

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
        huertoDao = OperacionDatabase.getInstance(context).huertoDao()
        sectores = emptyList()
        cultivos = emptyList()
    }

    private fun cl.duoc.huertoescolar.data.remote.SectorDto.modelo() = Sector(id, nombre, descripcion.orEmpty(), ubicacion.orEmpty(), cl.duoc.huertoescolar.domain.model.EstadoSector.valueOf(estado), cl.duoc.huertoescolar.data.UsuariosPrueba.todos.find { it.id == responsableId }?.nombre ?: "Sin responsable")
    private fun cl.duoc.huertoescolar.data.remote.CultivoDto.modelo() = Cultivo(id, nombre, descripcion.orEmpty(), sectorId, fechaSiembra.orEmpty(), fechaCosechaEstimada.orEmpty(), frecuenciaRiegoDias, cl.duoc.huertoescolar.domain.model.EstadoCultivo.valueOf(estado), observaciones.orEmpty(), fotoUri)

    suspend fun sincronizar() {
        val api = HuertoRemoteRepository()
        val nuevosSectores = api.obtenerSectores().map { it.modelo() }
        val nuevosCultivos = api.obtenerCultivos().map { it.modelo() }
        sectores = nuevosSectores
        cultivos = nuevosCultivos
        guardarCache()
    }
    suspend fun recibirSector(dto: cl.duoc.huertoescolar.data.remote.SectorDto) {
        if (!inicializado) return
        sectores = sectores.filterNot { it.id == dto.id } + dto.modelo()
        guardarCache()
    }
    suspend fun recibirCultivo(dto: cl.duoc.huertoescolar.data.remote.CultivoDto) {
        if (!inicializado) return
        cultivos = cultivos.filterNot { it.id == dto.id } + dto.modelo()
        guardarCache()
    }
    suspend fun quitarSector(id: Int) { sectores = sectores.filterNot { it.id == id }; guardarCache() }
    suspend fun quitarCultivo(id: Int) { cultivos = cultivos.filterNot { it.id == id }; guardarCache() }
    private suspend fun guardarCache() {
        val dao = huertoDao ?: return
        try {
            dao.reemplazarCatalogo(sectores.map { it.toEntity() }, cultivos.map { it.toEntity() })
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) { android.util.Log.w("HuertoRoom", "No se pudo actualizar la copia local", e) }
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
