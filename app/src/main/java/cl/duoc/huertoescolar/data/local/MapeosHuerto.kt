package cl.duoc.huertoescolar.data.local

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.Sector

internal fun Sector.toEntity() = SectorEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = estado.name
)

internal fun SectorEntity.toModelo() = Sector(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = EstadoSector.valueOf(estado),
    responsable = "Docente de prueba"
)

internal fun Cultivo.toEntity() = CultivoEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    sectorId = sectorId,
    fechaSiembra = fechaSiembra,
    fechaCosechaEstimada = fechaCosechaEstimada,
    frecuenciaRiegoDias = frecuenciaRiegoDias,
    estado = estado.name,
    observaciones = observaciones,
    fotoUri = fotoUri
)

internal fun CultivoEntity.toModelo() = Cultivo(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    sectorId = sectorId,
    fechaSiembra = fechaSiembra,
    fechaCosechaEstimada = fechaCosechaEstimada,
    frecuenciaRiegoDias = frecuenciaRiegoDias,
    estado = EstadoCultivo.valueOf(estado),
    observaciones = observaciones,
    fotoUri = fotoUri
)