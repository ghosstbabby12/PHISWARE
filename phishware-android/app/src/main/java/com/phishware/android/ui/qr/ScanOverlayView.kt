package com.phishware.android.ui.qr

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.ColorUtils

/**
 * Marco de escaneo sobre la cámara: oscurece el exterior, dibuja las cuatro esquinas,
 * una línea que barre mientras busca y un anillo de avance mientras el QR se mantiene quieto.
 *
 * [progress] va de 0 a 1 (tiempo que el mismo QR lleva estable). Al llegar a 1 el marco se pone verde.
 */
class ScanOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val dim = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(140, 0, 0, 0) }
    private val corner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(5f)
        color = Color.WHITE
    }
    private val scan = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dp(3f)
        strokeCap = Paint.Cap.ROUND
    }
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(6f)
        color = Color.argb(70, 255, 255, 255)
    }
    private val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(6f)
    }

    private val frame = RectF()
    private val outside = Path()
    private var sweep = 0f
    private var animator: ValueAnimator? = null

    /** 0..1: avance de «mantén el QR quieto». */
    var progress: Float = 0f
        set(value) {
            val v = value.coerceIn(0f, 1f)
            if (v != field) { field = v; invalidate() }
        }

    /** Resalta el marco una vez que el QR quedó fijado. */
    var locked: Boolean = false
        set(value) { if (value != field) { field = value; invalidate() } }

    private fun dp(v: Float) = v * resources.displayMetrics.density

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { sweep = it.animatedValue as Float; if (progress == 0f && !locked) invalidate() }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val side = minOf(w, h) * 0.68f
        frame.set((w - side) / 2f, (h - side) / 2f - dp(8f), (w + side) / 2f, (h + side) / 2f - dp(8f))
    }

    override fun onDraw(canvas: Canvas) {
        val radius = dp(22f)
        outside.reset()
        outside.fillType = Path.FillType.EVEN_ODD
        outside.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
        outside.addRoundRect(frame, radius, radius, Path.Direction.CW)
        canvas.drawPath(outside, dim)

        val accent = when {
            locked -> Color.parseColor("#2BD38B")
            progress > 0f -> ColorUtils.blendARGB(Color.WHITE, Color.parseColor("#2BD38B"), progress)
            else -> Color.WHITE
        }
        corner.color = accent
        val len = dp(34f)
        val l = frame.left; val t = frame.top; val r = frame.right; val b = frame.bottom
        // esquinas redondeadas
        for ((cx, cy, dx, dy) in listOf(
            Quad(l, t, 1f, 1f), Quad(r, t, -1f, 1f), Quad(l, b, 1f, -1f), Quad(r, b, -1f, -1f)
        )) {
            val p = Path()
            p.moveTo(cx, cy + dy * len)
            p.lineTo(cx, cy + dy * radius)
            p.quadTo(cx, cy, cx + dx * radius, cy)
            p.lineTo(cx + dx * len, cy)
            canvas.drawPath(p, corner)
        }

        if (progress == 0f && !locked) {
            // línea de búsqueda
            val y = frame.top + dp(14f) + (frame.height() - dp(28f)) * sweep
            scan.shader = android.graphics.LinearGradient(
                frame.left, y, frame.right, y,
                intArrayOf(Color.TRANSPARENT, Color.WHITE, Color.TRANSPARENT), null,
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawLine(frame.left + dp(18f), y, frame.right - dp(18f), y, scan)
        } else {
            // barra de avance bajo el marco
            val barW = frame.width() * 0.5f
            val x0 = frame.centerX() - barW / 2f
            val y = frame.bottom + dp(22f)
            canvas.drawLine(x0, y, x0 + barW, y, track)
            bar.color = accent
            canvas.drawLine(x0, y, x0 + barW * (if (locked) 1f else progress), y, bar)
        }
    }

    private data class Quad(val a: Float, val b: Float, val c: Float, val d: Float)
}
