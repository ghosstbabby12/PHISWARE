const BACKEND_HEALTH = 'http://localhost:8080/api/actuator/health';

function t(key) {
  return chrome.i18n.getMessage(key) || key;
}

function principleName(principle) {
  if (!principle || principle === 'NONE') return null;
  return t('principle_' + principle) || principle;
}

function timeAgo(ts) {
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return `${s}s`;
  const m = Math.floor(s / 60);
  if (m < 60) return `${m}m`;
  return `${Math.floor(m / 60)}h`;
}

function iconFor(cls) {
  return { DANGEROUS: '🚨', SUSPICIOUS: '⚠️', SAFE: '✅' }[cls] ?? '🔍';
}

// ── Backend connectivity ──────────────────────────────────────────────────────

async function checkBackend() {
  const dot    = document.getElementById('status-dot');
  const status = document.getElementById('backend-status');
  try {
    const res = await fetch(BACKEND_HEALTH, { signal: AbortSignal.timeout(3000) });
    if (res.ok) {
      dot.className = 'status-dot connected';
      status.textContent = t('popupBackendOn');
    } else {
      throw new Error();
    }
  } catch {
    dot.className = 'status-dot error';
    status.textContent = t('popupBackendOff');
  }
}

// ── Current page result ───────────────────────────────────────────────────────

function renderPageResult(result) {
  const badge    = document.getElementById('page-badge');
  const domain   = document.getElementById('page-domain');
  const score    = document.getElementById('page-score');
  const indList  = document.getElementById('indicators');

  if (!result) {
    badge.className = 'badge loading';
    badge.textContent = t('popupNoData');
    domain.textContent = '—';
    score.textContent  = '';
    return;
  }

  badge.className  = `badge ${result.classification}`;
  badge.textContent = result.classification;
  domain.textContent = result.domain || result.url || '—';
  score.textContent  = `${t('popupRisk')} ${Math.round(result.riskScore ?? 0)}/100`;

  indList.innerHTML = '';
  const explained = Array.isArray(result.explainedIndicators) ? result.explainedIndicators : [];
  const items = (explained.length ? explained : (result.indicators ?? []).slice(0, 3).map(toExplained)).slice(0, 3);
  if (result.classification !== 'SAFE') {
    items.forEach(ind => {
      const li = document.createElement('div');
      li.className = 'indicator-item';
      const label = typeof ind === 'string' ? ind : ind.description;
      const principle = typeof ind === 'string' ? null : principleName(ind.persuasionPrinciple);
      li.textContent = principle ? `${label} · ${t('principleLabel')}: ${principle}` : label;
      indList.appendChild(li);
    });
  }
}

function toExplained(text) {
  return { description: String(text), persuasionPrinciple: 'NONE' };
}

// ── Alerts list ───────────────────────────────────────────────────────────────

function renderAlerts(alerts) {
  const list = document.getElementById('alerts-list');
  list.innerHTML = '';
  if (!alerts?.length) {
    list.innerHTML = '';
    const empty = document.createElement('div');
    empty.className = 'empty';
    empty.textContent = t('popupNoAlerts');
    list.appendChild(empty);
    return;
  }
  alerts.slice(0, 10).forEach(a => {
    const div = document.createElement('div');
    div.className = 'alert-item';

    const icon = document.createElement('span');
    icon.className = 'alert-icon';
    icon.textContent = iconFor(a.classification);

    const content = document.createElement('div');
    content.className = 'alert-content';

    const domain = document.createElement('div');
    domain.className = 'alert-domain';
    domain.textContent = a.domain || a.url || t('popupUnknown');
    domain.title = a.url || '';

    const time = document.createElement('div');
    time.className = 'alert-time';
    time.textContent = `${a.classification} · ${timeAgo(a.ts)}`;

    content.appendChild(domain);
    content.appendChild(time);
    div.appendChild(icon);
    div.appendChild(content);
    list.appendChild(div);
  });
}

// ── Bootstrap ─────────────────────────────────────────────────────────────────

// Localize static labels
document.querySelectorAll('[data-i18n]').forEach(el => {
  el.textContent = t(el.getAttribute('data-i18n'));
});
document.documentElement.lang = chrome.i18n.getUILanguage?.()?.split('-')[0] || 'es';

checkBackend();

// Get current tab result
chrome.runtime.sendMessage({ type: 'GET_TAB_RESULT' }, response => {
  renderPageResult(response?.result ?? null);
});

// Get recent alerts
chrome.runtime.sendMessage({ type: 'GET_RECENT_ALERTS' }, response => {
  renderAlerts(response?.alerts ?? []);
});

// Clear alerts
document.getElementById('clear-btn').addEventListener('click', () => {
  chrome.storage.local.set({ alerts: [] }, () => {
    renderAlerts([]);
  });
});
