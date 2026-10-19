const BACKEND_HEALTH = 'http://localhost:8080/api/actuator/health';

function timeAgo(ts) {
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return `hace ${s}s`;
  const m = Math.floor(s / 60);
  if (m < 60) return `hace ${m}m`;
  return `hace ${Math.floor(m / 60)}h`;
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
      status.textContent = 'Backend conectado';
    } else {
      throw new Error();
    }
  } catch {
    dot.className = 'status-dot error';
    status.textContent = 'Backend sin conexión';
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
    badge.textContent = 'Sin datos';
    domain.textContent = '—';
    score.textContent  = '';
    return;
  }

  badge.className  = `badge ${result.classification}`;
  badge.textContent = result.classification;
  domain.textContent = result.domain || result.url || '—';
  score.textContent  = `Riesgo: ${Math.round(result.riskScore ?? 0)}/100`;

  indList.innerHTML = '';
  if (result.indicators?.length && result.classification !== 'SAFE') {
    result.indicators.slice(0, 3).forEach(ind => {
      const li = document.createElement('div');
      li.className = 'indicator-item';
      li.textContent = ind;
      indList.appendChild(li);
    });
  }
}

// ── Alerts list ───────────────────────────────────────────────────────────────

function renderAlerts(alerts) {
  const list = document.getElementById('alerts-list');
  list.innerHTML = '';
  if (!alerts?.length) {
    list.innerHTML = '<div class="empty">Sin alertas</div>';
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
    domain.textContent = a.domain || a.url || 'desconocido';
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
