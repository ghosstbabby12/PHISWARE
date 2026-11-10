'use strict';

/**
 * Lógica pura compartida (sin DOM ni APIs de extensión): se carga como script clásico
 * antes de content.js y también se importa en las pruebas de Node.
 *
 *  - unwrapRedirect: saca el destino REAL de enlaces envueltos por redirectores
 *    (Gmail/Google, WhatsApp, Instagram, Facebook, Outlook SafeLinks...). Sin esto la
 *    extensión revisaría "google.com/url?q=..." (siempre seguro) y no el sitio de verdad.
 *  - textDomain / isDeceptive: detecta el engaño clásico "el texto dice paypal.com pero
 *    el enlace lleva a otro dominio".
 */
(function (root) {
  const REDIRECTORS = [
    { host: /(^|\.)google\.[a-z.]+$/, path: /^\/(url|amp\/s)/, params: ['q', 'url'] },
    { host: /(^|\.)facebook\.com$/, path: /^\/(l|l\.php|flx\/warn)/, params: ['u'] },
    { host: /^l\.instagram\.com$/, path: /./, params: ['u'] },
    { host: /^l\.messenger\.com$/, path: /./, params: ['u'] },
    { host: /^l\.wl\.co$/, path: /./, params: ['u'] },
    { host: /\.safelinks\.protection\.outlook\.com$/, path: /./, params: ['url'] },
    { host: /(^|\.)youtube\.com$/, path: /^\/redirect/, params: ['q'] },
    { host: /(^|\.)linkedin\.com$/, path: /^\/safety\/go/, params: ['url'] },
    { host: /^out\.reddit\.com$/, path: /./, params: ['url'] },
    { host: /^(www\.)?(slack-redir\.net|duckduckgo\.com)$/, path: /^\/(link|l\/)/, params: ['url', 'uddg'] },
    { host: /^urldefense\.com$/, path: /^\/v3\/__/, params: [] },
  ];

  const SECOND_LEVEL = new Set(['co', 'com', 'org', 'net', 'gov', 'edu', 'ac']);

  function hostOf(url) {
    try { return new URL(url).hostname.toLowerCase().replace(/^www\./, ''); } catch { return ''; }
  }

  /** Dominio "registrable" aproximado: ejemplo.com, ejemplo.com.co, ejemplo.co.uk */
  function registrable(host) {
    const parts = String(host || '').toLowerCase().split('.').filter(Boolean);
    if (parts.length <= 2) return parts.join('.');
    const last = parts[parts.length - 1];
    const take = last.length === 2 && SECOND_LEVEL.has(parts[parts.length - 2]) ? 3 : 2;
    return parts.slice(-take).join('.');
  }

  /** Devuelve el destino final si el enlace pasa por un redirector conocido; si no, el mismo enlace. */
  function unwrapRedirect(href, maxDepth = 3) {
    let current = href;
    for (let i = 0; i < maxDepth; i++) {
      let u;
      try { u = new URL(current); } catch { return current; }
      const rule = REDIRECTORS.find(r => r.host.test(u.hostname.toLowerCase()) && r.path.test(u.pathname));
      if (!rule) return current;
      let next = null;
      for (const p of rule.params) {
        const v = u.searchParams.get(p);
        if (v && /^https?:\/\//i.test(v)) { next = v; break; }
      }
      if (!next) return current;
      current = next;
    }
    return current;
  }

  /**
   * Si el texto visible de un enlace ES una URL/dominio ("www.paypal.com", "https://bbva.es/login"),
   * devuelve ese host. Solo si el texto completo parece una URL: así las frases normales no
   * generan falsos positivos.
   */
  function textDomain(text) {
    const t = String(text || '').trim().replace(/\s+/g, '');
    if (!t || t.length > 120) return null;
    const m = t.match(/^(?:https?:\/\/)?((?:[a-z0-9-]+\.)+[a-z]{2,})(?:[/:?#]|$)/i);
    return m ? m[1].toLowerCase().replace(/^www\./, '') : null;
  }

  /** {shown, real} si el texto muestra un dominio distinto al del destino real; null si coincide o no aplica. */
  function isDeceptive(anchorText, realUrl) {
    const shown = textDomain(anchorText);
    if (!shown) return null;
    const real = hostOf(realUrl);
    if (!real) return null;
    return registrable(shown) !== registrable(real) ? { shown, real } : null;
  }

  const api = { unwrapRedirect, textDomain, isDeceptive, registrable, hostOf };
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
  root.PhishwareShared = api;
})(typeof globalThis !== 'undefined' ? globalThis : this);
