package com.phishware.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import com.phishware.android.R
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.notifications.NotificationHelper
import com.phishware.android.security.MessageTextAnalyzer
import com.phishware.android.security.UrlExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lee SMS entrantes en segundo plano, extrae SOLO las URLs y las analiza.
 * Nunca se envia el cuerpo del SMS al backend (Ley 1581: minimizacion de datos).
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var guard: GuardPreferences
    @Inject lateinit var session: SessionManager
    @Inject lateinit var repository: PhishwareRepository
    @Inject lateinit var notifications: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (!guard.isActive()) return@launch
                val smsOn = guard.smsEnabled
                // lectura suspendida del toggle
                val enabled = try {
                    smsOn.first()
                } catch (_: Exception) { true }
                if (!enabled) return@launch
                if (!session.isLoggedIn()) return@launch

                val msgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Telephony.Sms.Intents.getMessagesFromIntent(intent)
                } else emptyArray()
                val body = msgs.mapNotNull { it?.messageBody }.joinToString("\n")
                val urls = UrlExtractor.extractUrls(body)
                if (urls.isEmpty()) return@launch

                var notifOn = true
                try { notifOn = guard.notificationsEnabled.first() } catch (_: Exception) { }

                for (url in urls) {
                    if (!guard.shouldScan(url)) continue // dedup 5 min
                    val res = repository.analyzeUrl(url)
                    res.onSuccess { r ->
                        val level = r.riskLevel ?: "SAFE"
                        if (notifOn && level != "SAFE") {
                            notifications.showThreat(
                                url = r.originalUrl ?: url,
                                domain = r.domain ?: url,
                                riskLevel = level,
                                score = r.riskScore,
                                firstTip = r.recommendations?.firstOrNull()
                            )
                        }
                    }.onFailure {
                        // Sin backend: análisis local del texto completo (nunca sale del dispositivo)
                        val textRisk = MessageTextAnalyzer.analyze(body)
                        if (notifOn && textRisk.isThreat) {
                            val ctx = context.applicationContext
                            val tip = buildString {
                                append(ctx.getString(com.phishware.android.R.string.notif_offline_tip))
                                if (textRisk.principles.isNotEmpty()) {
                                    append(" · ")
                                    append(textRisk.principles.joinToString("/"))
                                }
                            }
                            notifications.showThreat(
                                url = url,
                                domain = url.substringAfter("://").substringBefore("/"),
                                riskLevel = textRisk.level,
                                score = textRisk.score,
                                firstTip = tip
                            )
                        }
                    }
                }
            } catch (_: Exception) {
            } finally { pending.finish() }
        }
    }
}
