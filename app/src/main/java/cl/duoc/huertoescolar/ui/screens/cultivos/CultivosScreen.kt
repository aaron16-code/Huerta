package cl.duoc.huertoescolar.ui.screens.cultivos

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
fun CultivosScreen(
    rol: RolUsuario,
    onCrearCultivo: () -> Unit,
    onEditarCultivo: () -> Unit,
    onVerDetalle: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cultivos",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Cultivo de prueba: Lechugas")

        Button(
            onClick = onVerDetalle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver detalle")
        }

        if (rol == RolUsuario.ADMINISTRADOR) {
            Button(
                onClick = onCrearCultivo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cultivo")
            }

            Button(
                onClick = onEditarCultivo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar cultivo")
            }
        }

        Button(onClick = onVolver) {
            Text("Volver")
        }
    }
}