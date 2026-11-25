package com.phishware.android.ui

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan

/**
 * Limpia los textos que llegan del servidor antes de mostrarlos:
 * quita símbolos de Markdown (#, **, -, `), emojis y etiquetas internas como «NIST CSF PR:».
 */
object TextClean {

    private val emoji = Regex("[\\p{So}\\uFE0F\\u200D]")
    private val nistBracket = Regex("\\s*\\[\\s*NIST CSF[^\\]]*\\]?", RegexOption.IGNORE_CASE)
    private val nistPrefix = Regex("^\\s*NIST CSF\\s+[A-Z]{2}(?:\\.[A-Z]{2}-\\d+)?\\s*:\\s*", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val heading = Regex("^\\s{0,3}#{1,6}\\s*(.*?)\\s*#*\\s*$")
    private val bullet = Regex("^\\s*[-*+•]\\s+")
    private val quote = Regex("^\\s*>\\s?")
    private val rule = Regex("^\\s*([-*_]\\s*){3,}$")
    private val link = Regex("\\[([^\\]]+)]\\([^)]*\\)")
    private val bold = Regex("(\\*\\*|__)(.+?)\\1")
    private val italic = Regex("(?<![\\w*])\\*(?!\\s)(.+?)(?<!\\s)\\*(?![\\w*])")
    private val code = Regex("`([^`]*)`")

    /** Texto simple de una sola pieza (mensajes, recomendaciones, avisos). */
    fun plain(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.lines().joinToString("\n") { cleanLine(it).first }.collapseBlank()
    }

    /** Lección o texto largo: títulos en negrita, viñetas con «•» y sin símbolos de Markdown. */
    fun rich(raw: String?, dropTitle: String? = null): CharSequence {
        if (raw.isNullOrBlank()) return ""
        val sb = SpannableStringBuilder()
        var firstHeadingSeen = false
        for (line in raw.lines()) {
            val (text, isHeading) = cleanLine(line)
            if (isHeading && !firstHeadingSeen) {
                firstHeadingSeen = true
                // El primer título repite el del cuadro de diálogo: se omite.
                if (dropTitle != null && sameTitle(text, dropTitle)) continue
            }
            if (sb.isNotEmpty() && sb.last() != '\n') sb.append('\n')
            val start = sb.length
            sb.append(text)
            if (isHeading && text.isNotBlank()) {
                sb.setSpan(StyleSpan(Typeface.BOLD), start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                sb.setSpan(RelativeSizeSpan(1.08f), start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        val cleaned = SpannableStringBuilder(sb)
        // Colapsa líneas en blanco repetidas sin perder los estilos.
        var i = 0
        var blanks = 0
        while (i < cleaned.length) {
            if (cleaned[i] == '\n') {
                blanks++
                if (blanks > 2) { cleaned.delete(i, i + 1); continue }
            } else blanks = 0
            i++
        }
        return cleaned.trimEnd()
    }

    private fun cleanLine(line: String): Pair<String, Boolean> {
        var s = line.trimEnd()
        if (rule.matches(s)) return "" to false
        val h = heading.matchEntire(s)
        val isHeading = h != null
        if (h != null) s = h.groupValues[1]
        s = quote.replace(s, "")
        s = bullet.replace(s, "• ")
        s = link.replace(s) { it.groupValues[1] }
        s = bold.replace(s) { it.groupValues[2] }
        s = italic.replace(s) { it.groupValues[1] }
        s = code.replace(s) { it.groupValues[1] }
        s = nistPrefix.replace(s, "")
        s = nistBracket.replace(s, "")
        s = emoji.replace(s, "")
        s = s.replace(Regex("[ \\t]{2,}"), " ").trim()
        return s to isHeading
    }

    private fun sameTitle(a: String, b: String): Boolean =
        a.trim().trimEnd(':', '.').equals(b.trim().trimEnd(':', '.'), ignoreCase = true)

    private fun String.collapseBlank(): String =
        replace(Regex("\\n{3,}"), "\n\n").trim()
}
