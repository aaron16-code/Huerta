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
        return api.obtenerSectores()
    }

    suspend fun crearSector(sector: SectorRequest): SectorDto {
        return api.crearSector(sector)
    }

    suspend fun actualizarSector(
        id: Int,
        nombre: String,
        descripcion: String,
        ubicacion: String,
        estado: EstadoSector
    ): SectorDto {
        val sectorActual = api.obtenerSector(id)

        val responsableId = requireNotNull(sectorActual.responsableId) {
            "El sector no tiene un responsable asignado."
        }

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
        )
    }

    suspend fun obtenerCultivos(): List<CultivoDto> {
        return api.obtenerCultivos()
    }

    suspend fun crearCultivo(cultivo: CultivoRequest): CultivoDto {
        return api.crearCultivo(cultivo)
    }

    suspend fun actualizarCultivo(
        id: Int,
        cultivo: CultivoRequest
    ): CultivoDto {
        return api.actualizarCultivo(id, cultivo)
    }

    suspend fun obtenerSector(id: Int): SectorDto {
        return api.obtenerSector(id)
    }



}