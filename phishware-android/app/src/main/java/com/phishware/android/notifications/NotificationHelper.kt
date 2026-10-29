package com.phishware.android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.phishware.android.R
import com.phishware.android.receiver.ActionReceiver
import com.phishware.android.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** Notificaciones enriquecidas estilo iOS: icono por severidad + acciones Ver/Reportar/Ignorar. */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_GUARD = "guard_persistent"
        const val CHANNEL_THREATS = "threat_alerts"
        const val NOTIF_GUARD_ID = 1001
        const val EXTRA_URL = "extra_url"
        const val EXTRA_RISK = "extra_risk"
    }

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_GUARD, "Proteccion PhishWare", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Servicio de proteccion en segundo plano"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_THREATS, "Alertas de amenazas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos de phishing detectado"
                enableVibration(true)
            }
        )
    }

    fun persistentGuardNotification(threatsBlocked: Long, unread: Long): android.app.Notification {
        ensureChannels()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("dest", "analysis")
        }
        val pi = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, CHANNEL_GUARD)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("PhishWare activo")
            .setContentText("Proteccion en segundo plano · $threatsBlocked amenazas · $unread sin leer")
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    fun showThreat(url: String, domain: String, riskLevel: String, score: Double, firstTip: String?) {
        ensureChannels()
        val (title, icon) = when (riskLevel) {
            "DANGEROUS" -> "Posible phishing bloqueado" to R.drawable.ic_alert
            "SUSPICIOUS" -> "Enlace sospechoso" to R.drawable.ic_analyze
            else -> return // SAFE no notifica (evita ruido)
        }
        val open = PendingIntent.getActivity(
            context, Random.nextInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("dest", "analysis"); putExtra(EXTRA_URL, url)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ignore = PendingIntent.getBroadcast(
            context, Random.nextInt(),
            Intent(context, ActionReceiver::class.java).apply {
                action = ActionReceiver.ACTION_IGNORE
                putExtra("notif_id", url.hashCode())
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val report = PendingIntent.getActivity(
            context, Random.nextInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("dest", "reports"); putExtra(EXTRA_URL, url)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = "$domain · riesgo ${"%.0f".format(score)}/100" +
            (firstTip?.let { "\n• $it" } ?: "")
        // Estilo iPhone: banner arriba (heads-up) con sonido + vibración + acciones
        val notif = NotificationCompat.Builder(context, CHANNEL_THREATS)
            .setSmallIcon(icon)
            .setColor(0xFFDC2626.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setSubText("PHISHWARE")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$text\n$url").setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setContentIntent(open)
            .addAction(0, "Ver", open)
            .addAction(0, "Reportar", report)
            .addAction(0, "Ignorar", ignore)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(url.hashCode(), notif)
    }
}
