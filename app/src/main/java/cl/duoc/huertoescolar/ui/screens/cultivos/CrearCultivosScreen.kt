package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun CrearCultivoScreen(
    onCultivoCreado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    cultivoViewModel: CultivoViewModel = viewModel()
) {
    PantallaHuerto(
        titulo = "Crear cultivo",
        subtitulo = "Registra una siembra y su planificación de cuidado.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            FormularioCultivo(
                uiState = cultivoViewModel.uiState,
                sectores = RepositorioHuerto.sectores,
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

            BotonPrincipal(
                texto = "Guardar cultivo",
                onClick = {
                    if (cultivoViewModel.guardarCultivo()) {
                        onCultivoCreado()
                    }
                }
            )
        }
    }
}