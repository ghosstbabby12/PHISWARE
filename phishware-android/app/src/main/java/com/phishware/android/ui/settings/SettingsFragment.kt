package com.phishware.android.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.phishware.android.R
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.data.local.SessionManager
import com.phishware.android.databinding.FragmentSettingsBinding
import com.phishware.android.notifications.NotificationHelper
import com.phishware.android.service.LinkGuardService
import com.phishware.android.ui.auth.LoginActivity
import com.phishware.android.worker.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Consentimiento Ley 1581 + toggles + idioma + cierre de sesión. */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var guard: GuardPreferences
    @Inject lateinit var session: SessionManager
    @Inject lateinit var notifications: NotificationHelper

    private val reqPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { res ->
        val denied = res.filterValues { !it }.keys
        Snackbar.make(binding.root,
            if (denied.isEmpty()) getString(R.string.guard_perms_ok)
            else getString(R.string.guard_perms_missing_fmt, denied.joinToString(", ")),
            Snackbar.LENGTH_LONG).show()
        refresh()
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            binding.swConsent.isChecked = guard.consent.first()
            binding.swGuard.isChecked = guard.guardEnabled.first()
            binding.swSms.isChecked = guard.smsEnabled.first()
            binding.swNotif.isChecked = guard.notificationsEnabled.first()
            binding.swNotifListener.isChecked = guard.notificationsListenerEnabled.first()
            refresh()
        }
        binding.swConsent.setOnCheckedChangeListener { _, v ->
            lifecycleScope.launch { guard.setConsent(v); refresh() } }
        binding.swGuard.setOnCheckedChangeListener { _, v ->
            lifecycleScope.launch { guard.setGuard(v); refresh() } }
        binding.swSms.setOnCheckedChangeListener { _, v ->
            lifecycleScope.launch { guard.setSms(v); refresh() } }
        binding.swNotif.setOnCheckedChangeListener { _, v ->
            lifecycleScope.launch { guard.setNotifications(v); refresh() } }
        binding.swNotifListener.setOnCheckedChangeListener { _, v ->
            lifecycleScope.launch { guard.setNotifListener(v); refresh() } }
        binding.btnPermissions.setOnClickListener { requestAll() }
        binding.btnBattery.setOnClickListener {
            try {
                val pm = requireContext().getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                if (!pm.isIgnoringBatteryOptimizations(requireContext().packageName)) {
                    startActivity(android.content.Intent(
                        android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        android.net.Uri.parse("package:${requireContext().packageName}")
                    ))
                } else {
                    Snackbar.make(binding.root, getString(R.string.guard_battery_ok), Snackbar.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Snackbar.make(binding.root, getString(R.string.guard_battery_how), Snackbar.LENGTH_LONG).show()
            }
        }
        binding.btnNotifAccess.setOnClickListener {
            try {
                startActivity(android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            } catch (_: Exception) {
                Snackbar.make(binding.root, getString(R.string.guard_notif_how), Snackbar.LENGTH_LONG).show()
            }
        }
        binding.btnTestNotif.setOnClickListener {
            notifications.showThreat(
                url = "https://nequi-verificacion.tk/login",
                domain = "nequi-verificacion.tk",
                riskLevel = "DANGEROUS",
                score = 85.0,
                firstTip = getString(R.string.guard_test_tip)
            )
        }
        binding.btnLangEs.setOnClickListener { applyLocale("es") }
        binding.btnLangEn.setOnClickListener { applyLocale("en") }
        binding.btnLogout.setOnClickListener { confirmLogout() }
        binding.btnStart.setOnClickListener {
            lifecycleScope.launch {
                if (!guard.consent.first()) {
                    Snackbar.make(binding.root, getString(R.string.guard_need_consent), Snackbar.LENGTH_LONG).show()
                    return@launch
                }
                LinkGuardService.start(requireContext())
                LinkGuardService.scheduleSync(requireContext())
                Snackbar.make(binding.root, getString(R.string.guard_activated), Snackbar.LENGTH_LONG).show()
                refresh()
            }
        }
    }

    private fun applyLocale(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.guard_logout_title))
            .setMessage(getString(R.string.guard_logout_msg))
            .setNegativeButton(getString(R.string.common_close), null)
            .setPositiveButton(getString(R.string.guard_logout_yes)) { _, _ -> doLogout() }
            .show()
    }

    private fun doLogout() {
        lifecycleScope.launch {
            try {
                runCatching { LinkGuardService.stop(requireContext()) }
                runCatching { WorkManager.getInstance(requireContext()).cancelUniqueWork(SyncWorker.TAG) }
                session.clearSession()
            } catch (_: Exception) { }
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }
    }

    private fun requestAll() {
        val list = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= 33) list += Manifest.permission.POST_NOTIFICATIONS
        // Solo pedir SMS si el usuario activo el toggle (permiso restringido Play)
        lifecycleScope.launch {
            if (guard.smsEnabled.first()) {
                list += Manifest.permission.RECEIVE_SMS
                list += Manifest.permission.READ_SMS
            }
            val pending = list.filter {
                ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
            if (pending.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.guard_perms_have), Snackbar.LENGTH_SHORT).show()
            } else reqPerms.launch(pending)
        }
    }

    private fun refresh() {
        lifecycleScope.launch {
            val ok = try { guard.isActive() } catch (_: Exception) { false }
            val sms = try {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS) ==
                    PackageManager.PERMISSION_GRANTED
            } catch (_: Exception) { false }
            binding.tvStatus.text = getString(
                R.string.guard_status_fmt,
                if (ok) getString(R.string.guard_on) else getString(R.string.guard_off),
                if (sms) getString(R.string.guard_sms_yes) else getString(R.string.guard_sms_no)
            )
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
