package com.phishware.android.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.phishware.android.R
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.data.local.SessionManager
import com.phishware.android.databinding.ActivityMainBinding
import com.phishware.android.service.LinkGuardService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    @Inject lateinit var guard: GuardPreferences
    @Inject lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        handleIntent(intent)
        // Aviso Ley 1581 antes de proteger correo/WhatsApp
        lifecycleScope.launch {
            try {
                if (!guard.consent.first()) {
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle(getString(com.phishware.android.R.string.consent_title))
                        .setMessage(getString(com.phishware.android.R.string.consent_msg))
                        .setPositiveButton(getString(com.phishware.android.R.string.consent_ok), null)
                        .show()
                }
            } catch (_: Exception) { }
        }
        // Arranca proteccion si ya habia consentimiento + sesion
        lifecycleScope.launch {
            try {
                if (guard.isActive() && session.isLoggedIn()) {
                    LinkGuardService.start(this@MainActivity)
                    LinkGuardService.scheduleSync(this@MainActivity)
                }
            } catch (_: Exception) { }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /** Deep-links desde notificaciones y ShareReceiver (SMS/QR/correo). */
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val navController = runCatching {
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)?.navController
        }.getOrNull() ?: return
        val sharedUrl = intent.getStringExtra("shared_url")
            ?: intent.getStringExtra("extra_url")
        val dest = intent.getStringExtra("dest")
        if (!sharedUrl.isNullOrBlank()) {
            val b = Bundle().apply { putString("prefill_url", sharedUrl) }
            runCatching { navController.navigate(R.id.url_analysis_fragment, b) }
            intent.removeExtra("shared_url")
            intent.removeExtra("extra_url")
            return
        }
        when (dest) {
            "reports" -> runCatching { navController.navigate(R.id.history_fragment) }
            "analysis" -> runCatching { navController.navigate(R.id.url_analysis_fragment) }
        }
        if (dest != null) intent.removeExtra("dest")
    }

    fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) ==
            PackageManager.PERMISSION_GRANTED

    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= 33)
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        else true
}
