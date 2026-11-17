package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.UsuarioPrueba
import cl.duoc.huertoescolar.data.UsuariosPrueba
import cl.duoc.huertoescolar.domain.model.*
import cl.duoc.huertoescolar.ui.screens.alertas.construirAlertas
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class CalculosAlertasTest {

    private val dia = 86_400_000L

    private val ahora = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.US
    ).parse("01/06/2026")!!.time

    private fun tarea(
        id: Int,
        tipo: TipoTarea = TipoTarea.RIEGO,
        dias: Int = 1,
        estado: EstadoTarea = EstadoTarea.PENDIENTE,
        responsable: Int? = 2,
        grupo: String? = null
    ) = Tarea(
        id = id,
        tipo = tipo,
        descripcion = "Actividad $id",
        instrucciones = "Instrucciones",
        sectorId = 1,
        cultivoId = null,
        fechaCreacion = ahora,
        fechaProgramada = ahora + dias * dia,
        responsableUsuarioId = responsable,
        grupoAsignado = grupo,
        creadaPorId = 1,
        estado = estado
    )

    private fun alertas(
        usuario: UsuarioPrueba = UsuariosPrueba.docente,
        tareas: List<Tarea> = emptyList(),
        problemas: List<Problema> = emptyList(),
        lecturas: List<LecturaSensor> = emptyList(),
        cultivos: List<Cultivo> = emptyList()
    ) = construirAlertas(
        tareas = tareas,
        problemas = problemas,
        lecturas = lecturas,
        cultivos = cultivos,
        usuario = usuario,
        nombreSector = { "Sector $it" },
        ahora = ahora
    )

    @Test
    fun listasVaciasGeneranSeccionesSinAlertas() {
        val docente = alertas()
        val estudiante = alertas(usuario = UsuariosPrueba.estudiante1)

        assertEquals(8, docente.size)
        assertEquals(5, estudiante.size)
        assertTrue(docente.all { it.lineas.isEmpty() })
        assertTrue(docente.all { it.textoVacio.isNotBlank() })
    }

    @Test
    fun riegosProximosIncluyenLimitesYSeOrdenanPorFecha() {
        val resultado = alertas(
            tareas = listOf(
                tarea(7, dias = 7),
                tarea(8, dias = 8),
                tarea(2, dias = 2, estado = EstadoTarea.EN_DESARROLLO),
                tarea(0, dias = 0),
                tarea(9, dias = 1, estado = EstadoTarea.VALIDADA)
            )
        ).first().lineas

        assertEquals(3, resultado.size)
        assertTrue(resultado[0].startsWith("Actividad 0 · Sector 1"))
        assertTrue(resultado[1].startsWith("Actividad 2 · Sector 1"))
        assertTrue(resultado[2].startsWith("Actividad 7 · Sector 1"))
    }

    @Test
    fun estudianteVeAsignacionesPropiasYDeSuGrupo() {
        val tareas = listOf(
            tarea(1, responsable = 2),
            tarea(
                2,
                responsable = null,
                grupo = UsuariosPrueba.estudiante1.grupo
            ),
            tarea(3, responsable = 3),
            tarea(
                4,
                responsable = null,
                grupo = UsuariosPrueba.estudiante3.grupo
            ),
            tarea(
                5,
                responsable = 3,
                grupo = UsuariosPrueba.estudiante1.grupo
            )
        )

        val estudiante = alertas(
            usuario = UsuariosPrueba.estudiante1,
            tareas = tareas
        ).first().lineas

        assertEquals(2, estudiante.size)
        assertTrue(estudiante.any { it.startsWith("Actividad 1 ·") })
        assertTrue(estudiante.any { it.startsWith("Actividad 2 ·") })
        assertEquals(5, alertas(tareas = tareas).first().lineas.size)
    }

    @Test
    fun atrasadasSeSeparanEntreRiegosYOtrasTareas() {
        val resultado = alertas(
            tareas = listOf(
                tarea(1, dias = -1),
                tarea(
                    2,
                    tipo = TipoTarea.LIMPIEZA,
                    dias = -2,
                    estado = EstadoTarea.EN_DESARROLLO
                ),
                tarea(3, dias = -3, estado = EstadoTarea.VALIDADA),
                tarea(4, dias = 0)
            )
        )

        val riegos = resultado.single {
            it.titulo == "Riegos atrasados"
        }.lineas

        val otras = resultado.single {
            it.titulo == "Otras tareas atrasadas"
        }.lineas

        assertEquals(1, riegos.size)
        assertTrue(riegos.single().startsWith("Actividad 1 ·"))
        assertEquals(1, otras.size)
        assertTrue(otras.single().startsWith("Actividad 2 ·"))
    }

    @Test
    fun cosechasYMantenimientosRespetanSusPlazos() {
        val resultado = alertas(
            tareas = listOf(
                tarea(1, tipo = TipoTarea.COSECHA, dias = 14),
                tarea(2, tipo = TipoTarea.COSECHA, dias = 15),
                tarea(3, tipo = TipoTarea.MANTENIMIENTO, dias = 30),
                tarea(4, tipo = TipoTarea.MANTENIMIENTO, dias = 31)
            )
        )

        val cosechas = resultado.single {
            it.titulo == "Próximas cosechas"
        }.lineas

        val mantenimientos = resultado.single {
            it.titulo == "Mantenimientos programados (30 días)"
        }.lineas

        assertEquals(1, cosechas.size)
        assertTrue(cosechas.single().startsWith("Actividad 1 ·"))
        assertEquals(1, mantenimientos.size)
        assertTrue(mantenimientos.single().startsWith("Actividad 3 ·"))
    }

    @Test
    fun cosechasEstimadasExcluyenCultivosTerminadosYFechasFueraDePlazo() {
        fun cultivo(
            id: Int,
            fecha: String,
            estado: EstadoCultivo = EstadoCultivo.EN_CRECIMIENTO
        ) = Cultivo(
            id = id,
            nombre = "Cultivo $id",
            descripcion = "Descripción",
            sectorId = 1,
            fechaSiembra = "01/05/2026",
            fechaCosechaEstimada = fecha,
            frecuenciaRiegoDias = 2,
            estado = estado
        )

        val lineas = alertas(
            cultivos = listOf(
                cultivo(1, "02/06/2026"),
                cultivo(2, "02/06/2026", EstadoCultivo.COSECHADO),
                cultivo(3, "02/06/2026", EstadoCultivo.FINALIZADO),
                cultivo(4, "01/12/2026"),
                cultivo(5, "incorrecta"),
                cultivo(6, "01/05/2026")
            )
        ).single { it.titulo == "Próximas cosechas" }.lineas

        assertEquals(
            listOf(
                "Cosecha estimada de Cultivo 1 · Sector 1 · 02/06/2026"
            ),
            lineas
        )
    }

    @Test
    fun docenteVeSoloValidacionesYProblemasPendientes() {
        val problemas = EstadoReporte.entries.mapIndexed { index, estado ->
            Problema(
                id = index,
                sectorId = 1,
                cultivoId = null,
                tipo = TipoProblema.FALTA_AGUA,
                descripcion = "Problema",
                fechaReporte = ahora,
                reportadoPorId = 2,
                estado = estado
            )
        }

        val resultado = alertas(
            tareas = listOf(
                tarea(1, estado = EstadoTarea.PENDIENTE_VALIDACION),
                tarea(2, estado = EstadoTarea.VALIDADA)
            ),
            problemas = problemas
        )

        assertEquals(
            listOf("Actividad 1 · Sector 1"),
            resultado.single {
                it.titulo == "Tareas pendientes de validación"
            }.lineas
        )

        assertEquals(
            listOf(
                "Falta de agua · Sector 1 · Informado",
                "Falta de agua · Sector 1 · En revisión"
            ),
            resultado.single {
                it.titulo == "Problemas pendientes de revisión"
            }.lineas
        )
    }

    @Test
    fun sensoresUsanSoloLaLecturaMasRecienteDeCadaSector() {
        fun lectura(
            id: Int,
            sector: Int,
            fecha: Long,
            estado: EstadoLectura
        ) = LecturaSensor(
            id = id,
            sectorId = sector,
            fecha = fecha,
            humedadSuelo = 20,
            temperaturaAmbiente = 22.0,
            humedadAmbiente = 50,
            estado = estado
        )

        val lecturas = listOf(
            lectura(1, 1, ahora - 1, EstadoLectura.BAJO),
            lectura(2, 1, ahora, EstadoLectura.NORMAL),
            lectura(3, 2, ahora, EstadoLectura.BAJO),
            lectura(4, 3, ahora, EstadoLectura.ALTO),
            lectura(5, 4, ahora, EstadoLectura.PENDIENTE_REVISION)
        )

        val resultado = alertas(lecturas = lecturas).single {
            it.titulo == "Lecturas simuladas fuera de rango (dato de prueba)"
        }.lineas

        assertEquals(
            listOf(
                "Sector 2 · Bajo · suelo 20%, 22.0 °C, aire 50%",
                "Sector 3 · Alto · suelo 20%, 22.0 °C, aire 50%"
            ),
            resultado
        )

        assertEquals(
            5,
            alertas(
                usuario = UsuariosPrueba.estudiante1,
                lecturas = lecturas
            ).size
        )
    }
}