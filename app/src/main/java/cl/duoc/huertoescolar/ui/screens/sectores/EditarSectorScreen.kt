package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun EditarSectorScreen(
    onSectorActualizado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    editarViewModel: EditarSectorViewModel = viewModel()
) {
    PantallaHuerto(
        titulo = "Editar sector",
        subtitulo = "Actualiza la información y disponibilidad de la zona.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        TarjetaHuerto {
            FormularioSector(
                uiState = editarViewModel.uiState,
                onNombreChange = { editarViewModel.nombre = it },
                onDescripcionChange = { editarViewModel.descripcion = it },
                onUbicacionChange = { editarViewModel.ubicacion = it }
            )

            Text(
                text = "Estado del sector",
                style = MaterialTheme.typography.titleMedium
            )

            EstadoSector.entries.forEach { estado ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = editarViewModel.estado == estado,
                        onClick = { editarViewModel.estado = estado }
                    )

                    Text(
                        text = when (estado) {
                            EstadoSector.DISPONIBLE -> "Disponible"
                            EstadoSector.EN_USO -> "En uso"
                            EstadoSector.EN_MANTENIMIENTO -> "En mantenimiento"
                            EstadoSector.FUERA_DE_USO -> "Fuera de uso"
                        }
                    )
                }
            }

            BotonPrincipal(
                texto = "Guardar cambios",
                onClick = {
                    if (editarViewModel.guardarCambios()) {
                        onSectorActualizado()
                    }
                }
            )
        }
    }
}