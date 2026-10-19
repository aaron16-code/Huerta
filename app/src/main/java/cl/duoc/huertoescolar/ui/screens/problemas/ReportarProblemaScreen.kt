package cl.duoc.huertoescolar.ui.screens.problemas

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.data.TipoProblema
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import cl.duoc.huertoescolar.ui.screens.tareas.CampoValidado
import cl.duoc.huertoescolar.ui.screens.tareas.SelectorFotoGaleria

/** Formulario del estudiante para reportar un problema del huerto. */
@Composable
fun ReportarProblemaScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportarProblemaViewModel = viewModel()
) {
    PantallaHuerto(
        titulo = "Reportar un problema",
        subtitulo = "Cuéntanos qué viste en el huerto",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (viewModel.enviado) {
            TarjetaHuerto {
                Text("Problema reportado.", style = MaterialTheme.typography.titleMedium)
                Text("Un docente lo revisará y podrás ver su estado en la lista de problemas.")
                BotonPrincipal(texto = "Volver a problemas", onClick = onVolver)
            }
        } else {
            TarjetaHuerto(modifier = Modifier.animateContentSize()) {
                Text("¿En qué sector?", style = MaterialTheme.typography.titleMedium)
                FilaOpciones {
                    RepositorioHuerto.sectores.forEach { sector ->
                        FilterChip(
                            selected = viewModel.sectorId == sector.id,
                            onClick = { viewModel.elegirSector(sector.id) },
                            label = { Text(sector.nombre) }
                        )
                    }
                }
                MensajeAnimado(viewModel.errorSector, esError = true)

                if (viewModel.sectorId != null) {
                    Text("¿Qué cultivo? (opcional)", style = MaterialTheme.typography.titleMedium)
                    FilaOpciones {
                        FilterChip(
                            selected = viewModel.cultivoId == null,
                            onClick = { viewModel.elegirCultivo(null) },
                            label = { Text("Ninguno") }
                        )
                        RepositorioHuerto.cultivos
                            .filter { it.sectorId == viewModel.sectorId }
                            .forEach { cultivo ->
                                FilterChip(
                                    selected = viewModel.cultivoId == cultivo.id,
                                    onClick = { viewModel.elegirCultivo(cultivo.id) },
                                    label = { Text(cultivo.nombre) }
                                )
                            }
                    }
                }

                Text("Tipo de problema", style = MaterialTheme.typography.titleMedium)
                FilaOpciones {
                    TipoProblema.entries.forEach { tipo ->
                        FilterChip(
                            selected = viewModel.tipo == tipo,
                            onClick = { viewModel.elegirTipo(tipo) },
                            label = { Text(tipo.etiqueta) }
                        )
                    }
                }

                CampoValidado(
                    valor = viewModel.descripcion,
                    alCambiar = viewModel::cambiarDescripcion,
                    etiqueta = "Describe brevemente el problema",
                    icono = Icons.Filled.Edit,
                    error = viewModel.errorDescripcion,
                    minLines = 3
                )

            }

            SelectorFotoGaleria(
                fotoUri = viewModel.fotoUri,
                onFotoSeleccionada = viewModel::elegirFoto,
                onQuitarFoto = viewModel::quitarFoto
            )

            BotonPrincipal(texto = "Enviar reporte", onClick = viewModel::pedirConfirmacion)

            MensajeAnimado(viewModel.mensaje, esError = viewModel.mensajeEsError)
        }

        if (viewModel.mostrarConfirmacion) {
            AlertDialog(
                onDismissRequest = viewModel::cancelarConfirmacion,
                title = { Text("¿Enviar el reporte?") },
                text = { Text("Un docente revisará el problema que describiste.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.enviar(usuario) }) { Text("Sí, enviar") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::cancelarConfirmacion) { Text("Cancelar") }
                }
            )
        }
    }
}

/** Fila de opciones que se desplaza hacia el lado si no caben en la pantalla. */
@Composable
private fun FilaOpciones(contenido: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = contenido
    )
}

/** Mensaje que aparece y desaparece con una animación suave. */
@Composable
private fun MensajeAnimado(texto: String?, esError: Boolean) {
    var ultimo by remember { mutableStateOf("") }
    LaunchedEffect(texto) {
        if (texto != null) ultimo = texto
    }
    AnimatedVisibility(
        visible = texto != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = texto ?: ultimo,
            style = MaterialTheme.typography.bodyMedium,
            color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}