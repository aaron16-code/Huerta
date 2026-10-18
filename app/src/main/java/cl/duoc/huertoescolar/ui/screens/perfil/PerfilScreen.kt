package cl.duoc.huertoescolar.ui.screens.perfil

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun PerfilScreen(
    usuario: UsuarioPrueba,
    onVolver: () -> Unit
) {
    val nombreRol = if (usuario.rol == RolUsuario.ADMINISTRADOR) {
        "Docente"
    } else {
        "Estudiante"
    }

    PantallaHuerto(
        titulo = "Mi perfil",
        subtitulo = "Información del usuario activo",
        onVolver = onVolver
    ) {
        TarjetaHuerto {
            Text(
                text = usuario.nombre,
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = nombreRol)

            Text("Nombre: ${usuario.nombre}")
            if (usuario.grupo.isNotBlank()) {
                Text("Grupo: ${usuario.grupo}")
            }
            Text("Establecimiento: Escuela de prueba")

            Text(
                text = if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                    "Puede administrar sectores, cultivos y validar actividades."
                } else {
                    "Puede revisar cultivos y completar las tareas asignadas."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
