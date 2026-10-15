package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repositorio en memoria (Persona B): tareas, problemas y lecturas simuladas.
 * Todo es ficticio. Más adelante se puede reemplazar por Room sin tocar las pantallas.
 * IDs fijos acordados: usuarios 1=docente, 2 y 3=estudiantes; sectores 1-3; cultivos 1-4.
 * Usa `RolUsuario` (ADMINISTRADOR, ESTUDIANTE) definido en Modelos.kt del proyecto.
 */
object RepositorioOperacion {

    private const val HORA = 60L * 60 * 1000
    private const val DIA = 24 * HORA
    private fun enDias(n: Int) = System.currentTimeMillis() + n * DIA
    private fun haceHoras(h: Int) = System.currentTimeMillis() - h * HORA
    private fun fallo(msg: String) = Result.failure<Unit>(IllegalStateException(msg))

    private val _tareas = MutableStateFlow(semillaTareas())
    val tareas: StateFlow<List<Tarea>> = _tareas.asStateFlow()

    private val _problemas = MutableStateFlow(semillaProblemas())
    val problemas: StateFlow<List<Problema>> = _problemas.asStateFlow()

    private val _lecturas = MutableStateFlow(semillaLecturas())
    val lecturas: StateFlow<List<LecturaSensor>> = _lecturas.asStateFlow()

    // ---------- TAREAS ----------

    fun obtenerTarea(id: Int): Tarea? = _tareas.value.find { it.id == id }

    // Tarea elegida en la lista (mismo estilo que RepositorioHuerto.seleccionarSector)
    var tareaSeleccionadaId: Int? = null
        private set

    fun seleccionarTarea(id: Int) {
        tareaSeleccionadaId = id
    }

    /** Tareas de un estudiante: las suyas o las de su grupo. */
    fun tareasDe(usuarioId: Int, grupo: String): List<Tarea> =
        _tareas.value.filter {
            it.responsableUsuarioId == usuarioId ||
                    (it.responsableUsuarioId == null && it.grupoAsignado == grupo)
        }

    fun crearTarea(rol: RolUsuario, nueva: Tarea): Result<Unit> {
        if (rol != RolUsuario.ADMINISTRADOR) return fallo("Solo docentes o talleristas pueden crear tareas")
        if (nueva.descripcion.isBlank()) return fallo("La descripción es obligatoria")
        if (nueva.responsableUsuarioId == null && nueva.grupoAsignado.isNullOrBlank())
            return fallo("Asigna la tarea a un estudiante o a un grupo")
        val id = (_tareas.value.maxOfOrNull { it.id } ?: 0) + 1
        _tareas.update { it + nueva.copy(id = id, fechaCreacion = System.currentTimeMillis(), estado = EstadoTarea.PENDIENTE) }
        return Result.success(Unit)
    }

    /** El estudiante marca como realizada: queda PENDIENTE DE VALIDACIÓN (no se valida sola). */
    fun marcarRealizada(tareaId: Int, usuarioId: Int, grupo: String, comentario: String, fotoUri: String?): Result<Unit> {
        val t = obtenerTarea(tareaId) ?: return fallo("La tarea no existe")
        val esSuya = t.responsableUsuarioId == usuarioId ||
                (t.responsableUsuarioId == null && t.grupoAsignado == grupo)
        if (!esSuya) return fallo("Esta tarea no está asignada a ti")
        val puedeEnviar = t.estado == EstadoTarea.PENDIENTE || t.estado == EstadoTarea.EN_DESARROLLO ||
                t.estado == EstadoTarea.RECHAZADA // rechazada = se puede repetir
        if (!puedeEnviar) return fallo("La tarea ya fue enviada o validada")
        actualizarTarea(tareaId) {
            it.copy(
                estado = EstadoTarea.PENDIENTE_VALIDACION,
                fechaRealizacion = System.currentTimeMillis(),
                realizadaPorId = usuarioId,
                fotoUri = fotoUri ?: it.fotoUri,
                comentarioEstudiante = comentario,
                validadorId = null,
                fechaValidacion = null
            )
        }
        return Result.success(Unit)
    }

