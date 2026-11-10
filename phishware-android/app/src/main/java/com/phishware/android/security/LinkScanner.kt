package com.phishware.android.security

import android.content.Context
import android.util.Log
import com.phishware.android.R
import com.phishware.android.data.local.BackgroundDetection
import com.phishware.android.data.local.BackgroundLog
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.notifications.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor único de revisión de enlaces en segundo plano (notificaciones, lectura de pantalla, SMS).
 *
 * Dos fases para avisar ANTES de que el usuario abra el enlace:
 *  1. Veredicto heurístico rápido (<1 s). Si ya es sospechoso/peligroso, la alerta sale de inmediato.
 *  2. Análisis completo (Safe Browsing + VirusTotal + contenido), que puede tardar ~15 s y
 *     confirma o corrige el veredicto.
 * Solo se envía la URL al backend, nunca el texto del mensaje (Ley 1581).
 */
@Singleton
class LinkScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PhishwareRepository,
    private val notifications: NotificationHelper,
    private val log: BackgroundLog,
    private val guard: GuardPreferences,
    private val session: SessionManager
) {
    private val recent = object : LinkedHashMap<String, Long>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?) = size > 200
    }

    /** True si esta URL ya se revisó hace poco (evita repetir el análisis mientras el chat sigue en pantalla). */
    @Synchronized
    private fun seenRecently(url: String, ttlMs: Long = 10 * 60 * 1000): Boolean {
        val now = System.currentTimeMillis()
        val last = recent[url]
        if (last != null && now - last < ttlMs) return true
        recent[url] = now
        return false
    }

    /** ¿Está activa la protección (consentimiento + interruptor general + toggle de avisos)? */
    suspend fun enabled(): Boolean {
        if (!guard.isActive()) return false
        return runCatching { guard.notificationsListenerEnabled.first() }.getOrDefault(true)
    }

    /** Explicación "el texto dice X pero lleva a Y", por URL de destino, para que no se pierda en las alertas posteriores. */
    private val deceptiveTips = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun noteDeceptive(hrefUrl: String, shownHost: String) {
        deceptiveTips[hrefUrl] = context.getString(R.string.deceptive_tip, shownHost, UrlExtractor.hostOf(hrefUrl))
    }

    suspend fun scan(rawUrl: String, source: String) {
        val url = rawUrl
        if (seenRecently(url)) return
        val notifOn = runCatching { guard.notificationsEnabled.first() }.getOrDefault(true)
        val domainGuess = url.substringAfter("://").substringBefore("/")

        // Fase 1: veredicto rápido
        var quickLevel = "SAFE"
        repository.quickCheck(url).onSuccess { q ->
            quickLevel = q.classification ?: "SAFE"
            Log.d("LinkScanner", "[$source] rápido $quickLevel ${q.riskScore} $domainGuess")
            if (quickLevel != "SAFE") {
                log.add(BackgroundDetection(url, q.domain ?: domainGuess, source, quickLevel,
                    q.riskScore, deceptiveTips[url] ?: q.recommendations?.firstOrNull(), System.currentTimeMillis()))
                if (notifOn) {
                    notifications.showThreat(
                        url = url, domain = q.domain ?: domainGuess, riskLevel = quickLevel,
                        score = q.riskScore, firstTip = deceptiveTips[url] ?: q.recommendations?.firstOrNull(),
                        sealBrand = q.officialSeal?.takeIf { it.official }?.brand, source = source
                    )
                }
            }
        }

        // Fase 2: análisis completo (requiere sesión)
        if (!session.isLoggedIn()) return
        repository.analyzeUrl(url).onSuccess { r ->
            val level = r.riskLevel ?: "SAFE"
            Log.d("LinkScanner", "[$source] completo $level ${r.riskScore} $domainGuess")
            log.add(BackgroundDetection(r.originalUrl ?: url, r.domain ?: domainGuess, source, level,
                r.riskScore, deceptiveTips[url] ?: r.recommendations?.firstOrNull(), System.currentTimeMillis()))
            if (!notifOn) return@onSuccess
            if (level == "SAFE") {
                if (quickLevel == "SAFE") {
                    notifications.showInfo(
                        context.getString(R.string.notif_safe_title),
                        context.getString(R.string.notif_safe_text, source, r.domain ?: domainGuess),
                        "safe:$url")
                }
            } else {
                val top = r.threats?.firstOrNull()
                notifications.showThreat(
                    url = r.originalUrl ?: url, domain = r.domain ?: domainGuess,
                    riskLevel = level, score = r.riskScore,
                    firstTip = deceptiveTips[url] ?: r.recommendations?.firstOrNull(),
                    principle = top?.persuasionPrinciple, microlessonSlug = top?.microlessonSlug,
                    sealBrand = r.officialSeal?.takeIf { it.official }?.brand, source = source
                )
            }
        }.onFailure {
            Log.w("LinkScanner", "[$source] análisis completo falló para $domainGuess", it)
            if (quickLevel == "SAFE" && notifOn) {
                notifications.showInfo(
                    context.getString(R.string.notif_fail_title),
                    context.getString(R.string.notif_fail_text, source),
                    "fail:$url")
            }
        }
    }

    /**
     * Enlace engañoso: el texto visible muestra un dominio (p. ej. "paypal.com") pero el destino real es otro.
     * Es la técnica de phishing por correo más común y no se ve a simple vista.
     */
    suspend fun scanDeceptive(shownHost: String, hrefUrl: String, source: String) {
        if (seenRecently("dec:$hrefUrl")) return
        val notifOn = runCatching { guard.notificationsEnabled.first() }.getOrDefault(true)
        val realHost = UrlExtractor.hostOf(hrefUrl)
        var level = "SUSPICIOUS"
        var score = 45.0
        repository.quickCheck(hrefUrl).onSuccess { q ->
            if (q.officialSeal?.official == true && q.classification == "SAFE") return  // p. ej. acortador/seguimiento legítimo de la propia marca
            if ((q.classification ?: "SAFE") != "SAFE") { level = q.classification ?: level; score = maxOf(score, q.riskScore) }
        }
        noteDeceptive(hrefUrl, shownHost)
        val tip = deceptiveTips.getValue(hrefUrl)
        log.add(BackgroundDetection(hrefUrl, realHost, source, level, score, tip, System.currentTimeMillis()))
        if (notifOn) {
            notifications.showThreat(url = hrefUrl, domain = realHost, riskLevel = level, score = score,
                firstTip = tip, source = source)
        }
    }

    /** Mensaje de ingeniería social SIN enlace (pide códigos, claves, urgencia...). Análisis 100 % local. */
    suspend fun scanText(text: String, source: String) {
        val risk = MessageTextAnalyzer.analyze(text)
        if (risk.level != "DANGEROUS") return
        val key = "txt:" + text.hashCode()
        if (seenRecently(key)) return
        val notifOn = runCatching { guard.notificationsEnabled.first() }.getOrDefault(true)
        log.add(BackgroundDetection("", source, source, "DANGEROUS", risk.score,
            context.getString(R.string.notif_text_body), System.currentTimeMillis()))
        if (notifOn) notifications.showTextThreat(source, risk.score, risk.principles, key)
    }
}
