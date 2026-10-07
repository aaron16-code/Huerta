package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    PantallaHuerto(
        titulo = "Crear sector",
        subtitulo = "Registra una nueva zona de trabajo para el huerto.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            OutlinedTextField(
                value = sectorViewModel.nombre,
                onValueChange = { sectorViewModel.nombre = it },
                label = { Text("Nombre") },
                isError = sectorViewModel.errorNombre,
                supportingText = {
                    if (sectorViewModel.errorNombre) Text("El nombre es obligatorio")
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sectorViewModel.descripcion,
                onValueChange = { sectorViewModel.descripcion = it },
                label = { Text("Descripción") },
                isError = sectorViewModel.errorDescripcion,
                supportingText = {
                    if (sectorViewModel.errorDescripcion) Text("Ingresa al menos 5 caracteres")
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sectorViewModel.ubicacion,
                onValueChange = { sectorViewModel.ubicacion = it },
                label = { Text("Ubicación") },
                isError = sectorViewModel.errorUbicacion,
                supportingText = {
                    if (sectorViewModel.errorUbicacion) Text("La ubicación es obligatoria")
                },
                modifier = Modifier.fillMaxWidth()
            )
            BotonPrincipal(
                texto = "Guardar sector",
                onClick = {
                    if (sectorViewModel.guardarSector()) onSectorCreado()
                }
            )
        }
    }
}
