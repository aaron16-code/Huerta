package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Campo de texto para formularios con:
 *  - un ícono al inicio que indica de qué es el campo,
 *  - un ícono al final: advertencia si hay error, visto bueno si el campo ya tiene contenido válido,
 *  - el mensaje de error con una animación de aparición y desaparición.
 * La validación NO se hace aquí: el ViewModel decide el error y esta pantalla solo lo muestra.
 */
@Composable
fun CampoValidado(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    // Se recuerda el último error para que el texto no desaparezca de golpe durante la animación de salida.
    var ultimoError by remember { mutableStateOf("") }
    LaunchedEffect(error) {
        if (error != null) ultimoError = error
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(imageVector = icono, contentDescription = null) },
            trailingIcon = {
                when {
                    error != null -> Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    valor.isNotBlank() -> Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Campo completo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            isError = error != null,
            singleLine = singleLine,
            minLines = minLines,
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error ?: ultimoError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}