package cl.duoc.huertoescolar.ui.screens.sectores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.EstadoSector
import cl.duoc.huertoescolar.RepositorioHuerto
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SectoresScreen(
    rol: RolUsuario,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    onCrearSector: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Sectores del huerto",
            style = MaterialTheme.typography.headlineMedium
        )

        RepositorioHuerto.sectores.forEach { sector ->
            val estadoTexto = when (sector.estado) {
                EstadoSector.DISPONIBLE -> "Disponible"
                EstadoSector.EN_USO -> "En uso"
                EstadoSector.EN_MANTENIMIENTO -> "En mantenimiento"
                EstadoSector.FUERA_DE_USO -> "Fuera de uso"
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = sector.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(sector.descripcion)
                    Text("Ubicación: ${sector.ubicacion}")
                    Text("Estado: $estadoTexto")
                    Text("Responsable: ${sector.responsable}")
                }
            }
        }

        if (rol == RolUsuario.ADMINISTRADOR) {
            Button(
                onClick = onCrearSector,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear sector")
            }
        }

        Button(onClick = onVolver) {
            Text("Volver")
        }
    }
}