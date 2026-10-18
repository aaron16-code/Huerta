package cl.duoc.huertoescolar.ui.screens.cultivos

import java.text.SimpleDateFormat
import java.util.Locale

fun validarNombreCultivo(nombre: String): Boolean {
    return nombre.isNotBlank()
}

fun validarDescripcionCultivo(descripcion: String): Boolean {
    return descripcion.length >= 5
}

fun validarFechaCultivo(fecha: String): Boolean {
    val texto = fecha.trim()
    if (!Regex("""\d{2}/\d{2}/\d{4}""").matches(texto)) return false

    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).apply {
            isLenient = false
        }.parse(texto) != null
    } catch (_: Exception) {
        false
    }
}

fun validarOrdenFechasCultivo(fechaSiembra: String, fechaCosecha: String): Boolean {
    if (!validarFechaCultivo(fechaSiembra) || !validarFechaCultivo(fechaCosecha)) return false

    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).apply {
        isLenient = false
    }
    val siembra = formato.parse(fechaSiembra.trim()) ?: return false
    val cosecha = formato.parse(fechaCosecha.trim()) ?: return false
    return !cosecha.before(siembra)
}

fun validarFrecuenciaRiego(frecuencia: String): Boolean {
    val dias = frecuencia.toIntOrNull()
    return dias != null && dias > 0
}
