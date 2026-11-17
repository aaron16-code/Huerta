package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.domain.model.Cultivo
import cl.duoc.huertoescolar.domain.model.EstadoCultivo
import cl.duoc.huertoescolar.domain.model.EstadoSector
import cl.duoc.huertoescolar.domain.model.Sector

internal fun listaSectoresInicial(): List<Sector> = listOf(
    Sector(
        id = 1,
        nombre = "Bancal 1",
        descripcion = "Sector de hortalizas",
        ubicacion = "Zona norte",
        estado = EstadoSector.EN_USO,
        responsable = "Docente de prueba"
    )
)

internal fun listaCultivosInicial(): List<Cultivo> = listOf(
    Cultivo(
        id = 1,
        nombre = "Lechugas",
        descripcion = "Cultivo de prueba",
        sectorId = 1,
        fechaSiembra = "01/10/2026",
        fechaCosechaEstimada = "30/11/2026",
        frecuenciaRiegoDias = 2,
        estado = EstadoCultivo.EN_CRECIMIENTO,
        observaciones = "Crecimiento normal"
    )
)