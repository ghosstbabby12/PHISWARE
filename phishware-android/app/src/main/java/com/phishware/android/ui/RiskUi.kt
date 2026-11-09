package com.phishware.android.ui

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan

/**
 * Puntos de estado monocromos (●) con el color semántico del sistema,
 * en lugar de emojis. Alineado con DESIGN.md: verde = seguro,
 * ámbar = sospechoso, rojo = peligroso, vino = informativo.
 */
object RiskUi {
    const val SAFE = "#16845B"
    const val SUSPICIOUS = "#C78316"
    const val DANGEROUS = "#C6283D"
    const val INFO = "#790D2B"

    fun colorFor(level: String?): String = when (level) {
        "DANGEROUS", "CRITICAL" -> DANGEROUS
        "SUSPICIOUS", "HIGH", "MEDIUM" -> SUSPICIOUS
        "SAFE", "LOW" -> SAFE
        else -> INFO
    }

    fun dotLine(level: String?, text: String?): CharSequence {
        val sb = SpannableStringBuilder("● ")
        sb.setSpan(
            ForegroundColorSpan(Color.parseColor(colorFor(level))),
            0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sb.append(text.orEmpty())
        return sb
    }
}
