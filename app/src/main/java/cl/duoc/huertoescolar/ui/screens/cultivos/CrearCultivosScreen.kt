package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun CrearCultivoScreen(
    onCultivoCreado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    cultivoViewModel: CultivoViewModel = viewModel()
) {
    LaunchedEffect(cultivoViewModel.cultivoCreado) {
        if (cultivoViewModel.cultivoCreado) {
            onCultivoCreado()
        }
    }

    PantallaHuerto(
        titulo = "Crear cultivo",
        subtitulo = "Registra una siembra y su planificación de cuidado.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        when {
            cultivoViewModel.cargandoSectores -> {
                Text("Cargando sectores...")
            }

            cultivoViewModel.errorSectores != null -> {
                Text(
                    text = cultivoViewModel.errorSectores.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )

                BotonSecundario(
                    texto = "Reintentar",
                    onClick = cultivoViewModel::cargarSectores
                )
            }

            cultivoViewModel.sectores.isEmpty() -> {
                Text("Primero debes registrar un sector.")
            }

            else -> {
                TarjetaHuerto {
                    FormularioCultivo(
                        uiState = cultivoViewModel.uiState,
                        sectores = cultivoViewModel.sectores,
                        onNombreChange = {
                            cultivoViewModel.nombre = it
                        },
                        onDescripcionChange = {
                            cultivoViewModel.descripcion = it
                        },
                        onSectorChange = {
                            cultivoViewModel.sectorId = it
                        },
                        onFechaSiembraChange = {
                            cultivoViewModel.fechaSiembra = it
                        },
                        onFechaCosechaChange = {
                            cultivoViewModel.fechaCosecha = it
                        },
                        onFrecuenciaRiegoChange = {
                            cultivoViewModel.frecuenciaRiego = it
                        },
                        onObservacionesChange = {
                            cultivoViewModel.observaciones = it
                        }
                    )

                    cultivoViewModel.errorGuardado?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    BotonPrincipal(
                        texto = if (cultivoViewModel.guardando) {
                            "Guardando..."
                        } else {
                            "Guardar cultivo"
                        },
                        onClick = {
                            cultivoViewModel.guardarCultivo()
                        }
                    )
                }
            }
        }
    }
}