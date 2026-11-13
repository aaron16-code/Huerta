package cl.duoc.huertoescolar.ui.screens.alertas

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun PanelAlertasScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: AlertasViewModel = viewModel()
) {
    val tareas by viewModel.tareas.collectAsState()
    val problemas by viewModel.problemas.collectAsState()
    val lecturas by viewModel.lecturas.collectAsState()

    val secciones = viewModel.secciones(
        tareas,
        problemas,
        lecturas,
        usuario,
        nombreSector
    )

    val totalAvisos = secciones.sumOf { it.lineas.size }

    PantallaHuerto(
        titulo = "Alertas del huerto",
        subtitulo = "Recordatorios informativos. La app no activa ningún dispositivo.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        Text(
            text = if (totalAvisos == 1) {
                "1 aviso disponible"
            } else {
                "$totalAvisos avisos disponibles"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (secciones.isEmpty()) {
            TarjetaHuerto {
                Text(
                    text = "No hay avisos disponibles",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Aquí aparecerán los recordatorios del huerto.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            CuadriculaAdaptable(
                elementos = secciones,
                anchoMinimo = 300.dp,
                maxColumnas = 2
            ) { seccion ->
                TarjetaHuerto {
                    EtiquetaEstado(
                        texto = when (seccion.lineas.size) {
                            0 -> "Sin avisos"
                            1 -> "1 aviso"
                            else -> "${seccion.lineas.size} avisos"
                        },
                        color = if (seccion.lineas.isEmpty()) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                    )

                    Text(
                        text = seccion.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (seccion.lineas.isEmpty()) {
                        Text(
                            text = seccion.textoVacio,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            seccion.lineas.forEachIndexed { indice, linea ->
                                if (indice > 0) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }

                                Text(
                                    text = linea,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}