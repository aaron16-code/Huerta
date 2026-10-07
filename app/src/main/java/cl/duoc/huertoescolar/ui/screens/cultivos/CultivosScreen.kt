package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.Cultivo
import cl.duoc.huertoescolar.EstadoCultivo
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun CultivosScreen(
    rol: RolUsuario,
    onCrearCultivo: () -> Unit,
    onEditarCultivo: (Cultivo) -> Unit,
    onVerDetalle: (Cultivo) -> Unit,
    onVolver: () -> Unit
) {
    PantallaHuerto(
        titulo = "Cultivos",
        subtitulo = "Siembras registradas en los sectores del huerto",
        onVolver = onVolver
    ) {
        RepositorioHuerto.cultivos.forEach { cultivo ->
            val sector = RepositorioHuerto.sectores.find { it.id == cultivo.sectorId }
            val estadoTexto = when (cultivo.estado) {
                EstadoCultivo.PLANIFICADO -> "Planificado"
                EstadoCultivo.SEMBRADO -> "Sembrado"
                EstadoCultivo.EN_CRECIMIENTO -> "En crecimiento"
                EstadoCultivo.LISTO_PARA_COSECHAR -> "Listo para cosechar"
                EstadoCultivo.COSECHADO -> "Cosechado"
                EstadoCultivo.CON_PROBLEMAS -> "Con problemas"
                EstadoCultivo.FINALIZADO -> "Finalizado"
            }

            TarjetaHuerto {
                Text(cultivo.nombre, style = MaterialTheme.typography.titleMedium)
                EtiquetaEstado(estadoTexto)
                Text("Sector: ${sector?.nombre ?: "Sin sector"}")
                Text("Riego cada ${cultivo.frecuenciaRiegoDias} días")

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonSecundario(
                        texto = "Ver detalle",
                        onClick = { onVerDetalle(cultivo) },
                    )

                    if (rol == RolUsuario.ADMINISTRADOR) {
                        BotonPrincipal(
                            texto = "Editar cultivo",
                            onClick = { onEditarCultivo(cultivo) },
                        )
                    }
                }
            }
        }

        if (rol == RolUsuario.ADMINISTRADOR) {
            BotonPrincipal("Crear cultivo", onCrearCultivo)
        }
    }
}
