package cl.duoc.huertoescolar.ui.screens.problemas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.domain.model.TipoProblema
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.domain.repository.ProblemasRepository

class ReportarProblemaViewModel(
    private val repository: ProblemasRepository = RepositorioOperacion
) : ViewModel() {

    var uiState by mutableStateOf(ReportarProblemaUiState())
        private set

    val sectorId: Int?
        get() = uiState.sectorId

    val cultivoId: Int?
        get() = uiState.cultivoId

    val tipo: TipoProblema
        get() = uiState.tipo

    val descripcion: String
        get() = uiState.descripcion

    val fotoUri: String?
        get() = uiState.fotoUri

    val mostrarConfirmacion: Boolean
        get() = uiState.mostrarConfirmacion

    val enviado: Boolean
        get() = uiState.enviado

    val mensaje: String?
        get() = uiState.mensaje

    val mensajeEsError: Boolean
        get() = uiState.mensajeEsError

    val errorSector: String?
        get() = uiState.errorSector

    val errorDescripcion: String?
        get() = uiState.errorDescripcion

    fun elegirSector(id: Int) {
        uiState = uiState.copy(
            sectorId = id,
            cultivoId = null,
            errorSector = null,
            mensaje = null
        )
    }

    fun elegirCultivo(id: Int?) {
        uiState = uiState.copy(cultivoId = id)
    }

    fun elegirTipo(nuevo: TipoProblema) {
        uiState = uiState.copy(tipo = nuevo)
    }

    fun cambiarDescripcion(texto: String) {
        uiState = uiState.copy(
            descripcion = texto,
            errorDescripcion = null
        )
    }

    fun elegirFoto(uri: String) {
        uiState = uiState.copy(fotoUri = uri)
    }

    fun quitarFoto() {
        uiState = uiState.copy(fotoUri = null)
    }

    fun pedirConfirmacion() {
        val estadoValidado = validarFormularioProblema(uiState)
        uiState = estadoValidado

        if (
            estadoValidado.errorSector == null &&
            estadoValidado.errorDescripcion == null
        ) {
            uiState = estadoValidado.copy(
                mensaje = null,
                mostrarConfirmacion = true
            )
        }
    }

    fun cancelarConfirmacion() {
        uiState = uiState.copy(mostrarConfirmacion = false)
    }

    fun enviar(usuario: UsuarioPrueba) {
        uiState = uiState.copy(mostrarConfirmacion = false)

        val sector = uiState.sectorId ?: return

        val resultado = repository.reportarProblema(
            sectorId = sector,
            cultivoId = uiState.cultivoId,
            tipo = uiState.tipo,
            descripcion = uiState.descripcion.trim(),
            reportanteId = usuario.id,
            fotoUri = uiState.fotoUri
        )

        if (resultado.isSuccess) {
            uiState = uiState.copy(
                enviado = true,
                mensaje = null
            )
        } else {
            mostrarError(
                resultado.exceptionOrNull()?.message
                    ?: "No se pudo enviar el reporte."
            )
        }
    }

    private fun mostrarError(texto: String) {
        uiState = uiState.copy(
            mensaje = texto,
            mensajeEsError = true
        )
    }
}