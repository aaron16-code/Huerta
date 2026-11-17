package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.domain.model.EstadoReporte
import cl.duoc.huertoescolar.domain.model.EstadoTarea
import cl.duoc.huertoescolar.domain.model.LecturaSensor
import cl.duoc.huertoescolar.domain.model.Problema
import cl.duoc.huertoescolar.domain.model.Tarea
import cl.duoc.huertoescolar.domain.model.TipoProblema
import cl.duoc.huertoescolar.domain.model.TipoTarea
import cl.duoc.huertoescolar.domain.model.calcularEstadoLectura

private const val HORA = 60L * 60 * 1000
private const val DIA = 24 * HORA

private fun enDias(dias: Int): Long =
    System.currentTimeMillis() + dias * DIA

private fun haceHoras(horas: Int): Long =
    System.currentTimeMillis() - horas * HORA

internal fun semillaTareas(): List<Tarea> = listOf(
    Tarea(
        1, TipoTarea.RIEGO,
        "Regar la lechuga",
        "Riega con regadera hasta humedecer la tierra, sin encharcar.",
        1, 1, enDias(-6), enDias(-2),
        responsableUsuarioId = 2,
        creadaPorId = 1
    ),
    Tarea(
        2, TipoTarea.RETIRO_MALEZAS,
        "Retirar malezas del sector norte",
        "Saca las malezas a mano y ponlas en el balde.",
        1, null, enDias(-1), enDias(1),
        responsableUsuarioId = 2,
        creadaPorId = 1
    ),
    Tarea(
        3, TipoTarea.OBSERVACION,
        "Observar el tomate",
        "Revisa hojas y tallo y anota cómo se ve la planta.",
        2, 2, enDias(-4), enDias(-1),
        responsableUsuarioId = 3,
        creadaPorId = 1,
        estado = EstadoTarea.PENDIENTE_VALIDACION,
        fechaRealizacion = haceHoras(5),
        realizadaPorId = 3,
        fotoUri = "foto_prueba_tomate",
        comentarioEstudiante = "Las hojas se ven verdes y firmes."
    ),
    Tarea(
        4, TipoTarea.SIEMBRA,
        "Sembrar zanahoria",
        "Haz surcos de 2 cm y deja caer las semillas separadas.",
        1, 3, enDias(-9), enDias(-5),
        responsableUsuarioId = 2,
        creadaPorId = 1,
        estado = EstadoTarea.VALIDADA,
        fechaRealizacion = enDias(-5),
        realizadaPorId = 2,
        comentarioEstudiante = "Listo, sembrado.",
        comentarioDocente = "Muy bien, buen espaciado.",
        validadorId = 1,
        fechaValidacion = enDias(-4)
    ),
    Tarea(
        5, TipoTarea.RIEGO,
        "Regar la albahaca del invernadero",
        "Un vaso de agua por maceta.",
        3, 4, enDias(-1), enDias(1),
        grupoAsignado = "Taller Huerto A",
        creadaPorId = 1
    ),
    Tarea(
        6, TipoTarea.LIMPIEZA,
        "Limpiar el sector sur",
        "Recoge hojas secas y restos.",
        2, null, enDias(-7), enDias(-3),
        responsableUsuarioId = 2,
        creadaPorId = 1,
        estado = EstadoTarea.RECHAZADA,
        fechaRealizacion = enDias(-3),
        realizadaPorId = 2,
        comentarioEstudiante = "Terminé.",
        comentarioDocente = "Faltó retirar los restos del fondo. Repítela.",
        validadorId = 1,
        fechaValidacion = enDias(-2)
    ),
    Tarea(
        8, TipoTarea.COSECHA,
        "Cosechar la lechuga",
        "Corta las hojas exteriores con cuidado.",
        1, 1, enDias(-1), enDias(6),
        responsableUsuarioId = 4,
        creadaPorId = 1
    )
)

internal fun semillaProblemas(): List<Problema> = listOf(
    Problema(
        1, 1, 1, TipoProblema.FALTA_AGUA,
        "La tierra de la lechuga está muy seca.",
        haceHoras(8), 2, "foto_prueba_lechuga"
    ),
    Problema(
        2, 2, 2, TipoProblema.PLAGA_SIMULADA,
        "Se ven manchas en las hojas del tomate (prueba).",
        haceHoras(30), 3, null,
        EstadoReporte.EN_REVISION,
        "Lo estamos revisando."
    ),
    Problema(
        3, 3, null, TipoProblema.DETERIORO_SECTOR,
        "La tapa del invernadero estaba floja.",
        haceHoras(80), 2, null,
        EstadoReporte.CERRADO,
        "Se ajustó la tapa.",
        haceHoras(50)
    )
)

private fun lectura(
    id: Int,
    sector: Int,
    horas: Int,
    suelo: Int,
    temperatura: Double,
    humedadAmbiente: Int
): LecturaSensor = LecturaSensor(
    id = id,
    sectorId = sector,
    fecha = haceHoras(horas),
    humedadSuelo = suelo,
    temperaturaAmbiente = temperatura,
    humedadAmbiente = humedadAmbiente,
    estado = calcularEstadoLectura(
        suelo,
        temperatura,
        humedadAmbiente
    )
)

internal fun semillaLecturas(): List<LecturaSensor> = listOf(
    lectura(1, 1, 1, 55, 21.5, 60),
    lectura(2, 2, 1, 22, 24.0, 55),
    lectura(3, 3, 1, 60, 33.5, 70),
    lectura(4, 1, 25, 48, 19.0, 65),
    lectura(5, 2, 2, 105, 22.0, 50)
)