    /** Validar o rechazar. Reglas del caso: solo ADMINISTRADOR y nunca su propia actividad. */
    fun validarTarea(tareaId: Int, validadorId: Int, rol: RolUsuario, aprobada: Boolean, comentario: String): Result<Unit> {
        if (rol != RolUsuario.ADMINISTRADOR) return fallo("Solo docentes o talleristas pueden validar")
        val t = obtenerTarea(tareaId) ?: return fallo("La tarea no existe")
        if (t.estado != EstadoTarea.PENDIENTE_VALIDACION) return fallo("La tarea no está pendiente de validación")
        if (t.realizadaPorId == validadorId) return fallo("No puedes validar tu propia actividad")
        if (!aprobada && comentario.isBlank()) return fallo("Escribe un comentario al rechazar")
        actualizarTarea(tareaId) {
            it.copy(
                estado = if (aprobada) EstadoTarea.VALIDADA else EstadoTarea.RECHAZADA,
                comentarioDocente = comentario,
                validadorId = validadorId,
                fechaValidacion = System.currentTimeMillis()
            )
        }
        return Result.success(Unit)
    }

    private fun actualizarTarea(id: Int, cambio: (Tarea) -> Tarea) =
        _tareas.update { lista -> lista.map { if (it.id == id) cambio(it) else it } }

    // ---------- PROBLEMAS ----------

    fun reportarProblema(sectorId: Int, cultivoId: Int?, tipo: TipoProblema, descripcion: String, reportanteId: Int, fotoUri: String?): Result<Unit> {
        if (descripcion.isBlank()) return fallo("Describe brevemente el problema")
        val id = (_problemas.value.maxOfOrNull { it.id } ?: 0) + 1
        _problemas.update {
            it + Problema(id, sectorId, cultivoId, tipo, descripcion, System.currentTimeMillis(), reportanteId, fotoUri)
        }
        return Result.success(Unit)
    }

    fun actualizarProblema(id: Int, rol: RolUsuario, nuevoEstado: EstadoReporte, comentario: String): Result<Unit> {
        if (rol != RolUsuario.ADMINISTRADOR) return fallo("Solo docentes o talleristas pueden dar seguimiento")
        _problemas.update { lista ->
            lista.map {
                if (it.id != id) it else it.copy(
                    estado = nuevoEstado,
                    comentarioDocente = comentario,
                    fechaCierre = if (nuevoEstado == EstadoReporte.ATENDIDO || nuevoEstado == EstadoReporte.CERRADO)
                        (it.fechaCierre ?: System.currentTimeMillis()) else null
                )
            }
        }
        return Result.success(Unit)
    }

    // ---------- ALERTAS (solo informativas) ----------

    fun tareasAtrasadas(): List<Tarea> = _tareas.value.filter { it.estaAtrasada() }.sortedBy { it.fechaProgramada }

    /** Próximas tareas de un tipo (RIEGO, COSECHA, MANTENIMIENTO) dentro de N días. */
    fun proximas(tipo: TipoTarea, dias: Int = 7): List<Tarea> {
        val ahora = System.currentTimeMillis()
        return _tareas.value.filter {
            it.tipo == tipo && it.fechaProgramada in ahora..enDias(dias) &&
                    (it.estado == EstadoTarea.PENDIENTE || it.estado == EstadoTarea.EN_DESARROLLO)
        }.sortedBy { it.fechaProgramada }
    }

    fun pendientesDeValidacion(): List<Tarea> = _tareas.value.filter { it.estado == EstadoTarea.PENDIENTE_VALIDACION }

    fun problemasPendientes(): List<Problema> =
        _problemas.value.filter { it.estado == EstadoReporte.INFORMADO || it.estado == EstadoReporte.EN_REVISION }

    fun lecturasFueraDeRango(): List<LecturaSensor> =
        _lecturas.value.filter { it.estado == EstadoLectura.BAJO || it.estado == EstadoLectura.ALTO }

    // ---------- SENSORES SIMULADOS ----------

    fun ultimaLectura(sectorId: Int): LecturaSensor? =
        _lecturas.value.filter { it.sectorId == sectorId }.maxByOrNull { it.fecha }

    // ---------- HISTORIAL (cronológico, más reciente primero) ----------

    fun historial(): List<EventoHistorial> {
        val eventos = mutableListOf<EventoHistorial>()
        _tareas.value.forEach { t ->
            t.fechaRealizacion?.let {
                eventos += EventoHistorial(it, TipoEvento.TAREA, "Tarea realizada: ${t.tipo.etiqueta}", t.comentarioEstudiante, t.fotoUri)
            }
            t.fechaValidacion?.let {
                val res = if (t.estado == EstadoTarea.VALIDADA) "validada" else "rechazada"
                eventos += EventoHistorial(it, TipoEvento.VALIDACION, "Tarea $res: ${t.tipo.etiqueta}", t.comentarioDocente)
            }
        }
        _problemas.value.forEach { p ->
            eventos += EventoHistorial(p.fechaReporte, TipoEvento.PROBLEMA, "Problema reportado: ${p.tipo.etiqueta}", p.descripcion, p.fotoUri)
        }
        return eventos.sortedByDescending { it.fecha }
    }

