package com.puntomira

import android.content.Context
import android.content.SharedPreferences

class Prefs(ctx: Context) {
    val sp: SharedPreferences = ctx.getSharedPreferences("mira", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", false)
        set(v) = sp.edit().putBoolean("enabled", v).apply()
    var shape: Int
        get() = sp.getInt("shape", 0)
        set(v) = sp.edit().putInt("shape", v).apply()
    var size: Int // índice en SIZES_DP
        get() = sp.getInt("size", 3)
        set(v) = sp.edit().putInt("size", v).apply()
    var color: Int // índice en COLORS
        get() = sp.getInt("color", 0)
        set(v) = sp.edit().putInt("color", v).apply()
    var opacity: Int // porcentaje 20..100
        get() = sp.getInt("opacity", 100)
        set(v) = sp.edit().putInt("opacity", v).apply()
    var offX: Int // píxeles
        get() = sp.getInt("offX", 0)
        set(v) = sp.edit().putInt("offX", v).apply()
    var offY: Int
        get() = sp.getInt("offY", 0)
        set(v) = sp.edit().putInt("offY", v).apply()

    companion object {
        val SHAPES = listOf("Punto", "Punto con aro", "Cruz pequeña")
        val SIZES_DP = intArrayOf(2, 3, 4, 6, 8, 12, 16) // diámetro del punto
        val COLORS = listOf(
            "Rojo" to 0xFFFF1E1E.toInt(),
            "Verde" to 0xFF22FF22.toInt(),
            "Cian" to 0xFF00E5FF.toInt(),
            "Amarillo" to 0xFFFFE600.toInt(),
            "Blanco" to 0xFFFFFFFF.toInt()
        )
    }
}
