package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun DetalleCultivoScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    detalleViewModel: DetalleCultivoViewModel = viewModel()
) {
    val cultivo = detalleViewModel.cultivo

    val estadoTexto = when (cultivo?.estado) {
        EstadoCultivo.PLANIFICADO -> "Planificado"
        EstadoCultivo.SEMBRADO -> "Sembrado"
        EstadoCultivo.EN_CRECIMIENTO -> "En crecimiento"
        EstadoCultivo.LISTO_PARA_COSECHAR -> "Listo para cosechar"
        EstadoCultivo.COSECHADO -> "Cosechado"
        EstadoCultivo.CON_PROBLEMAS -> "Con problemas"
        EstadoCultivo.FINALIZADO -> "Finalizado"
        null -> "Sin información"
    }

    PantallaHuerto(
        titulo = "Detalle del cultivo",
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            if (cultivo != null) {
                Text(
                    text = cultivo.nombre,
                    style = MaterialTheme.typography.titleLarge
                )

                EtiquetaEstado(estadoTexto)
                Text(cultivo.descripcion)

                when {
                    detalleViewModel.cargandoSector -> {
                        Text("Sector: cargando...")
                    }

                    detalleViewModel.errorSector != null -> {
                        Text("Sector: #${cultivo.sectorId}")

                        Text(
                            text = detalleViewModel.errorSector.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )

                        BotonSecundario(
                            texto = "Reintentar",
                            onClick = detalleViewModel::cargarSector
                        )
                    }

                    else -> {
                        Text(
                            text = "Sector: ${
                                detalleViewModel.nombreSector
                                    ?: "#${cultivo.sectorId}"
                            }"
                        )
                    }
                }

                Text("Fecha de siembra: ${cultivo.fechaSiembra}")
                Text("Cosecha estimada: ${cultivo.fechaCosechaEstimada}")
                Text("Riego cada ${cultivo.frecuenciaRiegoDias} días")
                Text(
                    text = "Observaciones: ${
                        cultivo.observaciones.ifBlank { "Sin observaciones" }
                    }"
                )
            } else {
                Text("No se encontró el cultivo")
            }
        }
    }
}