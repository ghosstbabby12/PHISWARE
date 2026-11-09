'use strict';

/**
 * Offline lexical heuristics for the PHISWARE extension (Manifest V3).
 * Mirrors the strongest server-side rules (NistThreatClassifier) so the
 * extension can warn even when the backend is unreachable (fail-safe).
 *
 * Output shape matches POST /analysis and GET /public/url/check responses:
 * { classification, riskScore, isThreat, domain, indicators, recommendations,
 *   explainedIndicators, officialSeal, source, offline }
 */

const BRANDS = [
  { name: 'nequi', official: ['nequi.com.co', 'nequi.co'] },
  { name: 'bancolombia', official: ['bancolombia.com', 'grupobancolombia.com', 'bancolombia.com.co'] },
  { name: 'davivienda', official: ['davivienda.com'] },
  { name: 'daviplata', official: ['daviplata.com', 'davivienda.com'] },
  { name: 'bbva', official: ['bbva.com', 'bbva.com.co', 'bbva.mx', 'bbva.es'] },
  { name: 'santander', official: ['santander.com', 'santander.com.mx', 'santander.com.br', 'bancosantander.es'] },
  { name: 'mercadopago', official: ['mercadopago.com', 'mercadopago.com.co', 'mercadopago.com.mx'] },
  { name: 'mercadolibre', official: ['mercadolibre.com', 'mercadolibre.com.co', 'mercadolibre.com.mx'] },
  { name: 'dian', official: ['dian.gov.co'] },
  { name: 'paypal', official: ['paypal.com', 'paypal.me'] },
  { name: 'amazon', official: ['amazon.com', 'amazon.es', 'amazon.com.mx'] },
  { name: 'google', official: ['google.com', 'google.com.co', 'gmail.com'] },
  { name: 'microsoft', official: ['microsoft.com', 'live.com', 'outlook.com', 'office.com'] },
  { name: 'apple', official: ['apple.com', 'icloud.com'] },
  { name: 'netflix', official: ['netflix.com'] },
];

const HIGH_RISK_TLDS = ['tk', 'ml', 'ga', 'cf', 'gq', 'xyz', 'top', 'click', 'loan', 'win', 'download', 'stream', 'party', 'review', 'icu', 'cyou', 'buzz', 'rest', 'monster', 'sbs', 'cfd', 'zip', 'mov', 'country', 'kim', 'work', 'support'];
const URL_SHORTENERS = ['bit.ly', 'tinyurl.com', 't.co', 'goo.gl', 'ow.ly', 'buff.ly', 'short.link', 'rb.gy', 'cutt.ly', 'is.gd', 'v.gd', 'shorturl.at', 'tiny.cc', 's.id'];
const FREE_HOSTING = ['000webhostapp', 'weebly', 'wixsite', 'firebaseapp', 'blogspot', 'ngrok', 'duckdns', 'repl.co', 'github.io', 'netlify.app', 'vercel.app'];
const KEYWORDS = ['verify', 'confirm', 'login', 'secure', 'account', 'update', 'verificar', 'actualizar', 'cuenta', 'clave', 'bloqueo', 'seguridad', 'banco'];
const PHISHING_TERMS = ['secure-login', 'login-secure', 'verify-account', 'account-verify', 'credential', 'webscr', 'signin-secure'];

const PRINCIPLE_FOR_TYPE = {
  BRAND_SQUATTING: 'AUTHORITY', TYPOSQUATTING: 'TRUST', PHISHING_DOMAIN_PATTERN: 'AUTHORITY',
  SUSPICIOUS_SUBDOMAIN: 'TRUST', URL_SHORTENER: 'CURIOSITY', HIGH_RISK_TLD: 'NONE',
  PHISHING_KEYWORDS: 'URGENCY', EXCESSIVE_URL_LENGTH: 'NONE', IP_AS_DOMAIN: 'TRUST',
  PUNYCODE_HOMOGRAPH: 'TRUST', USERINFO_TRICK: 'TRUST', NON_STANDARD_PORT: 'NONE',
  NO_HTTPS: 'NONE', EMBEDDED_REDIRECT: 'TRUST', RANDOM_LOOKING_DOMAIN: 'NONE',
  DOMAIN_COMPOSITION: 'TRUST', FREE_HOSTING: 'TRUST', EXCESSIVE_SUBDOMAINS: 'TRUST',
};

