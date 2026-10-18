package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

/**
 * Selector de foto con el Photo Picker de Android (PickVisualMedia), como se vio en clases.
 * No pide permisos: el usuario elige una foto y la app recibe solo una referencia (URI) a ella.
 * La cámara es opcional según el caso, por eso aquí solo se elige de la galería.
 */
@Composable
fun SelectorFotoGaleria(
    fotoUri: String?,
    onFotoSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier,
    onQuitarFoto: (() -> Unit)? = null
) {
    val selector = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onFotoSeleccionada(uri.toString())
        }
    }

    val hayFoto = !fotoUri.isNullOrBlank()

    TarjetaHuerto(modifier = modifier) {
        Text("Foto del avance (opcional)", style = MaterialTheme.typography.titleMedium)
        Text(
            "Solo fotos de plantas, cultivos o herramientas. No incluyas personas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (hayFoto) {
            FotoEvidencia(fotoUri)
        } else {
            Text("Todavía no hay una foto seleccionada")
        }

        BotonSecundario(
            texto = if (hayFoto) "Cambiar foto" else "Seleccionar foto",
            onClick = {
                selector.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        if (hayFoto && onQuitarFoto != null) {
            BotonSecundario(texto = "Quitar foto", onClick = onQuitarFoto)
        }
    }
}