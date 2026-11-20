package cl.duoc.huertoescolar.ui.screens.login

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import cl.duoc.huertoescolar.data.remote.HuertoRetrofitClient
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.data.remote.PersistenciaRetrofit
import kotlinx.coroutines.CancellationException
import androidx.compose.ui.Modifier
import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.ui.components.BotonPrincipal
import cl.duoc.huertoescolar.ui.components.BotonSecundario
import cl.duoc.huertoescolar.ui.components.PantallaHuerto
import cl.duoc.huertoescolar.ui.components.TarjetaHuerto

@Composable
fun LoginScreen(
    onIngresar: (UsuarioPrueba) -> Unit,
    modifier: Modifier = Modifier
) {
    var intento by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(intento) {
        cargando = true; error = null
        try {
            UsuariosPrueba.actualizarCuentas(HuertoRetrofitClient.api.obtenerUsuarios().map { it.aModelo() })
            RepositorioHuerto.sincronizar()
            RepositorioOperacion.conectar(PersistenciaRetrofit())
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { error = "No se pudo conectar al servidor. Revisa MariaDB y Spring Boot." }
        finally { cargando = false }
    }
    PantallaHuerto(
        titulo = "Huerto Escolar",
        subtitulo = "Gestión colaborativa de cultivos y actividades",
        modifier = modifier
    ) {
        TarjetaHuerto {
            Text(
                text = "Cuenta de demostración",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Selecciona una cuenta ficticia para ingresar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when {
                cargando -> Text("Conectando al servidor...")
                error != null -> {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    BotonSecundario(texto = "Reintentar", onClick = { intento++ })
                }
                else -> UsuariosPrueba.todos.forEach { cuenta ->
                    BotonPrincipal(texto = "${cuenta.rol.name} · ${cuenta.nombre}", onClick = { onIngresar(cuenta) })
                }
            }
        }
        Text(
            text = "Todos los usuarios y datos utilizados son ficticios.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
