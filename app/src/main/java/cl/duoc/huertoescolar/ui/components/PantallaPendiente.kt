package cl.duoc.huertoescolar.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PantallaPendiente(
    titulo: String,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaHuerto(
        titulo = titulo,
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            Text("Esta pantalla está preparada para agregar su formulario y funcionalidad.")
        }
    }
}
