package es.cleantv.tv

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var repo: AdbRepository
    private lateinit var status: TextView
    private lateinit var items: LinearLayout
    private lateinit var search: EditText
    private var allPackages: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = AdbRepository(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 28, 40, 20)
            setBackgroundColor(Color.rgb(16, 24, 32))
        }
        setContentView(root)
        root.addView(text("CleanTV", 32, true))
        root.addView(text("Limpieza reversible de Android TV · Versión experimental", 16))
        status = text("Activa depuración ADB por TCP en el puerto 5555. Autoriza la conexión si el televisor lo solicita.", 16)
        root.addView(status)
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        root.addView(bar)
        bar.addView(button("Conectar") { runTask { repo.probe() } })
        bar.addView(button("Analizar apps") { runTask {
            val found = repo.packages()
            runOnUiThread { allPackages = found; display() }
            "Encontrados ${found.size} paquetes"
        } })
        bar.addView(button("Restaurar cambios") { showRestores() })
        search = EditText(this).apply { hint = "Filtrar por nombre de paquete"; setSingleLine(true); setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY) }
        root.addView(search)
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { display() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        val scroller = ScrollView(this)
        root.addView(scroller, LinearLayout.LayoutParams(-1, 0, 1f))
        items = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroller.addView(items)
        root.addView(text("No desactives aplicaciones desconocidas. No se sustituye el launcher de forma automática.", 13))
    }

    private fun display() {
        items.removeAllViews()
        val filter = search.text.toString().trim()
        allPackages.filter { it.contains(filter, true) }.take(250).forEach { pkg ->
            val isProtected = ProtectedPackages.isProtected(pkg, packageName)
            val isChanged = pkg in repo.savedChanges()
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val info = text("$pkg\n${if (isProtected) "PROTEGIDA" else if (isChanged) "DESACTIVADA POR CLEANTV" else ProtectedPackages.label(pkg)}", 15)
            line.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            line.addView(button(if (isProtected) "Bloqueada" else if (isChanged) "Restaurar" else "Desactivar") {
                if (isChanged) {
                    AlertDialog.Builder(this).setTitle("¿Restaurar esta app?")
                        .setMessage(pkg).setNegativeButton("Cancelar", null)
                        .setPositiveButton("Restaurar") { _, _ -> runTask { repo.restore(pkg) } }.show()
                } else AlertDialog.Builder(this).setTitle("¿Desactivar esta app?")
                    .setMessage("$pkg\n\nPodrías perder funciones de tu TV. Se guardará un registro para poder restaurarla. ¿Continuar?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Desactivar") { _, _ -> runTask { repo.disable(pkg) } }
                    .show()
            }.apply { isEnabled = !isProtected })
            items.addView(line)
        }
    }

    private fun showRestores() {
        val saved = repo.savedChanges().sorted()
        if (saved.isEmpty()) { status.text = "No hay cambios registrados en esta instalación"; return }
        AlertDialog.Builder(this).setTitle("Restaurar paquete")
            .setItems(saved.toTypedArray()) { _, which ->
                val pkg = saved[which]
                AlertDialog.Builder(this).setTitle("Restaurar $pkg")
                    .setMessage("¿Volver a habilitar esta aplicación?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Restaurar") { _, _ -> runTask { repo.restore(pkg) } }.show()
            }.setNegativeButton("Cerrar", null).show()
    }

    private fun runTask(task: () -> String) {
        status.text = "Trabajando…"
        worker.execute {
            val result = try { task() } catch (t: Exception) { "Error: ${t.message ?: t.javaClass.simpleName}" }
            runOnUiThread { status.text = result; if (allPackages.isNotEmpty()) display() }
        }
    }

    private fun text(value: String, size: Int, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(Color.WHITE)
        setPadding(8, 8, 8, 12)
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun button(label: String, clicked: () -> Unit) = Button(this).apply {
        text = label; isFocusable = true; setOnClickListener { clicked() }
    }
    override fun onDestroy() { worker.shutdownNow(); super.onDestroy() }
}