    // ---------- DATOS FICTICIOS ----------

    private fun semillaTareas() = listOf(
        Tarea(1, TipoTarea.RIEGO, "Regar la lechuga", "Riega con regadera hasta humedecer la tierra, sin encharcar.",
            1, 1, enDias(-6), enDias(-2), responsableUsuarioId = 2, creadaPorId = 1),
        Tarea(2, TipoTarea.RETIRO_MALEZAS, "Retirar malezas del sector norte", "Saca las malezas a mano y ponlas en el balde.",
            1, null, enDias(-1), enDias(1), responsableUsuarioId = 2, creadaPorId = 1),
        Tarea(3, TipoTarea.OBSERVACION, "Observar el tomate", "Revisa hojas y tallo y anota cómo se ve la planta.",
            2, 2, enDias(-4), enDias(-1), responsableUsuarioId = 3, creadaPorId = 1,
            estado = EstadoTarea.PENDIENTE_VALIDACION, fechaRealizacion = haceHoras(5), realizadaPorId = 3,
            fotoUri = "foto_prueba_tomate", comentarioEstudiante = "Las hojas se ven verdes y firmes."),
        Tarea(4, TipoTarea.SIEMBRA, "Sembrar zanahoria", "Haz surcos de 2 cm y deja caer las semillas separadas.",
            1, 3, enDias(-9), enDias(-5), responsableUsuarioId = 2, creadaPorId = 1,
            estado = EstadoTarea.VALIDADA, fechaRealizacion = enDias(-5), realizadaPorId = 2,
            comentarioEstudiante = "Listo, sembrado.", comentarioDocente = "Muy bien, buen espaciado.",
            validadorId = 1, fechaValidacion = enDias(-4)),
        Tarea(5, TipoTarea.RIEGO, "Regar la albahaca del invernadero", "Un vaso de agua por maceta.",
            3, 4, enDias(-1), enDias(1), grupoAsignado = "Taller Huerto A", creadaPorId = 1),
        Tarea(6, TipoTarea.LIMPIEZA, "Limpiar el sector sur", "Recoge hojas secas y restos.",
            2, null, enDias(-7), enDias(-3), responsableUsuarioId = 3, creadaPorId = 1,
            estado = EstadoTarea.RECHAZADA, fechaRealizacion = enDias(-3), realizadaPorId = 3,
            comentarioEstudiante = "Terminé.", comentarioDocente = "Faltó retirar los restos del fondo. Repítela.",
            validadorId = 1, fechaValidacion = enDias(-2)),
        Tarea(7, TipoTarea.MANTENIMIENTO, "Revisar la tapa del invernadero", "Revisa que cierre bien y avisa si ves daños.",
            3, null, enDias(-1), enDias(3), responsableUsuarioId = 2, creadaPorId = 1),
        Tarea(8, TipoTarea.COSECHA, "Cosechar la lechuga", "Corta las hojas exteriores con cuidado.",
            1, 1, enDias(-1), enDias(6), responsableUsuarioId = 3, creadaPorId = 1)
    )

    private fun semillaProblemas() = listOf(
        Problema(1, 1, 1, TipoProblema.FALTA_AGUA, "La tierra de la lechuga está muy seca.",
            haceHoras(8), 2, "foto_prueba_lechuga"),
        Problema(2, 2, 2, TipoProblema.PLAGA_SIMULADA, "Se ven manchas en las hojas del tomate (prueba).",
            haceHoras(30), 3, null, EstadoReporte.EN_REVISION, "Lo estamos revisando."),
        Problema(3, 3, null, TipoProblema.DETERIORO_SECTOR, "La tapa del invernadero estaba floja.",
            haceHoras(80), 2, null, EstadoReporte.CERRADO, "Se ajustó la tapa.", haceHoras(50))
    )

    private fun lectura(id: Int, sector: Int, horas: Int, suelo: Int, temp: Double, amb: Int) =
        LecturaSensor(id, sector, haceHoras(horas), suelo, temp, amb, calcularEstadoLectura(suelo, temp, amb))

    private fun semillaLecturas() = listOf(
        lectura(1, 1, 1, 55, 21.5, 60),   // normal
        lectura(2, 2, 1, 22, 24.0, 55),   // humedad de suelo baja
        lectura(3, 3, 1, 60, 33.5, 70),   // temperatura alta
        lectura(4, 1, 25, 48, 19.0, 65),  // normal (día anterior)
        lectura(5, 2, 2, 105, 22.0, 50)   // valor inválido -> pendiente de revisión
    )
}