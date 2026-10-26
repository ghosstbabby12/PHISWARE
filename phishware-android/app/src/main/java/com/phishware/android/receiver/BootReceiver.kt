package com.phishware.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.data.local.SessionManager
import com.phishware.android.service.LinkGuardService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Rearranca la proteccion tras reiniciar el equipo (solo si hubo consentimiento + sesion). */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var guard: GuardPreferences
    @Inject lateinit var session: SessionManager

    override fun onReceive(context: Context, intent: Intent) {
        val a = intent.action
        if (a != Intent.ACTION_BOOT_COMPLETED && a != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (guard.isActive() && session.isLoggedIn()) {
                    LinkGuardService.start(context)
                    LinkGuardService.scheduleSync(context)
                }
            } finally { pending.finish() }
        }
    }
}
