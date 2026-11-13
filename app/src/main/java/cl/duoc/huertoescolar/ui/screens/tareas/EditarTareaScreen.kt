package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.TipoTarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun EditarTareaScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditarTareaViewModel = viewModel()
) {
    PantallaHuerto(
        titulo = "Editar tarea",
        subtitulo = "Modifica la actividad, fecha o asignación",
        onVolver = onVolver,
        modifier = modifier
    ) {
        when {
            usuario.rol != RolUsuario.ADMINISTRADOR ->
                TarjetaHuerto { Text("Solo docentes o talleristas autorizados pueden editar tareas.") }

            !viewModel.tareaDisponible ->
                TarjetaHuerto {
                    Text("No se encontró la tarea seleccionada.")
                    BotonPrincipal(texto = "Volver a las tareas", onClick = onVolver)
                }

            viewModel.actualizada ->
                TarjetaHuerto {
                    Text("Tarea actualizada", style = MaterialTheme.typography.titleMedium)
                    Text("Los cambios fueron guardados correctamente.")
                    BotonPrincipal(texto = "Volver a las tareas", onClick = onVolver)
                }

            else -> {
                TarjetaHuerto {
                    Text("Tipo de tarea", style = MaterialTheme.typography.titleMedium)
                    FilaEdicion {
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

                TarjetaHuerto {
                    Text("Sector", style = MaterialTheme.typography.titleMedium)
                    FilaEdicion {
                        RepositorioHuerto.sectores.forEach { sector ->
                            FilterChip(
                                selected = viewModel.sectorId == sector.id,
                                onClick = { viewModel.elegirSector(sector.id) },
                                label = { Text(sector.nombre) }
                            )
                        }
                    }
                    viewModel.errorSector?.let { ErrorEdicion(it) }

                    if (viewModel.sectorId != null) {
                        Text("Cultivo (opcional)", style = MaterialTheme.typography.titleMedium)
                        FilaEdicion {
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

                TarjetaHuerto {
                    OutlinedTextField(
                        value = viewModel.fechaTexto,
                        onValueChange = viewModel::cambiarFecha,
                        label = { Text("Fecha programada (dd/MM/aaaa)") },
                        isError = viewModel.errorFecha != null,
                        supportingText = { viewModel.errorFecha?.let { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TarjetaHuerto {
                    Text("Asignar a", style = MaterialTheme.typography.titleMedium)
                    FilaEdicion {
                        viewModel.destinatarios.forEach { destino ->
                            FilterChip(
                                selected = viewModel.destinatario == destino,
                                onClick = { viewModel.elegirDestinatario(destino) },
                                label = { Text(destino.etiqueta) }
                            )
                        }
                    }
                    viewModel.errorDestinatario?.let { ErrorEdicion(it) }
                }

                viewModel.mensajeError?.let { ErrorEdicion(it) }
                BotonPrincipal(
                    texto = "Guardar cambios",
                    onClick = { viewModel.guardar(usuario) }
                )
            }
        }
    }
}

@Composable
private fun FilaEdicion(contenido: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = contenido
    )
}

@Composable
private fun ErrorEdicion(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}
