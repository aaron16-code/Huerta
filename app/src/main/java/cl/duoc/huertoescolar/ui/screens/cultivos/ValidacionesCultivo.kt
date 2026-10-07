package cl.duoc.huertoescolar.ui.screens.cultivos

fun validarNombreCultivo(nombre: String): Boolean {
    return nombre.isNotBlank()
}

fun validarDescripcionCultivo(descripcion: String): Boolean {
    return descripcion.length >= 5
}

fun validarFechaCultivo(fecha: String): Boolean {
    return fecha.isNotBlank()
}

fun validarFrecuenciaRiego(frecuencia: String): Boolean {
    val dias = frecuencia.toIntOrNull()
    return dias != null && dias > 0
}