package cl.duoc.huertoescolar.ui.screens.problemas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
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

    var enviando by mutableStateOf(false)
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
        if (enviando || enviado) return

        val formulario = validarFormularioProblema(uiState).copy(
            mostrarConfirmacion = false,
            mensaje = null,
            mensajeEsError = false
        )
        uiState = formulario
        if (formulario.errorSector != null || formulario.errorDescripcion != null) return
        val sector = formulario.sectorId ?: return

        enviando = true
        viewModelScope.launch {
            try {
                val resultado = repository.reportarProblema(
                    sectorId = sector,
                    cultivoId = formulario.cultivoId,
                    tipo = formulario.tipo,
                    descripcion = formulario.descripcion.trim(),
                    reportanteId = usuario.id,
                    fotoUri = formulario.fotoUri
                )

                if (resultado.isSuccess) {
                    uiState = uiState.copy(
                        enviado = true,
                        mensaje = null,
                        mensajeEsError = false
                    )
                } else {
                    mostrarError(
                        resultado.exceptionOrNull()?.message
                            ?: "No se pudo enviar el reporte."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarError(e.message ?: "No se pudo enviar el reporte.")
            } finally {
                enviando = false
            }
        }
    }

    private fun mostrarError(texto: String) {
        uiState = uiState.copy(
            mensaje = texto,
            mensajeEsError = true
        )
    }
}