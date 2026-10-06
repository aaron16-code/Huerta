package cl.duoc.huertoescolar.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.RolUsuario


@Composable
fun InicioScreen(
    rol: RolUsuario,
    onIrSectores: () -> Unit,
    onIrTareas: () -> Unit,
    modifier: Modifier = Modifier,
    onCerrarSesion: () -> Unit,


) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Huerto Escolar",

            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = if (rol == RolUsuario.ADMINISTRADOR) {
                "Perfil: Docente"
            } else {
                "Perfil: Estudiante"
            }
        )

        Text(
            text = "Organiza los cultivos y las tareas del huerto"
        )

        Button(
            onClick = onIrSectores,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sectores y cultivos")
        }

        Button(
            onClick = onIrTareas,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mis tareas")
        }
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }


    }

}
