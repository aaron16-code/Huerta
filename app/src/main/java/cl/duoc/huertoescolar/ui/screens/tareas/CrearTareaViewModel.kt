package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.data.CatalogoHuertoRepository
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.TareasRepository
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.UsuariosPrueba

class CrearTareaViewModel(
    private val repository: TareasRepository = RepositorioOperacion,
    private val catalogoRepository: CatalogoHuertoRepository = RepositorioHuerto
) : ViewModel() {

    val destinatarios = destinatariosDisponibles()
    val sectores get() = catalogoRepository.sectores
    val cultivos get() = catalogoRepository.cultivos

    var uiState by mutableStateOf(TareaFormUiState())
        private set

    // Propiedades de solo lectura conservadas para las pruebas existentes.
    val tipo get() = uiState.tipo
    val descripcion get() = uiState.descripcion
    val instrucciones get() = uiState.instrucciones
    val sectorId get() = uiState.sectorId
    val cultivoId get() = uiState.cultivoId
    val fechaTexto get() = uiState.fechaTexto
    val destinatario get() = uiState.destinatario
    val errorDescripcion get() = uiState.errorDescripcion
    val errorInstrucciones get() = uiState.errorInstrucciones
    val errorSector get() = uiState.errorSector
    val errorFecha get() = uiState.errorFecha
    val errorDestinatario get() = uiState.errorDestinatario
    val mensajeError get() = uiState.mensajeError
    val mostrarConfirmacion get() = uiState.mostrarConfirmacion
    val creada get() = uiState.guardada

    fun elegirTipo(tipo: TipoTarea) = actualizar { copy(tipo = tipo) }
    fun cambiarDescripcion(texto: String) = actualizar {
        copy(descripcion = texto, errorDescripcion = null)
    }
    fun cambiarInstrucciones(texto: String) = actualizar {
        copy(instrucciones = texto, errorInstrucciones = null)
    }
    fun cambiarFecha(texto: String) = actualizar { copy(fechaTexto = texto, errorFecha = null) }
    fun elegirDestinatario(destinatario: DestinatarioTareaUi) = actualizar {
        copy(destinatario = destinatario, errorDestinatario = null)
    }
    fun elegirCultivo(id: Int?) = actualizar { copy(cultivoId = id) }

    fun elegirSector(id: Int) = actualizar {
        copy(sectorId = id, cultivoId = null, errorSector = null)
    }

    fun elegirFechaRapida(dias: Int) = cambiarFecha(fechaRapida(dias))

    fun pedirConfirmacion() {
        val errores = ValidadorFormularioTarea.validar(
            state = uiState,
            exigirFechaActualOFutura = true
        )
        uiState = uiState.conErrores(errores)
        if (errores.esValido) {
            uiState = uiState.copy(mostrarConfirmacion = true, mensajeError = null)
        }
    }

    fun cancelarConfirmacion() {
        uiState = uiState.copy(mostrarConfirmacion = false)
    }

    fun crear(usuario: UsuarioPrueba) {
        val state = uiState
        val sector = state.sectorId ?: return
        val fecha = parsearFechaFinDelDia(state.fechaTexto) ?: return
        val destino = state.destinatario ?: return

        uiState = state.copy(mostrarConfirmacion = false)
        val resultado = repository.crearTarea(
            rol = usuario.rol,
            nueva = Tarea(
                id = 0,
                tipo = state.tipo,
                descripcion = state.descripcion.trim(),
                instrucciones = state.instrucciones.trim(),
                sectorId = sector,
                cultivoId = state.cultivoId,
                fechaCreacion = 0,
                fechaProgramada = fecha,
                responsableUsuarioId = destino.usuarioId,
                grupoAsignado = destino.grupo,
                creadaPorId = usuario.id
            )
        )
        uiState = if (resultado.isSuccess) {
            uiState.copy(guardada = true, mensajeError = null)
        } else {
            uiState.copy(
                mensajeError = resultado.exceptionOrNull()?.message ?: "No se pudo crear la tarea."
            )
        }
    }

    fun crearOtra() {
        uiState = TareaFormUiState()
    }

    private fun actualizar(cambio: TareaFormUiState.() -> TareaFormUiState) {
        uiState = uiState.cambio()
    }
}

internal fun destinatariosDisponibles(): List<DestinatarioTareaUi> =
    UsuariosPrueba.estudiantes.map { estudiante ->
        DestinatarioTareaUi(estudiante.nombre, estudiante.id, null)
    } + DestinatarioTareaUi("Grupo Taller Huerto A", null, "Taller Huerto A")

internal fun TareaFormUiState.conErrores(errores: ErroresFormularioTarea): TareaFormUiState =
    copy(
        errorDescripcion = errores.descripcion,
        errorInstrucciones = errores.instrucciones,
        errorSector = errores.sector,
        errorFecha = errores.fecha,
        errorDestinatario = errores.destinatario
    )
