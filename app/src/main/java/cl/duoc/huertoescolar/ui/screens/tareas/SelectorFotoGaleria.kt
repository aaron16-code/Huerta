package cl.duoc.huertoescolar.ui.screens.tareas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun SelectorFotoGaleria(
    fotoUri: String?,
    onFotoSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier,
    onQuitarFoto: (() -> Unit)? = null
) {
    val contexto = LocalContext.current
    var uriPendienteTexto by rememberSaveable { mutableStateOf<String?>(null) }

    val selector = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                copiarFotografiaAPersistencia(contexto, uri)
            }.onSuccess { uriPermanente ->
                onFotoSeleccionada(uriPermanente.toString())
            }
        }
    }

    val camara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            uriPendienteTexto?.let(onFotoSeleccionada)
        }
        uriPendienteTexto = null
    }

    val hayFoto = !fotoUri.isNullOrBlank()
    val colorBoton by animateColorAsState(
        targetValue = if (hayFoto) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(durationMillis = 600),
        label = "Color del botón de fotografía"
    )

    TarjetaHuerto(modifier = modifier) {
        Text("Foto del avance (opcional)", style = MaterialTheme.typography.titleMedium)
        Text(
            "Solo fotos de plantas, cultivos o herramientas. No incluyas personas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Crossfade(
            targetState = hayFoto,
            label = "Estado de fotografía"
        ) { tieneFoto ->
            Text(
                if (tieneFoto) {
                    "Fotografía preparada"
                } else {
                    "Selecciona una fotografía o toma una nueva"
                }
            )
        }

        AnimatedVisibility(
            visible = hayFoto,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            FotoEvidencia(fotoUri)
        }

        Button(
            onClick = {
                selector.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorBoton)
        ) {
            Text(if (hayFoto) "Cambiar foto" else "Seleccionar foto")
        }

        BotonSecundario(
            texto = "Tomar fotografía",
            onClick = {
                try {
                    val nuevaUri: Uri = crearUriFotografia(contexto)
                    uriPendienteTexto = nuevaUri.toString()
                    camara.launch(nuevaUri)
                } catch (_: Exception) {
                    uriPendienteTexto = null
                }
            }
        )

        if (hayFoto && onQuitarFoto != null) {
            BotonSecundario(texto = "Quitar foto", onClick = onQuitarFoto)
        }
    }
}
