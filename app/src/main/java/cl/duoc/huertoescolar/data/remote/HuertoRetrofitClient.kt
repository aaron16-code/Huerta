package cl.duoc.huertoescolar.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HuertoRetrofitClient {

    val api: HuertoApi by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuertoApi::class.java)
    }
}