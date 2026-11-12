# Qlic Frontend Mobile

Aplicación móvil nativa de Qlic desarrollada con Kotlin, Jetpack Compose y Material 3. Consume el servicio desplegado de Qlic y mantiene estados honestos cuando una capacidad todavía no existe en el backend.

## Tecnologías

- Kotlin y corrutinas.
- Jetpack Compose y Material 3.
- Navigation Compose.
- Retrofit, OkHttp y Gson.
- DataStore y Android Keystore para la sesión.
- CameraX y ML Kit para el registro mediante código QR.
- JUnit y pruebas instrumentadas de Compose.

## Funcionalidades conectadas

- Registro de cuenta e inicio de sesión.
- Persistencia cifrada de la sesión y cierre de sesión local.
- Dashboard con cantidades reales de dispositivos, alertas y suscripción.
- Listado y registro de dispositivos.
- Lectura de código QR con alternativa de ingreso manual.
- Asignación de un punto de agua mediante su identificador.
- Listado, filtrado, detalle y reconocimiento de alertas.
- Configuración de umbrales, preferencias y destinatarios autorizados.
- Consulta y administración de suscripciones.
- Consulta del historial de pagos proporcionado por el backend.
- Interfaz completa en inglés y español latinoamericano (`es-419`).

## Capacidades todavía no disponibles

El backend actual no expone telemetría de consumo, caudal o presión, reportes, inventario de tanques, catálogo de puntos de agua, tickets de soporte ni edición del perfil. Estas secciones muestran un estado de no disponibilidad y no presentan datos de ejemplo como si fueran reales.

## Requisitos

- Android Studio con su JDK integrado.
- Android SDK 37.
- Emulador o dispositivo físico con Android 8.0 (API 26) o superior.
- Cámara disponible para probar el escaneo QR. En un emulador también se puede usar el ingreso manual.
- Conexión a Internet para acceder al backend desplegado.

## Ejecución en Android Studio

1. Abrir la carpeta del proyecto en Android Studio.
2. Esperar a que finalice la sincronización de Gradle.
3. Crear o seleccionar un dispositivo virtual con API 26 o superior.
4. Seleccionar la configuración `app`.
5. Ejecutar la aplicación.
6. Crear una cuenta de prueba o iniciar sesión con una cuenta existente. El repositorio no incluye credenciales predeterminadas.

Si el proyecto está dentro de OneDrive, los archivos generados se guardan en `C:\Android\Build\qlic-frontend-mobile` para evitar bloqueos de sincronización. La variable `QLIC_ANDROID_BUILD_ROOT` permite elegir otra ubicación local.

## Backend

La URL base utilizada por las variantes `debug` y `release` es:

```text
https://qlic-backend-api.onrender.com/
```

La instancia gratuita puede tardar en responder la primera vez mientras se reactiva. La aplicación conserva estados de carga y permite reintentar las consultas.

## Verificación

Desde PowerShell, usando el JDK de Android Studio:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:\Android\Temp\qlic-jvm-tmp'
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleRelease
.\gradlew.bat connectedDebugAndroidTest
```

La segunda instrucción requiere un emulador o dispositivo conectado. El APK de depuración queda firmado con la clave de desarrollo local. El APK de producción generado por `assembleRelease` no está firmado; para distribuirlo se debe configurar una clave de publicación administrada de forma segura y fuera del repositorio.

## Seguridad y alcance de demostración

- El tráfico HTTP sin cifrar está deshabilitado.
- Los tokens no se registran ni se almacenan en texto plano.
- La sesión se cifra con AES-GCM y una clave protegida por Android Keystore.
- Las copias de seguridad de la aplicación están deshabilitadas para no exportar la sesión.
- La compilación `release` aplica reducción y ofuscación.
- No se incluyen claves, tokens, contraseñas, certificados ni credenciales de demostración.

### Bloqueo para producción

El backend acepta algunos identificadores de cuenta o de recurso enviados por el cliente sin demostrar en cada operación que pertenecen al usuario autenticado. Antes de publicar el sistema en producción, el backend debe obtener la identidad desde el token y validar la propiedad de cada recurso. La aplicación móvil reduce la exposición usando el identificador de la sesión, pero esta protección del cliente no sustituye la autorización del servidor.

Hasta corregirlo, las demostraciones deben realizarse únicamente con cuentas y datos de prueba propios, sin información real, sensible o de terceros.
