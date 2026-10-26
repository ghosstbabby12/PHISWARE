package com.phishware.android.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Reintento periodico: sincroniza no-leidos y refresca la notificacion persistente. */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: PhishwareRepository,
    private val guard: GuardPreferences,
    private val session: SessionManager,
    private val notifications: NotificationHelper
) : CoroutineWorker(appContext, params) {

    companion object { const val TAG = "phishware_sync" }

    override suspend fun doWork(): Result {
        if (!guard.isActive() || !session.isLoggedIn()) return Result.success()
        var unread = 0L
        var threats = 0L
        repository.getUnreadAlertCount().onSuccess { unread = it }
        repository.getDashboard().onSuccess { threats = it.threatsDetected }
        return try {
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
            nm.notify(NotificationHelper.NOTIF_GUARD_ID,
                notifications.persistentGuardNotification(threats, unread))
            Result.success()
        } catch (_: Exception) { Result.retry() }
    }
}
