package cl.duoc.huertoescolar.data.remote

import retrofit2.http.DELETE
import cl.duoc.huertoescolar.data.UsuarioPrueba
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HuertoApi {
    @GET("api/usuarios")
    suspend fun obtenerUsuarios(): List<UsuarioRemoto>
    @DELETE("api/sectores/{id}")
    suspend fun eliminarSector(@Path("id") id: Int)
    @DELETE("api/cultivos/{id}")
    suspend fun eliminarCultivo(@Path("id") id: Int)

    @PUT("api/cultivos/{id}")
    suspend fun actualizarCultivo(
        @Path("id") id: Int,
        @Body cultivo: CultivoRequest
    ): CultivoDto
    @POST("api/cultivos")
    suspend fun crearCultivo(
        @Body cultivo: CultivoRequest
    ): CultivoDto

    @GET("api/cultivos")
    suspend fun obtenerCultivos(): List<CultivoDto>

    @GET("api/sectores")
    suspend fun obtenerSectores(): List<SectorDto>

    @GET("api/sectores/{id}")
    suspend fun obtenerSector(
        @Path("id") id: Int
    ): SectorDto

    @POST("api/sectores")
    suspend fun crearSector(
        @Body sector: SectorRequest
    ): SectorDto

    @PUT("api/sectores/{id}")
    suspend fun actualizarSector(
        @Path("id") id: Int,
        @Body sector: SectorRequest
    ): SectorDto
}