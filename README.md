# CleanTV para Android TV — MVP 0.1

Proyecto Android TV en Kotlin, controlable con mando a distancia, inspirado en las funciones de limpieza reversible de [CleanYourTV](https://github.com/Gathin23/cleanyourtv). **No es un producto oficial ni una adaptación completa.**

## Funciones implementadas
- Inicio en el lanzador Android TV / Google TV.
- Conexión ADB TCP al **propio dispositivo** en `127.0.0.1:5555`, usando [dadb](https://github.com/mobile-dev-inc/dadb).
- Lectura del fabricante/modelo y listado de paquetes instalados; búsqueda por paquete.
- Desactivación individual de paquetes tras confirmación explícita, mediante `pm disable-user --user 0`.
- Bloqueo conservador de determinados paquetes críticos, historial local de acciones y restauración individual mediante `pm enable --user 0`.
- No requiere root ni desinstala paquetes.

## Restricciones importantes
- **No puede actuar sin ADB autorizado**: una app ordinaria no tiene permisos para desactivar paquetes de sistema por sí misma.
- Requiere que el daemon ADB del televisor acepte conexiones TCP **en el puerto 5555**, incluso desde `127.0.0.1`. No todos los modelos lo permiten.
- **No implementa la vinculación TLS con código de Android 11+**. Activar sólo "Depuración inalámbrica" con código generalmente no es suficiente. No usar este MVP para modificar paquetes hasta conseguir una conexión ADB autorizada.
- Los paquetes protegidos son una lista defensiva no exhaustiva: desactivar otros paquetes aún puede romper funciones del televisor. Evita desactivar launcher, mando, entradas HDMI, voz, conectividad, actualizaciones o servicios esenciales.
- El historial permanece en almacenamiento privado de la app. **Si borras los datos o desinstalas CleanTV, perderás la lista de cambios.** Antes de borrar datos, restaura todas las apps.
- La versión 0.1 **no instala ni modifica el launcher**, no hace copia completa del estado de paquetes, ni incluye los perfiles oficiales de cada modelo.
- Se usa `targetSdk 28` para compatibilidad con instalaciones TV antiguas; conviene actualizarlo antes de distribuir ampliamente.

## Compilación automática en GitHub Actions

**Corrección v0.4 (noviembre 2026):** el workflow localiza `sdkmanager` mediante ruta absoluta dentro del Android SDK. Si no existe, instala las herramientas de línea de comandos oficiales de Google. Se instalan Android SDK 35 y build-tools 35.0.0 y se evita el error `sdkmanager: command not found`. La compilación del APK aún no se ha verificado.


Este ZIP incluye `.github/workflows/build-apk.yml`. Para obtener el APK sin Android Studio:

1. Crea un repositorio **privado** nuevo en GitHub y sube **el contenido de la carpeta** `CleanTV-AndroidTV` (no el ZIP sin descomprimir).
2. En GitHub abre **Actions** → **Compilar APK Android TV** → **Run workflow**, o realiza un push a `main`.
3. Si la compilación se completa en verde, abre la ejecución → **Artifacts** → `CleanTV-AndroidTV-debug-apk`.
4. Descomprime el artefacto: contiene `app-debug.apk` (APK de depuración firmado por la compilación).
5. Para instalar con Downloader by AFTVnews **necesitas alojar ese APK como archivo público descargable por URL HTTPS**. Los artefactos de Actions requieren sesión y no son enlaces públicos directos. Puedes publicar el APK en una Release de GitHub en un repositorio público, tras comprobarlo y decidir compartirlo.

**Estado real:** el proyecto no ha podido compilarse en este entorno, porque carece de Android SDK/Gradle y no hay acceso de red para descargarlos. El workflow es la vía de compilación, pero su resultado no se ha verificado aquí. No hay todavía un APK o URL de Downloader.

## Compilar
1. Abrir este directorio en Android Studio (JDK 17 y Android SDK 35).
2. Dejar sincronizar Gradle y compilar **Build > Build APK(s)**.
3. Resultado típico: `app/build/outputs/apk/debug/app-debug.apk`.
4. Probar en un Android TV compatible **antes** de cambiar apps de sistema.

Requiere Gradle 8.9. La acción de GitHub lo instala directamente, sin wrapper. Para compilación local instala Gradle 8.9 o genera su wrapper con una instalación confiable.

## Instalar con Downloader de AFTVnews
1. Compilar el APK de la aplicación.
2. Alojar el `.apk` en una URL **HTTPS directa y pública** (por ejemplo, un archivo de GitHub Releases).
3. En la TV, abrir **Downloader by AFTVnews**, escribir esa URL y descargar el APK.
4. Autorizar la instalación desde Downloader en los ajustes de Android TV cuando se solicite.
5. Instalar y abrir CleanTV.

**Este paquete incluye código fuente, no un APK compilado ni un código corto AFTVnews.** No se puede descargar directamente en Downloader hasta compilar y alojar el APK. Ninguna URL o código de descarga ha sido inventado.

## Seguridad
Habilitar la depuración sólo en una red de confianza, aceptar únicamente solicitudes propias y **desactivarla al terminar**. El puerto TCP 5555 puede ofrecer acceso de depuración a otros equipos de la red. No compartas claves ADB. Un registro local no equivale a una copia de seguridad externa.

## Licencias
Código nuevo de este MVP bajo licencia MIT (ver LICENSE). La dependencia `dadb` se distribuye bajo Apache-2.0. CleanYourTV es MIT © 2026 Gathin; se ha consultado su documentación pero **no se han copiado scripts ni el launcher**.

## Corrección v0.5 — compilación Kotlin
El paquete Kotlin de la biblioteca `dev.mobile:dadb:1.2.6` es `dadb`, no `dev.mobile.dadb`. En `AdbRepository.kt` se corrigió el import a `import dadb.Dadb`. El error de compilación `Unresolved reference 'dev'` y los errores encadenados de `Dadb`, `shell`, `map`, `filter` y `it` se originaban en ese import. La compilación completa debe verificarse en GitHub Actions.
