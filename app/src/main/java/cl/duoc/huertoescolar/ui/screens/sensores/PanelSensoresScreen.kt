package cl.duoc.huertoescolar.ui.screens.sensores

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.EstadoLectura
import cl.duoc.huertoescolar.data.LecturaSensor
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lecturas SIMULADAS de humedad del suelo, temperatura y humedad ambiental.
 * No vienen de dispositivos físicos y se muestran siempre como "dato de prueba".
 */
@Composable
fun PanelSensoresScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: SensoresViewModel = viewModel()
) {
    val lecturas by viewModel.lecturas.collectAsState()
    val porSector = viewModel.agrupar(lecturas)

    PantallaHuerto(
        titulo = "Sensores simulados",
        subtitulo = "Datos de prueba para mostrar una posible integración futura",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (usuario.rol != RolUsuario.ADMINISTRADOR) {
            TarjetaHuerto { Text("Solo docentes o talleristas autorizados pueden ver esta información.") }
        } else {
            TarjetaHuerto {
                EtiquetaEstado("SIMULACIÓN ACADÉMICA", MaterialTheme.colorScheme.tertiaryContainer)
                Text(
                    "Estos valores son ficticios. No vienen de sensores reales, no se conectan " +
                            "con ningún dispositivo y la app no activa el riego.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (porSector.isEmpty()) {
                TarjetaHuerto { Text("No hay lecturas simuladas.") }
            } else {
                porSector.forEach { grupo ->
                    TarjetaHuerto {
                        Text(nombreSector(grupo.sectorId), style = MaterialTheme.typography.titleMedium)
                        EtiquetaEstado(
                            texto = "Dato de prueba · ${grupo.ultima.origen}",
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                        EtiquetaEstado(
                            texto = "Estado: ${grupo.ultima.estado.etiqueta}",
                            color = colorEstadoLectura(grupo.ultima.estado)
                        )
                        Text("Humedad del suelo: ${grupo.ultima.humedadSuelo}%", style = MaterialTheme.typography.bodyLarge)
                        Text("Temperatura ambiental: ${formatearTemperatura(grupo.ultima.temperaturaAmbiente)} °C", style = MaterialTheme.typography.bodyLarge)
                        Text("Humedad ambiental: ${grupo.ultima.humedadAmbiente}%", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Lectura simulada del ${formatearFechaHora(grupo.ultima.fecha)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (grupo.anteriores.isNotEmpty()) {
                            Text("Lecturas anteriores (simuladas)", style = MaterialTheme.typography.titleSmall)
                            grupo.anteriores.forEach { lectura -> LineaAnterior(lectura) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineaAnterior(lectura: LecturaSensor) {
    Text(
        "• ${formatearFechaHora(lectura.fecha)} · suelo ${lectura.humedadSuelo}%, " +
                "${formatearTemperatura(lectura.temperaturaAmbiente)} °C, aire ${lectura.humedadAmbiente}% · ${lectura.estado.etiqueta}",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun colorEstadoLectura(estado: EstadoLectura): Color = when (estado) {
    EstadoLectura.NORMAL -> MaterialTheme.colorScheme.primaryContainer
    EstadoLectura.BAJO, EstadoLectura.ALTO -> MaterialTheme.colorScheme.errorContainer
    EstadoLectura.PENDIENTE_REVISION -> MaterialTheme.colorScheme.surfaceVariant
}

private fun formatearTemperatura(valor: Double): String =
    String.format(Locale("es", "CL"), "%.1f", valor)

private fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))