package cl.duoc.huertoescolar.ui.screens.login

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun LoginScreen(
    onIngresar: (UsuarioPrueba) -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaHuerto(
        titulo = "Huerto Escolar",
        subtitulo = "Gestión colaborativa de cultivos y actividades",
        modifier = modifier
    ) {
        TarjetaHuerto {
            Text(
                text = "Cuenta de demostración",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Selecciona una cuenta ficticia para ingresar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BotonPrincipal(
                texto = "Docente · ${UsuariosPrueba.docente.nombre}",
                onClick = { onIngresar(UsuariosPrueba.docente) }
            )
            UsuariosPrueba.estudiantes.forEach { estudiante ->
                BotonSecundario(
                    texto = "Estudiante · ${estudiante.nombre}",
                    onClick = { onIngresar(estudiante) }
                )
            }
        }
        Text(
            text = "Todos los usuarios y datos utilizados son ficticios.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
