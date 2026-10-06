package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.PantallaPendiente

@Composable
fun EditarSectorScreen(onVolver: () -> Unit) {
    PantallaPendiente(
        titulo = "Editar sector",
        onVolver = onVolver
    )
}