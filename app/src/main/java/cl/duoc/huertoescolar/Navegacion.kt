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
import cl.duoc.huertoescolar.ui.screens.sectores.CrearSectorScreen
import cl.duoc.huertoescolar.ui.screens.cultivos.CrearCultivoScreen
import cl.duoc.huertoescolar.ui.screens.cultivos.CultivosScreen
import cl.duoc.huertoescolar.ui.screens.cultivos.DetalleCultivoScreen
import cl.duoc.huertoescolar.ui.screens.cultivos.EditarCultivoScreen
import cl.duoc.huertoescolar.ui.screens.sectores.DetalleSectorScreen
import cl.duoc.huertoescolar.ui.screens.sectores.EditarSectorScreen
import cl.duoc.huertoescolar.ui.screens.alertas.AlertasScreen
import cl.duoc.huertoescolar.ui.screens.historial.HistorialScreen
import cl.duoc.huertoescolar.ui.screens.perfil.PerfilScreen
import cl.duoc.huertoescolar.ui.screens.problemas.ProblemasScreen
import cl.duoc.huertoescolar.ui.screens.sensores.SensoresScreen
import cl.duoc.huertoescolar.ui.screens.tareas.ValidarTareasScreen

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
                        rolSeleccionado = null

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

        composable(Rutas.SECTORES) {
            rolSeleccionado?.let { rol ->
                SectoresScreen(
                    rol = rol,
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

        composable(Rutas.TAREAS) {
            TareasScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
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
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.CULTIVOS) {
            rolSeleccionado?.let { rol ->
                CultivosScreen(
                    rol = rol,
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
                onCultivoCreado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.EDITAR_CULTIVO) {
            EditarCultivoScreen(
                onCultivoActualizado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.DETALLE_CULTIVO) {
            DetalleCultivoScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.PERFIL) {
            rolSeleccionado?.let { rol ->
                PerfilScreen(
                    rol = rol,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Rutas.VALIDAR_TAREAS) {
            ValidarTareasScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.PROBLEMAS) {
            ProblemasScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.ALERTAS) {
            AlertasScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.SENSORES) {
            SensoresScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.HISTORIAL) {
            HistorialScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

    }
}
