package cl.duoc.huertoescolar.ui.components

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp


@Composable
fun SelectorFoto(
    fotoUri: String?,
    onFotoSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onFotoSeleccionada(uri.toString())
        }
    }

    TarjetaHuerto(modifier = modifier) {
        Text(
            text = "Foto del cultivo",
            style = MaterialTheme.typography.titleMedium
        )

        if (fotoUri.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Todavía no hay una foto seleccionada",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            AndroidView(
                factory = { contexto ->
                    ImageView(contexto).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imagen ->
                    imagen.setImageURI(Uri.parse(fotoUri))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        BotonSecundario(
            texto = if (fotoUri.isNullOrBlank()) {
                "Seleccionar foto"
            } else {
                "Cambiar foto"
            },
            onClick = {
                selectorFoto.launch("image/*")
            }
        )
    }
}