package cl.duoc.huertoescolar.ui.screens.problemas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.data.TipoProblema
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
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
            TarjetaHuerto {
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

                OutlinedTextField(
                    value = viewModel.descripcion,
                    onValueChange = viewModel::cambiarDescripcion,
                    label = { Text("Describe brevemente el problema") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

            }

            SelectorFotoGaleria(
                fotoUri = viewModel.fotoUri,
                onFotoSeleccionada = viewModel::elegirFoto,
                onQuitarFoto = viewModel::quitarFoto
            )

            BotonPrincipal(texto = "Enviar reporte", onClick = viewModel::pedirConfirmacion)

            viewModel.mensaje?.let { texto ->
                Text(
                    text = texto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (viewModel.mensajeEsError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
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