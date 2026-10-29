package com.phishware.android.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val MONITORED = setOf(
        "com.whatsapp", "com.whatsapp.w4b",
        "com.google.android.gm", "com.microsoft.office.outlook",
        "org.telegram.messenger", "com.facebook.orca"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (pkg !in MONITORED) return
        // Ignora nuestras propias notificaciones
        if (pkg == packageName) return
        scope.launch {
            try {
                if (!guard.isActive()) return@launch
                if (!guard.notificationsListenerEnabled.first()) return@launch
                if (!session.isLoggedIn()) return@launch
                val extras = sbn.notification.extras
                val title = extras.getCharSequence("android.title")?.toString().orEmpty()
                val text = extras.getCharSequence("android.text")?.toString().orEmpty()
                val big = extras.getCharSequence("android.bigText")?.toString().orEmpty()
                val urls = UrlExtractor.extractUrls("$title\n$text\n$big")
                if (urls.isEmpty()) return@launch
                var notifOn = true
                try { notifOn = guard.notificationsEnabled.first() } catch (_: Exception) { }
                if (!notifOn) return@launch
                for (url in urls) {
                    if (!guard.shouldScan("notif:$url")) continue
                    repository.analyzeUrl(url).onSuccess { r ->
                        val level = r.riskLevel ?: "SAFE"
                        if (level != "SAFE") {
                            notifications.showThreat(
                                url = r.originalUrl ?: url,
                                domain = r.domain ?: url,
                                riskLevel = level,
                                score = r.riskScore,
                                firstTip = r.recommendations?.firstOrNull()
                            )
                        }
                    }
                }
            } catch (_: Exception) { }
        }
    }
}
