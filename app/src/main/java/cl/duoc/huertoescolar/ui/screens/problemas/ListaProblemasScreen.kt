package cl.duoc.huertoescolar.ui.screens.problemas

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.EstadoReporte
import cl.duoc.huertoescolar.data.Problema
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import cl.duoc.huertoescolar.ui.screens.tareas.FotoEvidencia
import cl.duoc.huertoescolar.ui.screens.tareas.formatearFecha

@Composable
fun ListaProblemasScreen(
    usuario: UsuarioPrueba,
    onReportar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    nombreCultivo: (Int?) -> String = {
        if (it == null) "Sin cultivo asociado" else "Cultivo $it"
    },
    viewModel: ProblemasViewModel = viewModel()
) {
    val todos by viewModel.problemas.collectAsState()
    val lista = viewModel.visibles(todos, usuario)
    val esAdmin = usuario.rol == RolUsuario.ADMINISTRADOR

    PantallaHuerto(
        titulo = if (esAdmin) "Problemas reportados" else "Mis reportes",
        subtitulo = if (esAdmin) {
            "Revisa las evidencias y actualiza el seguimiento."
        } else {
            "Informa lo que ocurre en el huerto y consulta las respuestas."
        },
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (!esAdmin) {
            BotonPrincipal(
                texto = "Reportar un problema",
                onClick = onReportar
            )
        }

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
            text = "Reportes: ${lista.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (lista.isEmpty()) {
            TarjetaHuerto {
                Text(
                    text = "No hay problemas reportados",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (esAdmin) {
                        "Aquí aparecerán los reportes enviados por los estudiantes."
                    } else {
                        "Cuando reportes un problema, podrás consultar su seguimiento aquí."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            CuadriculaAdaptable(
                elementos = lista,
                anchoMinimo = 340.dp,
                maxColumnas = 2
            ) { problema ->
                TarjetaProblema(
                    problema = problema,
                    sector = nombreSector(problema.sectorId),
                    cultivo = nombreCultivo(problema.cultivoId),
                    esAdmin = esAdmin,
                    comentario = viewModel.comentarioDe(problema.id),
                    onComentario = {
                        viewModel.cambiarComentario(problema.id, it)
                    },
                    onCambiarEstado = { nuevo ->
                        viewModel.cambiarEstado(problema, nuevo, usuario)
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaProblema(
    problema: Problema,
    sector: String,
    cultivo: String,
    esAdmin: Boolean,
    comentario: String,
    onComentario: (String) -> Unit,
    onCambiarEstado: (EstadoReporte) -> Unit
) {
    TarjetaHuerto {
        EtiquetaEstado(
            texto = problema.estado.etiqueta,
            color = colorEstadoReporte(problema.estado)
        )

        Text(
            text = problema.tipo.etiqueta,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = problema.descripcion,
            style = MaterialTheme.typography.bodyLarge
        )

        DatoProblema(
            etiqueta = "Sector",
            valor = sector
        )

        DatoProblema(
            etiqueta = "Cultivo",
            valor = cultivo
        )

        DatoProblema(
            etiqueta = "Fecha del reporte",
            valor = formatearFecha(problema.fechaReporte)
        )

        Text(
            text = "Evidencia fotográfica",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        FotoEvidencia(problema.fotoUri)

        if (problema.comentarioDocente.isNotBlank()) {
            DatoProblema(
                etiqueta = "Respuesta del docente",
                valor = problema.comentarioDocente
            )
        }

        problema.fechaCierre?.let { fecha ->
            DatoProblema(
                etiqueta = "Fecha de atención o cierre",
                valor = formatearFecha(fecha)
            )
        }

        if (esAdmin && problema.estado != EstadoReporte.CERRADO) {
            Text(
                text = "Actualizar seguimiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = comentario,
                onValueChange = onComentario,
                label = {
                    Text("Comentario de seguimiento")
                },
                supportingText = {
                    Text("Escribe la respuesta antes de seleccionar el nuevo estado.")
                },
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            // Conserva las mismas opciones de transición del archivo original.
            EstadoReporte.entries
                .filter {
                    it != problema.estado &&
                            it != EstadoReporte.INFORMADO
                }
                .forEach { nuevo ->
                    BotonSecundario(
                        texto = when (nuevo) {
                            EstadoReporte.EN_REVISION -> "Marcar en revisión"
                            EstadoReporte.ATENDIDO -> "Marcar como atendido"
                            EstadoReporte.CERRADO -> "Cerrar reporte"
                            EstadoReporte.INFORMADO -> "Marcar como informado"
                        },
                        onClick = {
                            onCambiarEstado(nuevo)
                        }
                    )
                }
        }
    }
}

@Composable
private fun DatoProblema(
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

@Composable
private fun colorEstadoReporte(
    estado: EstadoReporte
): Color = when (estado) {
    EstadoReporte.INFORMADO ->
        MaterialTheme.colorScheme.tertiaryContainer

    EstadoReporte.EN_REVISION ->
        MaterialTheme.colorScheme.secondaryContainer

    EstadoReporte.ATENDIDO ->
        MaterialTheme.colorScheme.primaryContainer

    EstadoReporte.CERRADO ->
        MaterialTheme.colorScheme.surfaceVariant
}