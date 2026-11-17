package es.cleantv.tv

import android.content.Context
import dadb.Dadb
import dadb.AdbKeyPair
import java.io.File

/** Operaciones limitadas a un daemon ADB local autorizado; nunca invoca su. */
class AdbRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("changes", Context.MODE_PRIVATE)

    // Conserva la identidad ADB en almacenamiento privado de esta aplicación.
    // No se utiliza ~/.android: en Android TV no es un directorio escribible fiable.
    private fun adbKeyPair(): AdbKeyPair {
        val folder = File(context.filesDir, "adb")
        val privateKey = File(folder, "adbkey")
        val publicKey = File(folder, "adbkey.pub")
        if (!privateKey.exists() || !publicKey.exists()) {
            check(folder.exists() || folder.mkdirs()) { "No se pudo crear el directorio privado de ADB" }
            AdbKeyPair.generate(privateKey, publicKey)
        }
        return AdbKeyPair.read(privateKey, publicKey)
    }

    private fun <T> withAdb(block: (Dadb) -> T): T =
        Dadb.create(
            host = "127.0.0.1",
            port = 5555,
            keyPair = adbKeyPair()
        ).use(block)

    fun probe(): String = withAdb { adb ->
        val manufacturer = adb.shell("getprop ro.product.manufacturer").output.trim()
        val model = adb.shell("getprop ro.product.model").output.trim()
        "Conectado: $manufacturer $model"
    }

    fun packages(): List<String> = withAdb { adb ->
        val result = adb.shell("pm list packages")
        require(result.exitCode == 0) { "No se pudo listar paquetes: ${result.output}" }
        result.output.lineSequence().map { it.trim() }
            .filter { it.startsWith("package:") }.map { it.removePrefix("package:") }
            .filter { validPackage(it) }.distinct().sorted().toList()
    }

    fun savedChanges(): Set<String> = prefs.getStringSet("disabled", emptySet())!!.toSet()

    fun disable(packageName: String): String {
        require(validPackage(packageName)) { "Nombre de paquete no válido" }
        require(!ProtectedPackages.isProtected(packageName, context.packageName)) { "Paquete protegido" }
        return withAdb { adb ->
            // Nunca desactivar sin conocer el estado anterior: sólo apps habilitadas.
            val enabled = adb.shell("pm list packages -e").output.lineSequence()
                .any { it.trim() == "package:$packageName" }
            require(enabled) { "La app ya estaba desactivada o no se puede comprobar su estado" }
            val response = adb.shell("pm disable-user --user 0 $packageName")
            require(response.exitCode == 0 && response.output.contains("disabled", ignoreCase = true)) {
                "No se ha confirmado la desactivación: ${response.output}"
            }
            persist(savedChanges() + packageName)
            "Desactivada: $packageName"
        }
    }

    fun restore(packageName: String): String {
        require(packageName in savedChanges()) { "Este paquete no consta en el registro de cambios" }
        require(validPackage(packageName)) { "Nombre de paquete no válido" }
        return withAdb { adb ->
            val response = adb.shell("pm enable --user 0 $packageName")
            require(response.exitCode == 0 && response.output.contains("enabled", ignoreCase = true)) {
                "No se ha confirmado la restauración: ${response.output}"
            }
            persist(savedChanges() - packageName)
            "Restaurada: $packageName"
        }
    }

    private fun persist(changes: Set<String>) {
        check(prefs.edit().putStringSet("disabled", changes.toSet()).commit()) { "No se pudo guardar el historial local" }
    }

    companion object {
        fun validPackage(p: String): Boolean = p.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)+"))
    }
}
