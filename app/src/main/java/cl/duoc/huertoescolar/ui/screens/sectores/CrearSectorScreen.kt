package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CrearSectorScreen(
    onSectorCreado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    sectorViewModel: SectorViewModel = viewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Crear sector",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = sectorViewModel.nombre,
            onValueChange = { sectorViewModel.nombre = it },
            label = { Text("Nombre") },
            isError = sectorViewModel.errorNombre,
            supportingText = {
                if (sectorViewModel.errorNombre) {
                    Text("El nombre es obligatorio")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sectorViewModel.descripcion,
            onValueChange = { sectorViewModel.descripcion = it },
            label = { Text("Descripción") },
            isError = sectorViewModel.errorDescripcion,
            supportingText = {
                if (sectorViewModel.errorDescripcion) {
                    Text("Ingresa al menos 5 caracteres")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sectorViewModel.ubicacion,
            onValueChange = { sectorViewModel.ubicacion = it },
            label = { Text("Ubicación") },
            isError = sectorViewModel.errorUbicacion,
            supportingText = {
                if (sectorViewModel.errorUbicacion) {
                    Text("La ubicación es obligatoria")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (sectorViewModel.validarFormulario()) {
                    onSectorCreado()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar sector")
        }

        Button(onClick = onVolver) {
            Text("Volver")
        }
    }
}