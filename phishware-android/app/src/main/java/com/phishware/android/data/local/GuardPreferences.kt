package com.phishware.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Toggles de proteccion + consentimiento Ley 1581/1273 + cache anti-duplicados (5 min). */
@Singleton
class GuardPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_CONSENT = booleanPreferencesKey("guard_consent")
        private val KEY_GUARD = booleanPreferencesKey("guard_enabled")
        private val KEY_SMS = booleanPreferencesKey("guard_sms")
        private val KEY_NOTIF = booleanPreferencesKey("guard_notifications")
        private val KEY_LAST_PREFIX = "last_scan_"
    }

    val consent: Flow<Boolean> = context.dataStore.data.map { it[KEY_CONSENT] == true }
    val guardEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_GUARD] != false }
    val smsEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_SMS] != false }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIF] != false }

    suspend fun setConsent(v: Boolean) = context.dataStore.edit { it[KEY_CONSENT] = v }
    suspend fun setGuard(v: Boolean) = context.dataStore.edit { it[KEY_GUARD] = v }
    suspend fun setSms(v: Boolean) = context.dataStore.edit { it[KEY_SMS] = v }
    suspend fun setNotifications(v: Boolean) = context.dataStore.edit { it[KEY_NOTIF] = v }

    suspend fun isActive(): Boolean {
        val p = context.dataStore.data.first()
        return p[KEY_CONSENT] == true && p[KEY_GUARD] != false
    }

    /** Evita notificar 2 veces la misma URL en 5 minutos (igual que la extension). */
    suspend fun shouldScan(url: String, ttlMs: Long = 5 * 60 * 1000): Boolean {
        val key = stringPreferencesKey(KEY_LAST_PREFIX + url.hashCode())
        val now = System.currentTimeMillis()
        val last = context.dataStore.data.first()[key]?.toLongOrNull() ?: 0L
        if (now - last < ttlMs) return false
        context.dataStore.edit { it[key] = now.toString() }
        return true
    }
}
