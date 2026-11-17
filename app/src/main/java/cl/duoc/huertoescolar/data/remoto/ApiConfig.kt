package cl.duoc.huertoescolar.data.remoto

/**
 * Dirección del microservicio Spring Boot (huerto-api).
 * En el emulador, 10.0.2.2 apunta al PC. Con un celular real hay que poner la IP del PC en la red wifi.
 */
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"
}
