package cl.duoc.huertoescolar.ui.screens.alertas

import androidx.lifecycle.ViewModel
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.LecturaSensor
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.data.UsuarioPrueba
import kotlinx.coroutines.flow.StateFlow

class AlertasViewModel : ViewModel() {

    val tareas: StateFlow<List<Tarea>> =
        RepositorioOperacion.tareas

    val problemas: StateFlow<List<Problema>> =
        RepositorioOperacion.problemas

    val lecturas: StateFlow<List<LecturaSensor>> =
        RepositorioOperacion.lecturas

    fun secciones(
        todas: List<Tarea>,
        listaProblemas: List<Problema>,
        listaLecturas: List<LecturaSensor>,
        usuario: UsuarioPrueba,
        nombreSector: (Int) -> String,
        ahora: Long = System.currentTimeMillis()
    ): List<SeccionAlerta> {
        return construirAlertas(
            tareas = todas,
            problemas = listaProblemas,
            lecturas = listaLecturas,
            cultivos = RepositorioHuerto.cultivos,
            usuario = usuario,
            nombreSector = nombreSector,
            ahora = ahora
        )
    }
}