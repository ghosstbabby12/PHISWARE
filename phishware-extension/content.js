(() => {
  'use strict';

  const SKIP_HOSTS = new Set([
    'localhost', '127.0.0.1', '0.0.0.0', '::1',
  ]);
  const SKIP_SCHEMES = new Set(['javascript:', 'mailto:', 'tel:', 'data:', '#']);
  const checked = new Map(); // url → result

  // ── Overlay warning ───────────────────────────────────────────────────────

  function escapeHtml(str) {
    return String(str ?? '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  function showWarning(result, continueHref, opts = {}) {
    const existing = document.getElementById('phisware-overlay');
    if (existing) existing.remove();

    const msg = k => chrome.i18n.getMessage(k) || k;
    const overlay = document.createElement('div');
    overlay.id = 'phisware-overlay';
    const safeDomain = escapeHtml(result.domain ?? result.url);
    const safeScore = Math.round(Number(result.riskScore) || 0);
    const baseExplained = Array.isArray(result.explainedIndicators) && result.explainedIndicators.length
      ? result.explainedIndicators
      : (result.indicators || []).slice(0, 3).map(toExplained);
    const explained = opts.deceptive
      ? [{ description: chrome.i18n.getMessage('deceptiveLink', [opts.deceptive.shown, opts.deceptive.real]) ||
            `The link says "${opts.deceptive.shown}" but goes to "${opts.deceptive.real}"`,
           persuasionPrinciple: 'TRUST' }, ...baseExplained]
      : baseExplained;
    const safeIndicators = explained.slice(0, 3).map(i => {
      const label = escapeHtml(typeof i === 'string' ? i : i.description);
      const principle = typeof i === 'string' ? null : principleName(i.persuasionPrinciple);
      const suffix = principle ? ` <span class="phisware-principle">${escapeHtml(msg('principleLabel'))}: ${escapeHtml(principle)}</span>` : '';
      return `<li>${label}${suffix}</li>`;
    }).join('');
    const parsed = new DOMParser().parseFromString(`
      <div id="phisware-modal">
        <div id="phisware-header">
          <span id="phisware-icon">⚠️</span>
          <span id="phisware-title">PHISWARE Link Guard</span>
        </div>
        <div id="phisware-body">
          <p id="phisware-classification">${escapeHtml(labelFor(result.classification))}</p>
          <p id="phisware-domain"><strong>${safeDomain}</strong></p>
          <p id="phisware-score">${escapeHtml(msg('overlayScore'))} <strong>${safeScore}/100</strong></p>
          ${safeIndicators ? `<ul id="phisware-indicators">${safeIndicators}</ul>` : ''}
        </div>
        <div id="phisware-actions">
          <button id="phisware-back">${escapeHtml(msg('overlayBack'))}</button>
          <button id="phisware-continue">${escapeHtml(msg('overlayContinue'))}</button>
        </div>
      </div>
    `, 'text/html');
    overlay.append(...parsed.body.childNodes);

    const light = window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches;
    const palette = light
      ? { bg: '#ffffff', text: '#17171c', muted: '#686873', border: '#e7e4e8', modalBorder: '#C65D7B', title: '#790D2B', domain: '#1d4ed8', score: '#3a3a44', ind: '#9a3412' }
      : { bg: '#1e1e2e', text: '#cdd6f4', muted: '#6c7086', border: '#45475a', modalBorder: '#f38ba8', title: '#f38ba8', domain: '#89dceb', score: '#a6adc8', ind: '#fab387' };

    const style = document.createElement('style');
    style.textContent = `
      #phisware-overlay {
        position: fixed; inset: 0; z-index: 2147483647;
        background: rgba(0,0,0,0.75); display: flex;
        align-items: center; justify-content: center;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
      }
      #phisware-modal {
        background: ${palette.bg}; color: ${palette.text}; border-radius: 12px;
        padding: 28px 32px; max-width: 440px; width: 90%;
        box-shadow: 0 20px 60px rgba(0,0,0,0.5);
        border: 2px solid ${palette.modalBorder};
      }
      #phisware-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
      #phisware-icon { font-size: 28px; }
      #phisware-title { font-size: 18px; font-weight: 700; color: ${palette.title}; }
      #phisware-classification { font-size: 22px; font-weight: 700; color: ${palette.title}; margin: 0 0 6px; }
      #phisware-domain { font-size: 16px; color: ${palette.domain}; margin: 0 0 8px; word-break: break-all; }
      #phisware-score { font-size: 14px; color: ${palette.score}; margin: 0 0 12px; }
      #phisware-indicators { padding-left: 18px; margin: 0 0 16px; font-size: 13px; color: ${palette.ind}; }
      #phisware-indicators li { margin-bottom: 4px; }
      .phisware-principle { opacity: 0.85; font-size: 12px; }
      #phisware-actions { display: flex; gap: 10px; margin-top: 8px; }
      #phisware-back {
        flex: 1; padding: 10px; background: #a6e3a1; color: #1e1e2e;
        border: none; border-radius: 8px; font-size: 14px; font-weight: 700;
        cursor: pointer;
      }
      #phisware-continue {
        flex: 1; padding: 10px; background: transparent; color: ${palette.muted};
        border: 1px solid ${palette.border}; border-radius: 8px; font-size: 13px;
        cursor: pointer;
      }
      #phisware-back:hover { opacity: 0.85; }
    `;

    document.head.appendChild(style);
    document.body.appendChild(overlay);

    overlay.querySelector('#phisware-back').addEventListener('click', () => overlay.remove());
    overlay.querySelector('#phisware-continue').addEventListener('click', () => {
      overlay.remove();
      openLink(continueHref, opts);
    });
  }

  function labelFor(classification) {
    const msg = k => chrome.i18n.getMessage(k) || k;
    return {
      DANGEROUS:  msg('overlayDanger'),
      SUSPICIOUS: msg('overlaySuspicious'),
      SAFE:       msg('overlaySafe'),
    }[classification] ?? classification;
  }

  function principleName(principle) {
    if (!principle || principle === 'NONE') return null;
    return chrome.i18n.getMessage('principle_' + principle) || principle;
  }

  function toExplained(text) {
    return { description: String(text), persuasionPrinciple: 'NONE' };
  }

  // ── URL normalization ─────────────────────────────────────────────────────

  function normalizeHref(href) {
    try {
      const url = new URL(href, window.location.href);
      if (SKIP_SCHEMES.has(url.protocol)) return null;
      if (SKIP_HOSTS.has(url.hostname)) return null;
      return url.href;
    } catch {
      return null;
    }
  }

  // ── Check a URL (with caching) ────────────────────────────────────────────

  function checkUrl(url) {
    return new Promise(resolve => {
      const cached = checked.get(url);
      if (cached !== undefined) { resolve(cached); return; }

      chrome.runtime.sendMessage({ type: 'CHECK_URL', url }, response => {
        const result = response?.result ?? null;
        checked.set(url, result);
        resolve(result);
      });
    });
  }

  // ── Navegación que respeta pestañas nuevas / teclas modificadoras ─────────

  function openLink(href, opts = {}) {
    if (opts.newTab) window.open(href, '_blank', 'noopener');
    else window.location.href = href;
  }

  // ── Link click interception ───────────────────────────────────────────────

  const Shared = globalThis.PhishwareShared;

  /** Resultado combinado: nunca "más seguro" de lo que dice el backend, y el engaño de texto sube a SUSPICIOUS. */
  function withDeceptive(result, deceptive) {
    if (!deceptive) return result;
    const base = result ?? { classification: 'SAFE', riskScore: 0, indicators: [], domain: deceptive.real };
    if (base.classification === 'DANGEROUS') return base;
    return { ...base, classification: 'SUSPICIOUS', riskScore: Math.max(Number(base.riskScore) || 0, 55), isThreat: true };
  }

  async function handleLinkClick(e) {
    const anchor = e.target.closest && e.target.closest('a[href]');
    if (!anchor) return;
    if (e.type === 'auxclick' && e.button !== 1) return; // solo clic central

    const abs = normalizeHref(anchor.getAttribute('href'));
    if (!abs) return;
    const target = Shared.unwrapRedirect(abs);          // destino REAL (Gmail/WhatsApp/Instagram envuelven los enlaces)
    const deceptive = Shared.isDeceptive(anchor.textContent, target);
    const opts = {
      newTab: anchor.target === '_blank' || e.ctrlKey || e.metaKey || e.shiftKey || e.button === 1,
      deceptive,
    };

    const cached = checked.get(target);
    if (cached !== undefined && cached !== null) {
      const merged = withDeceptive(cached, deceptive);
      if (merged.classification === 'DANGEROUS' || merged.classification === 'SUSPICIOUS') {
        e.preventDefault();
        e.stopImmediatePropagation();
        showWarning(merged, abs, opts);
      }
      return;
    }

    // Enlace desconocido: se frena un instante, se revisa y se decide
    e.preventDefault();
    e.stopImmediatePropagation();

    const result = await checkUrl(target);
    const merged = withDeceptive(result, deceptive);
    if (!merged) { openLink(abs, opts); return; } // sin backend y sin heurística: no bloquear

    if (merged.classification === 'DANGEROUS' || merged.classification === 'SUSPICIOUS') {
      showWarning(merged, abs, opts);
      if (merged.classification === 'DANGEROUS') {
        chrome.runtime.sendMessage({ type: 'SAVE_ALERT', alert: merged });
      }
    } else {
      openLink(abs, opts);
    }
  }

  // ── Pre-scan visible links (marcar enlaces peligrosos antes del clic) ─────

  function tintLink(anchor, result, deceptive) {
    if (result && result.classification === 'DANGEROUS') {
      anchor.style.outline = '2px solid #ef4444';
      anchor.title = result.domain ? `PHISWARE: ${result.domain}` : 'PHISWARE';
    } else if (result && result.classification === 'SUSPICIOUS') {
      anchor.style.outline = '2px solid #f59e0b';
    }
    if (deceptive) {
      anchor.style.outline = '2px dashed #b3234a';
      anchor.title = chrome.i18n.getMessage('deceptiveLink', [deceptive.shown, deceptive.real]) || anchor.title;
    }
  }

  function scanLinks(root) {
    const anchors = root.querySelectorAll ? root.querySelectorAll('a[href]') : [];
    let count = 0;
    for (const anchor of anchors) {
      const abs = normalizeHref(anchor.getAttribute('href'));
      if (!abs) continue;
      const target = Shared.unwrapRedirect(abs);
      const deceptive = Shared.isDeceptive(anchor.textContent, target);
      if (deceptive) tintLink(anchor, null, deceptive); // el engaño de texto se marca al instante, sin red

      const cached = checked.get(target);
      if (cached !== undefined) { tintLink(anchor, cached, deceptive); continue; }
      if (count >= 20) continue; // límite de peticiones simultáneas
      count++;
      checkUrl(target).then(result => tintLink(anchor, result, deceptive));
    }
  }

  // ── MutationObserver for SPAs ─────────────────────────────────────────────

  const observer = new MutationObserver(mutations => {
    for (const m of mutations) {
      for (const node of m.addedNodes) {
        if (node.nodeType !== 1) continue;
        scanLinks(node);
        if (node.matches && node.matches('a[href]')) scanLinks({ querySelectorAll: () => [node] });
      }
    }
  });

  if (document.body) observer.observe(document.body, { childList: true, subtree: true });

  // ── Bootstrap ─────────────────────────────────────────────────────────────

  document.addEventListener('click', handleLinkClick, true);
  document.addEventListener('auxclick', handleLinkClick, true);
  scanLinks(document);
})();
