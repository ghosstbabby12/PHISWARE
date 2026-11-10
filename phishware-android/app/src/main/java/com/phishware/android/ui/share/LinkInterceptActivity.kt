package com.phishware.android.ui.share

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.phishware.android.R
import com.phishware.android.data.local.BackgroundDetection
import com.phishware.android.data.local.BackgroundLog
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.databinding.ActivityLinkInterceptBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Intercepta el toque en un enlace (cuando PHISHWARE es el navegador predeterminado):
 * revisa la URL ANTES de abrirla y solo la entrega al navegador real si es segura.
 * Si es sospechosa o peligrosa, detiene el paso y muestra el motivo.
 */
@AndroidEntryPoint
class LinkInterceptActivity : AppCompatActivity() {

    @Inject lateinit var repository: PhishwareRepository
    @Inject lateinit var session: SessionManager
    @Inject lateinit var log: BackgroundLog

    private lateinit var binding: ActivityLinkInterceptBinding
    private var url: String = ""
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkInterceptBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        url = intent?.dataString.orEmpty()
        if (!url.startsWith("http://", true) && !url.startsWith("https://", true)) {
            finish(); return
        }
        binding.tvDomain.text = url.substringAfter("://").substringBefore("/")
        binding.btnOpenAnyway.setOnClickListener { openInRealBrowser() }
        binding.btnClose.setOnClickListener { finish() }
        check()
    }

    private fun check() {
        job = lifecycleScope.launch {
            // Fase 1: heurístico rápido
            var quickLevel = "SAFE"
            var quickTip: String? = null
            var quickScore = 0.0
            var official = false
            withTimeoutOrNull(4_000) { repository.quickCheck(url).getOrNull() }?.let { q ->
                quickLevel = q.classification ?: "SAFE"
                quickTip = q.recommendations?.firstOrNull()
                quickScore = q.riskScore
                official = q.officialSeal?.official == true
            }
            if (quickLevel == "DANGEROUS") return@launch showVerdict("DANGEROUS", quickScore, quickTip)
            if (quickLevel == "SAFE" && official) return@launch openInRealBrowser()

            // Fase 2: análisis completo (si hay sesión)
            if (session.isLoggedIn()) {
                binding.tvMessage.setText(R.string.lnk_checking_deep)
                val r = withTimeoutOrNull(18_000) { repository.analyzeUrl(url).getOrNull() }
                if (r != null) {
                    val level = r.riskLevel ?: "SAFE"
                    log.add(BackgroundDetection(r.originalUrl ?: url, r.domain ?: binding.tvDomain.text.toString(),
                        getString(R.string.lnk_source), level, r.riskScore,
                        r.recommendations?.firstOrNull(), System.currentTimeMillis()))
                    if (level == "SAFE") openInRealBrowser()
                    else showVerdict(level, r.riskScore, r.recommendations?.firstOrNull())
                    return@launch
                }
            }
            // Sin análisis completo: decide con lo que se tiene
            if (quickLevel == "SUSPICIOUS") showVerdict("SUSPICIOUS", quickScore, quickTip)
            else openInRealBrowser()
        }
    }

    private fun showVerdict(level: String, score: Double, tip: String?) {
        binding.progress.visibility = View.GONE
        binding.ivIcon.visibility = View.VISIBLE
        val danger = level == "DANGEROUS"
        binding.ivIcon.setImageResource(if (danger) R.drawable.ic_stat_danger else R.drawable.ic_stat_warning)
        binding.tvTitle.setText(if (danger) R.string.lnk_danger_title else R.string.lnk_susp_title)
        binding.tvTitle.setTextColor(ContextCompat.getColor(this,
            if (danger) R.color.danger_red_c else R.color.warning_amber_c))
        binding.tvMessage.text = buildString {
            append(getString(R.string.lnk_score, score.toInt()))
            if (!tip.isNullOrBlank()) append("\n\n").append(tip)
        }
        binding.btnClose.visibility = View.VISIBLE
        binding.btnOpenAnyway.setText(R.string.lnk_open_anyway)
    }

    /** Entrega el enlace a un navegador real (nunca a nosotros mismos, para no entrar en bucle). */
    private fun openInRealBrowser() {
        job?.cancel()
        val uri = Uri.parse(url)
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val browsers = packageManager.queryIntentActivities(probe, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .filter { it != packageName }
            .distinct()
        val preferred = listOf("com.android.chrome", "com.sec.android.app.sbrowser", "org.mozilla.firefox",
            "com.microsoft.emmx", "com.brave.browser", "com.opera.browser")
            .firstOrNull { it in browsers } ?: browsers.firstOrNull()
        val open = Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
        if (preferred != null) open.setPackage(preferred)
        runCatching { startActivity(open) }
        finish()
    }
}
