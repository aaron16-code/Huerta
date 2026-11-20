package cl.duoc.huertoescolar.data.remote

data class SectorRequest(
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: String,
    val responsableId: Int?
)