package cl.duoc.huertoescolar.data.repository

import cl.duoc.huertoescolar.data.DatosOperacion
import cl.duoc.huertoescolar.data.PersistenciaOperacion
import cl.duoc.huertoescolar.data.semillaLecturas
import cl.duoc.huertoescolar.data.semillaProblemas
import cl.duoc.huertoescolar.data.semillaTareas
import cl.duoc.huertoescolar.domain.model.EstadoLectura
import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.EventoHistorial
import cl.duoc.huertoescolar.domain.model.LecturaSensor
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.RolUsuario
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoEvento
import cl.duoc.huertoescolar.domain.model.TipoProblema
import cl.duoc.huertoescolar.domain.model.TipoTarea
import cl.duoc.huertoescolar.domain.repository.ProblemasRepository
import cl.duoc.huertoescolar.domain.repository.TareasRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Repositorio en memoria (Persona B): tareas, problemas y lecturas simuladas.
 * Trabaja en memoria y permite activar persistencia mediante Room o Retrofit. Guarda una copia
 * de los cambios y la carga al abrir la app. Las pruebas pueden inyectar persistencia simulada.
 * IDs fijos acordados: usuario 1=docente, 2-4=estudiantes; sectores 1-3; cultivos 1-4.
 * Usa `RolUsuario` (ADMINISTRADOR, ESTUDIANTE) definido en Modelos.kt del proyecto.
 */
