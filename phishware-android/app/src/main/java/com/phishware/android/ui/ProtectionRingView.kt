package com.phishware.android.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

/** Anillo de protección del inicio (porcentaje de enlaces seguros), como el de la web. */
class ProtectionRingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val stroke = 10f * resources.displayMetrics.density
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = stroke; color = Color.argb(46, 255, 255, 255)
    }
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND; color = Color.WHITE
    }
    private val box = RectF()
    private var shown = 0f

    /** Anima el anillo hasta [percent] (0..100). */
    fun setPercent(percent: Int) {
        ValueAnimator.ofFloat(shown, percent.coerceIn(0, 100).toFloat()).apply {
            duration = 1100
            interpolator = DecelerateInterpolator()
            addUpdateListener { shown = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val pad = stroke / 2f + 2f
        box.set(pad, pad, width - pad, height - pad)
        canvas.drawArc(box, 0f, 360f, false, track)
        canvas.drawArc(box, -90f, 360f * shown / 100f, false, arc)
    }
}
