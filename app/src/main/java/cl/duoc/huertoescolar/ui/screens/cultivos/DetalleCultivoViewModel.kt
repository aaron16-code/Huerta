package cl.duoc.huertoescolar.ui.screens.cultivos

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.huertoescolar.data.repository.HuertoRemoteRepository
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class DetalleCultivoViewModel(
    private val repository: HuertoRemoteRepository = HuertoRemoteRepository()
) : ViewModel() {

    val cultivo = RepositorioHuerto.cultivoSeleccionado

    var nombreSector by mutableStateOf<String?>(null)
        private set

    var cargandoSector by mutableStateOf(false)
        private set

    var errorSector by mutableStateOf<String?>(null)
        private set

    init {
        cargarSector()
    }

    fun cargarSector() {
        val idSector = cultivo?.sectorId ?: return
        if (cargandoSector) return

        cargandoSector = true
        errorSector = null

        viewModelScope.launch {
            try {
                nombreSector = repository.obtenerSector(idSector).nombre
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HuertoAPI", "Error al obtener el sector del cultivo", e)
                errorSector = "No se pudo consultar el nombre del sector."
            } finally {
                cargandoSector = false
            }
        }
    }
}