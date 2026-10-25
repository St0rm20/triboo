# Tribooo

Tribooo es una aplicación móvil para Android orientada a la comunidad; su propósito es que los habitantes de una ciudad publiquen, descubran y asistan a eventos locales. Entre los tipos de eventos se encuentran actividades deportivas, culturales, académicas, de voluntariado y sociales. Cada evento publicado pasa por un proceso de moderación antes de quedar visible para el resto de usuarios.

El proyecto fue desarrollado en Kotlin con Jetpack Compose (la herramienta moderna de Android para construir interfaces a partir de funciones) como parte del curso en la Universidad del Quindío.

## Estado actual del proyecto (segunda fase)

La segunda fase corresponde a una implementación funcional de la aplicación. En esta etapa se contemplan todas las pantallas, la navegación entre ellas y las funcionalidades definidas para el proyecto; además, la implementación respeta la arquitectura definida en clase y aplica buenas prácticas de desarrollo.

Los datos se gestionan en memoria; por lo tanto, la información creada durante el uso (eventos, comentarios, asistencias, etc.) se pierde al cerrar la aplicación. La única excepción es la sesión del usuario, que se conserva de forma local para no tener que iniciar sesión cada vez que se abre la app.

Según el alcance de esta fase, no se implementaron los siguientes elementos.

- Mapas reales; en su lugar se muestra un mapa simulado (marcador de posición).
- Carga de imágenes desde el dispositivo.
- Gestión de permisos del sistema (ubicación, cámara, almacenamiento).
- Autenticación real con un servidor; el inicio de sesión con Google también es una simulación que entra con la cuenta de prueba.
- Persistencia en una base de datos u otro medio de almacenamiento.

## Funcionalidades

- **Acceso.** Guía inicial (onboarding), inicio de sesión, registro de cuenta y recuperación de contraseña.
- **Inicio (feed).** Listado de eventos publicados con filtros por categoría, fecha y ubicación.
- **Explorar.** Búsqueda de eventos sobre un mapa simulado; también se detectan cruces de horario con otros eventos a los que el usuario ya asiste.
- **Agenda.** Calendario mensual con los eventos a los que el usuario confirmó asistencia.
- **Crear evento.** Formulario con validaciones; también permite editar, duplicar o reenviar un evento rechazado.
- **Detalle de evento.** Confirmación de asistencia, lista de espera cuando no hay cupos, guardado del evento, comentarios con reseñas y respuestas, y reporte de contenido inapropiado.
- **Estado del evento.** Seguimiento del evento enviado (pendiente, activo, rechazado o finalizado).
- **Perfil.** Estadísticas del usuario, edición de datos, eventos propios, eventos guardados y seguidores.
- **Perfil de otros usuarios.** Consulta de información pública y opción de seguir al organizador.
- **Notificaciones.** Avisos sobre moderación, asistencias, lista de espera y comentarios.
- **Moderación (solo administrador).** Revisión de eventos pendientes, verificación o rechazo con motivo, atención de reportes y restauración de eventos.

## Arquitectura

La aplicación sigue una arquitectura por capas basada en MVVM (Modelo, Vista y Modelo de Vista); en este patrón la vista solo muestra el estado y el modelo de vista se encarga de la lógica de presentación. La estructura del código se organiza de la siguiente manera.

```
app/src/main/java/edu/uniquindio/co/tribooo/
├── core/          Componentes visuales reutilizables, tema y utilidades
├── data/          Implementación de los repositorios (en memoria y sesión local)
├── di/            Módulos de inyección de dependencias con Hilt
├── domain/        Modelos, interfaces de repositorio y casos de uso
├── features/      Pantallas (Screen) y sus ViewModel, agrupadas por funcionalidad
└── navigation/    Rutas y grafo de navegación de la aplicación
```

- **Capa de dominio.** Contiene los modelos (`Event`, `User`, `Comment`, etc.), las interfaces de los repositorios y los casos de uso. Un caso de uso es una clase que representa una sola acción del negocio, por ejemplo `ToggleAttendanceUseCase` o `RejectEventUseCase`; esto permite que la lógica no dependa de la interfaz gráfica.
- **Capa de datos.** Implementa los repositorios con `MutableStateFlow`, un tipo de dato observable de Kotlin que avisa a la interfaz cada vez que la información cambia. Los datos iniciales de prueba están en `MockData.kt`.
- **Capa de presentación.** Cada pantalla tiene un `ViewModel` que expone un estado inmutable a la vista construida con Compose.
- **Inyección de dependencias.** Se usa Hilt; esta librería se encarga de crear los objetos que necesita cada clase y entregárselos automáticamente, lo que reduce el acoplamiento entre componentes.
- **Navegación.** Se usa Navigation Compose con rutas tipadas mediante `kotlinx.serialization`; así cada pantalla recibe sus argumentos con tipos seguros.

