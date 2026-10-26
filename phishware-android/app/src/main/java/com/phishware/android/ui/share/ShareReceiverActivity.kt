package com.phishware.android.ui.share

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.phishware.android.security.UrlExtractor
import com.phishware.android.ui.MainActivity

/**
 * Correo hibrido fase 1: recibe texto compartido desde Gmail/correo/SMS/WhatsApp/navegador,
 * extrae URLs y abre el analisis. Fase 2 (Gmail API OAuth) queda como trabajo futuro.
 */
class ShareReceiverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }
        val urls = UrlExtractor.extractUrls(text)
        val main = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("dest", "analysis")
            if (urls.isNotEmpty()) putExtra("shared_url", urls.first())
            if (!urls.isEmpty() && urls.size > 1) putExtra("shared_count", urls.size)
            if (urls.isEmpty() && !text.isNullOrBlank()) putExtra("shared_text", text)
        }
        startActivity(main)
        finish()
    }
}
