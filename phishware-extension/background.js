import { classifyLocal } from './heuristics.js';

const DEFAULT_BACKEND_ROOT = 'http://localhost:8080/api';
const CACHE_TTL = 5 * 60 * 1000; // 5 min
const BLOCKLIST_ALARM = 'phishware-blocklist-refresh';
const MAX_DNR_RULES = 100;
const cache = new Map();

// ── Configurable backend ────────────────────────────────────────────────────

async function backendRoot() {
  const { backendRoot } = await chrome.storage.sync.get({ backendRoot: DEFAULT_BACKEND_ROOT });
  return String(backendRoot || DEFAULT_BACKEND_ROOT).replace(/\/$/, '');
}

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

// ── Community blocklist (refreshed every 30 min) ──────────────────────────────

let blockedDomains = new Set();

async function refreshBlocklist() {
  try {
    const root = await backendRoot();
    const res = await fetch(`${root}/public/url/blocklist`, { signal: AbortSignal.timeout(10000) });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    const domains = Array.isArray(data.domains) ? data.domains.filter(d => typeof d === 'string') : [];
    blockedDomains = new Set(domains.map(d => d.toLowerCase()));
    await chrome.storage.local.set({ blocklist: [...blockedDomains], blocklistUpdatedAt: Date.now() });
    await applyBlockingRules([...blockedDomains].slice(0, MAX_DNR_RULES));
  } catch (e) {
    const stored = await chrome.storage.local.get({ blocklist: [] });
    blockedDomains = new Set(stored.blocklist);
  }
}

function warningUrl(url) {
  return `${chrome.runtime.getURL('warning.html')}?u=${encodeURIComponent(url)}`;
}

