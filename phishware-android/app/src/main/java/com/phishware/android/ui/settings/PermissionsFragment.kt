package com.phishware.android.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.phishware.android.R
import com.phishware.android.data.local.GuardPreferences
import com.phishware.android.databinding.FragmentPermissionsBinding
import com.phishware.android.service.LinkGuardService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lista de verificación de todo lo que la protección en segundo plano necesita,
 * con el estado real de cada permiso y un botón para activarlo.
 */
@AndroidEntryPoint
class PermissionsFragment : Fragment() {

    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var guard: GuardPreferences

    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        refresh()
    }

    private class Item(
        val title: Int,
        val desc: Int,
        val granted: Boolean,
        val action: () -> Unit
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPermissionsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val ctx = requireContext()
        viewLifecycleOwner.lifecycleScope.launch {
            val consent = guard.consent.first()
            if (_binding == null) return@launch
            val items = listOf(
                Item(R.string.perm_consent, R.string.perm_consent_desc, consent) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        guard.setConsent(true)
                        LinkGuardService.start(ctx)
                        LinkGuardService.scheduleSync(ctx)
                        refresh()
                    }
                },
                Item(R.string.perm_listener, R.string.perm_listener_desc, listenerGranted(ctx)) {
                    runCatching { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                },
                Item(R.string.perm_a11y, R.string.perm_a11y_desc, a11yGranted(ctx)) {
                    runCatching { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                },
                Item(R.string.perm_browser, R.string.perm_browser_desc, isDefaultBrowser(ctx)) {
                    requestBrowserRole(ctx)
                },
                Item(R.string.perm_post, R.string.perm_post_desc, postGranted(ctx)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                Item(R.string.perm_sms, R.string.perm_sms_desc, has(ctx, Manifest.permission.RECEIVE_SMS)) {
                    permLauncher.launch(Manifest.permission.RECEIVE_SMS)
                },
                Item(R.string.perm_camera, R.string.perm_camera_desc, has(ctx, Manifest.permission.CAMERA)) {
                    permLauncher.launch(Manifest.permission.CAMERA)
                },
                Item(R.string.perm_battery, R.string.perm_battery_desc, batteryOk(ctx)) {
                    runCatching {
                        startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${ctx.packageName}")))
                    }.onFailure { runCatching { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } }
                }
            )
            render(items)
        }
    }

    private fun render(items: List<Item>) {
        val container = binding.permContainer
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        items.forEach { it0 ->
            val row = inflater.inflate(R.layout.item_permission_row, container, false)
            row.findViewById<TextView>(R.id.tvPermTitle).setText(it0.title)
            row.findViewById<TextView>(R.id.tvPermDesc).setText(it0.desc)
            val status = row.findViewById<TextView>(R.id.tvPermStatus)
            val btn = row.findViewById<MaterialButton>(R.id.btnPermAction)
            if (it0.granted) {
                status.text = getString(R.string.perm_ok)
                status.setTextColor(ContextCompat.getColor(requireContext(), R.color.safe_green))
                btn.visibility = View.GONE
            } else {
                status.text = getString(R.string.perm_missing)
                status.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger_red_c))
                btn.visibility = View.VISIBLE
                btn.setOnClickListener { it0.action() }
            }
            container.addView(row)
        }
        val missing = items.count { !it.granted }
        binding.tvPermSummary.text =
            if (missing == 0) getString(R.string.perm_all_ok) else getString(R.string.perm_missing_fmt, missing)
    }

    private fun has(ctx: Context, p: String) =
        ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED

    private fun postGranted(ctx: Context) =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || has(ctx, Manifest.permission.POST_NOTIFICATIONS)

    private fun listenerGranted(ctx: Context) =
        NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)

    private fun a11yGranted(ctx: Context): Boolean {
        val enabled = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        return enabled.split(':').any { it.startsWith("${ctx.packageName}/") }
    }

    private fun isDefaultBrowser(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = ctx.getSystemService(android.app.role.RoleManager::class.java)
            if (rm != null && rm.isRoleHeld(android.app.role.RoleManager.ROLE_BROWSER)) return true
        }
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
        val def = ctx.packageManager.resolveActivity(probe, PackageManager.MATCH_DEFAULT_ONLY)
        return def?.activityInfo?.packageName == ctx.packageName
    }

    private fun requestBrowserRole(ctx: Context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val rm = ctx.getSystemService(android.app.role.RoleManager::class.java)
                if (rm != null && rm.isRoleAvailable(android.app.role.RoleManager.ROLE_BROWSER)) {
                    startActivity(rm.createRequestRoleIntent(android.app.role.RoleManager.ROLE_BROWSER))
                    return
                }
            }
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }
    }

    private fun batteryOk(ctx: Context) =
        (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(ctx.packageName)

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
