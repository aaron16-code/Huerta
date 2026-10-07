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
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.EstadoCultivo
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun EditarCultivoScreen(
    onCultivoActualizado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    editarViewModel: EditarCultivoViewModel = viewModel()
) {
    PantallaHuerto(
        titulo = "Editar cultivo",
        subtitulo = "Actualiza la planificación y el estado de la siembra.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            OutlinedTextField(
                value = editarViewModel.nombre,
                onValueChange = { editarViewModel.nombre = it },
                label = { Text("Nombre del cultivo") },
                isError = editarViewModel.errorNombre,
                supportingText = {
                    if (editarViewModel.errorNombre) Text("El nombre es obligatorio")
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editarViewModel.descripcion,
                onValueChange = { editarViewModel.descripcion = it },
                label = { Text("Descripción") },
                isError = editarViewModel.errorDescripcion,
                supportingText = {
                    if (editarViewModel.errorDescripcion) Text("Ingresa al menos 5 caracteres")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Sector", style = MaterialTheme.typography.titleMedium)
            RepositorioHuerto.sectores.forEach { sector ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = editarViewModel.sectorId == sector.id,
                        onClick = { editarViewModel.sectorId = sector.id }
                    )
                    Text(sector.nombre)
                }
            }
            if (editarViewModel.errorSector) {
                Text("Selecciona un sector", color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = editarViewModel.fechaSiembra,
                onValueChange = { editarViewModel.fechaSiembra = it },
                label = { Text("Fecha de siembra") },
                placeholder = { Text("DD/MM/AAAA") },
                isError = editarViewModel.errorFechaSiembra,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editarViewModel.fechaCosecha,
                onValueChange = { editarViewModel.fechaCosecha = it },
                label = { Text("Cosecha estimada") },
                placeholder = { Text("DD/MM/AAAA") },
                isError = editarViewModel.errorFechaCosecha,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editarViewModel.frecuenciaRiego,
                onValueChange = { editarViewModel.frecuenciaRiego = it.filter(Char::isDigit) },
                label = { Text("Frecuencia de riego") },
                suffix = { Text("días") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = editarViewModel.errorFrecuencia,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editarViewModel.observaciones,
                onValueChange = { editarViewModel.observaciones = it },
                label = { Text("Observaciones") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Estado del cultivo", style = MaterialTheme.typography.titleMedium)
            EstadoCultivo.entries.forEach { estado ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = editarViewModel.estado == estado,
                        onClick = { editarViewModel.estado = estado }
                    )
                    Text(estado.textoVisible())
                }
            }

            BotonPrincipal(
                texto = "Guardar cambios",
                onClick = {
                    if (editarViewModel.guardarCambios()) onCultivoActualizado()
                }
            )
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
