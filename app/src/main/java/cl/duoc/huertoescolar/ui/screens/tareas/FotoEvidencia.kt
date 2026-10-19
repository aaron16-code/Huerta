package cl.duoc.huertoescolar.ui.screens.tareas

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Muestra la foto adjunta como evidencia (para que el docente la revise).
 * Si la foto es un dato de ejemplo (por ejemplo "foto_prueba_tomate") muestra solo el texto.
 */
@Composable
fun FotoEvidencia(fotoUri: String?, modifier: Modifier = Modifier) {
    if (fotoUri.isNullOrBlank()) {
        Text("Sin foto adjunta", modifier = modifier)
    } else if (fotoUri.startsWith("content://") || fotoUri.startsWith("file://")) {
        val contexto = LocalContext.current
        val disponible by produceState<Boolean?>(initialValue = null, key1 = fotoUri) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    contexto.contentResolver.openInputStream(Uri.parse(fotoUri))
                        ?.use { true } ?: false
                }.getOrDefault(false)
            }
        }

        when (disponible) {
            null -> Text("Cargando fotografía…", modifier = modifier)
            false -> Text("Foto no disponible", modifier = modifier)
            true -> AndroidView(
                factory = { contextoImagen ->
                    ImageView(contextoImagen).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imagen ->
                    runCatching { imagen.setImageURI(Uri.parse(fotoUri)) }
                        .onFailure { imagen.setImageDrawable(null) }
                },
                modifier = modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    } else {
        Text("Foto de prueba: $fotoUri", modifier = modifier)
    }
}
