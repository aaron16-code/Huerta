package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DIA = 24L * 60 * 60 * 1000

/** A quién se asigna la tarea: un estudiante ficticio o un grupo. */
data class Destinatario(
    val etiqueta: String,
    val usuarioId: Int?,
    val grupo: String?
)

/**
 * Convierte "dd/MM/aaaa" en el último instante de ese día (así una tarea de hoy no nace atrasada).
 * Devuelve null si el texto no es una fecha válida.
 */
internal fun parsearFechaFinDelDia(texto: String): Long? {
    val limpio = texto.trim()
    if (!Regex("""\d{2}/\d{2}/\d{4}""").matches(limpio)) return null
    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).apply { isLenient = false }
        val fecha = formato.parse(limpio) ?: return null
        Calendar.getInstance().apply {
            time = fecha
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } catch (e: Exception) {
        null
    }
}

class CrearTareaViewModel : ViewModel() {

    // Usuarios y grupo ficticios de los datos de prueba
    val destinatarios = listOf(
        Destinatario("Estudiante de prueba (n.° 2)", 2, null),
        Destinatario("Estudiante ficticio n.° 3", 3, null),
        Destinatario("Grupo Taller Huerto A", null, "Taller Huerto A")
    )

    var tipo by mutableStateOf(TipoTarea.RIEGO)
        private set
    var descripcion by mutableStateOf("")
        private set
    var instrucciones by mutableStateOf("")
        private set
    var sectorId by mutableStateOf<Int?>(null)
        private set
    var cultivoId by mutableStateOf<Int?>(null)
        private set
    var fechaTexto by mutableStateOf("")
        private set
    var destinatario by mutableStateOf<Destinatario?>(null)
        private set

    var errorDescripcion by mutableStateOf<String?>(null)
        private set
    var errorInstrucciones by mutableStateOf<String?>(null)
        private set
    var errorSector by mutableStateOf<String?>(null)
        private set
    var errorFecha by mutableStateOf<String?>(null)
        private set
    var errorDestinatario by mutableStateOf<String?>(null)
        private set

    var mostrarConfirmacion by mutableStateOf(false)
        private set
    var creada by mutableStateOf(false)
        private set
    var mensajeError by mutableStateOf<String?>(null)
        private set

    fun elegirTipo(nuevo: TipoTarea) { tipo = nuevo }
    fun cambiarDescripcion(texto: String) { descripcion = texto; errorDescripcion = null }
    fun cambiarInstrucciones(texto: String) { instrucciones = texto; errorInstrucciones = null }
    fun cambiarFecha(texto: String) { fechaTexto = texto; errorFecha = null }
    fun elegirDestinatario(nuevo: Destinatario) { destinatario = nuevo; errorDestinatario = null }
    fun elegirCultivo(id: Int?) { cultivoId = id }

    fun elegirSector(id: Int) {
        sectorId = id
        cultivoId = null // el cultivo depende del sector
        errorSector = null
    }

    /** Atajo para no escribir la fecha: hoy más N días. */
    fun elegirFechaRapida(dias: Int) {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
        cambiarFecha(formato.format(Date(System.currentTimeMillis() + dias * DIA)))
    }

    /** Valida el formulario completo. Si todo está bien, pide la confirmación. */
    fun pedirConfirmacion() {
        if (validar()) {
            mensajeError = null
            mostrarConfirmacion = true
        }
    }

    fun cancelarConfirmacion() {
        mostrarConfirmacion = false
    }

    fun crear(usuario: UsuarioPrueba) {
        mostrarConfirmacion = false
        val sector = sectorId ?: return
        val fecha = parsearFechaFinDelDia(fechaTexto) ?: return
        val destino = destinatario ?: return

        val nueva = Tarea(
            id = 0,
            tipo = tipo,
            descripcion = descripcion.trim(),
            instrucciones = instrucciones.trim(),
            sectorId = sector,
            cultivoId = cultivoId,
            fechaCreacion = 0,
            fechaProgramada = fecha,
            responsableUsuarioId = destino.usuarioId,
            grupoAsignado = destino.grupo,
            creadaPorId = usuario.id
        )
        val resultado = RepositorioOperacion.crearTarea(usuario.rol, nueva)
        if (resultado.isSuccess) {
            creada = true
            mensajeError = null
        } else {
            mensajeError = resultado.exceptionOrNull()?.message ?: "No se pudo crear la tarea."
        }
    }

    /** Deja el formulario vacío para crear otra tarea. */
    fun crearOtra() {
        tipo = TipoTarea.RIEGO
        descripcion = ""
        instrucciones = ""
        sectorId = null
        cultivoId = null
        fechaTexto = ""
        destinatario = null
        creada = false
        mensajeError = null
    }

    private fun validar(): Boolean {
        errorDescripcion =
            if (descripcion.trim().length < 5) "Escribe una descripción de al menos 5 caracteres" else null
        errorInstrucciones =
            if (instrucciones.trim().length < 5) "Escribe instrucciones breves (al menos 5 caracteres)" else null
        errorSector = if (sectorId == null) "Elige un sector" else null
        errorDestinatario = if (destinatario == null) "Elige a quién se asigna la tarea" else null

        val fecha = parsearFechaFinDelDia(fechaTexto)
        errorFecha = when {
            fechaTexto.isBlank() -> "Indica la fecha programada"
            fecha == null -> "Usa el formato dd/MM/aaaa (por ejemplo 25/10/2026)"
            fecha < System.currentTimeMillis() -> "La fecha no puede ser anterior a hoy"
            else -> null
        }

        return listOf(
            errorDescripcion, errorInstrucciones, errorSector, errorDestinatario, errorFecha
        ).all { it == null }
    }
}
