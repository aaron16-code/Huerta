package cl.duoc.huertoescolar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.screens.cultivos.CrearCultivoScreen
import cl.duoc.huertoescolar.ui.screens.cultivos.CultivosScreen
import cl.duoc.huertoescolar.ui.screens.cultivos.DetalleCultivoScreen
import cl.duoc.huertoescolar.ui.screens.cultivos.EditarCultivoScreen

fun NavGraphBuilder.grafoCultivos(
    navController: NavController,
    usuarioActual: () -> UsuarioPrueba?
) {
    composable(Rutas.CULTIVOS) {
        usuarioActual()?.let { usuario ->
            CultivosScreen(
                rol = usuario.rol,
                onCrearCultivo = {
                    navController.navigate(Rutas.CREAR_CULTIVO)
                },
                onEditarCultivo = { cultivo ->
                    RepositorioHuerto.seleccionarCultivo(cultivo)
                    navController.navigate(Rutas.EDITAR_CULTIVO)
                },
                onVerDetalle = { cultivo ->
                    RepositorioHuerto.seleccionarCultivo(cultivo)
                    navController.navigate(Rutas.DETALLE_CULTIVO)
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }

    composable(Rutas.CREAR_CULTIVO) {
        CrearCultivoScreen(
            onCultivoCreado = {
                navController.popBackStack()
            },
            onVolver = {
                navController.popBackStack()
            }
        )
    }

    composable(Rutas.EDITAR_CULTIVO) {
        EditarCultivoScreen(
            onCultivoActualizado = {
                navController.popBackStack()
            },
            onVolver = {
                navController.popBackStack()
            }
        )
    }

    composable(Rutas.DETALLE_CULTIVO) {
        DetalleCultivoScreen(
            onVolver = {
                navController.popBackStack()
            }
        )
    }
}