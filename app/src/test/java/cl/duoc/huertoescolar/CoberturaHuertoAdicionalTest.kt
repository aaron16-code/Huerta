package cl.duoc.huertoescolar

import android.util.Log
import cl.duoc.huertoescolar.data.local.HuertoDao
import cl.duoc.huertoescolar.data.remote.*
import cl.duoc.huertoescolar.data.repository.RepositorioHuerto
import cl.duoc.huertoescolar.domain.model.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*

class CoberturaHuertoAdicionalTest {
    private val repo = RepositorioHuerto
    private fun campo(n: String) = repo.javaClass.getDeclaredField(n).apply { isAccessible = true }
    private val dao = mockk<HuertoDao>(relaxed = true)
    private var previoDao: Any? = null
    private var previoInicio: Any? = null
    private lateinit var sectores: List<Sector>
    private lateinit var cultivos: List<Cultivo>
    private var sector: Sector? = null
    private var cultivo: Cultivo? = null
    @Before fun preparar() {
        previoDao = campo("huertoDao").get(repo); previoInicio = campo("inicializado").get(repo)
        sectores = repo.sectores; cultivos = repo.cultivos
        sector = repo.sectorSeleccionado; cultivo = repo.cultivoSeleccionado
        campo("huertoDao").set(repo, null); campo("inicializado").set(repo, false)
    }
    @After fun restaurar() = runTest {
        campo("huertoDao").set(repo, null)
        repo.cultivos.toList().forEach { repo.quitarCultivo(it.id) }
        repo.sectores.toList().forEach { repo.quitarSector(it.id) }
        sectores.forEach { repo.agregarSector(it) }; cultivos.forEach { repo.agregarCultivo(it) }
        if (sector == null) { sectores.firstOrNull()?.let { repo.actualizarSector(it) } } else repo.seleccionarSector(sector!!)
        if (cultivo == null) { cultivos.firstOrNull()?.let { repo.actualizarCultivo(it) } } else repo.seleccionarCultivo(cultivo!!)
        campo("huertoDao").set(repo, previoDao); campo("inicializado").set(repo, previoInicio)
    }
    private fun s(id: Int) = Sector(id,"S$id","Desc","Patio",EstadoSector.DISPONIBLE,"Docente")
    private fun c(id: Int) = Cultivo(id,"C$id","Desc",900,"20/11/2026","20/12/2026",2)
    private fun sd(id: Int, responsable: Int?) = SectorDto(id,"S$id","Desc","Patio","DISPONIBLE",responsable)
    private fun cd(id: Int) = CultivoDto(id,"C$id","Desc",900,"20/11/2026","20/12/2026",2,"SEMBRADO","",null)
    @Test fun catalogoLocal() = runTest {
        repo.cultivos.toList().forEach { repo.quitarCultivo(it.id) }; repo.sectores.toList().forEach { repo.quitarSector(it.id) }
        assertEquals(1,repo.obtenerSiguienteId()); assertEquals(1,repo.obtenerSiguienteIdCultivo())
        repo.agregarSector(s(900)); repo.agregarSector(s(901)); repo.agregarCultivo(c(900)); repo.agregarCultivo(c(901))
        assertEquals(902,repo.obtenerSiguienteId()); assertEquals(902,repo.obtenerSiguienteIdCultivo())
        repo.seleccionarSector(s(900)); repo.seleccionarCultivo(c(900))
        repo.actualizarSector(s(900).copy(nombre="Nuevo")); repo.actualizarCultivo(c(900).copy(nombre="Nuevo"))
        assertEquals("Nuevo",repo.sectores.first().nombre); assertEquals("Nuevo",repo.cultivos.first().nombre)
        assertNull(repo.sectorSeleccionado); assertNull(repo.cultivoSeleccionado)
        repo.actualizarSector(s(999)); repo.actualizarCultivo(c(999)); repo.quitarSector(999); repo.quitarCultivo(999)
        assertEquals(2,repo.sectores.size); assertEquals(2,repo.cultivos.size)
        repo.quitarCultivo(900); repo.quitarSector(900); assertEquals(listOf(901),repo.sectores.map { it.id }); assertEquals(listOf(901),repo.cultivos.map { it.id })
    }
    @Test fun recibirYCache() = runTest {
        repo.recibirSector(sd(900,null)); repo.recibirCultivo(cd(900)); assertFalse(repo.sectores.any { it.id==900 })
        campo("inicializado").set(repo,true); campo("huertoDao").set(repo,dao)
        repo.recibirSector(sd(900,null)); repo.recibirSector(sd(901,1)); repo.recibirSector(sd(900,99999))
        repo.recibirCultivo(cd(900)); repo.recibirCultivo(cd(901)); repo.recibirCultivo(cd(900).copy(nombre="Editado"))
        assertEquals("Sin responsable",repo.sectores.single { it.id==900 }.responsable)
        assertEquals("Editado",repo.cultivos.single { it.id==900 }.nombre)
        coVerify(exactly=6) { dao.reemplazarCatalogo(any(),any()) }
        coEvery { dao.reemplazarCatalogo(any(),any()) } throws CancellationException("cancelado")
        try { repo.quitarCultivo(900); fail("Debe propagar cancelacion") } catch (_: CancellationException) {}
    }
    @Test fun sincronizaCatalogoRemoto() = runTest {
        mockkObject(HuertoRetrofitClient)
        try {
            val api=mockk<HuertoApi>()
            every { HuertoRetrofitClient.api } returns api
            coEvery { api.obtenerSectores() } returns listOf(sd(900,1),sd(901,null))
            coEvery { api.obtenerCultivos() } returns listOf(cd(900))
            campo("huertoDao").set(repo,dao)
            repo.sincronizar()
            assertEquals(listOf(900,901),repo.sectores.map { it.id }); assertEquals(900,repo.cultivos.single().id)
            coVerify { dao.reemplazarCatalogo(any(),any()) }
        } finally { unmockkObject(HuertoRetrofitClient) }
    }
    @Test fun escriturasDao() = runTest {
        campo("huertoDao").set(repo,dao)
        repo.agregarSector(s(900)); repo.agregarCultivo(c(900)); repo.actualizarSector(s(900)); repo.actualizarCultivo(c(900))
        coVerify(timeout=3000) { dao.insertarSector(any()); dao.insertarCultivo(any()); dao.actualizarSector(any()); dao.actualizarCultivo(any()) }
    }
}
