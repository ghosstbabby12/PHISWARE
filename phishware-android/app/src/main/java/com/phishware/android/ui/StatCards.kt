package com.phishware.android.ui

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import com.phishware.android.databinding.ItemStatCardBinding

/**
 * Tarjetas de estadísticas compartidas por Inicio y Escanear QR.
 * Misma idea que StatCard de la web: número que cuenta hacia arriba y barra de proporción opcional.
 */
object StatCards {

    fun bindStatic(card: ItemStatCardBinding, label: Int, icon: Int, color: String, showBar: Boolean = true) {
        card.tvStatLabel.setText(label)
        card.ivStatIcon.setImageResource(icon)
        ImageViewCompat.setImageTintList(card.ivStatIcon, ColorStateList.valueOf(Color.parseColor(color)))
        card.barStat.progressTintList = ColorStateList.valueOf(Color.parseColor(color))
        card.barStat.visibility = if (showBar) View.VISIBLE else View.GONE
    }

    fun setValue(card: ItemStatCardBinding, value: Long, ratio: Int, sub: String) {
        card.tvStatSub.text = sub
        countUp(card.tvStatValue, value.toInt())
        if (card.barStat.visibility == View.VISIBLE) animateProgress(card.barStat, ratio.coerceIn(0, 100))
    }

    /** Cuenta desde el valor mostrado hasta el nuevo con easing. */
    fun countUp(view: TextView, to: Int) {
        val from = (view.tag as? Int) ?: 0
        view.tag = to
        ValueAnimator.ofInt(from, to).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { view.text = it.animatedValue.toString() }
            start()
        }
    }

    fun animateProgress(bar: ProgressBar, to: Int) {
        ValueAnimator.ofInt(bar.progress, to).apply {
            duration = 900
            interpolator = DecelerateInterpolator()
            addUpdateListener { bar.progress = it.animatedValue as Int }
            start()
        }
    }
}
