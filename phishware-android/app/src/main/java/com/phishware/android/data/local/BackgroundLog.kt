package com.phishware.android.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** Un enlace detectado en segundo plano (notificación o SMS), con su veredicto. */
data class BackgroundDetection(
    val url: String,
    val domain: String,
    val source: String,       // "WhatsApp", "Instagram", "SMS"...
    val riskLevel: String,    // SAFE / SUSPICIOUS / DANGEROUS
    val score: Double,
    val tip: String?,
    val timestamp: Long
)

/**
 * Bitácora local de lo que PHISHWARE detectó en segundo plano. Solo guarda URL, dominio,
 * app de origen y veredicto: nunca el texto del mensaje (Ley 1581).
 */
@Singleton
class BackgroundLog @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("background_log", Context.MODE_PRIVATE)

    @Synchronized
    fun add(d: BackgroundDetection) {
        val arr = JSONArray()
        arr.put(toJson(d))
        val old = load().filter { it.url != d.url }
        for (i in 0 until minOf(old.size, MAX - 1)) arr.put(toJson(old[i]))
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    @Synchronized
    fun load(): List<BackgroundDetection> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun clear() = prefs.edit().remove(KEY).apply()

    private fun toJson(d: BackgroundDetection) = JSONObject()
        .put("url", d.url).put("domain", d.domain).put("source", d.source)
        .put("level", d.riskLevel).put("score", d.score).put("tip", d.tip ?: "")
        .put("ts", d.timestamp)

    private fun fromJson(o: JSONObject) = BackgroundDetection(
        url = o.optString("url"), domain = o.optString("domain"), source = o.optString("source"),
        riskLevel = o.optString("level", "SAFE"), score = o.optDouble("score", 0.0),
        tip = o.optString("tip").ifBlank { null }, timestamp = o.optLong("ts")
    )

    companion object {
        private const val KEY = "items"
        private const val MAX = 100

        /** Nombre legible de la app que originó la notificación. */
        fun sourceLabel(pkg: String): String = when {
            pkg.startsWith("com.whatsapp") -> "WhatsApp"
            pkg.startsWith("com.instagram") -> "Instagram"
            pkg.startsWith("com.facebook.orca") || pkg == "com.facebook.mlite" -> "Messenger"
            pkg == "com.facebook.katana" -> "Facebook"
            pkg.startsWith("org.telegram") -> "Telegram"
            pkg == "com.google.android.gm" -> "Gmail"
            pkg == "com.microsoft.office.outlook" -> "Outlook"
            pkg.contains("messag") || pkg == "com.android.mms" -> "SMS"
            pkg == "com.discord" -> "Discord"
            pkg == "com.snapchat.android" -> "Snapchat"
            pkg == "com.twitter.android" -> "X"
            pkg == "com.zhiliaoapp.musically" -> "TikTok"
            pkg == "com.linkedin.android" -> "LinkedIn"
            pkg == "org.thoughtcrime.securesms" -> "Signal"
            else -> pkg.substringAfterLast('.')
        }
    }
}
