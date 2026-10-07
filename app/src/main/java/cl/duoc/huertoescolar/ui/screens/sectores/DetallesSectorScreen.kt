package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huertoescolar.EstadoSector
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun DetalleSectorScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sector = RepositorioHuerto.sectorSeleccionado
    val estadoTexto = when (sector?.estado) {
        EstadoSector.DISPONIBLE -> "Disponible"
        EstadoSector.EN_USO -> "En uso"
        EstadoSector.EN_MANTENIMIENTO -> "En mantenimiento"
        EstadoSector.FUERA_DE_USO -> "Fuera de uso"
        null -> "Sin información"
    }

    PantallaHuerto(
        titulo = "Detalle del sector",
        subtitulo = "Información registrada para esta zona del huerto.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            if (sector != null) {
                Text(sector.nombre, style = MaterialTheme.typography.titleLarge)
                EtiquetaEstado(estadoTexto)
                Text(sector.descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Ubicación: ${sector.ubicacion}")
                Text("Responsable: ${sector.responsable}")
            } else {
                Text("No se encontró el sector")
            }
        }
    }
}
