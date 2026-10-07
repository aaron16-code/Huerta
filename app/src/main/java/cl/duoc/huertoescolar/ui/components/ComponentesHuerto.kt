package cl.duoc.huertoescolar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PantallaHuerto(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    onVolver: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onVolver != null) {
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.heightIn(min = 44.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "← Volver",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineMedium
            )

            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            contenido()
        }
    }
}

@Composable
fun TarjetaHuerto(
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = contenido
        )
    }
}

@Composable
fun EtiquetaEstado(
    texto: String,
    color: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
