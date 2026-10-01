package cl.duoc.huertoescolar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navegacion(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "Inicio",
        modifier = modifier
    ) {
        composable("Inicio") {
            InicioScreen(
                onIrTareas = {
                    navController.navigate("tareas")
                }
            )
        }

        composable("tareas") {
            TareasScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}