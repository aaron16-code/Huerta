package cl.duoc.huertoescolar

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.data.nombreDeCultivo
import cl.duoc.huertoescolar.data.nombreDeSector
import cl.duoc.huertoescolar.ui.screens.alertas.PanelAlertasScreen
import cl.duoc.huertoescolar.ui.screens.historial.ListaHistorialScreen
import cl.duoc.huertoescolar.ui.screens.problemas.ListaProblemasScreen
import cl.duoc.huertoescolar.ui.screens.problemas.ReportarProblemaScreen
import cl.duoc.huertoescolar.ui.screens.sensores.PanelSensoresScreen
import cl.duoc.huertoescolar.ui.screens.tareas.CrearTareaScreen
import cl.duoc.huertoescolar.ui.screens.tareas.DetalleTareaScreen
import cl.duoc.huertoescolar.ui.screens.tareas.GestionTareasScreen
import cl.duoc.huertoescolar.ui.screens.tareas.ListaTareasScreen
import cl.duoc.huertoescolar.ui.screens.tareas.PanelValidacionScreen

/** Rutas propias del módulo de operación (Rutas.kt es del otro integrante). */
object RutasOperacion {
    const val DETALLE_TAREA = "detalle_tarea"
    const val REPORTAR_PROBLEMA = "reportar_problema"
    const val CREAR_TAREA = "crear_tarea"
}

/**
 * Registra las pantallas del módulo de operación.
 * Se llama UNA vez dentro del NavHost de Navegacion.kt:
 *
 *     grafoOperacion(navController) { rolSeleccionado }
 *
 * `rolActual` es una función (y no un valor) para que siempre lea el rol vigente.
 */
fun NavGraphBuilder.grafoOperacion(
    navController: NavController,
    rolActual: () -> RolUsuario?
) {
    // Tareas: el docente gestiona todas (y crea nuevas); el estudiante ve las suyas
    composable(Rutas.TAREAS) {
        rolActual()?.let { rol ->
            if (rol == RolUsuario.ADMINISTRADOR) {
                GestionTareasScreen(
                    usuario = UsuariosPrueba.porRol(rol),
                    nombreSector = ::nombreDeSector,
                    onCrearTarea = { navController.navigate(RutasOperacion.CREAR_TAREA) },
                    onVerDetalle = { id ->
                        RepositorioOperacion.seleccionarTarea(id)
                        navController.navigate(RutasOperacion.DETALLE_TAREA)
                    },
                    onVolver = { navController.popBackStack() }
                )
            } else {
                ListaTareasScreen(
                    usuario = UsuariosPrueba.porRol(rol),
                    nombreSector = ::nombreDeSector,
                    onTareaClick = { id ->
                        RepositorioOperacion.seleccionarTarea(id)
                        navController.navigate(RutasOperacion.DETALLE_TAREA)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }

    // Docente: formulario para crear y asignar una tarea
    composable(RutasOperacion.CREAR_TAREA) {
        rolActual()?.let { rol ->
            CrearTareaScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Estudiante: detalle de una tarea
    composable(RutasOperacion.DETALLE_TAREA) {
        rolActual()?.let { rol ->
            DetalleTareaScreen(
                usuario = UsuariosPrueba.porRol(rol),
                nombreSector = ::nombreDeSector,
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Administrador: validar tareas
    composable(Rutas.VALIDAR_TAREAS) {
        rolActual()?.let { rol ->
            PanelValidacionScreen(
                usuario = UsuariosPrueba.porRol(rol),
                nombreSector = ::nombreDeSector,
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Problemas: lista (estudiante ve los suyos, docente ve todos)
    composable(Rutas.PROBLEMAS) {
        rolActual()?.let { rol ->
            ListaProblemasScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onReportar = { navController.navigate(RutasOperacion.REPORTAR_PROBLEMA) },
                onVolver = { navController.popBackStack() },
                nombreSector = ::nombreDeSector,
                nombreCultivo = ::nombreDeCultivo
            )
        }
    }

    // Problemas: formulario del estudiante
    composable(RutasOperacion.REPORTAR_PROBLEMA) {
        rolActual()?.let { rol ->
            ReportarProblemaScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Alertas y recordatorios (informativos)
    composable(Rutas.ALERTAS) {
        rolActual()?.let { rol ->
            PanelAlertasScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onVolver = { navController.popBackStack() },
                nombreSector = ::nombreDeSector
            )
        }
    }

    // Sensores simulados (solo docente; siempre como dato de prueba)
    composable(Rutas.SENSORES) {
        rolActual()?.let { rol ->
            PanelSensoresScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onVolver = { navController.popBackStack() },
                nombreSector = ::nombreDeSector
            )
        }
    }

    // Historial cronológico (docente ve todo; estudiante, solo lo suyo)
    composable(Rutas.HISTORIAL) {
        rolActual()?.let { rol ->
            ListaHistorialScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onVolver = { navController.popBackStack() },
                nombreSector = ::nombreDeSector
            )
        }
    }
}