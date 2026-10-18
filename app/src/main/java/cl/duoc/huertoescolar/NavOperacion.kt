package cl.duoc.huertoescolar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cl.duoc.huertoescolar.data.RepositorioOperacion
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.nombreDeCultivo
import cl.duoc.huertoescolar.data.nombreDeSector
import cl.duoc.huertoescolar.data.operacion.PersistenciaRoom
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

/**
 * Activa el guardado con Room la primera vez que se abre una pantalla de este módulo.
 * Si Room fallara, la app sigue funcionando en memoria.
 */
@Composable
private fun IniciarPersistencia() {
    val contexto = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        try {
            RepositorioOperacion.iniciarPersistencia(PersistenciaRoom(contexto))
        } catch (e: Throwable) {
            // se sigue en memoria
        }
    }
}

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
 *     grafoOperacion(navController) { usuarioSeleccionado }
 *
 * `usuarioActual` es una función para que siempre lea la cuenta vigente.
 */
fun NavGraphBuilder.grafoOperacion(
    navController: NavController,
    usuarioActual: () -> UsuarioPrueba?
) {
    // Tareas: el docente gestiona todas (y crea nuevas); el estudiante ve las suyas
    composable(Rutas.TAREAS) {
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            if (usuario.rol == RolUsuario.ADMINISTRADOR) {
                GestionTareasScreen(
                    usuario = usuario,
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
                    usuario = usuario,
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
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            CrearTareaScreen(
                usuario = usuario,
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Estudiante: detalle de una tarea
    composable(RutasOperacion.DETALLE_TAREA) {
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            DetalleTareaScreen(
                usuario = usuario,
                nombreSector = ::nombreDeSector,
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Administrador: validar tareas
    composable(Rutas.VALIDAR_TAREAS) {
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            PanelValidacionScreen(
                usuario = usuario,
                nombreSector = ::nombreDeSector,
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Problemas: lista (estudiante ve los suyos, docente ve todos)
    composable(Rutas.PROBLEMAS) {
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            ListaProblemasScreen(
                usuario = usuario,
                onReportar = { navController.navigate(RutasOperacion.REPORTAR_PROBLEMA) },
                onVolver = { navController.popBackStack() },
                nombreSector = ::nombreDeSector,
                nombreCultivo = ::nombreDeCultivo
            )
        }
    }

    // Problemas: formulario del estudiante
    composable(RutasOperacion.REPORTAR_PROBLEMA) {
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            ReportarProblemaScreen(
                usuario = usuario,
                onVolver = { navController.popBackStack() }
            )
        }
    }

    // Alertas y recordatorios (informativos)
    composable(Rutas.ALERTAS) {
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            PanelAlertasScreen(
                usuario = usuario,
                onVolver = { navController.popBackStack() },
                nombreSector = ::nombreDeSector
            )
        }
    }

    // Sensores simulados (solo docente; siempre como dato de prueba)
    composable(Rutas.SENSORES) {
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            PanelSensoresScreen(
                usuario = usuario,
                onVolver = { navController.popBackStack() },
                nombreSector = ::nombreDeSector
            )
        }
    }

    // Historial cronológico (docente ve todo; estudiante, solo lo suyo)
    composable(Rutas.HISTORIAL) {
        IniciarPersistencia()
        usuarioActual()?.let { usuario ->
            ListaHistorialScreen(
                usuario = usuario,
                onVolver = { navController.popBackStack() },
                nombreSector = ::nombreDeSector
            )
        }
    }
}
