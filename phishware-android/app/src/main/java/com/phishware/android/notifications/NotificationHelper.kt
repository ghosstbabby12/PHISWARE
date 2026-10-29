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
            NotificationChannel(CHANNEL_GUARD, context.getString(R.string.channel_guard), NotificationManager.IMPORTANCE_LOW).apply {
                description = context.getString(R.string.channel_guard_desc)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_THREATS, context.getString(R.string.channel_threats), NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.channel_threats_desc)
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
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(context.getString(R.string.notif_guard_title))
            .setContentText(context.getString(R.string.notif_guard_text_fmt, threatsBlocked, unread))
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    fun showThreat(url: String, domain: String, riskLevel: String, score: Double, firstTip: String?) {
        ensureChannels()
        val (title, icon) = when (riskLevel) {
            "DANGEROUS" -> context.getString(R.string.notif_threat_danger) to R.drawable.ic_alert
            "SUSPICIOUS" -> context.getString(R.string.notif_threat_susp) to R.drawable.ic_analyze
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
            .addAction(0, context.getString(R.string.notif_action_view), open)
            .addAction(0, context.getString(R.string.notif_action_report), report)
            .addAction(0, context.getString(R.string.notif_action_ignore), ignore)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(url.hashCode(), notif)
    }
}
