const BACKEND = 'http://localhost:8080/api/public/url/check';
const CACHE_TTL = 5 * 60 * 1000; // 5 min
const cache = new Map();

// ── Badge helpers ─────────────────────────────────────────────────────────────

function setBadge(tabId, classification) {
  const map = {
    DANGEROUS:  { text: '!', color: '#ef4444' },
    SUSPICIOUS: { text: '?', color: '#f59e0b' },
    SAFE:       { text: '',  color: '#22c55e' },
  };
  const cfg = map[classification] ?? { text: '', color: '#6b7280' };
  chrome.action.setBadgeText({ tabId, text: cfg.text });
  chrome.action.setBadgeBackgroundColor({ tabId, color: cfg.color });
}

// ── Cache ─────────────────────────────────────────────────────────────────────

function cacheGet(url) {
  const entry = cache.get(url);
  if (!entry) return null;
  if (Date.now() - entry.ts > CACHE_TTL) { cache.delete(url); return null; }
  return entry.data;
}

function cacheSet(url, data) {
  cache.set(url, { ts: Date.now(), data });
  // Evict oldest entries when cache grows large
  if (cache.size > 500) {
    const firstKey = cache.keys().next().value;
    cache.delete(firstKey);
  }
}

// ── API call ──────────────────────────────────────────────────────────────────

async function checkUrl(url) {
  const cached = cacheGet(url);
  if (cached) return cached;

  try {
    const res = await fetch(`${BACKEND}?url=${encodeURIComponent(url)}`, {
      signal: AbortSignal.timeout(8000),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    cacheSet(url, data);
    return data;
  } catch (e) {
    return null;
  }
}

// ── Tab navigation: check current page URL ────────────────────────────────────

async function checkTabUrl(tabId, url) {
  if (!url || !url.startsWith('http')) return;

  const result = await checkUrl(url);
  if (!result) return;

  setBadge(tabId, result.classification);

  // Store last result per tab for the popup
  await chrome.storage.session.set({ [`tab_${tabId}`]: result });

  // Notify on DANGEROUS pages
  if (result.classification === 'DANGEROUS') {
    chrome.notifications.create(`page_${tabId}_${Date.now()}`, {
      type: 'basic',
      iconUrl: 'icons/icon48.png',
      title: '⚠️ PHISWARE: Sitio Peligroso',
      message: `${result.domain} fue clasificado como PELIGROSO.\nPuntuación: ${Math.round(result.riskScore)}/100`,
      priority: 2,
    });
  }
}

chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.status === 'complete' && tab.url) {
    checkTabUrl(tabId, tab.url);
  }
});

chrome.tabs.onActivated.addListener(async ({ tabId }) => {
  const tab = await chrome.tabs.get(tabId);
  if (tab?.url) checkTabUrl(tabId, tab.url);
});

// ── Messages from content script ─────────────────────────────────────────────

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
  if (msg.type === 'CHECK_URL') {
    checkUrl(msg.url).then(result => {
      sendResponse({ result });
      // Update badge color with most severe finding for this tab
      if (sender.tab?.id && result?.classification) {
        chrome.storage.session.get(`tab_${sender.tab.id}`, stored => {
          const current = stored[`tab_${sender.tab.id}`];
          // Escalate badge if a link on the page is more dangerous than the page itself
          const severity = { DANGEROUS: 3, SUSPICIOUS: 2, SAFE: 1 };
          if (!current || (severity[result.classification] ?? 0) > (severity[current.classification] ?? 0)) {
            setBadge(sender.tab.id, result.classification);
          }
        });
      }
    });
    return true; // keep channel open for async response
  }

  if (msg.type === 'GET_TAB_RESULT') {
    chrome.tabs.query({ active: true, currentWindow: true }, async tabs => {
      const tabId = tabs[0]?.id;
      if (!tabId) { sendResponse(null); return; }
      const stored = await chrome.storage.session.get(`tab_${tabId}`);
      sendResponse({ result: stored[`tab_${tabId}`] ?? null, tabId });
    });
    return true;
  }

  if (msg.type === 'GET_RECENT_ALERTS') {
    chrome.storage.local.get('alerts', data => {
      sendResponse({ alerts: data.alerts ?? [] });
    });
    return true;
  }
});

// ── Persist dangerous alerts ──────────────────────────────────────────────────

chrome.runtime.onMessage.addListener((msg) => {
  if (msg.type === 'SAVE_ALERT') {
    chrome.storage.local.get('alerts', data => {
      const alerts = data.alerts ?? [];
      alerts.unshift({ ...msg.alert, ts: Date.now() });
      if (alerts.length > 50) alerts.length = 50;
      chrome.storage.local.set({ alerts });
    });
  }
});
