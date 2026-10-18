package cl.duoc.huertoescolar.ui.screens.tareas

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Muestra la foto adjunta como evidencia (para que el docente la revise).
 * Si la foto es un dato de ejemplo (por ejemplo "foto_prueba_tomate") muestra solo el texto.
 */
@Composable
fun FotoEvidencia(fotoUri: String?, modifier: Modifier = Modifier) {
    if (fotoUri.isNullOrBlank()) {
        Text("Sin foto adjunta", modifier = modifier)
    } else if (fotoUri.startsWith("content://") || fotoUri.startsWith("file://")) {
        AndroidView(
            factory = { contexto ->
                ImageView(contexto).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            },
            update = { imagen ->
                try {
                    imagen.setImageURI(Uri.parse(fotoUri))
                } catch (e: Exception) {
                    imagen.setImageDrawable(null) // si ya no hay permiso para leerla, no se cae la app
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    } else {
        Text("Foto de prueba: $fotoUri", modifier = modifier)
    }
}