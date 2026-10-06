package cl.duoc.huertoescolar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.huertoescolar.ui.screens.home.InicioScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cl.duoc.huertoescolar.RolUsuario
import cl.duoc.huertoescolar.ui.screens.login.LoginScreen
import cl.duoc.huertoescolar.ui.screens.sectores.SectoresScreen

@Composable
fun Navegacion(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    var rolSeleccionado by remember {
        mutableStateOf<RolUsuario?>(null)
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onIngresar = { rol ->
                    rolSeleccionado = rol

                    navController.navigate(Rutas.INICIO) {
                        popUpTo(Rutas.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Rutas.INICIO) {
            rolSeleccionado?.let { rol ->
                InicioScreen(
                    rol = rol,
                    onIrSectores = {
                        navController.navigate(Rutas.SECTORES)
                    },
                    onIrTareas = {
                        navController.navigate(Rutas.TAREAS)
                    }
                )
            }
        }

        composable(Rutas.SECTORES) {
            rolSeleccionado?.let { rol ->
                SectoresScreen(
                    rol = rol,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Rutas.TAREAS) {
            TareasScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}