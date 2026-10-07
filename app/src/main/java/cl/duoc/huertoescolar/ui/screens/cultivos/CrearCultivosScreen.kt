package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RepositorioHuerto
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
            OutlinedTextField(
                value = cultivoViewModel.nombre,
                onValueChange = { cultivoViewModel.nombre = it },
                label = { Text("Nombre del cultivo") },
                isError = cultivoViewModel.errorNombre,
                supportingText = {
                    if (cultivoViewModel.errorNombre) Text("El nombre es obligatorio")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cultivoViewModel.descripcion,
                onValueChange = { cultivoViewModel.descripcion = it },
                label = { Text("Descripción") },
                isError = cultivoViewModel.errorDescripcion,
                supportingText = {
                    if (cultivoViewModel.errorDescripcion) Text("Ingresa al menos 5 caracteres")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Sector", style = MaterialTheme.typography.titleMedium)
            RepositorioHuerto.sectores.forEach { sector ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = cultivoViewModel.sectorId == sector.id,
                        onClick = { cultivoViewModel.sectorId = sector.id }
                    )
                    Text(sector.nombre)
                }
            }
            if (cultivoViewModel.errorSector) {
                Text(
                    text = "Selecciona un sector",
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = cultivoViewModel.fechaSiembra,
                onValueChange = { cultivoViewModel.fechaSiembra = it },
                label = { Text("Fecha de siembra") },
                placeholder = { Text("DD/MM/AAAA") },
                isError = cultivoViewModel.errorFechaSiembra,
                supportingText = {
                    if (cultivoViewModel.errorFechaSiembra) Text("Ingresa la fecha de siembra")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cultivoViewModel.fechaCosecha,
                onValueChange = { cultivoViewModel.fechaCosecha = it },
                label = { Text("Cosecha estimada") },
                placeholder = { Text("DD/MM/AAAA") },
                isError = cultivoViewModel.errorFechaCosecha,
                supportingText = {
                    if (cultivoViewModel.errorFechaCosecha) Text("Ingresa la fecha estimada")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cultivoViewModel.frecuenciaRiego,
                onValueChange = { cultivoViewModel.frecuenciaRiego = it.filter(Char::isDigit) },
                label = { Text("Frecuencia de riego") },
                suffix = { Text("días") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = cultivoViewModel.errorFrecuencia,
                supportingText = {
                    if (cultivoViewModel.errorFrecuencia) Text("Ingresa una cantidad mayor que cero")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cultivoViewModel.observaciones,
                onValueChange = { cultivoViewModel.observaciones = it },
                label = { Text("Observaciones (opcional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            BotonPrincipal(
                texto = "Guardar cultivo",
                onClick = {
                    if (cultivoViewModel.guardarCultivo()) onCultivoCreado()
                }
            )
        }
    }
}
