package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import cl.duoc.huertoescolar.domain.model.Sector

@Composable
fun FormularioCultivo(
    uiState: CultivoFormUiState,
    sectores: List<Sector>,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onSectorChange: (Int) -> Unit,
    onFechaSiembraChange: (String) -> Unit,
    onFechaCosechaChange: (String) -> Unit,
    onFrecuenciaRiegoChange: (String) -> Unit,
    onObservacionesChange: (String) -> Unit,
    etiquetaObservaciones: String = "Observaciones (opcional)",
    mostrarAyudaFrecuencia: Boolean = true
) {
    OutlinedTextField(
        value = uiState.nombre,
        onValueChange = onNombreChange,
        label = { Text("Nombre del cultivo") },
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

    Text(
        text = "Sector",
        style = MaterialTheme.typography.titleMedium
    )

    sectores.forEach { sector ->
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = uiState.sectorId == sector.id,
                onClick = { onSectorChange(sector.id) }
            )

            Text(sector.nombre)
        }
    }

    if (uiState.errorSector) {
        Text(
            text = "Selecciona un sector",
            color = MaterialTheme.colorScheme.error
        )
    }

    OutlinedTextField(
        value = uiState.fechaSiembra,
        onValueChange = onFechaSiembraChange,
        label = { Text("Fecha de siembra") },
        placeholder = { Text("DD/MM/AAAA") },
        isError = uiState.errorFechaSiembra,
        supportingText = {
            if (uiState.errorFechaSiembra) {
                Text("Usa una fecha válida: DD/MM/AAAA")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.fechaCosecha,
        onValueChange = onFechaCosechaChange,
        label = { Text("Cosecha estimada") },
        placeholder = { Text("DD/MM/AAAA") },
        isError = uiState.errorFechaCosecha,
        supportingText = {
            if (uiState.errorFechaCosecha) {
                Text("Usa una fecha válida posterior o igual a la siembra")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.frecuenciaRiego,
        onValueChange = {
            onFrecuenciaRiegoChange(it.filter(Char::isDigit))
        },
        label = { Text("Frecuencia de riego") },
        suffix = { Text("días") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        isError = uiState.errorFrecuencia,
        supportingText = if (mostrarAyudaFrecuencia) {
            {
                if (uiState.errorFrecuencia) {
                    Text("Ingresa una cantidad mayor que cero")
                }
            }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.observaciones,
        onValueChange = onObservacionesChange,
        label = { Text(etiquetaObservaciones) },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
}