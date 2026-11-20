package cl.duoc.huertoescolar.data.repository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

internal fun ViewModel.confirmarOperacion(repository: Any, accion: () -> Result<Unit>, terminar: (Result<Unit>) -> Unit) {
    if (repository === RepositorioOperacion && RepositorioHuerto.modoAplicacion) {
        viewModelScope.launch { terminar(RepositorioOperacion.confirmar(accion)) }
    } else terminar(accion())
}
