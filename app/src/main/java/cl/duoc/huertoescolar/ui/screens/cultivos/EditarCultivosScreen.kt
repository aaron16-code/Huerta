package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.PantallaPendiente

@Composable
fun EditarCultivoScreen(onVolver: () -> Unit) {
    PantallaPendiente("Editar cultivo", onVolver)
}