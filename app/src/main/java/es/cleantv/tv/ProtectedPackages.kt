package es.cleantv.tv

/** Denylist defensiva, incompleta por diseño: el usuario siempre debe confirmar. */
object ProtectedPackages {
    private val exact = setOf(
        "android", "com.android.systemui", "com.android.settings", "com.android.shell",
        "com.android.packageinstaller", "com.google.android.packageinstaller",
        "com.google.android.permissioncontroller", "com.android.permissioncontroller",
        "com.google.android.gms", "com.google.android.gsf", "com.android.providers.settings",
        "com.android.providers.media", "com.android.providers.downloads",
        "com.android.networkstack", "com.google.android.networkstack",
        "com.google.android.tvlauncher", "com.google.android.apps.tv.launcherx",
        "com.android.tv.launcher", "com.google.android.tv", "com.android.inputdevices"
    )
    private val prefixes = listOf("com.android.providers.", "com.android.bluetooth", "com.android.wifi", "com.android.adbd")
    fun isProtected(p: String, self: String) = p == self || p in exact || prefixes.any { p.startsWith(it) }
    fun label(p: String): String = when {
        p.contains("recommend", true) || p.contains("promotion", true) || p.contains("advert", true) -> "Posible recomendación/publicidad"
        p.contains("telemetry", true) || p.contains("analytics", true) -> "Posible telemetría"
        else -> "Revisar función antes de desactivar"
    }
}
