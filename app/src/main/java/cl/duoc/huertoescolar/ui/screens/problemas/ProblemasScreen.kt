package cl.duoc.huertoescolar.ui.screens.problemas

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun ProblemasScreen(
    onVolver: () -> Unit
) {
    PantallaHuerto(
        titulo = "Problemas del huerto",
        subtitulo = "Situaciones informadas en sectores y cultivos",
        onVolver = onVolver
    ) {
        TarjetaHuerto {
            Text(
                text = "Hojas dañadas",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = "Pendiente")

            Text("Sector: Bancal 1")
            Text("Cultivo: Lechugas")
            Text("Descripción: Se detectaron hojas con daños.")
            Text("Reportado por: Estudiante de prueba")
        }
    }
}