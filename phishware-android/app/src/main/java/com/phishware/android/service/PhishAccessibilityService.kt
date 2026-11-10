package com.phishware.android.service

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.phishware.android.data.local.BackgroundLog
import com.phishware.android.security.LinkScanner
import com.phishware.android.security.UrlExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Detecta enlaces que aparecen EN PANTALLA mientras el usuario tiene abierto el chat
 * (WhatsApp, Instagram, Telegram, Gmail, SMS...). Las notificaciones no cubren ese caso
 * porque la app de mensajería no las publica cuando su chat está abierto.
 *
 * Privacidad (Ley 1581): solo se extraen URLs del texto visible; el resto del contenido
 * se descarta en el momento y jamás se almacena ni se envía. Servicio limitado por
 * `packageNames` a apps de mensajería/correo.
 */
@AndroidEntryPoint
class PhishAccessibilityService : AccessibilityService() {

    @Inject lateinit var scanner: LinkScanner

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastScanAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg == packageName) return
        val now = SystemClock.elapsedRealtime()
        // El contenido de un chat cambia sin parar al hacer scroll: máx. 1 lectura cada 700 ms
        if (now - lastScanAt < 700) return
        lastScanAt = now

        val root = rootInActiveWindow ?: return
        val text = StringBuilder()
        val links = mutableListOf<Pair<String, String>>()   // (texto visible, destino real)
        try {
            collect(root, text, links, 0, intArrayOf(0))
        } finally {
            @Suppress("DEPRECATION") root.recycle()
        }
        if (text.isEmpty() && links.isEmpty()) return

        val source = BackgroundLog.sourceLabel(pkg)
        val raw = text.toString()
        scope.launch {
            try {
                if (!scanner.enabled()) return@launch
                val urls = LinkedHashSet(UrlExtractor.extractUrls(raw))
                // Destino real de enlaces ocultos tras un texto (típico de correo): "Haz clic aquí" -> https://...
                for ((shown, href) in links) {
                    val real = UrlExtractor.normalize(href)
                    if (!UrlExtractor.isScannable(real)) continue
                    urls += real
                    val shownUrl = UrlExtractor.extractUrls(shown).firstOrNull() ?: continue
                    val shownReg = UrlExtractor.registrable(UrlExtractor.hostOf(shownUrl))
                    val realReg = UrlExtractor.registrable(UrlExtractor.hostOf(real))
                    if (shownReg.isNotEmpty() && realReg.isNotEmpty() && shownReg != realReg) {
                        scanner.noteDeceptive(real, UrlExtractor.hostOf(shownUrl))
                        launch { scanner.scanDeceptive(UrlExtractor.hostOf(shownUrl), real, source) }
                    }
                }
                if (urls.isEmpty()) return@launch
                android.util.Log.d("PhishA11y", "$pkg -> ${urls.size} URL(s) en pantalla, ${links.size} enlaces con destino")
                urls.take(8).map { u -> launch { scanner.scan(u, source) } }
            } catch (e: Exception) {
                android.util.Log.w("PhishA11y", "fallo leyendo pantalla de $pkg", e)
            }
        }
    }

    /** Recorre el árbol de vistas acumulando texto visible (con tope de nodos y profundidad). */
    private fun collect(
        node: AccessibilityNodeInfo?, out: StringBuilder, links: MutableList<Pair<String, String>>,
        depth: Int, count: IntArray
    ) {
        if (node == null || depth > 40 || count[0] > 600) return
        count[0]++
        if (node.isVisibleToUser) {
            val t = node.text
            t?.let { out.append(it).append('\n') }
            node.contentDescription?.let { out.append(it).append('\n') }
            val shown = (t ?: node.contentDescription)?.toString().orEmpty()
            // 1) WebView/Chrome (Gmail, Outlook): el destino real del enlace va en un extra del nodo
            runCatching { node.extras?.getString("AccessibilityNodeInfo.targetUrl") }.getOrNull()
                ?.takeIf { it.isNotBlank() }?.let { links += shown to it }
            // 2) Texto con URLSpan (TextView con enlaces: chats, Instagram, SMS)
            if (t is android.text.Spanned) {
                for (span in t.getSpans(0, t.length, android.text.style.URLSpan::class.java)) {
                    val label = t.subSequence(t.getSpanStart(span), t.getSpanEnd(span)).toString()
                    span.url?.takeIf { it.isNotBlank() }?.let { links += label to it }
                }
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collect(child, out, links, depth + 1, count)
            @Suppress("DEPRECATION") child.recycle()
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
