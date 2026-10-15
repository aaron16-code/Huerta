package cl.duoc.huertoescolar.ui.screens.problemas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.TipoProblema
import cl.duoc.huertoescolar.data.UsuarioPrueba

class ReportarProblemaViewModel : ViewModel() {

    var sectorId by mutableStateOf<Int?>(null)
        private set
    var cultivoId by mutableStateOf<Int?>(null)
        private set
    var tipo by mutableStateOf(TipoProblema.FALTA_AGUA)
        private set
    var descripcion by mutableStateOf("")
        private set
    var fotoUri by mutableStateOf<String?>(null)
        private set
    var mostrarConfirmacion by mutableStateOf(false)
        private set
    var enviado by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var mensajeEsError by mutableStateOf(false)
        private set

    fun elegirSector(id: Int) {
        sectorId = id
        cultivoId = null // el cultivo depende del sector
        mensaje = null
    }

    fun elegirCultivo(id: Int?) {
        cultivoId = id
    }

    fun elegirTipo(nuevo: TipoProblema) {
        tipo = nuevo
    }

    fun cambiarDescripcion(texto: String) {
        descripcion = texto
    }

    /** Foto ficticia de prueba. Más adelante se reemplaza por la cámara o la galería. */
    fun adjuntarFotoDePrueba() {
        fotoUri = "foto_prueba_problema"
    }

    /** Valida el formulario y, si está bien, pide la confirmación al usuario. */
    fun pedirConfirmacion() {
        when {
            sectorId == null -> mostrarError("Elige el sector donde viste el problema.")
            descripcion.trim().length < 5 -> mostrarError("Describe el problema con al menos 5 caracteres.")
            else -> {
                mensaje = null
                mostrarConfirmacion = true
            }
        }
    }

    fun cancelarConfirmacion() {
        mostrarConfirmacion = false
    }

    fun enviar(usuario: UsuarioPrueba) {
        mostrarConfirmacion = false
        val sector = sectorId ?: return
        val resultado = RepositorioOperacion.reportarProblema(
            sectorId = sector,
            cultivoId = cultivoId,
            tipo = tipo,
            descripcion = descripcion.trim(),
            reportanteId = usuario.id,
            fotoUri = fotoUri
        )
        if (resultado.isSuccess) {
            enviado = true
            mensaje = null
        } else {
            mostrarError(resultado.exceptionOrNull()?.message ?: "No se pudo enviar el reporte.")
        }
    }

    private fun mostrarError(texto: String) {
        mensaje = texto
        mensajeEsError = true
    }
}