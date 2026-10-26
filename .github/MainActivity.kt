package com.puntomira

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Fila navegable con el mando: ◀ ▶ cambian el valor, OK avanza al siguiente. */
class Row(
    ctx: Context,
    private val label: String,
    private val values: List<String>,
    var index: Int,
    private val wrap: Boolean = true,
    private val onChange: (Int) -> Unit
) : TextView(ctx) {

    init {
        isFocusable = true
        textSize = 24f
        val d = resources.displayMetrics.density
        setPadding((16 * d).toInt(), (10 * d).toInt(), (16 * d).toInt(), (10 * d).toInt())
        setTextColor(0xFFB8B8C8.toInt())
        setOnFocusChangeListener { _, f ->
            setBackgroundColor(if (f) 0xFF2A2A3C.toInt() else 0)
            setTextColor(if (f) 0xFFFFFFFF.toInt() else 0xFFB8B8C8.toInt())
        }
        refresh()
    }

    fun refresh() {
        text = "◀   $label: ${values[index]}   ▶"
    }

    fun set(i: Int, notify: Boolean = true) {
        index = i
        refresh()
        if (notify) onChange(i)
    }

    private fun move(delta: Int) {
        val n = values.size
        val next = if (wrap) (index + delta + n) % n else (index + delta).coerceIn(0, n - 1)
        if (next != index) set(next)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT -> { move(-1); true }
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER -> { move(1); true }
        else -> super.onKeyDown(keyCode, event)
    }
}

class MainActivity : Activity() {
    private lateinit var prefs: Prefs
    private lateinit var toggleRow: Row
    private lateinit var preview: CrosshairView

    // Campo (no lambda suelta): SharedPreferences guarda solo una referencia débil
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        preview.update(prefs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        val d = resources.displayMetrics.density
        fun px(v: Int) = (v * d).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF101018.toInt())
            setPadding(px(48), px(32), px(48), px(32))
        }

        // ---------- Columna izquierda: logo + título + opciones ----------
        val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, px(16))
        }
        header.addView(ImageView(this).apply {
            setImageResource(R.drawable.logo)
            adjustViewBounds = true
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, px(72))
        })
        header.addView(TextView(this).apply {
            text = "HNVK CROSSHAIR"
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(px(20), 0, 0, 0)
        })
        left.addView(header)

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        toggleRow = Row(this, "Mira", listOf("Apagada", "Encendida"), if (prefs.enabled) 1 else 0) {
            setOverlay(it == 1)
        }
        list.addView(toggleRow)
        list.addView(Row(this, "Forma", Prefs.SHAPES, prefs.shape) { prefs.shape = it })
        list.addView(Row(this, "Tamaño", Prefs.SIZES_DP.map { "$it dp" }, prefs.size) { prefs.size = it })
        list.addView(Row(this, "Color", Prefs.COLORS.map { it.first }, prefs.color) { prefs.color = it })

        val opac = (20..100 step 10).toList()
        list.addView(Row(this, "Opacidad", opac.map { "$it%" }, ((prefs.opacity - 20) / 10).coerceIn(0, opac.size - 1)) {
            prefs.opacity = opac[it]
        })

        val pos = (-200..200).toList()
        list.addView(Row(this, "Posición X", pos.map { "$it px" }, (prefs.offX + 200).coerceIn(0, 400), false) {
            prefs.offX = pos[it]
        })
        list.addView(Row(this, "Posición Y", pos.map { "$it px" }, (prefs.offY + 200).coerceIn(0, 400), false) {
            prefs.offY = pos[it]
        })

        left.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // ---------- Columna derecha: vista previa ----------
        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        right.addView(TextView(this).apply {
            text = "Vista previa (zoom x3)"
            textSize = 18f
            setTextColor(0xFFB8B8C8.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, px(8))
        })
        preview = CrosshairView(this).apply { ignoreOffset = true; previewZoom = 3f }
        val box = FrameLayout(this).apply { setBackgroundColor(0xFF1C1C28.toInt()) }
        box.addView(preview, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        right.addView(box, LinearLayout.LayoutParams(px(320), px(200)))
        right.addView(TextView(this).apply {
            text = "La mira se mantiene activa al salir de la app"
            textSize = 16f
            setTextColor(0xFF80809A.toInt())
            gravity = Gravity.CENTER
            setPadding(0, px(12), 0, 0)
        })

        root.addView(left, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.3f))
        root.addView(right, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        setContentView(root)

        preview.update(prefs)
        prefs.sp.registerOnSharedPreferenceChangeListener(listener)
        toggleRow.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        // Si se revocó el permiso, reflejarlo en la fila
        if (prefs.enabled && !Settings.canDrawOverlays(this)) {
            prefs.enabled = false
            toggleRow.set(0, notify = false)
        }
    }

    override fun onDestroy() {
        prefs.sp.unregisterOnSharedPreferenceChangeListener(listener)
        super.onDestroy()
    }

    private fun setOverlay(on: Boolean) {
        if (on) {
            if (!Settings.canDrawOverlays(this)) {
                askPermission()
                toggleRow.set(0, notify = false)
                return
            }
            prefs.enabled = true
            startForegroundService(Intent(this, OverlayService::class.java))
        } else {
            prefs.enabled = false
            stopService(Intent(this, OverlayService::class.java))
        }
    }

    private fun askPermission() {
        try {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Concede el permiso por ADB:\nadb shell appops set $packageName SYSTEM_ALERT_WINDOW allow",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
