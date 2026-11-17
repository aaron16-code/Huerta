package cl.duoc.huertoescolar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.ui.screens.home.InicioScreen
import cl.duoc.huertoescolar.ui.screens.login.LoginScreen
import cl.duoc.huertoescolar.ui.screens.perfil.PerfilScreen

@Composable
fun Navegacion(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    var usuarioId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    val usuarios = listOf(UsuariosPrueba.docente) +
            UsuariosPrueba.estudiantes

    val usuarioSeleccionado = usuarios.firstOrNull {
        it.id == usuarioId
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onIngresar = { usuario ->
                    usuarioId = usuario.id

                    navController.navigate(Rutas.INICIO) {
                        popUpTo(Rutas.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Rutas.INICIO) {
            usuarioSeleccionado?.let { usuario ->
                InicioScreen(
                    rol = usuario.rol,
                    onIrPerfil = {
                        navController.navigate(Rutas.PERFIL)
                    },
                    onIrSectores = {
                        navController.navigate(Rutas.SECTORES)
                    },
                    onIrCultivos = {
                        navController.navigate(Rutas.CULTIVOS)
                    },
                    onIrTareas = {
                        navController.navigate(Rutas.TAREAS)
                    },
                    onIrValidarTareas = {
                        navController.navigate(Rutas.VALIDAR_TAREAS)
                    },
                    onIrProblemas = {
                        navController.navigate(Rutas.PROBLEMAS)
                    },
                    onIrAlertas = {
                        navController.navigate(Rutas.ALERTAS)
                    },
                    onIrSensores = {
                        navController.navigate(Rutas.SENSORES)
                    },
                    onIrHistorial = {
                        navController.navigate(Rutas.HISTORIAL)
                    },
                    onCerrarSesion = {
                        usuarioId = null

                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.INICIO) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Rutas.PERFIL) {
            usuarioSeleccionado?.let { usuario ->
                PerfilScreen(
                    usuario = usuario,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }

        grafoSectores(
            navController = navController,
            usuarioActual = { usuarioSeleccionado }
        )

        grafoCultivos(
            navController = navController,
            usuarioActual = { usuarioSeleccionado }
        )

        grafoOperacion(
            navController = navController,
            usuarioActual = { usuarioSeleccionado }
        )
    }
}