package cl.duoc.huertoescolar.ui.screens.problemas

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.EstadoReporte
import cl.duoc.huertoescolar.data.Problema
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import cl.duoc.huertoescolar.ui.screens.tareas.FotoEvidencia
import cl.duoc.huertoescolar.ui.screens.tareas.formatearFecha

/**
 * Lista de problemas reportados.
 * Estudiante: ve los suyos y puede reportar uno nuevo. Docente: ve todos y les da seguimiento.
 */
@Composable
fun ListaProblemasScreen(
    usuario: UsuarioPrueba,
    onReportar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    nombreCultivo: (Int?) -> String = { if (it == null) "Sin cultivo asociado" else "Cultivo $it" },
    viewModel: ProblemasViewModel = viewModel()
) {
    val todos by viewModel.problemas.collectAsState()
    val lista = viewModel.visibles(todos, usuario)
    val esAdmin = usuario.rol == RolUsuario.ADMINISTRADOR

    PantallaHuerto(
        titulo = "Problemas",
        subtitulo = if (esAdmin) "Da seguimiento a los problemas reportados"
        else "Reporta lo que ves en el huerto y revisa su estado",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (!esAdmin) {
            BotonPrincipal(texto = "Reportar un problema", onClick = onReportar)
        }

        viewModel.mensaje?.let { texto ->
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                color = if (viewModel.mensajeEsError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }

        if (lista.isEmpty()) {
            TarjetaHuerto { Text("No hay problemas reportados.") }
        } else {
            lista.forEach { problema ->
                TarjetaProblema(
                    problema = problema,
                    sector = nombreSector(problema.sectorId),
                    cultivo = nombreCultivo(problema.cultivoId),
                    esAdmin = esAdmin,
                    comentario = viewModel.comentarioDe(problema.id),
                    onComentario = { viewModel.cambiarComentario(problema.id, it) },
                    onCambiarEstado = { nuevo -> viewModel.cambiarEstado(problema, nuevo, usuario) }
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
        Text(problema.tipo.etiqueta, style = MaterialTheme.typography.titleMedium)
        EtiquetaEstado(problema.estado.etiqueta, colorEstadoReporte(problema.estado))
        Text("$sector · $cultivo")
        Text(problema.descripcion)
        Text("Reportado el ${formatearFecha(problema.fechaReporte)}")
        FotoEvidencia(problema.fotoUri)

        if (problema.comentarioDocente.isNotBlank()) {
            Text(
                "Comentario del docente: ${problema.comentarioDocente}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        problema.fechaCierre?.let { Text("Atendido o cerrado el ${formatearFecha(it)}") }

        if (esAdmin && problema.estado != EstadoReporte.CERRADO) {
            OutlinedTextField(
                value = comentario,
                onValueChange = onComentario,
                label = { Text("Comentario de seguimiento") },
                modifier = Modifier.fillMaxWidth()
            )
            EstadoReporte.entries
                .filter { it != problema.estado && it != EstadoReporte.INFORMADO }
                .forEach { nuevo ->
                    BotonSecundario(
                        texto = "Pasar a: ${nuevo.etiqueta}",
                        onClick = { onCambiarEstado(nuevo) }
                    )
                }
        }
    }
}

@Composable
private fun colorEstadoReporte(estado: EstadoReporte): Color = when (estado) {
    EstadoReporte.INFORMADO -> MaterialTheme.colorScheme.tertiaryContainer
    EstadoReporte.EN_REVISION -> MaterialTheme.colorScheme.secondaryContainer
    EstadoReporte.ATENDIDO -> MaterialTheme.colorScheme.primaryContainer
    EstadoReporte.CERRADO -> MaterialTheme.colorScheme.surfaceVariant
}