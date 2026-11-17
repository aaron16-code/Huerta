package cl.duoc.huertoescolar.data.remote

data class SectorDto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: String,
    val responsableId: Int?
)