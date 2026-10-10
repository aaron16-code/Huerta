package cl.duoc.huertoescolar

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.ui.screens.tareas.DetalleTareaScreen
import cl.duoc.huertoescolar.ui.screens.tareas.ListaTareasScreen
import cl.duoc.huertoescolar.ui.screens.tareas.PanelValidacionScreen

/** Rutas propias del módulo de operación (Rutas.kt es del otro integrante). */
object RutasOperacion {
    const val DETALLE_TAREA = "detalle_tarea"
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
    // Estudiante: mis tareas
    composable(Rutas.TAREAS) {
        rolActual()?.let { rol ->
            ListaTareasScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onTareaClick = { id ->
                    RepositorioOperacion.seleccionarTarea(id)
                    navController.navigate(RutasOperacion.DETALLE_TAREA)
                },
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Estudiante: detalle de una tarea
    composable(RutasOperacion.DETALLE_TAREA) {
        rolActual()?.let { rol ->
            DetalleTareaScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Administrador: validar tareas
    composable(Rutas.VALIDAR_TAREAS) {
        rolActual()?.let { rol ->
            PanelValidacionScreen(
                usuario = UsuariosPrueba.porRol(rol),
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Más adelante se agregan aquí: PROBLEMAS, ALERTAS, SENSORES e HISTORIAL.
}