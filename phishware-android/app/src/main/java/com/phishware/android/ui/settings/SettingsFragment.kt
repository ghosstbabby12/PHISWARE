package com.phishware.android.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.databinding.FragmentSettingsBinding
import com.phishware.android.service.LinkGuardService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Consentimiento Ley 1581 + toggles SMS/fondo/notificaciones + arranque del servicio. */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var guard: GuardPreferences

    private val reqPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { res ->
        val denied = res.filterValues { !it }.keys
        Snackbar.make(binding.root,
            if (denied.isEmpty()) "Permisos listos" else "Falta: $denied (la app sigue manual)",
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
        binding.btnPermissions.setOnClickListener { requestAll() }
        binding.btnStart.setOnClickListener {
            lifecycleScope.launch {
                if (!guard.consent.first()) {
                    Snackbar.make(binding.root, "Primero acepta el consentimiento", Snackbar.LENGTH_LONG).show()
                    return@launch
                }
                LinkGuardService.start(requireContext())
                LinkGuardService.scheduleSync(requireContext())
                Snackbar.make(binding.root, "Protección activada en segundo plano", Snackbar.LENGTH_LONG).show()
                refresh()
            }
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
                Snackbar.make(binding.root, "Ya tienes los permisos", Snackbar.LENGTH_SHORT).show()
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
            binding.tvStatus.text = "Estado: ${if (ok) "ACTIVA" else "INACTIVA (falta consentimiento o está apagada)"}" +
                "\nPermiso SMS: ${if (sms) "sí" else "no (manual/compartir sigue disponible)"}"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
