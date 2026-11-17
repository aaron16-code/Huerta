package cl.duoc.huertoescolar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.ui.screens.sectores.CrearSectorScreen
import cl.duoc.huertoescolar.ui.screens.sectores.DetalleSectorScreen
import cl.duoc.huertoescolar.ui.screens.sectores.EditarSectorScreen
import cl.duoc.huertoescolar.ui.screens.sectores.SectoresScreen

fun NavGraphBuilder.grafoSectores(
    navController: NavController,
    usuarioActual: () -> UsuarioPrueba?
) {
    composable(Rutas.SECTORES) {
        usuarioActual()?.let { usuario ->
            SectoresScreen(
                rol = usuario.rol,
                onCrearSector = {
                    navController.navigate(Rutas.CREAR_SECTOR)
                },
                onEditarSector = { sector ->
                    RepositorioHuerto.seleccionarSector(sector)
                    navController.navigate(Rutas.EDITAR_SECTOR)
                },
                onVerDetalle = { sector ->
                    RepositorioHuerto.seleccionarSector(sector)
                    navController.navigate(Rutas.DETALLE_SECTOR)
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }

    composable(Rutas.CREAR_SECTOR) {
        CrearSectorScreen(
            onSectorCreado = {
                navController.popBackStack()
            },
            onVolver = {
                navController.popBackStack()
            }
        )
    }

    composable(Rutas.EDITAR_SECTOR) {
        EditarSectorScreen(
            onSectorActualizado = {
                navController.popBackStack()
            },
            onVolver = {
                navController.popBackStack()
            }
        )
    }

    composable(Rutas.DETALLE_SECTOR) {
        DetalleSectorScreen(
            onVolver = {
                navController.popBackStack()
            }
        )
    }
}