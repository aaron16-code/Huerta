package cl.duoc.huertoescolar.data.remoto

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints del microservicio para tareas y problemas. */
interface ApiOperacion {

    @GET("api/tareas")
    suspend fun listarTareas(): List<TareaRemota>

    @POST("api/tareas")
    suspend fun crearTarea(@Body tarea: TareaRemota): TareaRemota

    @PUT("api/tareas/{id}")
    suspend fun actualizarTarea(@Path("id") id: Int, @Body tarea: TareaRemota): TareaRemota

    @DELETE("api/tareas/{id}")
    suspend fun eliminarTarea(@Path("id") id: Int)

    @GET("api/problemas")
    suspend fun listarProblemas(): List<ProblemaRemoto>

    @POST("api/problemas")
    suspend fun crearProblema(@Body problema: ProblemaRemoto): ProblemaRemoto

    @PUT("api/problemas/{id}")
    suspend fun actualizarProblema(@Path("id") id: Int, @Body problema: ProblemaRemoto): ProblemaRemoto
}

/** Crea el cliente Retrofit una sola vez. */
object ClienteApi {
    val operacion: ApiOperacion by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiOperacion::class.java)
    }
}
