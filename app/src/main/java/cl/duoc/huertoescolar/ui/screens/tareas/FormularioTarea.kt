package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.Cultivo
import cl.duoc.huertoescolar.Sector
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

/** Formulario reutilizado al crear y editar una tarea. */
@Composable
fun FormularioTarea(
    state: TareaFormUiState,
    destinatarios: List<DestinatarioTareaUi>,
    sectores: List<Sector>,
    cultivos: List<Cultivo>,
    onTipoChange: (TipoTarea) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onInstruccionesChange: (String) -> Unit,
    onSectorChange: (Int) -> Unit,
    onCultivoChange: (Int?) -> Unit,
    onFechaChange: (String) -> Unit,
    onDestinatarioChange: (DestinatarioTareaUi) -> Unit,
    onFechaRapida: ((Int) -> Unit)? = null
) {
    TarjetaHuerto {
        Text("Tipo de tarea", style = MaterialTheme.typography.titleMedium)
        FilaOpcionesTarea {
            TipoTarea.entries.forEach { tipo ->
                FilterChip(
                    selected = state.tipo == tipo,
                    onClick = { onTipoChange(tipo) },
                    label = { Text(tipo.etiqueta) }
                )
            }
        }
        CampoValidado(
            valor = state.descripcion,
            alCambiar = onDescripcionChange,
            etiqueta = "Descripción",
            icono = Icons.Filled.Edit,
            error = state.errorDescripcion
        )
        CampoValidado(
            valor = state.instrucciones,
            alCambiar = onInstruccionesChange,
            etiqueta = "Instrucciones breves",
            icono = Icons.Filled.Info,
            error = state.errorInstrucciones,
            minLines = 2
        )
    }

    TarjetaHuerto(modifier = Modifier.animateContentSize()) {
        Text("Sector", style = MaterialTheme.typography.titleMedium)
        FilaOpcionesTarea {
            sectores.forEach { sector ->
                FilterChip(
                    selected = state.sectorId == sector.id,
                    onClick = { onSectorChange(sector.id) },
                    label = { Text(sector.nombre) }
                )
            }
        }
        MensajeErrorFormulario(state.errorSector)

        if (state.sectorId != null) {
            Text("Cultivo (opcional)", style = MaterialTheme.typography.titleMedium)
            FilaOpcionesTarea {
                FilterChip(
                    selected = state.cultivoId == null,
                    onClick = { onCultivoChange(null) },
                    label = { Text("Ninguno") }
                )
                cultivos.filter { it.sectorId == state.sectorId }.forEach { cultivo ->
                    FilterChip(
                        selected = state.cultivoId == cultivo.id,
                        onClick = { onCultivoChange(cultivo.id) },
                        label = { Text(cultivo.nombre) }
                    )
                }
            }
        }
    }

    TarjetaHuerto {
        Text("Fecha programada", style = MaterialTheme.typography.titleMedium)
        CampoValidado(
            valor = state.fechaTexto,
            alCambiar = onFechaChange,
            etiqueta = "dd/MM/aaaa",
            icono = Icons.Filled.DateRange,
            error = state.errorFecha,
            singleLine = true
        )
        onFechaRapida?.let { seleccionarFecha ->
            FilaOpcionesTarea {
                listOf(0 to "Hoy", 1 to "Mañana", 3 to "En 3 días", 7 to "En 7 días")
                    .forEach { (dias, etiqueta) ->
                        FilterChip(
                            selected = false,
                            onClick = { seleccionarFecha(dias) },
                            label = { Text(etiqueta) }
                        )
                    }
            }
        }
    }

    TarjetaHuerto {
        Text("Asignar a", style = MaterialTheme.typography.titleMedium)
        FilaOpcionesTarea {
            destinatarios.forEach { destino ->
                FilterChip(
                    selected = state.destinatario == destino,
                    onClick = { onDestinatarioChange(destino) },
                    label = { Text(destino.etiqueta) }
                )
            }
        }
        MensajeErrorFormulario(state.errorDestinatario)
    }
}

@Composable
fun MensajeErrorFormulario(texto: String?) {
    var ultimoTexto by remember { mutableStateOf("") }
    LaunchedEffect(texto) {
        if (texto != null) ultimoTexto = texto
    }
    AnimatedVisibility(
        visible = texto != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = texto ?: ultimoTexto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun FilaOpcionesTarea(contenido: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = contenido
    )
}
