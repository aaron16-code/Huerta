package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.DosColumnasAdaptables
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun PanelValidacionScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: PanelValidacionViewModel = viewModel()
) {
    val todas by viewModel.tareas.collectAsState()
    val pendientes = viewModel.pendientes(todas)
    val esDocente = usuario.rol == RolUsuario.ADMINISTRADOR

    PantallaHuerto(
        titulo = "Validar tareas",
        subtitulo = "Revisa las evidencias y responde a las entregas.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (!esDocente) {
            TarjetaHuerto {
                Text(
                    text = "Acceso para docentes",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Solo docentes o talleristas autorizados pueden validar tareas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            viewModel.mensaje?.let { texto ->
                TarjetaHuerto {
                    Text(
                        text = texto,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (viewModel.mensajeEsError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            Text(
                text = "Entregas pendientes: ${pendientes.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (pendientes.isEmpty()) {
                TarjetaHuerto {
                    Text(
                        text = "No hay entregas pendientes",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Aquí aparecerán las tareas que los estudiantes envíen a revisión.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                CuadriculaAdaptable(
                    elementos = pendientes,
                    anchoMinimo = 340.dp,
                    maxColumnas = 2
                ) { tarea ->
                    TarjetaPendiente(
                        tarea = tarea,
                        sector = nombreSector(tarea.sectorId),
                        comentario = viewModel.comentarioDe(tarea.id),
                        onComentario = {
                            viewModel.cambiarComentario(tarea.id, it)
                        },
                        onConfirmar = {
                            viewModel.resolver(tarea, true, usuario)
                        },
                        onRechazar = {
                            viewModel.resolver(tarea, false, usuario)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaPendiente(
    tarea: Tarea,
    sector: String,
    comentario: String,
    onComentario: (String) -> Unit,
    onConfirmar: () -> Unit,
    onRechazar: () -> Unit
) {
    val estudiante = UsuariosPrueba.todos.find {
        it.id == tarea.realizadaPorId
    }

    val nombreEstudiante = estudiante?.nombre
        ?: tarea.realizadaPorId?.let {
            "Estudiante ficticio n.° $it"
        }
        ?: "Sin información"

    TarjetaHuerto {
        EtiquetaEstado(
            texto = tarea.estado.etiqueta,
            color = colorEstadoContenedor(tarea.estado)
        )

        Text(
            text = tarea.descripcion,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        DatoEntrega(
            etiqueta = "Tipo de actividad",
            valor = tarea.tipo.etiqueta
        )

        DatoEntrega(
            etiqueta = "Sector",
            valor = sector
        )

        DatoEntrega(
            etiqueta = "Enviada por",
            valor = nombreEstudiante
        )

        tarea.fechaRealizacion?.let { fecha ->
            DatoEntrega(
                etiqueta = "Fecha de realización",
                valor = formatearFecha(fecha)
            )
        }

        DatoEntrega(
            etiqueta = "Comentario del estudiante",
            valor = tarea.comentarioEstudiante.ifBlank {
                "Sin comentario"
            }
        )

        Text(
            text = "Evidencia fotográfica",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        FotoEvidencia(tarea.fotoUri)

        OutlinedTextField(
            value = comentario,
            onValueChange = onComentario,
            label = {
                Text("Comentario para el estudiante")
            },
            supportingText = {
                Text("Debes escribir un comentario para rechazar la tarea.")
            },
            minLines = 3,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        DosColumnasAdaptables(
            primero = {
                BotonPrincipal(
                    texto = "Confirmar tarea",
                    onClick = onConfirmar
                )
            },
            segundo = {
                BotonSecundario(
                    texto = "Rechazar tarea",
                    onClick = onRechazar
                )
            }
        )
    }
}

@Composable
private fun DatoEntrega(
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