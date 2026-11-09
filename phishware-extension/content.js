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

  function showWarning(result, continueHref) {
    const existing = document.getElementById('phisware-overlay');
    if (existing) existing.remove();

    const msg = k => chrome.i18n.getMessage(k) || k;
    const overlay = document.createElement('div');
    overlay.id = 'phisware-overlay';
    const safeDomain = escapeHtml(result.domain ?? result.url);
    const safeScore = Math.round(Number(result.riskScore) || 0);
    const explained = Array.isArray(result.explainedIndicators) && result.explainedIndicators.length
      ? result.explainedIndicators
      : (result.indicators || []).slice(0, 3).map(toExplained);
    const safeIndicators = explained.slice(0, 3).map(i => {
      const label = escapeHtml(typeof i === 'string' ? i : i.description);
      const principle = typeof i === 'string' ? null : principleName(i.persuasionPrinciple);
      const suffix = principle ? ` <span class="phisware-principle">${escapeHtml(msg('principleLabel'))}: ${escapeHtml(principle)}</span>` : '';
      return `<li>${label}${suffix}</li>`;
    }).join('');
    overlay.innerHTML = `
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
    `;

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
      window.location.href = continueHref;
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
    // limitamos para no mandar 100 requests de una
    let count = 0;
    for (const anchor of anchors) {
      if (count >= 20) break;
      const url = normalizeHref(anchor.getAttribute('href'));
      if (!url) continue;

      // Use cached result to tint immediately
      const cached = checked.get(url);
      if (cached !== undefined) { tintLink(anchor, cached); continue; }

      count++;
      // Background pre-fetch
      checkUrl(url).then(result => tintLink(anchor, result));
    }
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
