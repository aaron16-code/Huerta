package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huertoescolar.EstadoSector
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.Sector
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun SectoresScreen(
    rol: RolUsuario,
    onCrearSector: () -> Unit,
    onEditarSector: (Sector) -> Unit,
    onVerDetalle: (Sector) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaHuerto(
        titulo = "Sectores del huerto",
        subtitulo = "Espacios disponibles y responsables de cada zona.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        RepositorioHuerto.sectores.forEach { sector ->
            val estadoTexto = when (sector.estado) {
                EstadoSector.DISPONIBLE -> "Disponible"
                EstadoSector.EN_USO -> "En uso"
                EstadoSector.EN_MANTENIMIENTO -> "En mantenimiento"
                EstadoSector.FUERA_DE_USO -> "Fuera de uso"
            }

            TarjetaHuerto {
                Text(sector.nombre, style = MaterialTheme.typography.titleLarge)
                EtiquetaEstado(estadoTexto)
                Text(sector.descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Ubicación: ${sector.ubicacion}")
                Text("Responsable: ${sector.responsable}")

                BotonSecundario(
                    texto = "Ver detalle",
                    onClick = { onVerDetalle(sector) },
                )

                if (rol == RolUsuario.ADMINISTRADOR) {
                    BotonPrincipal(
                        texto = "Editar sector",
                        onClick = { onEditarSector(sector) },
                    )
                }
            }
        }

        if (rol == RolUsuario.ADMINISTRADOR) {
            BotonPrincipal("Crear nuevo sector", onCrearSector)
        }
    }
}
