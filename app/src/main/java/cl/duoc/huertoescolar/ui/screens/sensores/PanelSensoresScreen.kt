package cl.duoc.huertoescolar.ui.screens.sensores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.EstadoLectura
import cl.duoc.huertoescolar.data.LecturaSensor
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        subtitulo = "Consulta las condiciones de prueba de cada sector.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (usuario.rol != RolUsuario.ADMINISTRADOR) {
            TarjetaHuerto {
                Text(
                    text = "Acceso para docentes",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Solo docentes o talleristas autorizados pueden ver esta información.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            TarjetaHuerto {
                EtiquetaEstado(
                    texto = "Simulación académica",
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )

                Text(
                    text = "Estos valores son ficticios. No vienen de sensores reales, " +
                            "no se conectan con ningún dispositivo y la app no activa el riego.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Sectores con lecturas: ${porSector.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (porSector.isEmpty()) {
                TarjetaHuerto {
                    Text(
                        text = "No hay lecturas simuladas",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Aquí aparecerán los datos de prueba de cada sector.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                CuadriculaAdaptable(
                    elementos = porSector,
                    anchoMinimo = 300.dp,
                    maxColumnas = 2
                ) { grupo ->
                    TarjetaLectura(
                        sector = nombreSector(grupo.sectorId),
                        ultima = grupo.ultima,
                        anteriores = grupo.anteriores
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaLectura(
    sector: String,
    ultima: LecturaSensor,
    anteriores: List<LecturaSensor>
) {
    TarjetaHuerto {
        EtiquetaEstado(
            texto = ultima.estado.etiqueta,
            color = colorEstadoLectura(ultima.estado)
        )

        Text(
            text = sector,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Dato de prueba · ${ultima.origen}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MedicionSimulada(
            titulo = "Humedad del suelo",
            valor = "${ultima.humedadSuelo}%"
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        MedicionSimulada(
            titulo = "Temperatura ambiental",
            valor = "${formatearTemperatura(ultima.temperaturaAmbiente)} °C"
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        MedicionSimulada(
            titulo = "Humedad ambiental",
            valor = "${ultima.humedadAmbiente}%"
        )

        Text(
            text = "Lectura simulada del ${formatearFechaHora(ultima.fecha)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (anteriores.isNotEmpty()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = "Lecturas anteriores · Simuladas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            anteriores.forEach { lectura ->
                LineaAnterior(lectura)
            }
        }
    }
}

@Composable
private fun MedicionSimulada(
    titulo: String,
    valor: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LineaAnterior(lectura: LecturaSensor) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = formatearFechaHora(lectura.fecha),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Suelo: ${lectura.humedadSuelo}% · " +
                    "Temperatura: ${formatearTemperatura(lectura.temperaturaAmbiente)} °C · " +
                    "Aire: ${lectura.humedadAmbiente}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Estado: ${lectura.estado.etiqueta}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun colorEstadoLectura(
    estado: EstadoLectura
): Color = when (estado) {
    EstadoLectura.NORMAL ->
        MaterialTheme.colorScheme.primaryContainer

    EstadoLectura.BAJO,
    EstadoLectura.ALTO ->
        MaterialTheme.colorScheme.errorContainer

    EstadoLectura.PENDIENTE_REVISION ->
        MaterialTheme.colorScheme.surfaceVariant
}

private fun formatearTemperatura(valor: Double): String =
    String.format(Locale("es", "CL"), "%.1f", valor)

private fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale("es", "CL")
    ).format(Date(millis))