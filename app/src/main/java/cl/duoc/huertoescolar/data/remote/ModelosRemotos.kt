package cl.duoc.huertoescolar.data.remote

import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoProblema
import cl.duoc.huertoescolar.domain.model.TipoTarea

// Así viajan las tareas y los problemas por la red (JSON del microservicio).
// Los nombres de los campos son los mismos que devuelve la API.

data class TareaRemota(
    val id: Int? = null,
    val tipo: TipoTarea,
    val descripcion: String,
    val instrucciones: String,
    val sectorId: Int,
    val cultivoId: Int? = null,
    val fechaCreacion: Long? = null,
    val fechaProgramada: Long,
    val responsableUsuarioId: Int? = null,
    val grupoAsignado: String? = null,
    val creadaPorId: Int,
    val estado: EstadoTarea? = null,
    val fechaRealizacion: Long? = null,
    val realizadaPorId: Int? = null,
    val fotoUri: String? = null,
    val comentarioEstudiante: String? = null,
    val comentarioDocente: String? = null,
    val validadorId: Int? = null,
    val fechaValidacion: Long? = null
)

data class ProblemaRemoto(
    val id: Int? = null,
    val sectorId: Int,
    val cultivoId: Int? = null,
    val tipo: TipoProblema,
    val descripcion: String,
    val fechaReporte: Long? = null,
    val reportadoPorId: Int,
    val fotoUri: String? = null,
    val estado: EstadoReporte? = null,
    val comentarioDocente: String? = null,
    val fechaCierre: Long? = null
)

fun TareaRemota.aModelo() = Tarea(
    id = id ?: 0,
    tipo = tipo,
    descripcion = descripcion,
    instrucciones = instrucciones,
    sectorId = sectorId,
    cultivoId = cultivoId,
    fechaCreacion = fechaCreacion ?: 0L,
    fechaProgramada = fechaProgramada,
    responsableUsuarioId = responsableUsuarioId,
    grupoAsignado = grupoAsignado,
    creadaPorId = creadaPorId,
    estado = estado ?: EstadoTarea.PENDIENTE,
    fechaRealizacion = fechaRealizacion,
    realizadaPorId = realizadaPorId,
    fotoUri = fotoUri,
    comentarioEstudiante = comentarioEstudiante ?: "",
    comentarioDocente = comentarioDocente ?: "",
    validadorId = validadorId,
    fechaValidacion = fechaValidacion
)

fun Tarea.aRemota() = TareaRemota(
    id = id,
    tipo = tipo,
    descripcion = descripcion,
    instrucciones = instrucciones,
    sectorId = sectorId,
    cultivoId = cultivoId,
    fechaCreacion = fechaCreacion,
    fechaProgramada = fechaProgramada,
    responsableUsuarioId = responsableUsuarioId,
    grupoAsignado = grupoAsignado,
    creadaPorId = creadaPorId,
    estado = estado,
    fechaRealizacion = fechaRealizacion,
    realizadaPorId = realizadaPorId,
    fotoUri = fotoUri,
    comentarioEstudiante = comentarioEstudiante,
    comentarioDocente = comentarioDocente,
    validadorId = validadorId,
    fechaValidacion = fechaValidacion
)

fun ProblemaRemoto.aModelo() = Problema(
    id = id ?: 0,
    sectorId = sectorId,
    cultivoId = cultivoId,
    tipo = tipo,
    descripcion = descripcion,
    fechaReporte = fechaReporte ?: 0L,
    reportadoPorId = reportadoPorId,
    fotoUri = fotoUri,
    estado = estado ?: EstadoReporte.INFORMADO,
    comentarioDocente = comentarioDocente ?: "",
    fechaCierre = fechaCierre
)

fun Problema.aRemoto() = ProblemaRemoto(
    id = id,
    sectorId = sectorId,
    cultivoId = cultivoId,
    tipo = tipo,
    descripcion = descripcion,
    fechaReporte = fechaReporte,
    reportadoPorId = reportadoPorId,
    fotoUri = fotoUri,
    estado = estado,
    comentarioDocente = comentarioDocente,
    fechaCierre = fechaCierre
)
