(() => {
  'use strict';

  const SKIP_HOSTS = new Set([
    'localhost', '127.0.0.1', '0.0.0.0', '::1',
  ]);
  const SKIP_SCHEMES = new Set(['javascript:', 'mailto:', 'tel:', 'data:', '#']);
  const checked = new Map(); // url → result

  // ── Overlay warning ───────────────────────────────────────────────────────

  function showWarning(result, continueHref) {
    const existing = document.getElementById('phisware-overlay');
    if (existing) existing.remove();

    const overlay = document.createElement('div');
    overlay.id = 'phisware-overlay';
    overlay.innerHTML = `
      <div id="phisware-modal">
        <div id="phisware-header">
          <span id="phisware-icon">⚠️</span>
          <span id="phisware-title">PHISWARE Link Guard</span>
        </div>
        <div id="phisware-body">
          <p id="phisware-classification">${labelFor(result.classification)}</p>
          <p id="phisware-domain"><strong>${result.domain ?? result.url}</strong></p>
          <p id="phisware-score">Puntuación de riesgo: <strong>${Math.round(result.riskScore)}/100</strong></p>
          ${result.indicators?.length ? `<ul id="phisware-indicators">${result.indicators.slice(0, 3).map(i => `<li>${i}</li>`).join('')}</ul>` : ''}
        </div>
        <div id="phisware-actions">
          <button id="phisware-back">← Volver</button>
          <button id="phisware-continue">Continuar de todos modos</button>
        </div>
      </div>
    `;

    const style = document.createElement('style');
    style.textContent = `
      #phisware-overlay {
        position: fixed; inset: 0; z-index: 2147483647;
        background: rgba(0,0,0,0.75); display: flex;
        align-items: center; justify-content: center;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
      }
      #phisware-modal {
        background: #1e1e2e; color: #cdd6f4; border-radius: 12px;
        padding: 28px 32px; max-width: 440px; width: 90%;
        box-shadow: 0 20px 60px rgba(0,0,0,0.5);
        border: 2px solid #f38ba8;
      }
      #phisware-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
      #phisware-icon { font-size: 28px; }
      #phisware-title { font-size: 18px; font-weight: 700; color: #f38ba8; }
      #phisware-classification { font-size: 22px; font-weight: 700; color: #f38ba8; margin: 0 0 6px; }
      #phisware-domain { font-size: 16px; color: #89dceb; margin: 0 0 8px; word-break: break-all; }
      #phisware-score { font-size: 14px; color: #a6adc8; margin: 0 0 12px; }
      #phisware-indicators { padding-left: 18px; margin: 0 0 16px; font-size: 13px; color: #fab387; }
      #phisware-indicators li { margin-bottom: 4px; }
      #phisware-actions { display: flex; gap: 10px; margin-top: 8px; }
      #phisware-back {
        flex: 1; padding: 10px; background: #a6e3a1; color: #1e1e2e;
        border: none; border-radius: 8px; font-size: 14px; font-weight: 700;
        cursor: pointer;
      }
      #phisware-continue {
        flex: 1; padding: 10px; background: transparent; color: #6c7086;
        border: 1px solid #45475a; border-radius: 8px; font-size: 13px;
        cursor: pointer;
      }
      #phisware-back:hover { opacity: 0.85; }
      #phisware-continue:hover { color: #cdd6f4; border-color: #cdd6f4; }
    `;

    document.head.appendChild(style);
    document.body.appendChild(overlay);

    overlay.querySelector('#phisware-back').addEventListener('click', () => overlay.remove());
    overlay.querySelector('#phisware-continue').addEventListener('click', () => {
      overlay.remove();
      window.location.href = continueHref;
    });
  }

  function labelFor(classification) {
    return {
      DANGEROUS:  '🚨 Sitio Peligroso',
      SUSPICIOUS: '⚠️ Sitio Sospechoso',
      SAFE:       '✅ Sitio Seguro',
    }[classification] ?? classification;
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

  // ── Link click interception ───────────────────────────────────────────────

  async function handleLinkClick(e) {
    const anchor = e.target.closest('a[href]');
    if (!anchor) return;

    const url = normalizeHref(anchor.getAttribute('href'));
    if (!url) return;

    // Pre-check from cache
    const cached = checked.get(url);
    if (cached !== null && cached !== undefined) {
      if (cached.classification === 'DANGEROUS' || cached.classification === 'SUSPICIOUS') {
        e.preventDefault();
        e.stopImmediatePropagation();
        showWarning(cached, url);
      }
      return;
    }

    // Unknown link: prevent navigation, check now
    e.preventDefault();
    e.stopImmediatePropagation();

    const result = await checkUrl(url);
    if (!result) {
      // Backend unreachable — let through silently
      window.location.href = url;
      return;
    }

    if (result.classification === 'DANGEROUS' || result.classification === 'SUSPICIOUS') {
      showWarning(result, url);
      if (result.classification === 'DANGEROUS') {
        chrome.runtime.sendMessage({ type: 'SAVE_ALERT', alert: result });
      }
    } else {
      window.location.href = url;
    }
  }

  // ── Pre-scan visible links (hover badge tinting) ──────────────────────────

  function tintLink(anchor, result) {
    if (!result) return;
    if (result.classification === 'DANGEROUS') {
      anchor.style.outline = '2px solid #ef4444';
    } else if (result.classification === 'SUSPICIOUS') {
      anchor.style.outline = '2px solid #f59e0b';
    }
  }

  function scanLinks(root) {
    const anchors = root.querySelectorAll('a[href]');
    anchors.forEach(anchor => {
      const url = normalizeHref(anchor.getAttribute('href'));
      if (!url) return;

      // Use cached result to tint immediately
      const cached = checked.get(url);
      if (cached !== undefined) { tintLink(anchor, cached); return; }

      // Background pre-fetch
      checkUrl(url).then(result => tintLink(anchor, result));
    });
  }

  // ── MutationObserver for SPAs ─────────────────────────────────────────────

  const observer = new MutationObserver(mutations => {
    for (const m of mutations) {
      for (const node of m.addedNodes) {
        if (node.nodeType === 1) scanLinks(node);
      }
    }
  });

  observer.observe(document.body, { childList: true, subtree: true });

  // ── Bootstrap ─────────────────────────────────────────────────────────────

  document.addEventListener('click', handleLinkClick, true);
  scanLinks(document);
})();
