package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FormularioSector(
    uiState: SectorFormUiState,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onUbicacionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = uiState.nombre,
        onValueChange = onNombreChange,
        label = { Text("Nombre") },
        isError = uiState.errorNombre,
        supportingText = {
            if (uiState.errorNombre) {
                Text("El nombre es obligatorio")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.descripcion,
        onValueChange = onDescripcionChange,
        label = { Text("Descripción") },
        isError = uiState.errorDescripcion,
        supportingText = {
            if (uiState.errorDescripcion) {
                Text("Ingresa al menos 5 caracteres")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.ubicacion,
        onValueChange = onUbicacionChange,
        label = { Text("Ubicación") },
        isError = uiState.errorUbicacion,
        supportingText = {
            if (uiState.errorUbicacion) {
                Text("La ubicación es obligatoria")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}