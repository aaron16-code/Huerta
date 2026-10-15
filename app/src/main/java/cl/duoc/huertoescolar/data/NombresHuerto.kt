package cl.duoc.huertoescolar.data

import cl.duoc.huertoescolar.RepositorioHuerto

/** Nombre real del sector (lo lee de RepositorioHuerto). Si no existe, muestra un texto genérico. */
fun nombreDeSector(id: Int): String =
    RepositorioHuerto.sectores.find { it.id == id }?.nombre ?: "Sector $id"

/** Nombre real del cultivo. Si la tarea no tiene cultivo asociado, lo indica. */
fun nombreDeCultivo(id: Int?): String =
    if (id == null) "Sin cultivo asociado"
    else RepositorioHuerto.cultivos.find { it.id == id }?.nombre ?: "Cultivo $id"