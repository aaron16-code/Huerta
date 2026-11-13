package cl.duoc.huertoescolar.ui.screens.tareas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.data.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.CuadriculaAdaptable
import cl.duoc.huertoescolar.ui.components.DosColumnasAdaptables
import cl.duoc.huertoescolar.ui.components.EtiquetaEstado
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun GestionTareasScreen(
    usuario: UsuarioPrueba,
    onCrearTarea: () -> Unit,
    onEditarTarea: (Int) -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    nombreSector: (Int) -> String = { "Sector $it" },
    viewModel: GestionTareasViewModel = viewModel()
) {
    val todas by viewModel.tareas.collectAsState()
    val lista = viewModel.visibles(todas)
    val esDocente = usuario.rol == RolUsuario.ADMINISTRADOR

    PantallaHuerto(
        titulo = "Tareas del huerto",
        subtitulo = "Organiza las actividades y consulta su avance.",
        onVolver = onVolver,
        modifier = modifier
    ) {
        if (!esDocente) {
            TarjetaHuerto {
                Text(
                    text = "Acceso para docentes",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Solo docentes o talleristas autorizados pueden administrar tareas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            BotonPrincipal(
                texto = "Crear y asignar una tarea",
                onClick = onCrearTarea
            )

            OutlinedTextField(
                value = viewModel.busqueda,
                onValueChange = viewModel::cambiarBusqueda,
                label = {
                    Text("Buscar tareas")
                },
                supportingText = {
                    Text("Por fecha, responsable, tipo o descripción.")
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Filtrar por estado",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroTareas.entries.forEach { filtro ->
                        FilterChip(
                            selected = viewModel.filtro == filtro,
                            onClick = {
                                viewModel.cambiarFiltro(filtro)
                            },
                            label = {
                                Text(filtro.etiqueta)
                            }
                        )
                    }
                }
            }

            Text(
                text = "Tareas encontradas: ${lista.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (lista.isEmpty()) {
                TarjetaHuerto {
                    Text(
                        text = "No hay tareas para mostrar",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Prueba otra búsqueda o selecciona un filtro diferente.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                CuadriculaAdaptable(
                    elementos = lista,
                    anchoMinimo = 300.dp,
                    maxColumnas = 2
                ) { tarea ->
                    TarjetaTareaDocente(
                        tarea = tarea,
                        sector = nombreSector(tarea.sectorId),
                        onEditar = {
                            onEditarTarea(tarea.id)
                        },
                        onVerDetalle = {
                            onVerDetalle(tarea.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaTareaDocente(
    tarea: Tarea,
    sector: String,
    onEditar: () -> Unit,
    onVerDetalle: () -> Unit
) {
    val estado = tarea.estadoVisible()

    TarjetaHuerto {
        EtiquetaEstado(
            texto = estado.etiqueta,
            color = colorEstadoContenedor(estado)
        )

        Text(
            text = tarea.descripcion,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        DatoTareaDocente(
            etiqueta = "Tipo de actividad",
            valor = tarea.tipo.etiqueta
        )

        DatoTareaDocente(
            etiqueta = "Sector",
            valor = sector
        )

        DatoTareaDocente(
            etiqueta = "Asignada a",
            valor = textoResponsable(tarea)
        )

        DatoTareaDocente(
            etiqueta = "Fecha programada",
            valor = formatearFecha(tarea.fechaProgramada)
        )

        DosColumnasAdaptables(
            primero = {
                BotonSecundario(
                    texto = "Ver detalle",
                    onClick = onVerDetalle
                )
            },
            segundo = {
                BotonPrincipal(
                    texto = "Editar tarea",
                    onClick = onEditar
                )
            }
        )
    }
}

@Composable
private fun DatoTareaDocente(
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

internal fun textoResponsable(tarea: Tarea): String = when {
    tarea.responsableUsuarioId != null -> UsuariosPrueba.todos
        .find { it.id == tarea.responsableUsuarioId }
        ?.nombre
        ?: "estudiante ficticio n.° ${tarea.responsableUsuarioId}"

    !tarea.grupoAsignado.isNullOrBlank() ->
        "grupo ${tarea.grupoAsignado}"

    else -> "sin asignar"
}