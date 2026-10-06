package cl.duoc.huertoescolar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TareasViewModel: ViewModel() {

    var tarea by mutableStateOf(
        Tarea(
            id = 1,
            titulo = "Regar las lechugas",
            sector = "bancal 1",
            responsable = "Pepito",
            instrucciones = "Regar la tierra alrededor de las plantas."
        )
    )
        private set

    fun enviarAValidacion() {
        if(tarea.estado == EstadoTarea.PENDIENTE) {
            tarea = tarea.copy(
                estado = EstadoTarea.PENDIENTE_VALIDACION
            )
        }
    }

}