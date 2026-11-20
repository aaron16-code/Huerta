package cl.duoc.huertoescolar

import cl.duoc.huertoescolar.data.*
import cl.duoc.huertoescolar.data.repository.RepositorioOperacion
import cl.duoc.huertoescolar.domain.model.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*

class CoberturaOperacionAdicionalTest {
    private val r=RepositorioOperacion
    private val admin=RolUsuario.ADMINISTRADOR
    private val estudiante=RolUsuario.ESTUDIANTE
    private val p=mockk<PersistenciaOperacion>(relaxed=true)
    private fun campo(n:String)=r.javaClass.superclass.getDeclaredField(n).apply { isAccessible=true }
    private var anterior:Any?=null
    private lateinit var tareas:List<Tarea>
    private lateinit var problemas:List<Problema>
    @Suppress("UNCHECKED_CAST") private fun <T> flujo(n:String)=campo(n).get(r) as MutableStateFlow<List<T>>
    @Before fun preparar(){ anterior=campo("persistencia").get(r); campo("persistencia").set(r,null); tareas=r.tareas.value; problemas=r.problemas.value; flujo<Tarea>("_tareas").value=emptyList(); flujo<Problema>("_problemas").value=emptyList() }
    @After fun restaurar(){ campo("persistencia").set(r,anterior); flujo<Tarea>("_tareas").value=tareas; flujo<Problema>("_problemas").value=problemas }
    private fun t(id:Int=1)=Tarea(id,TipoTarea.RIEGO,"Regar","Regar bien",1,null,0,System.currentTimeMillis()+86400000L,responsableUsuarioId=2,creadaPorId=1)
    private suspend fun conectar(ts:List<Tarea> = emptyList(), ps:List<Problema> = emptyList()) { coEvery { p.cargar() } returns DatosOperacion(ts,ps,emptyList()); r.conectar(p) }
    @Test fun confirmacionSinServidorYConServidor()=runTest {
        assertTrue(r.confirmar { Result.success(Unit) }.isFailure)
        conectar(); assertTrue(r.confirmar { Result.success(Unit) }.isSuccess)
        assertTrue(r.confirmar { r.crearTarea(estudiante,t()) }.isFailure)
        coEvery { p.insertarTarea(any()) } answers { error("sin red") }
        assertTrue(r.confirmar { r.crearTarea(admin,t()) }.isFailure); assertTrue(r.tareas.value.isEmpty())
        coEvery { p.insertarTarea(any()) } throws CancellationException("cancelado")
        try { r.confirmar { r.crearTarea(admin,t()) }; fail("Debe cancelar") } catch (_:CancellationException) {}
        assertTrue(r.tareas.value.isEmpty())
        coEvery { p.insertarTarea(any()) } answers { firstArg<Tarea>().copy(id=99) }
        assertTrue(r.confirmar { r.crearTarea(admin,t()) }.isSuccess); assertEquals(99,r.tareas.value.single().id)
        assertTrue(r.eliminarTarea(99,estudiante).isFailure); assertTrue(r.eliminarTarea(99,admin).isSuccess); assertTrue(r.tareas.value.isEmpty())
        coVerify { p.eliminarTarea(99) }
    }
    @Test fun validacionesYEstados()=runTest {
        val base=t(); conectar(listOf(base,base.copy(id=2,responsableUsuarioId=null,grupoAsignado="A"),base.copy(id=3,estado=EstadoTarea.EN_DESARROLLO),base.copy(id=4,estado=EstadoTarea.RECHAZADA),base.copy(id=5,estado=EstadoTarea.VALIDADA)))
        assertTrue(r.confirmar { r.editarTarea(admin,base.copy(id=999)) }.isFailure)
        assertTrue(r.confirmar { r.editarTarea(admin,base.copy(descripcion="")) }.isFailure)
        assertTrue(r.confirmar { r.editarTarea(admin,base.copy(instrucciones="")) }.isFailure)
        assertTrue(r.confirmar { r.editarTarea(admin,base.copy(responsableUsuarioId=null,grupoAsignado="")) }.isFailure)
        assertTrue(r.confirmar { r.editarTarea(admin,base.copy(descripcion=" Nuevo ")) }.isSuccess)
        assertEquals("Nuevo",r.obtenerTarea(1)?.descripcion)
        assertTrue(r.confirmar { r.marcarRealizada(999,2,"A","",null) }.isFailure)
        assertTrue(r.confirmar { r.marcarRealizada(2,2,"","",null) }.isFailure)
        assertTrue(r.confirmar { r.marcarRealizada(2,2,"B","",null) }.isFailure)
        assertTrue(r.confirmar { r.marcarRealizada(5,2,"A","",null) }.isFailure)
        for(id in 1..4) assertTrue(r.confirmar { r.marcarRealizada(id,2,"A","Listo",if(id==1) "foto" else null) }.isSuccess)
        assertTrue(r.confirmar { r.validarTarea(999,1,admin,true,"") }.isFailure)
        assertTrue(r.confirmar { r.validarTarea(5,1,admin,true,"") }.isFailure)
        assertTrue(r.confirmar { r.validarTarea(1,1,admin,true,"") }.isSuccess)
        assertTrue(r.confirmar { r.validarTarea(2,1,admin,false,"Repetir") }.isSuccess)
        assertEquals(EstadoTarea.RECHAZADA,r.obtenerTarea(2)?.estado)
        assertTrue(r.historial().isNotEmpty())
    }
    @Test fun problemasYFechas()=runTest {
        conectar(); coEvery { p.insertarProblema(any()) } answers { firstArg<Problema>().copy(id=99) }
        assertTrue(r.reportarProblema(1,null,TipoProblema.FALTA_AGUA,"",2,null).isFailure)
        assertTrue(r.reportarProblema(1,null,TipoProblema.FALTA_AGUA,"Seco",2,null).isSuccess)
        assertEquals(99,r.problemas.value.single().id)
        assertTrue(r.actualizarProblema(99,estudiante,EstadoReporte.ATENDIDO,"").isFailure)
        for(estado in listOf(EstadoReporte.EN_REVISION,EstadoReporte.ATENDIDO,EstadoReporte.CERRADO,EstadoReporte.INFORMADO)) {
            assertTrue(r.actualizarProblema(99,admin,estado,"Comentario").isSuccess)
            val actual=r.problemas.value.single(); assertEquals(estado,actual.estado); assertEquals("Comentario",actual.comentarioDocente)
            assertEquals(estado==EstadoReporte.ATENDIDO || estado==EstadoReporte.CERRADO,actual.fechaCierre!=null)
        }
        assertTrue(r.problemasPendientes().any { it.id==99 }); assertTrue(r.historial().isNotEmpty())
    }
    @Test fun bloqueoConcurrente()=runTest {
        conectar(); val entrada=CompletableDeferred<Unit>(); val salida=CompletableDeferred<Unit>()
        coEvery { p.insertarTarea(any()) } coAnswers { entrada.complete(Unit); salida.await(); firstArg<Tarea>() }
        val trabajo=async { r.confirmar { r.crearTarea(admin,t()) } }
        entrada.await(); try { assertTrue(r.confirmar { Result.success(Unit) }.isFailure) } finally { salida.complete(Unit) }
        assertTrue(trabajo.await().isSuccess)
    }
}

