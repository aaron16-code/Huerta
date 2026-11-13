package cl.duoc.huertoescolar.ui.screens.historial

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
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
import cl.duoc.huertoescolar.data.EventoHistorial
import cl.duoc.huertoescolar.data.TipoEvento
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import cl.duoc.huertoescolar.ui.screens.tareas.FotoEvidencia
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListaHistorialScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: HistorialViewModel = viewModel()
) {
    val tareas by viewModel.tareas.collectAsState()
    val problemas by viewModel.problemas.collectAsState()

    val eventos = viewModel.eventos(
        tareas,
        problemas,
        usuario,
        nombreSector
    )

    PantallaHuerto(
        titulo = "Historial",
        subtitulo = "Actividades y reportes, de lo más reciente a lo más antiguo.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Filtrar por actividad",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroHistorial.entries.forEach { filtro ->
                    FilterChip(
                        selected = viewModel.filtro == filtro,
                        onClick = {
                            viewModel.cambiarFiltro(filtro)
                        },
                        label = {
                            Text(filtro.etiqueta)
                        }
                    )
                }
            }
        }

        Text(
            text = "Registros encontrados: ${eventos.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (eventos.isEmpty()) {
            TarjetaHuerto {
                Text(
                    text = "Todavía no hay registros en esta categoría",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Selecciona otro filtro o vuelve cuando haya nuevas actividades.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            CuadriculaAdaptable(
                elementos = eventos,
                anchoMinimo = 300.dp,
                maxColumnas = 2
            ) { evento ->
                TarjetaEvento(evento)
            }
        }
    }
}

@Composable
private fun TarjetaEvento(
    evento: EventoHistorial
) {
    TarjetaHuerto {
        EtiquetaEstado(
            texto = etiquetaTipo(evento.tipo),
            color = colorTipo(evento.tipo)
        )

        Text(
            text = evento.titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = formatearFechaHora(evento.fecha),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (evento.detalle.isNotBlank()) {
            Text(
                text = evento.detalle,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        evento.fotoUri?.takeIf { it.isNotBlank() }?.let { foto ->
            Text(
                text = "Evidencia fotográfica",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            FotoEvidencia(foto)
        }
    }
}

private fun etiquetaTipo(
    tipo: TipoEvento
): String = when (tipo) {
    TipoEvento.TAREA -> "Tarea"
    TipoEvento.VALIDACION -> "Validación"
    TipoEvento.PROBLEMA -> "Problema"
}

@Composable
private fun colorTipo(
    tipo: TipoEvento
): Color = when (tipo) {
    TipoEvento.TAREA ->
        MaterialTheme.colorScheme.secondaryContainer

    TipoEvento.VALIDACION ->
        MaterialTheme.colorScheme.primaryContainer

    TipoEvento.PROBLEMA ->
        MaterialTheme.colorScheme.tertiaryContainer
}

private fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale("es", "CL")
    ).format(Date(millis))