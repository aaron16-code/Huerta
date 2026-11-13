package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.EstadoSector
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.Sector
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.DosColumnasAdaptables
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
    val esDocente = rol == RolUsuario.ADMINISTRADOR
    val sectores = RepositorioHuerto.sectores.toList()

    PantallaHuerto(
        titulo = "Sectores del huerto",
        subtitulo = "Consulta las zonas de cultivo y sus responsables.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (esDocente) {
            BotonPrincipal(
                texto = "Crear nuevo sector",
                onClick = onCrearSector
            )
        }

        Text(
            text = "Sectores registrados: ${sectores.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (sectores.isEmpty()) {
            TarjetaHuerto {
                Text(
                    text = "Todavía no hay sectores",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (esDocente) {
                        "Crea el primer sector para organizar las zonas del huerto."
                    } else {
                        "Aquí aparecerán las zonas que registre tu docente."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            CuadriculaAdaptable(
                elementos = sectores,
                anchoMinimo = 280.dp,
                maxColumnas = 2
            ) { sector ->
                TarjetaSectorAdaptable(
                    sector = sector,
                    esDocente = esDocente,
                    onVerDetalle = { onVerDetalle(sector) },
                    onEditar = { onEditarSector(sector) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaSectorAdaptable(
    sector: Sector,
    esDocente: Boolean,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit
) {
    val estadoTexto = when (sector.estado) {
        EstadoSector.DISPONIBLE -> "Disponible"
        EstadoSector.EN_USO -> "En uso"
        EstadoSector.EN_MANTENIMIENTO -> "En mantenimiento"
        EstadoSector.FUERA_DE_USO -> "Fuera de uso"
    }

    val colorEstado = when (sector.estado) {
        EstadoSector.DISPONIBLE ->
            MaterialTheme.colorScheme.primaryContainer

        EstadoSector.EN_USO ->
            MaterialTheme.colorScheme.secondaryContainer

        EstadoSector.EN_MANTENIMIENTO ->
            MaterialTheme.colorScheme.tertiaryContainer

        EstadoSector.FUERA_DE_USO ->
            MaterialTheme.colorScheme.surfaceVariant
    }

    TarjetaHuerto {
        EtiquetaEstado(
            texto = estadoTexto,
            color = colorEstado
        )

        Text(
            text = sector.nombre,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (sector.descripcion.isNotBlank()) {
            Text(
                text = sector.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DatoSector(
            etiqueta = "Ubicación",
            valor = sector.ubicacion
        )

        DatoSector(
            etiqueta = "Responsable",
            valor = sector.responsable
        )

        if (esDocente) {
            DosColumnasAdaptables(
                primero = {
                    BotonSecundario(
                        texto = "Ver detalle",
                        onClick = onVerDetalle
                    )
                },
                segundo = {
                    BotonPrincipal(
                        texto = "Editar sector",
                        onClick = onEditar
                    )
                }
            )
        } else {
            BotonSecundario(
                texto = "Ver detalle",
                onClick = onVerDetalle
            )
        }
    }
}

@Composable
private fun DatoSector(
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