package com.phishware.android.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.phishware.android.R
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.notifications.NotificationHelper
import com.phishware.android.security.UrlExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Protege correo (Gmail/Outlook) y WhatsApp/Telegram sin leer el inbox:
 * solo analiza el texto visible de la NOTIFICACIÓN entrante.
 * Privacidad (Ley 1581): nunca se envía el cuerpo del mensaje al backend,
 * solo la URL extraída para análisis. Requiere consentimiento + acceso a
 * notificaciones activado por el usuario en Ajustes del sistema.
 */
@AndroidEntryPoint
class PhishNotificationListener : NotificationListenerService() {

    @Inject lateinit var guard: GuardPreferences
    @Inject lateinit var session: SessionManager
    @Inject lateinit var repository: PhishwareRepository
    @Inject lateinit var notifications: NotificationHelper
    @Inject lateinit var bgLog: com.phishware.android.data.local.BackgroundLog
    @Inject lateinit var scanner: com.phishware.android.security.LinkScanner

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val MONITORED = setOf(
        // WhatsApp
        "com.whatsapp", "com.whatsapp.w4b",
        // Instagram / Facebook / Messenger
        "com.instagram.android", "com.instagram.barcelona",
        "com.facebook.katana", "com.facebook.orca", "com.facebook.mlite",
        // Correo
        "com.google.android.gm", "com.microsoft.office.outlook", "com.yahoo.mobile.client.android.mail",
        "com.samsung.android.email.provider", "ch.protonmail.android", "com.fsck.k9", "com.my.mail",
        // Otras mensajerías
        "org.telegram.messenger", "org.telegram.messenger.web", "org.thoughtcrime.securesms",
        "com.discord", "com.snapchat.android", "com.twitter.android", "com.zhiliaoapp.musically",
        "com.linkedin.android",
        // SMS / Mensajes (Google y Samsung)
        "com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms"
    )

    /** Junta todo el texto visible de la notificación, incluidos mensajes agrupados (MessagingStyle/InboxStyle). */
    private fun collectText(n: android.app.Notification): String {
        val e = n.extras
        val parts = mutableListOf<String>()
        for (k in listOf("android.title", "android.text", "android.bigText", "android.subText",
            "android.summaryText", "android.infoText", "android.title.big")) {
            e.getCharSequence(k)?.toString()?.let { parts += it }
        }
        e.getCharSequenceArray("android.textLines")?.forEach { parts += it.toString() }
        try {
            @Suppress("DEPRECATION")
            val msgs = e.getParcelableArray("android.messages")
            msgs?.forEach { m ->
                (m as? android.os.Bundle)?.getCharSequence("text")?.toString()?.let { parts += it }
            }
        } catch (_: Exception) { }
        n.tickerText?.toString()?.let { parts += it }
        return parts.joinToString("\n")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (pkg !in MONITORED || pkg == packageName) return
        val text = collectText(sbn.notification)
        scope.launch {
            try {
                if (!scanner.enabled()) return@launch
                val urls = UrlExtractor.extractUrls(text)
                android.util.Log.d("PhishListener", "$pkg -> ${urls.size} URL(s)")
                val source = com.phishware.android.data.local.BackgroundLog.sourceLabel(pkg)
                if (urls.isEmpty()) { scanner.scanText(text, source); return@launch }
                // En paralelo: la alerta rápida de un enlace no espera al análisis lento de otro
                urls.map { url -> launch { scanner.scan(url, source) } }
            } catch (e: Exception) {
                android.util.Log.w("PhishListener", "fallo analizando notificación de $pkg", e)
            }
        }
    }
}
