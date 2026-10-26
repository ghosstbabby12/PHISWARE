package com.phishware.android.security

import java.util.Locale

/** Extrae URLs de textos libres (SMS, correos compartidos, QR). Solo URLs: nunca se envia el cuerpo del mensaje al backend. */
object UrlExtractor {

    private val URL_REGEX = Regex(
        """(?i)\b((?:https?://|www\.)[^\s<>"'()]+)""",
        RegexOption.IGNORE_CASE
    )
    // Dominios desnudos típicos de smishing (sin https:// ni www): nequi-verificacion.tk/login
    private val BARE_DOMAIN_REGEX = Regex(
        """(?i)\b((?:[a-z0-9-]+\.)+(?:com|net|org|info|biz|xyz|tk|ml|ga|cf|top|icu|sbs|buzz|work|date|review|stream|science|racing|download|cricket|wang|faith|bid|men|loan|trade|party|zip|country|co|io|app|link|site|online|click|live|shop|store)(?:/[^\s<>"'()]*)?)""",
        RegexOption.IGNORE_CASE
    )
    private const val MAX_URL_LEN = 2048

    fun extractUrls(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        val withScheme = URL_REGEX.findAll(text).map { it.value }
        // Solo buscar desnudos fuera de lo ya capturado para no duplicar
        val masked = URL_REGEX.replace(text, " ")
        val bare = BARE_DOMAIN_REGEX.findAll(masked).map { it.value }
        return (withScheme + bare)
            .map { it.trim().trimEnd('.', ',', ';', '!', '?', ')', ']', '"', '\'') }
            .map { normalize(it) }
            .filter { it.length <= MAX_URL_LEN && isScannable(it) }
            .distinct()
            .take(5) // tope por mensaje: evita rate-limit 30/min del backend
            .toList()
    }

    fun normalize(raw: String): String {
        var u = raw.trim()
        if (!u.startsWith("http://", ignoreCase = true) && !u.startsWith("https://", ignoreCase = true)) {
            u = "https://$u"
        }
        return u
    }

    /** Ignora localhost/RFC1918 y esquemas propios, igual que la extension de navegador. */
    fun isScannable(url: String): Boolean {
        val lower = url.lowercase(Locale.US)
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) return false
        val host = runCatching {
            val noScheme = lower.substringAfter("://").substringBefore("/")
            noScheme.substringAfter("@").substringBefore(":")
        }.getOrDefault("")
        if (host.isBlank()) return false
        if (host == "localhost" || host.startsWith("127.") || host == "::1") return false
        if (host.startsWith("10.") || host.startsWith("192.168.")) return false
        if (host.startsWith("172.")) {
            val second = host.split(".").getOrNull(1)?.toIntOrNull()
            if (second != null && second in 16..31) return false
        }
        return true
    }
}
