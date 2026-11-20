package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.remote.HuertoApi
import cl.duoc.huertoescolar.data.remote.HuertoRetrofitClient
import cl.duoc.huertoescolar.data.remote.SectorDto
import cl.duoc.huertoescolar.data.remote.SectorRequest
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.data.remote.CultivoDto
import cl.duoc.huertoescolar.data.remote.CultivoRequest

class HuertoRemoteRepository(
    private val api: HuertoApi = HuertoRetrofitClient.api
) {

    suspend fun obtenerSectores(): List<SectorDto> {
        return api.obtenerSectores().map { it.seguro() }
    }

    suspend fun crearSector(sector: SectorRequest): SectorDto {
        return api.crearSector(sector).seguro().also { RepositorioHuerto.recibirSector(it) }
    }

    suspend fun actualizarSector(
        id: Int,
        nombre: String,
        descripcion: String,
        ubicacion: String,
        estado: EstadoSector
    ): SectorDto {
        val sectorActual = api.obtenerSector(id)

        val responsableId = sectorActual.responsableId

        val solicitud = SectorRequest(
            nombre = nombre,
            descripcion = descripcion,
            ubicacion = ubicacion,
            estado = estado.name,
            responsableId = responsableId
        )

        return api.actualizarSector(
            id = id,
            sector = solicitud
        ).seguro().also { RepositorioHuerto.recibirSector(it) }
    }

    suspend fun obtenerCultivos(): List<CultivoDto> {
        return api.obtenerCultivos().map { it.seguro() }
    }

    suspend fun crearCultivo(cultivo: CultivoRequest): CultivoDto {
        return api.crearCultivo(cultivo).seguro().also { RepositorioHuerto.recibirCultivo(it) }
    }

    suspend fun actualizarCultivo(
        id: Int,
        cultivo: CultivoRequest
    ): CultivoDto {
        return api.actualizarCultivo(id, cultivo).seguro().also { RepositorioHuerto.recibirCultivo(it) }
    }

    suspend fun obtenerSector(id: Int): SectorDto {
        return api.obtenerSector(id).seguro()
    }



    suspend fun eliminarSector(id: Int) { api.eliminarSector(id); RepositorioHuerto.quitarSector(id) }
    suspend fun eliminarCultivo(id: Int) { api.eliminarCultivo(id); RepositorioHuerto.quitarCultivo(id) }
}
private fun SectorDto.seguro() = copy(descripcion = descripcion.orEmpty(), ubicacion = ubicacion.orEmpty())
private fun CultivoDto.seguro() = copy(descripcion = descripcion.orEmpty(), fechaSiembra = fechaSiembra.orEmpty(), fechaCosechaEstimada = fechaCosechaEstimada.orEmpty(), observaciones = observaciones.orEmpty())
