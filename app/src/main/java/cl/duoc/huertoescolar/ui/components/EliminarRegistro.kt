package cl.duoc.huertoescolar.ui.components
import androidx.compose.runtime.*
import androidx.compose.material3.*
import kotlinx.coroutines.launch

@Composable
fun EliminarRegistro(etiqueta: String, eliminar: suspend () -> Result<Unit>) {
    var confirmar by remember { mutableStateOf(false) }
    var ocupado by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    if (!ocupado) BotonSecundario(texto = "Eliminar $etiqueta", onClick = { confirmar = true })
    else Text("Eliminando...")
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("Eliminar $etiqueta") }, text = { Text("Se eliminará del servidor. ¿Continuar?") }, confirmButton = {
        TextButton(onClick = {
            confirmar = false; ocupado = true; error = null
            scope.launch { try { error = eliminar().exceptionOrNull()?.message } finally { ocupado = false } }
        }) { Text("Eliminar") }
    }, dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } })
}
