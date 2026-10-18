# Huerto Escolar

Aplicación móvil Android (MVP académico) para la gestión colaborativa del huerto de una escuela:
sectores, cultivos, tareas con validación, problemas reportados, alertas, sensores simulados e historial.

Proyecto de la asignatura **DSY1105 · Desarrollo de Aplicaciones Móviles** (Duoc UC, CITT Puente Alto).

> **Todos los usuarios, datos y fotos son ficticios.** La app no usa GPS, micrófono, datos biométricos
> ni sensores reales, y no controla ningún dispositivo ni sistema de riego.

## Integrantes

- _Integrante 1_ (acceso, sectores y cultivos)
- _Integrante 2_ (tareas, problemas, alertas, sensores e historial)

## Perfiles

El inicio de sesión es de demostración: se elige el perfil con un botón.

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
- **Fotos:** se eligen con el Photo Picker de Android (no requiere permisos).

## Tecnologías

Kotlin · Jetpack Compose · Material Design 3 · arquitectura MVVM · Navigation Compose · Room ·
Photo Picker · pruebas unitarias con JUnit · Git y GitHub.

## Cómo ejecutarla

1. Abrir el proyecto en Android Studio y esperar a que Gradle sincronice.
2. Elegir un emulador o un celular Android (minSdk 24) y presionar **Run** (configuración `app`).
3. En el emulador, para probar las fotos hay que tener alguna imagen en la galería
   (por ejemplo, sacándola con la app Camera del emulador).

## Pruebas unitarias

En Android Studio: clic derecho sobre `app/src/test/java/cl/duoc/huertoescolar` y **Run Tests**.
Cubren las reglas del caso: validación y rechazo de tareas, permisos por perfil, tareas atrasadas,
estados de las lecturas simuladas, seguimiento de problemas y validaciones del formulario de tareas.

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

- Los sectores y cultivos se guardan con Room. Las tareas, los problemas y las lecturas simuladas
  viven en memoria durante la sesión y se reinician al cerrar la app.
- Los usuarios son fijos y ficticios; no hay registro ni cuentas reales.
- Las fotos se refieren por una URI temporal del selector de Android.
- No se integra ningún sistema externo, sensor físico ni sistema de riego.