package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.UsuariosPrueba

class EditarTareaViewModel : ViewModel() {

    private val tareaOriginal = RepositorioOperacion.tareaSeleccionadaId
        ?.let(RepositorioOperacion::obtenerTarea)

    val destinatarios = UsuariosPrueba.estudiantes.map { estudiante ->
        Destinatario(estudiante.nombre, estudiante.id, null)
    } + Destinatario("Grupo Taller Huerto A", null, "Taller Huerto A")

    var tipo by mutableStateOf(tareaOriginal?.tipo ?: TipoTarea.RIEGO)
        private set
    var descripcion by mutableStateOf(tareaOriginal?.descripcion.orEmpty())
        private set
    var instrucciones by mutableStateOf(tareaOriginal?.instrucciones.orEmpty())
        private set
    var sectorId by mutableStateOf(tareaOriginal?.sectorId)
        private set
    var cultivoId by mutableStateOf(tareaOriginal?.cultivoId)
        private set
    var fechaTexto by mutableStateOf(tareaOriginal?.let { formatearFecha(it.fechaProgramada) }.orEmpty())
        private set
    var destinatario by mutableStateOf(
        destinatarios.find {
            it.usuarioId == tareaOriginal?.responsableUsuarioId &&
                    it.grupo == tareaOriginal?.grupoAsignado
        }
    )
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
    var mensajeError by mutableStateOf<String?>(null)
        private set
    var actualizada by mutableStateOf(false)
        private set

    val tareaDisponible: Boolean get() = tareaOriginal != null

    fun elegirTipo(nuevo: TipoTarea) { tipo = nuevo }
    fun cambiarDescripcion(texto: String) { descripcion = texto; errorDescripcion = null }
    fun cambiarInstrucciones(texto: String) { instrucciones = texto; errorInstrucciones = null }
    fun cambiarFecha(texto: String) { fechaTexto = texto; errorFecha = null }
    fun elegirDestinatario(nuevo: Destinatario) { destinatario = nuevo; errorDestinatario = null }
    fun elegirCultivo(id: Int?) { cultivoId = id }

    fun elegirSector(id: Int) {
        if (sectorId != id) cultivoId = null
        sectorId = id
        errorSector = null
    }

    fun guardar(usuario: UsuarioPrueba) {
        val original = tareaOriginal ?: return
        if (!validar()) return
        val sector = sectorId ?: return
        val fecha = parsearFechaFinDelDia(fechaTexto) ?: return
        val destino = destinatario ?: return

        val resultado = RepositorioOperacion.editarTarea(
            rol = usuario.rol,
            editada = original.copy(
                tipo = tipo,
                descripcion = descripcion,
                instrucciones = instrucciones,
                sectorId = sector,
                cultivoId = cultivoId,
                fechaProgramada = fecha,
                responsableUsuarioId = destino.usuarioId,
                grupoAsignado = destino.grupo
            )
        )
        if (resultado.isSuccess) {
            actualizada = true
            mensajeError = null
        } else {
            mensajeError = resultado.exceptionOrNull()?.message ?: "No se pudo actualizar la tarea."
        }
    }

    private fun validar(): Boolean {
        errorDescripcion =
            if (descripcion.trim().length < 5) "Escribe una descripción de al menos 5 caracteres" else null
        errorInstrucciones =
            if (instrucciones.trim().length < 5) "Escribe instrucciones breves (al menos 5 caracteres)" else null
        errorSector = if (sectorId == null) "Elige un sector" else null
        errorDestinatario = if (destinatario == null) "Elige a quién se asigna la tarea" else null
        errorFecha = when {
            fechaTexto.isBlank() -> "Indica la fecha programada"
            parsearFechaFinDelDia(fechaTexto) == null -> "Usa el formato dd/MM/aaaa"
            else -> null
        }
        return listOf(
            errorDescripcion, errorInstrucciones, errorSector, errorFecha, errorDestinatario
        ).all { it == null }
    }
}
