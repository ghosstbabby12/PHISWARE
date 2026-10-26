package com.phishware.android.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/** Acciones de la notificacion: Ignorar descarta; Ver/Reportar abren la app (PendingIntents). */
class ActionReceiver : BroadcastReceiver() {
    companion object { const val ACTION_IGNORE = "com.phishware.android.ACTION_IGNORE" }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_IGNORE) {
            val id = intent.getIntExtra("notif_id", 0)
            try {
                context.getSystemService(NotificationManager::class.java).cancel(id)
            } catch (_: Exception) { }
        }
    }
}
