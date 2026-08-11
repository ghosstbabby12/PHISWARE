package com.phishware.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "phishware_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_TOKEN    = stringPreferencesKey("access_token")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_EMAIL    = stringPreferencesKey("email")
        private val KEY_USER_ID  = stringPreferencesKey("user_id")
    }

    suspend fun saveSession(token: String, userId: Long, username: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]    = token
            prefs[KEY_USER_ID]  = userId.toString()
            prefs[KEY_USERNAME] = username
            prefs[KEY_EMAIL]    = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    fun getToken(): String? = runBlocking {
        context.dataStore.data.first()[KEY_TOKEN]
    }

    fun isLoggedIn(): Boolean = getToken() != null

    val username: Flow<String?> = context.dataStore.data.map { it[KEY_USERNAME] }
    val email: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
}
