package cl.duoc.huertoescolar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Distribuye elementos en columnas según el espacio disponible.
 * Con letra grande, cada elemento recibe más ancho.
 *
 * Debe colocarse dentro de un contenedor con ancho limitado,
 * como PantallaHuerto.
 */
@Composable
fun <T> CuadriculaAdaptable(
    elementos: List<T>,
    modifier: Modifier = Modifier,
    anchoMinimo: Dp = 280.dp,
    maxColumnas: Int = 2,
    contenido: @Composable (T) -> Unit
) {
    val escalaFuente = LocalDensity.current.fontScale.coerceAtLeast(1f)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val separacion = 16.dp
        val anchoElemento = anchoMinimo * escalaFuente

        val columnas = (
                (maxWidth + separacion) / (anchoElemento + separacion)
                ).toInt().coerceIn(1, maxColumnas.coerceAtLeast(1))

        Column(
            verticalArrangement = Arrangement.spacedBy(separacion)
        ) {
            elementos.chunked(columnas).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(separacion)
                ) {
                    fila.forEach { elemento ->
                        Box(modifier = Modifier.weight(1f)) {
                            contenido(elemento)
                        }
                    }

                    repeat(columnas - fila.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Coloca dos controles uno al lado del otro cuando caben.
 * En pantallas pequeñas o con letra grande los apila.
 */
@Composable
fun DosColumnasAdaptables(
    modifier: Modifier = Modifier,
    primero: @Composable () -> Unit,
    segundo: @Composable () -> Unit
) {
    val escalaFuente = LocalDensity.current.fontScale.coerceAtLeast(1f)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth >= 560.dp * escalaFuente) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    primero()
                }

                Box(modifier = Modifier.weight(1f)) {
                    segundo()
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                primero()
                segundo()
            }
        }
    }
}