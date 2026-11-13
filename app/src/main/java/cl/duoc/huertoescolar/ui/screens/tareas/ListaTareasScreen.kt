package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.EstadoTarea
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import cl.duoc.huertoescolar.ui.theme.HuertoEscolarTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListaTareasScreen(
    usuario: UsuarioPrueba,
    onTareaClick: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: TareasEstudianteViewModel = viewModel()
) {
    val todas by viewModel.tareas.collectAsState()
    val lista = viewModel.misTareas(
        todas,
        usuario.id,
        usuario.grupo
    )

    PantallaHuerto(
        titulo = "Mis tareas",
        subtitulo = "Consulta tus actividades y registra tu trabajo.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        ListaTareasContenido(
            tareas = lista,
            filtro = viewModel.filtro,
            onFiltro = viewModel::cambiarFiltro,
            onTareaClick = onTareaClick,
            nombreSector = nombreSector
        )
    }
}

@Composable
fun ListaTareasContenido(
    tareas: List<Tarea>,
    filtro: FiltroTareas,
    onFiltro: (FiltroTareas) -> Unit,
    onTareaClick: (Int) -> Unit,
    nombreSector: (Int) -> String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Filtrar por estado",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroTareas.entries.forEach { opcion ->
                    FilterChip(
                        selected = filtro == opcion,
                        onClick = { onFiltro(opcion) },
                        label = {
                            Text(opcion.etiqueta)
                        }
                    )
                }
            }
        }

        Text(
            text = "Tareas encontradas: ${tareas.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (tareas.isEmpty()) {
            TarjetaHuerto {
                Text(
                    text = "No hay tareas en esta categoría",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Selecciona otro filtro para consultar tus actividades.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            CuadriculaAdaptable(
                elementos = tareas,
                anchoMinimo = 300.dp,
                maxColumnas = 2
            ) { tarea ->
                TarjetaTarea(
                    tarea = tarea,
                    sector = nombreSector(tarea.sectorId),
                    onVerDetalle = {
                        onTareaClick(tarea.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaTarea(
    tarea: Tarea,
    sector: String,
    onVerDetalle: () -> Unit
) {
    val estado = tarea.estadoVisible()

    TarjetaHuerto {
        EtiquetaEstado(
            texto = estado.etiqueta,
            color = colorEstadoContenedor(estado)
        )

        Text(
            text = tarea.descripcion,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        DatoTareaEstudiante(
            etiqueta = "Tipo de actividad",
            valor = tarea.tipo.etiqueta
        )

        DatoTareaEstudiante(
            etiqueta = "Sector",
            valor = sector
        )

        DatoTareaEstudiante(
            etiqueta = "Fecha programada",
            valor = formatearFecha(tarea.fechaProgramada)
        )

        BotonSecundario(
            texto = "Ver instrucciones y detalle",
            onClick = onVerDetalle
        )
    }
}

@Composable
private fun DatoTareaEstudiante(
    etiqueta: String,
    valor: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = valor.ifBlank { "Sin información" },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Compartida con las otras pantallas de tareas.
@Composable
internal fun colorEstadoContenedor(
    estado: EstadoTarea
): Color {
    val objetivo = when (estado) {
        EstadoTarea.ATRASADA,
        EstadoTarea.RECHAZADA ->
            MaterialTheme.colorScheme.errorContainer

        EstadoTarea.VALIDADA ->
            MaterialTheme.colorScheme.primaryContainer

        EstadoTarea.PENDIENTE_VALIDACION ->
            MaterialTheme.colorScheme.tertiaryContainer

        else ->
            MaterialTheme.colorScheme.secondaryContainer
    }

    return animateColorAsState(
        targetValue = objetivo,
        animationSpec = tween(durationMillis = 500),
        label = "colorEstado"
    ).value
}

// Compartida con otras pantallas del proyecto.
internal fun formatearFecha(millis: Long): String =
    SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("es", "CL")
    ).format(Date(millis))

@Preview(
    name = "Celular",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Preview(
    name = "Horizontal",
    showBackground = true,
    widthDp = 800,
    heightDp = 400
)
@Preview(
    name = "Letra grande",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun ListaTareasPreview() {
    HuertoEscolarTheme {
        PantallaHuerto(
            titulo = "Mis tareas",
            subtitulo = "Vista previa",
            onVolver = {}
        ) {
            ListaTareasContenido(
                tareas = RepositorioOperacion.tareasDe(
                    2,
                    "Taller Huerto A"
                ),
                filtro = FiltroTareas.TODAS,
                onFiltro = {},
                onTareaClick = {},
                nombreSector = { "Sector $it" }
            )
        }
    }
}