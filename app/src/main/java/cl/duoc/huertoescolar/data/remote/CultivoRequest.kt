package cl.duoc.huertoescolar.data.remote

data class CultivoRequest(
    val nombre: String,
    val descripcion: String,
    val sectorId: Int,
    val fechaSiembra: String,
    val fechaCosechaEstimada: String,
    val frecuenciaRiegoDias: Int,
    val estado: String,
    val observaciones: String,
    val fotoUri: String? = null
)