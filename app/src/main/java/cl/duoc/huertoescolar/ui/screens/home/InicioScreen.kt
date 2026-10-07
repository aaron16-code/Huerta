package cl.duoc.huertoescolar.ui.screens.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun InicioScreen(
    rol: RolUsuario,
    onIrSectores: () -> Unit,
    onIrCultivos: () -> Unit,
    onIrTareas: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nombrePerfil = if (rol == RolUsuario.ADMINISTRADOR) "Docente" else "Estudiante"

    PantallaHuerto(
        titulo = "Huerto Escolar",
        subtitulo = "Organiza los cultivos y revisa el trabajo del huerto.",
        modifier = modifier
    ) {
        TarjetaHuerto {
            Text("Sesión activa", style = MaterialTheme.typography.titleMedium)
            EtiquetaEstado(texto = nombrePerfil)
            Text(
                text = if (rol == RolUsuario.ADMINISTRADOR) {
                    "Puedes administrar sectores, cultivos y actividades."
                } else {
                    "Puedes consultar información y completar tus tareas asignadas."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("¿Qué deseas revisar?", style = MaterialTheme.typography.titleLarge)
        BotonPrincipal("Sectores del huerto", onIrSectores)
        BotonPrincipal("Cultivos", onIrCultivos)
        BotonPrincipal("Mis tareas", onIrTareas)
        BotonSecundario("Cerrar sesión", onCerrarSesion)
    }
}
