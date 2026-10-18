package cl.duoc.huertoescolar.ui.screens.tareas

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun crearUriFotografia(context: Context): Uri {
    val carpeta = File(context.filesDir, "fotos")
    carpeta.mkdirs()

    val archivo = File.createTempFile("foto_", ".jpg", carpeta)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}

/**
 * Copia una imagen elegida con Photo Picker al almacenamiento privado de la app.
 * Así la evidencia sigue disponible aunque el selector externo pierda el permiso
 * o la aplicación se cierre y vuelva a abrirse.
 */
fun copiarFotografiaAPersistencia(context: Context, origen: Uri): Uri {
    val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
    val extension = when (context.contentResolver.getType(origen)) {
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        else -> ".jpg"
    }
    val destino = File.createTempFile("foto_seleccionada_", extension, carpeta)

    try {
        val entrada = requireNotNull(context.contentResolver.openInputStream(origen)) {
            "No se pudo abrir la fotografía seleccionada"
        }
        entrada.use { input ->
            destino.outputStream().use { output -> input.copyTo(output) }
        }
    } catch (error: Exception) {
        destino.delete()
        throw error
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        destino
    )
}
