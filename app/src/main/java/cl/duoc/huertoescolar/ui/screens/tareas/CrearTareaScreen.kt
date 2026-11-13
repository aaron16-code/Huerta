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
                CampoValidado(
                    valor = viewModel.descripcion,
                    alCambiar = viewModel::cambiarDescripcion,
                    etiqueta = "Descripción",
                    icono = Icons.Filled.Edit,
                    error = viewModel.errorDescripcion
                )
                CampoValidado(
                    valor = viewModel.instrucciones,
                    alCambiar = viewModel::cambiarInstrucciones,
                    etiqueta = "Instrucciones breves",
                    icono = Icons.Filled.Info,
                    error = viewModel.errorInstrucciones,
                    minLines = 2
                )
            }

            // 2) Dónde (la tarjeta se expande con animación al elegir un sector)
            TarjetaHuerto(modifier = Modifier.animateContentSize()) {
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
                MensajeError(viewModel.errorSector)

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
                CampoValidado(
                    valor = viewModel.fechaTexto,
                    alCambiar = viewModel::cambiarFecha,
                    etiqueta = "dd/MM/aaaa",
                    icono = Icons.Filled.DateRange,
                    error = viewModel.errorFecha,
                    singleLine = true
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
                MensajeError(viewModel.errorDestinatario)
            }

            BotonPrincipal(texto = "Crear tarea", onClick = viewModel::pedirConfirmacion)

            MensajeError(viewModel.mensajeError)
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

/** Mensaje de error que aparece y desaparece con una animación suave. */
@Composable
private fun MensajeError(texto: String?) {
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
            color = MaterialTheme.colorScheme.error
        )
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