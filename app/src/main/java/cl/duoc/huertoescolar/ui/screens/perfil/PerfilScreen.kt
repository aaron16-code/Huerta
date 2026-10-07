package cl.duoc.huertoescolar.ui.screens.perfil

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun PerfilScreen(
    rol: RolUsuario,
    onVolver: () -> Unit
) {
    val nombreRol = if (rol == RolUsuario.ADMINISTRADOR) {
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
                text = "Usuario de prueba",
                style = MaterialTheme.typography.titleLarge
            )

            EtiquetaEstado(texto = nombreRol)

            Text("Nombre: Usuario Huerto Escolar")
            Text("Correo: usuario@huertoescolar.cl")
            Text("Establecimiento: Escuela de prueba")

            Text(
                text = if (rol == RolUsuario.ADMINISTRADOR) {
                    "Puede administrar sectores, cultivos y validar actividades."
                } else {
                    "Puede revisar cultivos y completar las tareas asignadas."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}