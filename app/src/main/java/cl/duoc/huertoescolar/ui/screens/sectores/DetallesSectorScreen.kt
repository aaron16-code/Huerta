package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.PantallaPendiente

@Composable
fun DetalleSectorScreen(onVolver: () -> Unit) {
    PantallaPendiente(
        titulo = "Detalle del sector",
        onVolver = onVolver
    )
}