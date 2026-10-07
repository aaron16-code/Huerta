package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huertoescolar.EstadoCultivo
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun DetalleCultivoScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cultivo = RepositorioHuerto.cultivoSeleccionado
    val sector = RepositorioHuerto.sectores.find { it.id == cultivo?.sectorId }
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
                Text(cultivo.nombre, style = MaterialTheme.typography.titleLarge)
                EtiquetaEstado(estadoTexto)
                Text(cultivo.descripcion)
                Text("Sector: ${sector?.nombre ?: "Sin sector"}")
                Text("Fecha de siembra: ${cultivo.fechaSiembra}")
                Text("Cosecha estimada: ${cultivo.fechaCosechaEstimada}")
                Text("Riego cada ${cultivo.frecuenciaRiegoDias} días")
                Text("Observaciones: ${cultivo.observaciones}")
            } else {
                Text("No se encontró el cultivo")
            }
        }
    }
}
