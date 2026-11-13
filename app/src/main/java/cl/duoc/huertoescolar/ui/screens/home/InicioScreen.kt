package cl.duoc.huertoescolar.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto

private data class Acceso(
    val titulo: String,
    val descripcion: String,
    val onClick: () -> Unit
)

@Composable
fun InicioScreen(
    rol: RolUsuario,
    onIrPerfil: () -> Unit,
    onIrSectores: () -> Unit,
    onIrCultivos: () -> Unit,
    onIrTareas: () -> Unit,
    onIrValidarTareas: () -> Unit,
    onIrProblemas: () -> Unit,
    onIrAlertas: () -> Unit,
    onIrSensores: () -> Unit,
    onIrHistorial: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esDocente = rol == RolUsuario.ADMINISTRADOR

    PantallaHuerto(
        titulo = "Mi huerto",
        subtitulo = "Actividades, cultivos y cuidado del huerto escolar.",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EtiquetaEstado(
                texto = if (esDocente) "Docente" else "Estudiante"
            )

            TextButton(onClick = onIrPerfil) {
                Text("Mi perfil")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (esDocente) {
                        "Acompaña cada actividad"
                    } else {
                        "Tu trabajo hace crecer el huerto"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (esDocente) {
                        "Organiza las tareas y revisa el avance de tus estudiantes."
                    } else {
                        "Consulta tus tareas y registra lo que realizaste."
                    },
                    style = MaterialTheme.typography.bodyLarge
                )

                BotonPrincipal(
                    texto = if (esDocente) "Gestionar tareas" else "Ver mis tareas",
                    onClick = onIrTareas
                )
            }
        }

        if (esDocente) {
            AccesoHorizontal(
                titulo = "Revisar entregas",
                descripcion = "Aprueba o rechaza las tareas enviadas.",
                onClick = onIrValidarTareas
            )
        }

        TituloSeccion("Explora el huerto")

        CuadriculaAccesos(
            accesos = listOf(
                Acceso(
                    titulo = "Sectores",
                    descripcion = "Zonas y responsables",
                    onClick = onIrSectores
                ),
                Acceso(
                    titulo = "Cultivos",
                    descripcion = "Especies, fechas y estados",
                    onClick = onIrCultivos
                )
            )
        )

        TituloSeccion("Seguimiento")

        CuadriculaAccesos(
            accesos = listOf(
                Acceso(
                    titulo = "Problemas",
                    descripcion = if (esDocente) {
                        "Revisa los reportes"
                    } else {
                        "Reporta y consulta"
                    },
                    onClick = onIrProblemas
                ),
                Acceso(
                    titulo = "Alertas",
                    descripcion = "Avisos y pendientes",
                    onClick = onIrAlertas
                )
            )
        )

        if (esDocente) {
            AccesoHorizontal(
                titulo = "Sensores simulados",
                descripcion = "Humedad y temperatura · Datos de prueba",
                onClick = onIrSensores
            )
        }

        AccesoHorizontal(
            titulo = "Historial",
            descripcion = "Consulta el registro de actividades.",
            onClick = onIrHistorial
        )

        TextButton(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CuadriculaAccesos(accesos: List<Acceso>) {
    val escalaFuente = LocalDensity.current.fontScale

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Las tarjetas necesitan más espacio cuando aumenta la letra.
        val anchoMinimo = 160.dp * escalaFuente.coerceAtLeast(1f)
        val separacion = 12.dp

        val columnas = (
                (maxWidth + separacion) /
                        (anchoMinimo + separacion)
                )
            .toInt()
            .coerceIn(1, 3)

        Column(
            verticalArrangement = Arrangement.spacedBy(separacion)
        ) {
            accesos.chunked(columnas).forEach { fila ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(separacion)
                ) {
                    fila.forEach { acceso ->
                        TarjetaAcceso(
                            acceso = acceso,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    repeat(columnas - fila.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaAcceso(
    acceso: Acceso,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = acceso.onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Text(
                text = acceso.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = acceso.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccesoHorizontal(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}