function levenshtein(a, b) {
  const m = a.length, n = b.length;
  const dp = Array.from({ length: m + 1 }, (_, i) => [i, ...Array(n).fill(0)]);
  for (let j = 1; j <= n; j++) dp[0][j] = j;
  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      dp[i][j] = Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + (a[i - 1] === b[j - 1] ? 0 : 1));
    }
  }
  return dp[m][n];
}

function entropy(s) {
  const freq = {};
  for (const c of s) freq[c] = (freq[c] || 0) + 1;
  return -Object.values(freq).reduce((acc, f) => {
    const p = f / s.length;
    return acc + p * Math.log2(p);
  }, 0);
}

function isIp(host) {
  return /^\d{1,3}(\.\d{1,3}){3}$/.test(host) || host.includes(':');
}

function officialSealFor(host) {
  const h = host.toLowerCase();
  for (const brand of BRANDS) {
    for (const official of brand.official) {
      if (h === official || h.endsWith('.' + official)) {
        return { official: true, brand: brand.name, officialDomain: official };
      }
    }
  }
  return { official: false, brand: null, officialDomain: null };
}

function checkBrand(host, sld) {
  const hits = [];
  for (const brand of BRANDS) {
    if (brand.official.some(d => host === d || host.endsWith('.' + d))) continue;
    const name = brand.name;
    const literal = name.length >= 5 ? host.includes(name) : host.split(/[.-]/).includes(name);
    if (literal) {
      hits.push({ type: 'BRAND_SQUATTING', description: `The domain contains "${name}" but is not its official site`, severity: 'HIGH', score: 40 });
      continue;
    }
    if (name.length < 6) continue;
    const maxDistance = name.length >= 9 ? 2 : 1;
    const nearMiss = sld.split('-').some(t => Math.abs(t.length - name.length) <= maxDistance && levenshtein(t, name) <= maxDistance);
    if (nearMiss) {
      hits.push({ type: 'TYPOSQUATTING', description: `The domain imitates "${name}" with changed letters`, severity: 'HIGH', score: 40 });
    }
  }
  return hits;
}

