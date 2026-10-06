package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.PantallaPendiente

@Composable
fun DetalleCultivoScreen(onVolver: () -> Unit) {
    PantallaPendiente("Detalle del cultivo", onVolver)
}