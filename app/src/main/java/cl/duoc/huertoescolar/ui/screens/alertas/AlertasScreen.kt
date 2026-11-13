package cl.duoc.huertoescolar.ui.screens.alertas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

private data class AvisoDemostracion(
    val titulo: String,
    val descripcion: String,
    val importante: Boolean,
    val sector: String? = null,
    val cultivo: String? = null
)

@Composable
fun AlertasScreen(
    onVolver: () -> Unit
) {
    val avisos = listOf(
        AvisoDemostracion(
            titulo = "Riego pendiente",
            descripcion = "El cultivo necesita riego durante el día de hoy.",
            importante = true,
            sector = "Bancal 1",
            cultivo = "Lechugas"
        ),
        AvisoDemostracion(
            titulo = "Revisión de cultivo",
            descripcion = "Se recomienda revisar el estado de las hojas.",
            importante = false
        )
    )

    PantallaHuerto(
        titulo = "Alertas",
        subtitulo = "Consulta los avisos sobre el cuidado del huerto.",
        onVolver = onVolver
    ) {
        Text(
            text = "Avisos de demostración: ${avisos.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        CuadriculaAdaptable(
            elementos = avisos,
            anchoMinimo = 280.dp,
            maxColumnas = 2
        ) { aviso ->
            TarjetaAviso(aviso)
        }
    }
}

@Composable
private fun TarjetaAviso(
    aviso: AvisoDemostracion
) {
    TarjetaHuerto {
        EtiquetaEstado(
            texto = if (aviso.importante) {
                "Importante"
            } else {
                "Informativa"
            },
            color = if (aviso.importante) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )

        Text(
            text = aviso.titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = aviso.descripcion,
            style = MaterialTheme.typography.bodyLarge
        )

        aviso.sector?.let { sector ->
            DatoAviso(
                etiqueta = "Sector",
                valor = sector
            )
        }

        aviso.cultivo?.let { cultivo ->
            DatoAviso(
                etiqueta = "Cultivo",
                valor = cultivo
            )
        }
    }
}

@Composable
private fun DatoAviso(
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
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}