function classifyLocal(rawUrl) {
  const indicators = [];
  const push = (type, description, severity, score) => indicators.push({ type, description, severity, score });

  let url;
  try {
    url = new URL(rawUrl);
  } catch {
    return { classification: 'SUSPICIOUS', riskScore: 30, isThreat: true, domain: rawUrl, indicators: [], recommendations: [], explainedIndicators: [], officialSeal: { official: false, brand: null, officialDomain: null }, source: 'LOCAL_HEURISTICS', offline: true };
  }
  const host = url.hostname.toLowerCase();
  const sld = host.split('.').slice(-2, -1)[0] || host;

  const seal = officialSealFor(host);
  if (!seal.official) {
    for (const hit of checkBrand(host, sld)) push(hit.type, hit.description, hit.severity, hit.score);
  }

  if (PHISHING_TERMS.some(t => host.includes(t))) {
    push('PHISHING_DOMAIN_PATTERN', 'The domain combines typical phishing terms (secure-login, verify-account, etc.)', 'HIGH', 35);
  }
  if (/^(secure|login|account|verify|update|support|service)\./.test(host)) {
    push('SUSPICIOUS_SUBDOMAIN', 'The subdomain uses terms that imitate legitimate services', 'MEDIUM', 20);
  }
  if (URL_SHORTENERS.includes(host)) {
    push('URL_SHORTENER', 'The URL uses a shortener that may hide a malicious destination', 'MEDIUM', 20);
  }
  const tld = host.split('.').pop();
  if (HIGH_RISK_TLDS.includes(tld)) {
    push('HIGH_RISK_TLD', `The domain uses a TLD frequently linked to malicious activity: .${tld}`, 'MEDIUM', 15);
  }
  if (KEYWORDS.some(k => rawUrl.toLowerCase().includes(k))) {
    push('PHISHING_KEYWORDS', 'The URL contains urgency/verification keywords', 'MEDIUM', 15);
  }
  if (rawUrl.length > 150) push('EXCESSIVE_URL_LENGTH', 'The URL is unusually long', 'LOW', 5);
  if (isIp(host)) push('IP_AS_DOMAIN', 'The site is accessed by IP instead of a domain name', 'HIGH', 35);
  if (host.split('.').length - 1 > 3) push('EXCESSIVE_SUBDOMAINS', 'The domain has an unusual number of subdomains', 'MEDIUM', 15);
  if (host.startsWith('xn--') || host.includes('.xn--')) push('PUNYCODE_HOMOGRAPH', 'The domain uses punycode, possibly imitating another brand', 'HIGH', 30);
  if (url.username) push('USERINFO_TRICK', 'The URL hides its real destination before an @ sign', 'HIGH', 35);
  if (url.port && !['80', '443', ''].includes(url.port)) push('NON_STANDARD_PORT', 'The URL uses a non-standard port', 'MEDIUM', 15);
  if (url.protocol === 'http:') push('NO_HTTPS', 'The site does not use HTTPS encryption', 'LOW', 10);
  if (/[?&](url|redirect|redir|next|continue|dest|target|return)=https?/i.test(rawUrl) || /\/\//.test(url.pathname)) {
    push('EMBEDDED_REDIRECT', 'The URL embeds a redirect to another destination', 'MEDIUM', 15);
  }
  if (sld.length >= 12 && entropy(sld) > 3.6 && !isIp(host)) {
    push('RANDOM_LOOKING_DOMAIN', 'The domain looks randomly generated', 'MEDIUM', 15);
  }
  const hyphens = (sld.match(/-/g) || []).length;
  const digits = (sld.match(/\d/g) || []).length;
  if (hyphens >= 3 || (digits >= 4 && digits / sld.length > 0.3)) {
    push('DOMAIN_COMPOSITION', 'The domain has an odd mix of hyphens and digits', 'LOW', 10);
  }
  if (FREE_HOSTING.some(f => host.endsWith('.' + f))) {
    push('FREE_HOSTING', 'The site lives on a free hosting service often abused by phishing', 'MEDIUM', 15);
  }

  const score = Math.min(100, indicators.reduce((acc, i) => acc + i.score, 0));
  const classification = score >= 40 ? 'DANGEROUS' : score >= 15 ? 'SUSPICIOUS' : 'SAFE';

  const recommendations = classification === 'DANGEROUS'
    ? ['Do not enter credentials or personal data on this site.', 'Close this tab immediately.']
    : classification === 'SUSPICIOUS'
      ? ['Proceed with caution — verify the URL before continuing.']
      : ['No local heuristic indicators were found.'];

  return {
    classification,
    riskScore: Math.round(score * 100) / 100,
    isThreat: classification !== 'SAFE',
    domain: host,
    indicators: indicators.map(i => `${i.type}: ${i.description}`),
    recommendations,
    explainedIndicators: indicators.map(i => ({
      type: i.type,
      description: i.description,
      persuasionPrinciple: PRINCIPLE_FOR_TYPE[i.type] || 'NONE',
      microlessonSlug: null,
    })),
    officialSeal: seal,
    source: 'LOCAL_HEURISTICS',
    offline: true,
  };
}

// Export for the service worker (ES module) and for tests.
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { classifyLocal, officialSealFor, BRANDS };
}
export { classifyLocal, officialSealFor, BRANDS };
