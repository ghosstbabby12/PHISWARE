'use strict';

const $ = id => document.getElementById(id);
const t = (k, subs) => chrome.i18n.getMessage(k, subs) || '';

/** La URL original viaja tras "?u=" (codificada por el código, o cruda por la regla DNR). */
function originalUrl() {
  const raw = location.href.split('?u=')[1];
  if (!raw) return '';
  let u = raw;
  if (/^https?%3A/i.test(u)) { try { u = decodeURIComponent(u); } catch { /* cruda */ } }
  return /^https?:\/\//i.test(u) ? u : '';
}

function principleName(p) {
  if (!p || p === 'NONE') return null;
  return t('principle_' + p) || p;
}

function render(result, url) {
  let host = '';
  try { host = new URL(url).hostname; } catch { host = url; }
  $('domain').textContent = host;

  const score = Math.max(0, Math.min(100, Math.round(Number(result?.riskScore) || 0)));
  $('meterFill').style.width = score + '%';
  $('score').textContent = `${t('overlayScore')} ${score}/100`;

  const reasons = $('reasons');
  reasons.textContent = '';
  const explained = (result?.explainedIndicators?.length
    ? result.explainedIndicators
    : (result?.indicators || []).map(d => ({ description: String(d), persuasionPrinciple: 'NONE' }))
  ).slice(0, 4);

  if (explained.length === 0) {
    const li = document.createElement('li');
    li.textContent = result?.source === 'COMMUNITY_BLOCKLIST' ? t('warnCommunity') : t('warnGeneric');
    reasons.appendChild(li);
  }
  for (const i of explained) {
    const li = document.createElement('li');
    li.textContent = i.description;
    const pr = principleName(i.persuasionPrinciple);
    if (pr) {
      const tag = document.createElement('span');
      tag.className = 'tag';
      tag.textContent = ` · ${t('principleLabel')}: ${pr}`;
      li.appendChild(document.createElement('br'));
      li.appendChild(tag);
    }
    reasons.appendChild(li);
  }
}

function translateStatic() {
  const map = {
    title: 'warnTitle', sub: 'warnSub', whyTitle: 'warnWhy',
    back: 'warnBack', continue: 'warnContinue', fine: 'warnFine',
  };
  for (const [id, key] of Object.entries(map)) {
    const msg = t(key);
    if (msg) $(id).textContent = msg;
  }
}

(function main() {
  translateStatic();
  const url = originalUrl();
  if (!url) { $('domain').textContent = '—'; return; }
  $('domain').textContent = url;

  chrome.runtime.sendMessage({ type: 'CHECK_URL', url }, response => {
    render(response?.result ?? { riskScore: 0, indicators: [] }, url);
  });

  $('back').addEventListener('click', () => {
    if (history.length > 1) history.back();
    else chrome.runtime.sendMessage({ type: 'GO_SAFE' });
  });
  $('continue').addEventListener('click', () => {
    chrome.runtime.sendMessage({ type: 'ALLOW_CONTINUE', url });
  });
})();
