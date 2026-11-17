package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun CrearSectorScreen(
    onSectorCreado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    sectorViewModel: SectorViewModel = viewModel()
) {
    LaunchedEffect(sectorViewModel.sectorCreado) {
        if (sectorViewModel.sectorCreado) {
            onSectorCreado()
        }
    }

    PantallaHuerto(
        titulo = "Crear sector",
        subtitulo = "Registra una nueva zona de trabajo para el huerto.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            FormularioSector(
                uiState = sectorViewModel.uiState,
                onNombreChange = {
                    sectorViewModel.nombre = it
                },
                onDescripcionChange = {
                    sectorViewModel.descripcion = it
                },
                onUbicacionChange = {
                    sectorViewModel.ubicacion = it
                }
            )

            sectorViewModel.errorGuardado?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            BotonPrincipal(
                texto = if (sectorViewModel.guardando) {
                    "Guardando..."
                } else {
                    "Guardar sector"
                },
                onClick = {
                    sectorViewModel.guardarSector()
                }
            )
        }
    }
}