function escapeRegex(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

async function applyBlockingRules(domains) {
  try {
    const existing = await chrome.declarativeNetRequest.getDynamicRules();
    const removeRuleIds = existing.map(r => r.id);
    const base = chrome.runtime.getURL('warning.html');
    // Antes de que salga una sola petición de red, el dominio confirmado se redirige a una
    // página de advertencia propia (en vez de la pantalla de error genérica del navegador).
    const addRules = domains.map((domain, i) => ({
      id: i + 1,
      priority: 1,
      action: { type: 'redirect', redirect: { regexSubstitution: `${base}?u=\\1` } },
      condition: {
        regexFilter: `^(https?://([^/]*\\.)?${escapeRegex(domain)}(:[0-9]+)?(/.*)?)$`,
        resourceTypes: ['main_frame'],
      },
    }));
    await chrome.declarativeNetRequest.updateDynamicRules({ removeRuleIds, addRules });
  } catch (e) {
    // declarativeNetRequest may be unavailable in some contexts; in-page interception still applies
  }
}

chrome.runtime.onInstalled.addListener(() => {
  chrome.alarms.create(BLOCKLIST_ALARM, { periodInMinutes: 30 });
  refreshBlocklist();
});

chrome.runtime.onStartup.addListener(() => {
  refreshBlocklist();
});

chrome.alarms.onAlarm.addListener(alarm => {
  if (alarm.name === BLOCKLIST_ALARM) refreshBlocklist();
});

function blocklistedDomain(url) {
  try {
    const host = new URL(url).hostname.toLowerCase();
    for (const domain of blockedDomains) {
      if (host === domain || host.endsWith('.' + domain)) return domain;
    }
  } catch { /* ignore */ }
  return null;
}

// ── API call with offline fail-safe ───────────────────────────────────────────

async function checkUrl(url) {
  const cached = cacheGet(url);
  if (cached) return cached;

  const blocked = blocklistedDomain(url);
  if (blocked) {
    const result = {
      classification: 'DANGEROUS',
      riskScore: 95,
      isThreat: true,
      domain: new URL(url).hostname,
      indicators: [`COMMUNITY_CONFIRMED: reported and confirmed by the community (${blocked})`],
      recommendations: ['This domain was confirmed as phishing by the community. Do not continue.'],
      explainedIndicators: [{ type: 'COMMUNITY_CONFIRMED', description: 'Community-confirmed phishing domain', persuasionPrinciple: 'NONE', microlessonSlug: null }],
      officialSeal: { official: false, brand: null, officialDomain: null },
      source: 'COMMUNITY_BLOCKLIST',
      offline: false,
    };
    cacheSet(url, result);
    return result;
  }

  try {
    const root = await backendRoot();
    const res = await fetch(`${root}/public/url/check?url=${encodeURIComponent(url)}`, {
      signal: AbortSignal.timeout(8000),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    cacheSet(url, data);
    return data;
  } catch (e) {
    // Backend unreachable — fail safe with local heuristics instead of letting through
    const local = classifyLocal(url);
    cacheSet(url, local);
    return local;
  }
}

// ── Bloqueo ANTES de cargar la página ─────────────────────────────────────────

const LOCAL_HOSTS = new Set(['localhost', '127.0.0.1', '0.0.0.0', '[::1]']);
const ALLOW_TTL = 30 * 60 * 1000; // "continuar de todos modos" vale 30 min para ese sitio
const ALLOW_RULE_BASE = 1000;

async function allowedHosts() {
  const { allowed = {} } = await chrome.storage.session.get('allowed');
  const now = Date.now();
  let changed = false;
  for (const [h, exp] of Object.entries(allowed)) {
    if (exp < now) { delete allowed[h]; changed = true; }
  }
  if (changed) await chrome.storage.session.set({ allowed });
  return allowed;
}

async function isAllowed(url) {
  try { return Boolean((await allowedHosts())[new URL(url).hostname.toLowerCase()]); } catch { return false; }
}

async function allowHost(url) {
  const host = new URL(url).hostname.toLowerCase();
  const allowed = await allowedHosts();
  allowed[host] = Date.now() + ALLOW_TTL;
  await chrome.storage.session.set({ allowed });
  // La regla comunitaria (DNR) también debe dejar pasar este host mientras dure el permiso
  try {
    const id = ALLOW_RULE_BASE + (Math.abs([...host].reduce((a, c) => (a * 31 + c.charCodeAt(0)) | 0, 7)) % 5000);
    await chrome.declarativeNetRequest.updateSessionRules({
      removeRuleIds: [id],
      addRules: [{ id, priority: 10, action: { type: 'allow' },
        condition: { requestDomains: [host], resourceTypes: ['main_frame'] } }],
    });
  } catch { /* DNR de sesión no disponible: basta con la lista en memoria */ }
}

// Con backend se confía en su veredicto; sin backend (heurística local) solo se bloquea con
// puntaje alto, para no frenar sitios legítimos por una heurística débil.
const OFFLINE_BLOCK_SCORE = 60;

async function guardNavigation(details) {
  if (details.frameId !== 0 || !/^https?:/i.test(details.url)) return;
  let host;
  try { host = new URL(details.url).hostname.toLowerCase(); } catch { return; }
  if (LOCAL_HOSTS.has(host)) return;
  if (await isAllowed(details.url)) return;

  const local = classifyLocal(details.url);
  if (local.officialSeal?.official && local.classification === 'SAFE') return; // sitio oficial: sin demora

  let result = local;
  if (local.classification !== 'DANGEROUS' || local.riskScore < 100) {
    result = await Promise.race([
      checkUrl(details.url),
      new Promise(r => setTimeout(() => r(local), 2500)),
    ]);
  }
  const block = result.classification === 'DANGEROUS' &&
    (!result.offline || (result.riskScore ?? 0) >= OFFLINE_BLOCK_SCORE);
  if (!block) return;

  await chrome.storage.session.set({ [`tab_${details.tabId}`]: result });
  chrome.action.setBadgeText({ tabId: details.tabId, text: '!' });
  chrome.action.setBadgeBackgroundColor({ tabId: details.tabId, color: '#ef4444' });
  chrome.tabs.update(details.tabId, { url: warningUrl(details.url) }).catch(() => {});
}

chrome.webNavigation.onBeforeNavigate.addListener(details => { guardNavigation(details); });

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
      title: chrome.i18n.getMessage('notifDangerTitle'),
      message: chrome.i18n.getMessage('notifDangerBody', [result.domain, String(Math.round(result.riskScore))]),
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

  if (msg.type === 'ALLOW_CONTINUE' && msg.url && sender.tab?.id) {
    allowHost(msg.url).then(() => {
      chrome.tabs.update(sender.tab.id, { url: msg.url });
      sendResponse({ ok: true });
    });
    return true;
  }

  if (msg.type === 'REFRESH_BLOCKLIST') {
    refreshBlocklist().then(() => sendResponse({ ok: true, count: blockedDomains.size }));
    return true;
  }

  if (msg.type === 'GO_SAFE' && sender.tab?.id) {
    chrome.tabs.update(sender.tab.id, { url: 'about:blank' });
    sendResponse({ ok: true });
    return false;
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
