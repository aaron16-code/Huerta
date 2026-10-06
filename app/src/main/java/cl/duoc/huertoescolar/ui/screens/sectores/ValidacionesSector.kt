package cl.duoc.huertoescolar.ui.screens.sectores

fun validarNombreSector(nombre: String): Boolean {
    return nombre.isNotBlank()
}

fun validarDescripcionSector(descripcion: String): Boolean {
    return descripcion.length >= 5
}

fun validarUbicacionSector(ubicacion: String): Boolean {
    return ubicacion.isNotBlank()
}