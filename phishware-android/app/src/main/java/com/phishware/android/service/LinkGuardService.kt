package com.phishware.android.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.notifications.NotificationHelper
import com.phishware.android.worker.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Servicio en primer plano: mantiene la proteccion viva y actualiza la notificacion persistente. */
@AndroidEntryPoint
class LinkGuardService : Service() {

    @Inject lateinit var notifications: NotificationHelper
    @Inject lateinit var guard: GuardPreferences
    @Inject lateinit var session: SessionManager
    @Inject lateinit var repository: PhishwareRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        fun start(ctx: Context) {
            val i = Intent(ctx, LinkGuardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
            else ctx.startService(i)
        }
        fun stop(ctx: Context) = ctx.stopService(Intent(ctx, LinkGuardService::class.java))

        fun scheduleSync(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(SyncWorker.TAG)
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                SyncWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, req
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notifications.ensureChannels()
        startForeground(NotificationHelper.NOTIF_GUARD_ID, notifications.persistentGuardNotification(0, 0))
        scheduleSync(this)
        refreshBadge()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refreshBadge()
        return START_STICKY
    }

    private fun refreshBadge() {
        scope.launch {
            if (!guard.isActive() || !session.isLoggedIn()) {
                startForeground(NotificationHelper.NOTIF_GUARD_ID,
                    notifications.persistentGuardNotification(0, 0))
                return@launch
            }
            var unread = 0L
            var threats = 0L
            repository.getUnreadAlertCount().onSuccess { unread = it }
            repository.getDashboard().onSuccess { threats = it.threatsDetected }
            try {
                val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                nm.notify(NotificationHelper.NOTIF_GUARD_ID,
                    notifications.persistentGuardNotification(threats, unread))
            } catch (_: Exception) { }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
