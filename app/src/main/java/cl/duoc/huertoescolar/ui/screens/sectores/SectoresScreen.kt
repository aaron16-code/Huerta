package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.Sector
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun SectoresScreen(
    rol: RolUsuario,
    onCrearSector: () -> Unit,
    onEditarSector: (Sector) -> Unit,
    onVerDetalle: (Sector) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listaViewModel: ListaSectoresViewModel = viewModel()
    val uiState by listaViewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        listaViewModel.cargarSectores()
    }

    val esDocente = rol == RolUsuario.ADMINISTRADOR

    val sectores = uiState.sectores.map { dto ->
        Sector(
            id = dto.id,
            nombre = dto.nombre,
            descripcion = dto.descripcion,
            ubicacion = dto.ubicacion,
            estado = EstadoSector.valueOf(dto.estado),
            responsable = dto.responsableId?.let { "Usuario #$it" }
                ?: "Sin responsable"
        )
    }

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

        when {
            uiState.cargando -> {
                Text("Cargando sectores...")
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )

                BotonSecundario(
                    texto = "Reintentar",
                    onClick = listaViewModel::cargarSectores
                )
            }

            else -> {
                Text(
                    text = "Sectores registrados: ${sectores.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                BotonSecundario(
                    texto = "Actualizar lista",
                    onClick = listaViewModel::cargarSectores
                )

                if (sectores.isEmpty()) {
                    TarjetaHuerto {
                        Text("Todavía no hay sectores registrados.")
                    }
                } else {
                    CuadriculaAdaptable(
                        elementos = sectores,
                        anchoMinimo = 280.dp,
                        maxColumnas = 2
                    ) { sector ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TarjetaSectorAdaptable(
                                sector = sector,
                                onVerDetalle = {
                                    onVerDetalle(sector)
                                }
                            )

                            if (esDocente) {
                                BotonPrincipal(
                                    texto = "Editar sector",
                                    onClick = {
                                        onEditarSector(sector)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaSectorAdaptable(
    sector: Sector,
    onVerDetalle: () -> Unit
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

        BotonSecundario(
            texto = "Ver detalle",
            onClick = onVerDetalle
        )
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