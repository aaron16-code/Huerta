# Huerto Escolar

Aplicación móvil Android (MVP académico) para la gestión colaborativa del huerto de una escuela:
sectores, cultivos, tareas con validación, problemas reportados, alertas, sensores simulados e historial.

Proyecto de la asignatura **DSY1105 · Desarrollo de Aplicaciones Móviles** (Duoc UC, CITT Puente Alto).

> **Todos los usuarios, datos y fotos son ficticios.** La app no usa GPS, micrófono, datos biométricos
> ni sensores reales, y no controla ningún dispositivo ni sistema de riego.

## Equipo y repositorio

| Integrante (GitHub) | Módulos |
|---|---|
| [aaron16-code](https://github.com/aaron16-code) | Tareas, problemas, alertas, sensores simulados, historial y pruebas unitarias |
| [Matip9902](https://github.com/Matip9902) | Inicio de sesión por perfil, sectores, cultivos y persistencia con Room |

- Repositorio **público**. Los integrantes y el docente del curso son colaboradores del proyecto.
- Rama de entrega: **`main`**. Cada integrante trabaja en su propia rama (`huerta-aaron` y `Huerta-matias`)
  y se une a `main` mediante Pull Requests.
- El APK firmado en modo release se entrega aparte y **no** está en el repositorio
  (tampoco la llave de firma).

## Perfiles

El inicio de sesión es de demostración: se elige una de estas cuatro cuentas ficticias.

| Cuenta | Rol | Grupo |
|---|---|---|
| Profesora Laura | Docente / administradora | — |
| Sofía Martínez | Estudiante | Taller Huerto A |
| Tomás González | Estudiante | Taller Huerto A |
| Valentina Rojas | Estudiante | Taller Huerto B |

| | Estudiante | Docente / tallerista |
|---|---|---|
| Ve | Solo sus tareas y las de su grupo; sus problemas | Todas las tareas, problemas y datos |
| Crea y asigna tareas | No | Sí |
| Valida o rechaza tareas | No | Sí |
| Marca tareas como realizadas y adjunta foto | Sí | No |
| Reporta problemas | Sí | No (les da seguimiento) |
| Sensores simulados | Acceso restringido | Sí |
| Administra sectores y cultivos | No | Sí |

Una tarea que el estudiante marca como realizada queda **pendiente de validación** hasta que un docente
la confirma o la rechaza con un comentario. Nadie puede validar su propia actividad.

## Funcionalidades

- **Sectores y cultivos:** crear, editar y consultar, con estados y fechas (guardados con Room).
- **Tareas:** el docente crea y asigna tareas (a un estudiante o a un grupo, con sector, cultivo,
  instrucciones y fecha). El estudiante las consulta, comenta, adjunta una foto y las envía a validación.
- **Validación:** el docente confirma o rechaza con comentario; una tarea rechazada se puede repetir.
- **Problemas:** el estudiante reporta con foto; el docente les da seguimiento (informado, en revisión,
  atendido, cerrado).
- **Alertas:** próximos riegos, tareas y riegos atrasados, cosechas, mantenimientos, validaciones y
  problemas pendientes. Son solo informativas.
- **Sensores simulados:** humedad del suelo, temperatura y humedad ambiental, siempre marcados como
  "simulación académica / dato de prueba".
- **Historial:** tareas realizadas, validaciones y problemas en orden cronológico.
- **Fotos:** se pueden seleccionar con Photo Picker o capturar con la aplicación de cámara mediante
  `TakePicture` y `FileProvider`, sin solicitar acceso general a la galería ni permiso directo de cámara.
- **Animaciones:** la fotografía aparece y desaparece progresivamente, el mensaje cambia con `Crossfade`
  y el botón anima su color según exista una imagen.

## Tecnologías

Kotlin · Jetpack Compose · Material Design 3 · arquitectura MVVM · Navigation Compose · Room ·
Photo Picker · cámara con FileProvider · animaciones Compose · pruebas unitarias con JUnit · Git y GitHub.

## Cómo ejecutarla

1. Abrir el proyecto en Android Studio y esperar a que Gradle sincronice.
2. Elegir un emulador o un celular Android (minSdk 24) y presionar **Run** (configuración `app`).
3. Para probar fotografías se puede elegir una imagen de la galería o utilizar el botón
   **Tomar fotografía** dentro de una tarea o reporte.

## Pruebas unitarias

En Android Studio: clic derecho sobre `app/src/test/java/cl/duoc/huertoescolar` y **Run Tests**.
Son 43 pruebas. Cubren las reglas del caso: validación y rechazo de tareas, permisos por perfil,
tareas atrasadas, estados de las lecturas simuladas, seguimiento de problemas y validaciones del
formulario de crear tareas, además del formato y orden de las fechas de los cultivos.

## Estructura

```
app/src/main/java/cl/duoc/huertoescolar/
├── data/            modelos y repositorios (operación y Room)
├── ui/components/   componentes visuales compartidos
├── ui/screens/      pantallas por módulo (login, home, sectores, cultivos,
│                    tareas, problemas, alertas, sensores, historial)
├── Navegacion.kt    navegación principal
└── NavOperacion.kt  rutas del módulo de operación
```

## Alcance y limitaciones

Es un **Producto Mínimo Viable académico**, no una plataforma productiva.

- Sectores, cultivos, tareas, evidencias, validaciones, problemas y lecturas se guardan en una sola
  base Room llamada `huerto_escolar_unificado.db`.
- Los usuarios son fijos y ficticios; no hay registro ni cuentas reales.
- Las fotografías de cámara se almacenan en la caché privada de la aplicación y Android puede limpiar
  esos archivos cuando necesita espacio.
- No se integra ningún sistema externo, sensor físico ni sistema de riego.
