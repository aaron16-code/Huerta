package cl.duoc.huertoescolar.ui.screens.cultivos

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
import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun CultivosScreen(
    rol: RolUsuario,
    onCrearCultivo: () -> Unit,
    onEditarCultivo: (Cultivo) -> Unit,
    onVerDetalle: (Cultivo) -> Unit,
    onVolver: () -> Unit
) {
    val listaViewModel: ListaCultivosViewModel = viewModel()
    val uiState by listaViewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        listaViewModel.cargarCultivos()
    }

    PantallaHuerto(
        titulo = "Cultivos",
        subtitulo = "Consulta las siembras, su estado y frecuencia de riego.",
        onVolver = onVolver
    ) {
        if (rol == RolUsuario.ADMINISTRADOR) {
            BotonPrincipal(
                texto = "Crear cultivo",
                onClick = onCrearCultivo
            )
        }
        when {
            uiState.cargando -> {
                Text("Cargando cultivos...")
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )

                BotonSecundario(
                    texto = "Reintentar",
                    onClick = listaViewModel::cargarCultivos
                )
            }

            else -> {
                Text(
                    text = "Cultivos registrados: ${uiState.cultivos.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                BotonSecundario(
                    texto = "Actualizar lista",
                    onClick = listaViewModel::cargarCultivos
                )

                if (uiState.cultivos.isEmpty()) {
                    TarjetaHuerto {
                        Text(
                            text = "Todavía no hay cultivos",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Aquí aparecerán los cultivos registrados.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    CuadriculaAdaptable(
                        elementos = uiState.cultivos,
                        anchoMinimo = 280.dp,
                        maxColumnas = 2
                    ) { cultivo ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TarjetaCultivoAdaptable(
                                cultivo = cultivo,
                                nombreSector = uiState.nombresSectores[
                                    cultivo.sectorId
                                ] ?: "Sector #${cultivo.sectorId}",
                                onVerDetalle = {
                                    onVerDetalle(cultivo)
                                }
                            )

                            if (rol == RolUsuario.ADMINISTRADOR) {
                                BotonPrincipal(
                                    texto = "Editar cultivo",
                                    onClick = {
                                        onEditarCultivo(cultivo)
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
private fun TarjetaCultivoAdaptable(
    cultivo: Cultivo,
    nombreSector: String,
    onVerDetalle: () -> Unit
) {
    val estadoTexto = when (cultivo.estado) {
        EstadoCultivo.PLANIFICADO -> "Planificado"
        EstadoCultivo.SEMBRADO -> "Sembrado"
        EstadoCultivo.EN_CRECIMIENTO -> "En crecimiento"
        EstadoCultivo.LISTO_PARA_COSECHAR -> "Listo para cosechar"
        EstadoCultivo.COSECHADO -> "Cosechado"
        EstadoCultivo.CON_PROBLEMAS -> "Con problemas"
        EstadoCultivo.FINALIZADO -> "Finalizado"
    }

    val colorEstado = when (cultivo.estado) {
        EstadoCultivo.PLANIFICADO ->
            MaterialTheme.colorScheme.secondaryContainer

        EstadoCultivo.SEMBRADO,
        EstadoCultivo.EN_CRECIMIENTO ->
            MaterialTheme.colorScheme.primaryContainer

        EstadoCultivo.LISTO_PARA_COSECHAR ->
            MaterialTheme.colorScheme.tertiaryContainer

        EstadoCultivo.COSECHADO,
        EstadoCultivo.FINALIZADO ->
            MaterialTheme.colorScheme.surfaceVariant

        EstadoCultivo.CON_PROBLEMAS ->
            MaterialTheme.colorScheme.errorContainer
    }

    TarjetaHuerto {
        EtiquetaEstado(
            texto = estadoTexto,
            color = colorEstado
        )

        Text(
            text = cultivo.nombre,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        DatoCultivo(
            etiqueta = "Sector",
            valor = nombreSector
        )

        DatoCultivo(
            etiqueta = "Frecuencia de riego",
            valor = if (cultivo.frecuenciaRiegoDias == 1) {
                "Cada día"
            } else {
                "Cada ${cultivo.frecuenciaRiegoDias} días"
            }
        )

        BotonSecundario(
            texto = "Ver detalle",
            onClick = onVerDetalle
        )
    }
}

@Composable
private fun DatoCultivo(
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