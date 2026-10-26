package com.puntomira

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.math.max

class CrosshairView(ctx: Context) : View(ctx) {
    private val d = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = d }

    private var shape = 0
    private var size = 6f
    private var color = Color.RED
    private var offX = 0f
    private var offY = 0f

    /** Para la vista previa del menú: sin desplazamiento y con zoom. */
    var ignoreOffset = false
    var previewZoom = 1f

    fun update(p: Prefs) {
        shape = p.shape
        size = Prefs.SIZES_DP[p.size.coerceIn(0, Prefs.SIZES_DP.size - 1)] * d
        val base = Prefs.COLORS[p.color.coerceIn(0, Prefs.COLORS.size - 1)].second
        val a = p.opacity.coerceIn(0, 100) * 255 / 100
        color = Color.argb(a, Color.red(base), Color.green(base), Color.blue(base))
        edge.color = Color.argb(a * 100 / 255, 0, 0, 0) // contorno oscuro muy sutil
        offX = if (ignoreOffset) 0f else p.offX.toFloat()
        offY = if (ignoreOffset) 0f else p.offY.toFloat()
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        val cx = width / 2f + offX
        val cy = height / 2f + offY
        val r = size / 2f
        paint.color = color
        c.save()
        c.scale(previewZoom, previewZoom, cx, cy)
        when (shape) {
            0 -> dot(c, cx, cy, r)
            1 -> {
                dot(c, cx, cy, r)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(d, 1f)
                c.drawCircle(cx, cy, r * 3.5f + 2 * d, paint)
                paint.style = Paint.Style.FILL
            }
            2 -> {
                val t = max(d, size / 4f)
                val gap = r + 2 * d
                val arm = size * 1.5f + 2 * d
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = t
                c.drawLine(cx - gap - arm, cy, cx - gap, cy, paint)
                c.drawLine(cx + gap, cy, cx + gap + arm, cy, paint)
                c.drawLine(cx, cy - gap - arm, cx, cy - gap, paint)
                c.drawLine(cx, cy + gap, cx, cy + gap + arm, paint)
                paint.style = Paint.Style.FILL
            }
        }
        c.restore()
    }

    private fun dot(c: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.FILL
        c.drawCircle(cx, cy, r, paint)
        c.drawCircle(cx, cy, r + d / 2f, edge)
    }
}
