package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun EditarCultivoScreen(
    onCultivoActualizado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    editarViewModel: EditarCultivoViewModel = viewModel()
) {
    LaunchedEffect(editarViewModel.cultivoActualizado) {
        if (editarViewModel.cultivoActualizado) {
            onCultivoActualizado()
        }
    }

    PantallaHuerto(
        titulo = "Editar cultivo",
        subtitulo = "Actualiza la planificación y el estado de la siembra.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        when {
            editarViewModel.cargandoSectores -> {
                Text("Cargando sectores...")
            }

            editarViewModel.errorSectores != null -> {
                Text(
                    text = editarViewModel.errorSectores.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )

                BotonSecundario(
                    texto = "Reintentar",
                    onClick = editarViewModel::cargarSectores
                )
            }

            editarViewModel.sectores.isEmpty() -> {
                Text("No hay sectores disponibles para asignar al cultivo.")
            }

            else -> {
                TarjetaHuerto {
                    FormularioCultivo(
                        uiState = editarViewModel.uiState,
                        sectores = editarViewModel.sectores,
                        onNombreChange = {
                            editarViewModel.nombre = it
                        },
                        onDescripcionChange = {
                            editarViewModel.descripcion = it
                        },
                        onSectorChange = {
                            editarViewModel.sectorId = it
                        },
                        onFechaSiembraChange = {
                            editarViewModel.fechaSiembra = it
                        },
                        onFechaCosechaChange = {
                            editarViewModel.fechaCosecha = it
                        },
                        onFrecuenciaRiegoChange = {
                            editarViewModel.frecuenciaRiego = it
                        },
                        onObservacionesChange = {
                            editarViewModel.observaciones = it
                        },
                        etiquetaObservaciones = "Observaciones",
                        mostrarAyudaFrecuencia = false
                    )

                    Text(
                        text = "Estado del cultivo",
                        style = MaterialTheme.typography.titleMedium
                    )

                    EstadoCultivo.entries.forEach { estado ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = editarViewModel.estado == estado,
                                enabled = !editarViewModel.guardando,
                                onClick = {
                                    editarViewModel.estado = estado
                                }
                            )

                            Text(estado.textoVisible())
                        }
                    }

                    editarViewModel.errorGuardado?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    BotonPrincipal(
                        texto = if (editarViewModel.guardando) {
                            "Guardando..."
                        } else {
                            "Guardar cambios"
                        },
                        onClick = {
                            editarViewModel.guardarCambios()
                        }
                    )
                }
            }
        }
    }
}

private fun EstadoCultivo.textoVisible(): String = when (this) {
    EstadoCultivo.PLANIFICADO -> "Planificado"
    EstadoCultivo.SEMBRADO -> "Sembrado"
    EstadoCultivo.EN_CRECIMIENTO -> "En crecimiento"
    EstadoCultivo.LISTO_PARA_COSECHAR -> "Listo para cosechar"
    EstadoCultivo.COSECHADO -> "Cosechado"
    EstadoCultivo.CON_PROBLEMAS -> "Con problemas"
    EstadoCultivo.FINALIZADO -> "Finalizado"
}