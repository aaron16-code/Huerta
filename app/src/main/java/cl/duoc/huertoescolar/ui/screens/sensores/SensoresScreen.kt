package cl.duoc.huertoescolar.ui.screens.sensores

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun SensoresScreen(
    onVolver: () -> Unit
) {
    PantallaHuerto(
        titulo = "Sensores",
        subtitulo = "Lecturas simuladas del huerto",
        onVolver = onVolver
    ) {
        TarjetaHuerto {
            Text(
                text = "Humedad del suelo",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = "Normal")

            Text("Sector: Bancal 1")
            Text("Lectura actual: 65 %")
            Text("Última actualización: Hoy")
        }

        TarjetaHuerto {
            Text(
                text = "Temperatura ambiental",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = "Normal")

            Text("Lectura actual: 22 °C")
            Text("Última actualización: Hoy")
        }
    }
}