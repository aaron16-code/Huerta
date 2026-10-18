package cl.duoc.huertoescolar.data.local

class HuertoRoomRepository(
    private val huertoDao: HuertoDao
) {
    val usuarios = huertoDao.observarUsuarios()
    val sectores = huertoDao.observarSectores()
    val cultivos = huertoDao.observarCultivos()
    val tareas = huertoDao.observarTareas()
    val reportes = huertoDao.observarReportes()

    suspend fun guardarUsuario(usuario: UsuarioEntity) = huertoDao.insertarUsuario(usuario)

    suspend fun guardarSector(sector: SectorEntity) = huertoDao.insertarSector(sector)
    suspend fun actualizarSector(sector: SectorEntity) = huertoDao.actualizarSector(sector)
    suspend fun eliminarSector(sector: SectorEntity) = huertoDao.eliminarSector(sector)

    suspend fun guardarCultivo(cultivo: CultivoEntity) = huertoDao.insertarCultivo(cultivo)
    suspend fun actualizarCultivo(cultivo: CultivoEntity) = huertoDao.actualizarCultivo(cultivo)
    suspend fun eliminarCultivo(cultivo: CultivoEntity) = huertoDao.eliminarCultivo(cultivo)

    suspend fun guardarTarea(tarea: TareaEntity) = huertoDao.insertarTarea(tarea)
    suspend fun actualizarTarea(tarea: TareaEntity) = huertoDao.actualizarTarea(tarea)

    suspend fun guardarEvidencia(evidencia: EvidenciaEntity) = huertoDao.insertarEvidencia(evidencia)
    suspend fun guardarValidacion(validacion: ValidacionEntity) = huertoDao.insertarValidacion(validacion)

    suspend fun guardarReporte(reporte: ReporteProblemaEntity) = huertoDao.insertarReporte(reporte)
    suspend fun actualizarReporte(reporte: ReporteProblemaEntity) = huertoDao.actualizarReporte(reporte)
}
