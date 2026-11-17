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

fun validarFormularioCultivo(
    estado: CultivoFormUiState
): CultivoFormUiState {
    return estado.copy(
        errorNombre = !validarNombreCultivo(estado.nombre),
        errorDescripcion = !validarDescripcionCultivo(
            estado.descripcion
        ),
        errorSector = estado.sectorId == null,
        errorFechaSiembra = !validarFechaCultivo(
            estado.fechaSiembra
        ),
        errorFechaCosecha = !validarFechaCultivo(
            estado.fechaCosecha
        ) || !validarOrdenFechasCultivo(
            estado.fechaSiembra,
            estado.fechaCosecha
        ),
        errorFrecuencia = !validarFrecuenciaRiego(
            estado.frecuenciaRiego
        )
    )
}
