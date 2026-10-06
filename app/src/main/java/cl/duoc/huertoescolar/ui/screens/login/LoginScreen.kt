package cl.duoc.huertoescolar.ui.screens.login

import android.R
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
fun LoginScreen(
    onIngresar : (RolUsuario)-> Unit,
    modifier: Modifier = Modifier
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

        Text("Selecciona una cuenta ficticia")

        Button(
            onClick = { onIngresar(RolUsuario.ADMINISTRADOR)},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar como docente")
        }
        Button(
            onClick = { onIngresar(RolUsuario.ESTUDIANTE)},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar como estudiante")
        }
    }
}