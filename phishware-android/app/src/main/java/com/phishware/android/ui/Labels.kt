package com.phishware.android.ui

import android.content.Context
import com.phishware.android.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Etiquetas legibles para valores que llegan del servidor en bruto (nivel, severidad, categoría, fecha). */
object Labels {

    fun risk(context: Context, level: String?): String = when (level) {
        "SAFE", "LOW" -> context.getString(R.string.qr_status_safe)
        "SUSPICIOUS", "MEDIUM" -> context.getString(R.string.qr_status_susp)
        "DANGEROUS", "HIGH", "CRITICAL" -> context.getString(R.string.qr_status_dang)
        else -> level.orEmpty()
    }

    fun severity(context: Context, severity: String?): String = when (severity?.uppercase()) {
        "CRITICAL" -> context.getString(R.string.sev_critical)
        "HIGH" -> context.getString(R.string.sev_high)
        "MEDIUM" -> context.getString(R.string.sev_medium)
        "LOW" -> context.getString(R.string.sev_low)
        else -> humanize(severity)
    }

    private val categoriesEs = mapOf(
        "SOCIAL_ENGINEERING" to "Ingeniería social",
        "PHISHING" to "Phishing",
        "MALWARE" to "Malware",
        "PASSWORDS" to "Contraseñas",
        "PRIVACY" to "Privacidad",
        "BASICS" to "Conceptos básicos",
    )

    /** «SOCIAL_ENGINEERING» → «Ingeniería social» / «Social engineering». */
    fun category(raw: String?): String {
        if (raw.isNullOrBlank()) return "PHISHWARE"
        val key = raw.trim().uppercase()
        if (Locale.getDefault().language == "es") categoriesEs[key]?.let { return it }
        return humanize(raw)
    }

    private fun humanize(raw: String?): String {
        val s = raw.orEmpty().trim().replace('_', ' ').lowercase()
        return s.replaceFirstChar { it.uppercase() }
    }

    /** «2026-11-25T04:22:39.283731» → «25 nov 2026, 04:22» según el idioma. */
    fun dateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return try {
            val clean = iso.substringBefore('Z').substringBefore('+').substringBefore(" ")
            val dt = LocalDateTime.parse(clean)
            dt.format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault()))
        } catch (_: Exception) {
            iso.replace('T', ' ').substringBefore('.')
        }
    }
}
