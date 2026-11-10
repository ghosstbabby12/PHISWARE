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
        const val CHANNEL_INFO = "link_checked"
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
            NotificationChannel(CHANNEL_INFO, context.getString(R.string.channel_info), NotificationManager.IMPORTANCE_LOW).apply {
                description = context.getString(R.string.channel_info_desc)
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

    fun showThreat(
        url: String,
        domain: String,
        riskLevel: String,
        score: Double,
        firstTip: String?,
        principle: String? = null,
        microlessonSlug: String? = null,
        sealBrand: String? = null,
        source: String? = null
    ) {
        ensureChannels()
        val (baseTitle, icon) = when (riskLevel) {
            "DANGEROUS" -> context.getString(R.string.notif_threat_danger) to R.drawable.ic_alert
            "SUSPICIOUS" -> context.getString(R.string.notif_threat_susp) to R.drawable.ic_analyze
            else -> return // SAFE no notifica (evita ruido)
        }
        val title = if (source.isNullOrBlank()) baseTitle else "$baseTitle · $source"
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
        val lines = mutableListOf("$domain · riesgo ${"%.0f".format(score)}/100")
        firstTip?.let { lines.add("• $it") }
        if (!principle.isNullOrBlank() && principle != "NONE") {
            lines.add("• " + context.getString(R.string.notif_principle_fmt, principle))
        }
        if (!microlessonSlug.isNullOrBlank()) {
            lines.add("• " + context.getString(R.string.notif_lesson_fmt, microlessonSlug))
        }
        if (!sealBrand.isNullOrBlank()) {
            lines.add(context.getString(R.string.notif_seal_fmt, sealBrand))
        }
        val text = lines.joinToString("\n")
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
            .apply {
                // Peligro real: vibración larga y repetida para que no pase desapercibida
                if (riskLevel == "DANGEROUS") setVibrate(longArrayOf(0, 400, 150, 400, 150, 600))
            }
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

    /** Aviso discreto (sin sonido, se borra solo) para que el usuario sepa que el enlace sí fue revisado. */
    fun showInfo(title: String, text: String, tag: String) {
        ensureChannels()
        val open = PendingIntent.getActivity(
            context, Random.nextInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_INFO)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setTimeoutAfter(15_000)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(tag.hashCode(), n)
    }

    /** Alerta fuerte para un mensaje de ingeniería social aunque NO traiga enlace (se analizó solo en el teléfono). */
    fun showTextThreat(source: String, score: Double, principles: List<String>, tag: String) {
        ensureChannels()
        val open = PendingIntent.getActivity(
            context, Random.nextInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = context.getString(R.string.notif_text_title, source)
        val why = if (principles.isEmpty()) "" else "\n" + context.getString(R.string.notif_principle_fmt, principles.joinToString("/"))
        val text = context.getString(R.string.notif_text_body) + why
        val n = NotificationCompat.Builder(context, CHANNEL_THREATS)
            .setSmallIcon(R.drawable.ic_alert)
            .setColor(0xFFDC2626.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setSubText("PHISHWARE")
            .setStyle(NotificationCompat.BigTextStyle().bigText(text).setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 400, 150, 400, 150, 600))
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(tag.hashCode(), n)
    }
}
