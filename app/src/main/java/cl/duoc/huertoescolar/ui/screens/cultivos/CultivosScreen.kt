package cl.duoc.huertoescolar.ui.screens.cultivos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.huertoescolar.Cultivo
import cl.duoc.huertoescolar.EstadoCultivo
import cl.duoc.huertoescolar.RepositorioHuerto
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.DosColumnasAdaptables
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun CultivosScreen(
    rol: RolUsuario,
    onCrearCultivo: () -> Unit,
    onEditarCultivo: (Cultivo) -> Unit,
    onVerDetalle: (Cultivo) -> Unit,
    onVolver: () -> Unit
) {
    val esDocente = rol == RolUsuario.ADMINISTRADOR
    val cultivos = RepositorioHuerto.cultivos.toList()

    PantallaHuerto(
        titulo = "Cultivos",
        subtitulo = "Consulta las siembras, su estado y frecuencia de riego.",
        onVolver = onVolver
    ) {
        if (esDocente) {
            BotonPrincipal(
                texto = "Crear cultivo",
                onClick = onCrearCultivo
            )
        }

        Text(
            text = "Cultivos registrados: ${cultivos.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (cultivos.isEmpty()) {
            TarjetaHuerto {
                Text(
                    text = "Todavía no hay cultivos",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (esDocente) {
                        "Registra el primer cultivo para comenzar su seguimiento."
                    } else {
                        "Aquí aparecerán los cultivos que registre tu docente."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            CuadriculaAdaptable(
                elementos = cultivos,
                anchoMinimo = 280.dp,
                maxColumnas = 2
            ) { cultivo ->
                val sector = RepositorioHuerto.sectores.find {
                    it.id == cultivo.sectorId
                }

                TarjetaCultivoAdaptable(
                    cultivo = cultivo,
                    nombreSector = sector?.nombre ?: "Sin sector",
                    esDocente = esDocente,
                    onVerDetalle = { onVerDetalle(cultivo) },
                    onEditar = { onEditarCultivo(cultivo) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaCultivoAdaptable(
    cultivo: Cultivo,
    nombreSector: String,
    esDocente: Boolean,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit
) {
    val estadoTexto = when (cultivo.estado) {
        EstadoCultivo.PLANIFICADO -> "Planificado"
        EstadoCultivo.SEMBRADO -> "Sembrado"
        EstadoCultivo.EN_CRECIMIENTO -> "En crecimiento"
        EstadoCultivo.LISTO_PARA_COSECHAR -> "Listo para cosechar"
        EstadoCultivo.COSECHADO -> "Cosechado"
        EstadoCultivo.CON_PROBLEMAS -> "Con problemas"
        EstadoCultivo.FINALIZADO -> "Finalizado"
    }

    val colorEstado = when (cultivo.estado) {
        EstadoCultivo.PLANIFICADO ->
            MaterialTheme.colorScheme.secondaryContainer

        EstadoCultivo.SEMBRADO,
        EstadoCultivo.EN_CRECIMIENTO ->
            MaterialTheme.colorScheme.primaryContainer

        EstadoCultivo.LISTO_PARA_COSECHAR ->
            MaterialTheme.colorScheme.tertiaryContainer

        EstadoCultivo.COSECHADO,
        EstadoCultivo.FINALIZADO ->
            MaterialTheme.colorScheme.surfaceVariant

        EstadoCultivo.CON_PROBLEMAS ->
            MaterialTheme.colorScheme.errorContainer
    }

    TarjetaHuerto {
        EtiquetaEstado(
            texto = estadoTexto,
            color = colorEstado
        )

        Text(
            text = cultivo.nombre,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        DatoCultivo(
            etiqueta = "Sector",
            valor = nombreSector
        )

        DatoCultivo(
            etiqueta = "Frecuencia de riego",
            valor = if (cultivo.frecuenciaRiegoDias == 1) {
                "Cada día"
            } else {
                "Cada ${cultivo.frecuenciaRiegoDias} días"
            }
        )

        if (esDocente) {
            DosColumnasAdaptables(
                primero = {
                    BotonSecundario(
                        texto = "Ver detalle",
                        onClick = onVerDetalle
                    )
                },
                segundo = {
                    BotonPrincipal(
                        texto = "Editar cultivo",
                        onClick = onEditar
                    )
                }
            )
        } else {
            BotonSecundario(
                texto = "Ver detalle",
                onClick = onVerDetalle
            )
        }
    }
}

@Composable
private fun DatoCultivo(
    etiqueta: String,
    valor: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = valor.ifBlank { "Sin información" },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}