### Tecnologías principales

| Tecnología | Uso |
|---|---|
| Kotlin 2.2 | Lenguaje de programación |
| Jetpack Compose y Material 3 | Construcción de la interfaz |
| Navigation Compose | Navegación entre pantallas |
| Hilt | Inyección de dependencias |
| Kotlin Coroutines y Flow | Manejo de tareas asíncronas y datos observables |
| DataStore | Conservación local de la sesión |
| JUnit y kotlinx-coroutines-test | Pruebas unitarias |

## Requisitos

- Android Studio en una versión reciente con soporte para Android Gradle Plugin 9.
- JDK 21; Gradle puede descargarlo automáticamente según la configuración del proyecto.
- Un dispositivo físico o un emulador con Android 9 (API 28) o superior.

## Cómo ejecutar la aplicación

### En un emulador

1. Clonar el repositorio.

   ```bash
   git clone https://github.com/St0rm20/triboo.git
   ```

2. Abrir la carpeta del proyecto en Android Studio y esperar a que termine la sincronización de Gradle.
3. Crear un emulador desde **Device Manager**; se recomienda una imagen con API 34 o superior.
4. Seleccionar el emulador en la barra superior y presionar **Run 'app'**.

### En un dispositivo Android físico

1. En el teléfono, abrir **Ajustes > Acerca del teléfono** y tocar siete veces la opción **Número de compilación**; con esto se activan las opciones de desarrollador.
2. Entrar a **Ajustes > Sistema > Opciones de desarrollador** y activar **Depuración por USB**. El nombre exacto de los menús puede cambiar según el fabricante.
3. Conectar el teléfono al computador con un cable USB y aceptar el mensaje de autorización que aparece en la pantalla del dispositivo.
4. En Android Studio, seleccionar el dispositivo en la barra superior y presionar **Run 'app'**.

También es posible generar el instalador (APK) desde la terminal y copiarlo al teléfono.

```bash
./gradlew assembleDebug
```

El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`. Para instalarlo desde el teléfono es necesario permitir la instalación de aplicaciones de origen desconocido; si el dispositivo está conectado con depuración USB, también puede instalarse con el siguiente comando.

```bash
./gradlew installDebug
```

### Ejecutar las pruebas unitarias

```bash
./gradlew testDebugUnitTest
```

## Uso de la aplicación

Al abrir la app por primera vez se muestra la guía inicial; después se llega a la pantalla de inicio de sesión. Allí se puede entrar con alguna de las cuentas de prueba o crear una cuenta nueva desde **Crear cuenta**. Hay que tener en cuenta que las cuentas nuevas solo existen mientras la app está abierta, ya que los datos se guardan en memoria.

### Credenciales de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Usuario | `daniela.restrepo@triboo.co` | `123456` |
| Moderador | `moderacion@triboo.co` | `admin1234` |

- **Usuario (Daniela Restrepo).** Esta cuenta ya tiene eventos creados, comentarios, asistencias y organizadores seguidos; por eso resulta útil para recorrer todas las pantallas. La opción **Continuar con mi cuenta Google** también ingresa con esta cuenta.
- **Moderador (Mauricio Salazar).** En la barra inferior el botón central cambia por un martillo que abre la pantalla de moderación; desde allí se aprueban o rechazan eventos y se atienden los reportes.

### Recorrido sugerido

1. Iniciar sesión como usuario y revisar el feed; luego aplicar algún filtro por categoría.
2. Abrir un evento, confirmar la asistencia y dejar un comentario.
3. Crear un evento nuevo con el botón central de la barra inferior; el evento queda en estado pendiente.
4. Cerrar sesión desde el perfil e ingresar como moderador.
5. Aprobar o rechazar el evento creado en la pestaña de pendientes.
6. Volver a la cuenta de usuario y revisar la notificación del resultado y el estado del evento.

Como los datos se guardan en memoria, este recorrido debe hacerse sin cerrar la aplicación; si se cierra, la información vuelve a su estado inicial.

## Uso de herramientas de inteligencia artificial

Durante el desarrollo del proyecto se hizo uso de herramientas de inteligencia artificial como apoyo. Estas herramientas se emplearon principalmente en las siguientes tareas.

- Generación de propuestas de código para pantallas, ViewModel y casos de uso.
- Revisión y corrección de errores de compilación y de lógica.
- Redacción de pruebas unitarias.
- Redacción de documentación, incluido este archivo.

Todo el contenido sugerido por estas herramientas fue revisado, ajustado e integrado por el equipo para asegurar que cumpliera con la arquitectura definida en clase y con los requisitos del proyecto.
