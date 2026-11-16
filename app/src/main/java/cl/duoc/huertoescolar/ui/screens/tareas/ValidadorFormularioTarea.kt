package cl.duoc.huertoescolar.ui.screens.tareas

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DIA_MILLIS = 24L * 60 * 60 * 1000

data class ErroresFormularioTarea(
    val descripcion: String? = null,
    val instrucciones: String? = null,
    val sector: String? = null,
    val fecha: String? = null,
    val destinatario: String? = null
) {
    val esValido: Boolean
        get() = listOf(descripcion, instrucciones, sector, fecha, destinatario).all { it == null }
}

object ValidadorFormularioTarea {
    fun validar(
        state: TareaFormUiState,
        exigirFechaActualOFutura: Boolean
    ): ErroresFormularioTarea {
        val fecha = parsearFechaFinDelDia(state.fechaTexto)
        return ErroresFormularioTarea(
            descripcion = if (state.descripcion.trim().length < 5)
                "Escribe una descripción de al menos 5 caracteres" else null,
            instrucciones = if (state.instrucciones.trim().length < 5)
                "Escribe instrucciones breves (al menos 5 caracteres)" else null,
            sector = if (state.sectorId == null) "Elige un sector" else null,
            fecha = when {
                state.fechaTexto.isBlank() -> "Indica la fecha programada"
                fecha == null -> "Usa el formato dd/MM/aaaa (por ejemplo 25/10/2026)"
                exigirFechaActualOFutura && fecha < System.currentTimeMillis() ->
                    "La fecha no puede ser anterior a hoy"
                else -> null
            },
            destinatario = if (state.destinatario == null)
                "Elige a quién se asigna la tarea" else null
        )
    }
}

internal fun parsearFechaFinDelDia(texto: String): Long? {
    val limpio = texto.trim()
    if (!Regex("""\d{2}/\d{2}/\d{4}""").matches(limpio)) return null
    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).apply {
            isLenient = false
        }
        val fecha = formato.parse(limpio) ?: return null
        Calendar.getInstance().apply {
            time = fecha
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } catch (_: Exception) {
        null
    }
}

internal fun fechaRapida(dias: Int): String {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
    return formato.format(Date(System.currentTimeMillis() + dias * DIA_MILLIS))
}
