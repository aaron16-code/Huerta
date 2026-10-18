package cl.duoc.huertoescolar.ui.screens.tareas

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
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

/** Formulario del docente o tallerista para crear una tarea y asignarla. */
@Composable
fun CrearTareaScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CrearTareaViewModel = viewModel()
) {
    PantallaHuerto(
        titulo = "Crear tarea",
        subtitulo = "Define la actividad y asígnala a un estudiante o grupo",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (usuario.rol != RolUsuario.ADMINISTRADOR) {
            TarjetaHuerto { Text("Solo docentes o talleristas autorizados pueden crear tareas.") }
        } else if (viewModel.creada) {
            TarjetaHuerto {
                Text("Tarea creada.", style = MaterialTheme.typography.titleMedium)
                Text("Ya aparece en la lista del estudiante o grupo al que la asignaste.")
                BotonPrincipal(texto = "Volver a las tareas", onClick = onVolver)
                BotonSecundario(texto = "Crear otra tarea", onClick = viewModel::crearOtra)
            }
        } else {
            // 1) Qué hay que hacer
            TarjetaHuerto {
                Text("Tipo de tarea", style = MaterialTheme.typography.titleMedium)
                FilaOpciones {
                    TipoTarea.entries.forEach { tipo ->
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
                    label = { Text("Descripción") },
                    isError = viewModel.errorDescripcion != null,
                    supportingText = { viewModel.errorDescripcion?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.instrucciones,
                    onValueChange = viewModel::cambiarInstrucciones,
                    label = { Text("Instrucciones breves") },
                    isError = viewModel.errorInstrucciones != null,
                    supportingText = { viewModel.errorInstrucciones?.let { Text(it) } },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2) Dónde
            TarjetaHuerto {
                Text("Sector", style = MaterialTheme.typography.titleMedium)
                FilaOpciones {
                    RepositorioHuerto.sectores.forEach { sector ->
                        FilterChip(
                            selected = viewModel.sectorId == sector.id,
                            onClick = { viewModel.elegirSector(sector.id) },
                            label = { Text(sector.nombre) }
                        )
                    }
                }
                viewModel.errorSector?.let { MensajeError(it) }

                if (viewModel.sectorId != null) {
                    Text("Cultivo (opcional)", style = MaterialTheme.typography.titleMedium)
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
            }

            // 3) Cuándo
            TarjetaHuerto {
                Text("Fecha programada", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = viewModel.fechaTexto,
                    onValueChange = viewModel::cambiarFecha,
                    label = { Text("dd/MM/aaaa") },
                    isError = viewModel.errorFecha != null,
                    supportingText = { viewModel.errorFecha?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FilaOpciones {
                    FilterChip(selected = false, onClick = { viewModel.elegirFechaRapida(0) }, label = { Text("Hoy") })
                    FilterChip(selected = false, onClick = { viewModel.elegirFechaRapida(1) }, label = { Text("Mañana") })
                    FilterChip(selected = false, onClick = { viewModel.elegirFechaRapida(3) }, label = { Text("En 3 días") })
                    FilterChip(selected = false, onClick = { viewModel.elegirFechaRapida(7) }, label = { Text("En 7 días") })
                }
            }

            // 4) Quién
            TarjetaHuerto {
                Text("Asignar a", style = MaterialTheme.typography.titleMedium)
                FilaOpciones {
                    viewModel.destinatarios.forEach { destino ->
                        FilterChip(
                            selected = viewModel.destinatario == destino,
                            onClick = { viewModel.elegirDestinatario(destino) },
                            label = { Text(destino.etiqueta) }
                        )
                    }
                }
                viewModel.errorDestinatario?.let { MensajeError(it) }
            }

            BotonPrincipal(texto = "Crear tarea", onClick = viewModel::pedirConfirmacion)

            viewModel.mensajeError?.let { MensajeError(it) }
        }

        if (viewModel.mostrarConfirmacion) {
            AlertDialog(
                onDismissRequest = viewModel::cancelarConfirmacion,
                title = { Text("¿Crear la tarea?") },
                text = { Text("Se asignará a ${viewModel.destinatario?.etiqueta ?: "el destinatario elegido"}.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.crear(usuario) }) { Text("Sí, crear") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::cancelarConfirmacion) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun MensajeError(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
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