open class RepositorioOperacionImpl(
    private val alcance: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : TareasRepository, ProblemasRepository {

    private val TAREA_RETIRADA = "Revisar la tapa del invernadero"
    private val HORA = 60L * 60 * 1000
    private val DIA = 24 * HORA
    private fun enDias(n: Int) = System.currentTimeMillis() + n * DIA
    private fun fallo(msg: String) = Result.failure<Unit>(IllegalStateException(msg))

    private val _tareas = MutableStateFlow(semillaTareas())
    override val tareas: StateFlow<List<Tarea>> = _tareas.asStateFlow()

    private val _problemas = MutableStateFlow(semillaProblemas())
    override val problemas: StateFlow<List<Problema>> =
        _problemas.asStateFlow()
    private val _lecturas = MutableStateFlow(semillaLecturas())
    val lecturas: StateFlow<List<LecturaSensor>> = _lecturas.asStateFlow()

    // ---------- PERSISTENCIA (Room) ----------

    @Volatile
    private var persistencia: PersistenciaOperacion? = null
    private var cargaInicial: Job? = null
    private var errorCargaInicial: Exception? = null
    private val mutexReportes = Mutex()


    /**
     * Activa el guardado. Si ya hay datos guardados los carga; si no, guarda los de ejemplo.
     * Si la carga falla se conserva la memoria, pero reportarProblema devuelve el error.
     * Sin activar persistencia se permite trabajar en memoria, por ejemplo en pruebas.
     */
    fun iniciarPersistencia(destino: PersistenciaOperacion) {
        if (persistencia != null) return
        persistencia = destino
        errorCargaInicial = null
        cargaInicial = alcance.launch {
            try {
                val guardado = destino.cargar()
                if (guardado == null) {
                    destino.guardarSemilla(
                        DatosOperacion(
                            _tareas.value,
                            _problemas.value,
                            _lecturas.value
                        )
                    )
                } else {
                    val tareasRetiradas = guardado.tareas.filter { it.descripcion == TAREA_RETIRADA }
                    _tareas.value = guardado.tareas.filterNot { it.descripcion == TAREA_RETIRADA }
                    tareasRetiradas.forEach { destino.eliminarTarea(it.id) }
                    _problemas.value = guardado.problemas
                    // las lecturas son simuladas: si el destino no las guarda, se conservan las de ejemplo
                    if (guardado.lecturas.isNotEmpty()) _lecturas.value = guardado.lecturas
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorCargaInicial = e
                persistencia = null
            }
        }
    }

    private fun guardar(bloque: suspend (PersistenciaOperacion) -> Unit) {
        val destino = persistencia ?: return
        alcance.launch {
            try {
                bloque(destino)
            } catch (e: Throwable) {
                // si no se pudo guardar, el dato sigue en memoria
            }
        }
    }

    // ---------- TAREAS ----------

    override fun obtenerTarea(id: Int): Tarea? = _tareas.value.find { it.id == id }

    // Tarea elegida en la lista (mismo estilo que RepositorioHuerto.seleccionarSector)
    final override var tareaSeleccionadaId: Int? = null
        private set

    override fun seleccionarTarea(id: Int) {
        tareaSeleccionadaId = id
    }

    /** Tareas de un estudiante: las suyas o las de su grupo. */
    fun tareasDe(usuarioId: Int, grupo: String): List<Tarea> =
        _tareas.value.filter {
            it.responsableUsuarioId == usuarioId ||
                    (it.responsableUsuarioId == null && it.grupoAsignado == grupo)
        }

    override fun crearTarea(rol: RolUsuario, nueva: Tarea): Result<Unit> {
        if (rol != RolUsuario.ADMINISTRADOR) return fallo("Solo docentes o talleristas pueden crear tareas")
        if (nueva.descripcion.isBlank()) return fallo("La descripción es obligatoria")
        if (nueva.responsableUsuarioId == null && nueva.grupoAsignado.isNullOrBlank())
            return fallo("Asigna la tarea a un estudiante o a un grupo")
        val id = (_tareas.value.maxOfOrNull { it.id } ?: 0) + 1
        val creada = nueva.copy(id = id, fechaCreacion = System.currentTimeMillis(), estado = EstadoTarea.PENDIENTE)
        _tareas.update { it + creada }
        guardar { destino ->
            val guardada = destino.insertarTarea(creada)
            if (guardada.id != creada.id) reemplazarTarea(creada.id, guardada)
        }
        return Result.success(Unit)
    }

    /** El docente modifica los datos generales sin perder evidencia, estado ni validación. */
    override fun editarTarea(rol: RolUsuario, editada: Tarea): Result<Unit> {
        if (rol != RolUsuario.ADMINISTRADOR) return fallo("Solo docentes o talleristas pueden editar tareas")
        val original = obtenerTarea(editada.id) ?: return fallo("La tarea no existe")
        if (editada.descripcion.isBlank()) return fallo("La descripción es obligatoria")
        if (editada.instrucciones.isBlank()) return fallo("Las instrucciones son obligatorias")
        if (editada.responsableUsuarioId == null && editada.grupoAsignado.isNullOrBlank())
            return fallo("Asigna la tarea a un estudiante o a un grupo")

        actualizarTarea(original.id) {
            original.copy(
                tipo = editada.tipo,
                descripcion = editada.descripcion.trim(),
                instrucciones = editada.instrucciones.trim(),
                sectorId = editada.sectorId,
                cultivoId = editada.cultivoId,
                fechaProgramada = editada.fechaProgramada,
                responsableUsuarioId = editada.responsableUsuarioId,
                grupoAsignado = editada.grupoAsignado
            )
        }
        return Result.success(Unit)
    }

    /** El estudiante marca como realizada: queda PENDIENTE DE VALIDACIÓN (no se valida sola). */
    override fun marcarRealizada(tareaId: Int, usuarioId: Int, grupo: String, comentario: String, fotoUri: String?): Result<Unit> {
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
    override fun validarTarea(tareaId: Int, validadorId: Int, rol: RolUsuario, aprobada: Boolean, comentario: String): Result<Unit> {
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

    /** Cambia la tarea creada en memoria por la que devolvió el servidor (con su id real). */
    private fun reemplazarTarea(idTemporal: Int, real: Tarea) {
        _tareas.update { lista -> lista.map { if (it.id == idTemporal) real else it } }
    }

    private fun actualizarTarea(id: Int, cambio: (Tarea) -> Tarea) {
        _tareas.update { lista -> lista.map { if (it.id == id) cambio(it) else it } }
        obtenerTarea(id)?.let { t -> guardar { it.actualizarTarea(t) } }
    }

    // ---------- PROBLEMAS ----------

    override suspend fun reportarProblema(
        sectorId: Int,
        cultivoId: Int?,
        tipo: TipoProblema,
        descripcion: String,
        reportanteId: Int,
        fotoUri: String?
    ): Result<Unit> {
        if (descripcion.isBlank()) return fallo("Describe brevemente el problema")

        return mutexReportes.withLock {
            try {
                // Espera la carga para evitar que sobrescriba un reporte recién creado.
                cargaInicial?.let { carga ->
                    carga.join()
                    if (carga.isCancelled) {
                        throw CancellationException("La carga inicial fue cancelada")
                    }
                }
                errorCargaInicial?.let { return@withLock Result.failure<Unit>(it) }

                val destino = persistencia
                val nuevo = Problema(
                    id = if (destino == null) {
                        (_problemas.value.maxOfOrNull { it.id } ?: 0) + 1
                    } else {
                        0 // La persistencia devuelve el identificador definitivo.
                    },
                    sectorId = sectorId,
                    cultivoId = cultivoId,
                    tipo = tipo,
                    descripcion = descripcion.trim(),
                    fechaReporte = System.currentTimeMillis(),
                    reportadoPorId = reportanteId,
                    fotoUri = fotoUri
                )
                val guardado = destino?.insertarProblema(nuevo) ?: nuevo

                // Publica solamente después de confirmar el guardado.
                _problemas.update { lista ->
                    lista.filterNot { it.id == guardado.id } + guardado
                }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun actualizarProblema(
        id: Int,
        rol: RolUsuario,
        nuevoEstado: EstadoReporte,
        comentario: String
    ): Result<Unit> {
        if (rol != RolUsuario.ADMINISTRADOR) {
            return fallo("Solo docentes o talleristas pueden dar seguimiento")
        }

        return mutexReportes.withLock {
            try {
                cargaInicial?.let { carga ->
                    carga.join()
                    if (carga.isCancelled) {
                        throw CancellationException("La carga inicial fue cancelada")
                    }
                }
                errorCargaInicial?.let { return@withLock Result.failure<Unit>(it) }

                val original = _problemas.value.find { it.id == id }
                    ?: return@withLock fallo("El reporte no existe")
                val actualizado = original.copy(
                    estado = nuevoEstado,
                    comentarioDocente = comentario.trim(),
                    fechaCierre = if (
                        nuevoEstado == EstadoReporte.ATENDIDO ||
                        nuevoEstado == EstadoReporte.CERRADO
                    ) {
                        original.fechaCierre ?: System.currentTimeMillis()
                    } else {
                        null
                    }
                )

                // Si falla, no se modifica la copia que muestra la aplicación.
                persistencia?.actualizarProblema(actualizado)
                _problemas.update { lista ->
                    lista.map { if (it.id == id) actualizado else it }
                }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
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

        _tareas.value.forEach { tarea ->
            tarea.fechaRealizacion?.let { fecha ->
                eventos.add(
                    EventoHistorial(
                        fecha,
                        TipoEvento.TAREA,
                        "Tarea realizada: ${tarea.tipo.etiqueta}",
                        tarea.comentarioEstudiante,
                        tarea.fotoUri
                    )
                )
            }

            tarea.fechaValidacion?.let { fecha ->
                val resultado = if (tarea.estado == EstadoTarea.VALIDADA) {
                    "validada"
                } else {
                    "rechazada"
                }

                eventos.add(
                    EventoHistorial(
                        fecha,
                        TipoEvento.VALIDACION,
                        "Tarea $resultado: ${tarea.tipo.etiqueta}",
                        tarea.comentarioDocente
                    )
                )
            }
        }

        _problemas.value.forEach { problema ->
            eventos.add(
                EventoHistorial(
                    problema.fechaReporte,
                    TipoEvento.PROBLEMA,
                    "Problema reportado: ${problema.tipo.etiqueta}",
                    problema.descripcion,
                    problema.fotoUri
                )
            )
        }

        return eventos.sortedByDescending { it.fecha }
    }

}

object RepositorioOperacion : RepositorioOperacionImpl()