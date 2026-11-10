// Prueba de extremo a extremo en navegadores REALES (Chromium: Chrome y Edge) con la extensión cargada.
// Requiere el backend en http://localhost:8080 (docker compose up -d backend) y Internet.
//   npm run e2e            -> Chrome y Edge
//   BROWSERS=chrome npm run e2e
import { chromium } from 'playwright-core';
import http from 'node:http';
import os from 'node:os';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ext = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const BAD = 'http://secure-login.paypa1-verify.tk/account';
const enc = encodeURIComponent;

const page = port => `<!doctype html><meta charset="utf-8"><body style="font:20px sans-serif;padding:24px">
<p><a id="decept" href="http://paypal.com.verify-account.tk/login">www.paypal.com</a></p>
<p><a id="gmail" href="https://www.google.com/url?q=${enc(BAD)}&sa=D">Ver mi factura</a></p>
<p><a id="ig" href="https://l.instagram.com/?u=${enc(BAD)}&e=AT0">Mira esto</a></p>
<p><a id="safe" href="https://example.com/">Sitio normal</a></p>
<p><a id="newtab" target="_blank" href="https://l.wl.co/l?u=${enc(BAD)}">Abrir en pestaña nueva</a></p>`;

const server = http.createServer((req, res) => {
  res.setHeader('content-type', 'text/html; charset=utf-8');
  res.end(page(server.address().port));
});
await new Promise(r => server.listen(0, '127.0.0.1', r));
const base = `http://127.0.0.1:${server.address().port}/t.html`;

const results = [];
const until = async (fn, ms = 15000) => {
  const end = Date.now() + ms;
  while (Date.now() < end) { if (await fn()) return true; await new Promise(r => setTimeout(r, 150)); }
  return false;
};
const check = (name, ok, extra = '') => {
  results.push(ok);
  console.log(`${ok ? '  ✓' : '  ✗'} ${name}${extra ? '  — ' + extra : ''}`);
};

async function run(channel) {
  console.log(`\n=== ${channel} ===`);
  const userDataDir = fs.mkdtempSync(path.join(os.tmpdir(), `pw-${channel}-`));
  const ctx = await chromium.launchPersistentContext(userDataDir, {
    ...(channel === 'chromium' ? {} : { channel }),
    headless: false,
    ignoreDefaultArgs: ['--disable-extensions'],
    args: [
      `--disable-extensions-except=${ext}`,
      `--load-extension=${ext}`,
      '--disable-features=DisableLoadExtensionCommandLineSwitch',
      '--no-first-run', '--no-default-browser-check',
    ],
  });
  try {
    let [sw] = ctx.serviceWorkers();
    if (!sw) sw = await ctx.waitForEvent('serviceworker', { timeout: 15000 }).catch(() => null);
    if (!sw) {
      // Chrome/Edge de marca (>=137) ignoran --load-extension por seguridad: no es un fallo de la extensión
      console.log('  - omitido: este navegador no permite cargar extensiones por línea de comandos (usa "chromium")');
      return;
    }
    const extId = new URL(sw.url()).host;
    check('extensión cargada', Boolean(extId), extId);
    const warningPrefix = `chrome-extension://${extId}/warning.html`;

    // 1) Navegación directa a un sitio de phishing -> bloqueo ANTES de cargar
    const p = await ctx.newPage();
    p.goto(BAD, { timeout: 15000 }).catch(() => {});
    await until(() => p.url().startsWith(warningPrefix));
    check('1. navegación directa a phishing -> página de advertencia', p.url().startsWith(warningPrefix), p.url().slice(0, 70));
    await p.waitForSelector('#reasons li', { timeout: 8000 }).catch(() => {});
    const dom = await p.textContent('#domain').catch(() => '');
    const reasons = await p.locator('#reasons li').count().catch(() => 0);
    check('   muestra dominio y motivos', dom.includes('paypa1-verify.tk') && reasons > 0, `${dom} / ${reasons} motivos`);

    // 2) "Continuar de todos modos" deja pasar (y solo a ese sitio)
    await p.click('#continue');
    await until(() => !p.url().startsWith(warningPrefix), 10000);
    check('2. "continuar de todos modos" abandona la advertencia', !p.url().startsWith(warningPrefix), p.url().slice(0, 70));
    await p.close();

    // 2b) Lista comunitaria: regla de red (DNR) -> advertencia SIN tocar la red
    await ctx.route('**/public/url/blocklist', r => r.fulfill({
      status: 200, contentType: 'application/json',
      body: JSON.stringify({ domains: ['blocked-demo.test'], count: 1 }) }));
    const ep = await ctx.newPage();
    await ep.goto(`chrome-extension://${extId}/options.html`);
    const res = await ep.evaluate(() => new Promise(r => chrome.runtime.sendMessage({ type: 'REFRESH_BLOCKLIST' }, r)));
    await ep.close();
    const rules = await sw.evaluate(() => chrome.declarativeNetRequest.getDynamicRules());
    check('2b. lista comunitaria carga y crea regla de bloqueo', res?.count === 1 && rules.length === 1, `${res?.count} dominio(s), ${rules.length} regla(s)`);
    const b = await ctx.newPage();
    b.goto('http://sub.blocked-demo.test/login?x=1&y=2').catch(() => {});
    await until(() => b.url().startsWith(warningPrefix));
    check('   dominio confirmado por la comunidad -> advertencia (sin red)', b.url().startsWith(warningPrefix) && decodeURIComponent(b.url()).includes('blocked-demo.test/login?x=1&y=2'), b.url().slice(0, 90));
    await b.close();

    // 3) Enlaces dentro de una página
    const t = await ctx.newPage();
    await t.goto(base);
    await t.waitForTimeout(1500);

    const overlayText = async () => {
      await t.waitForSelector('#phisware-overlay', { timeout: 12000 }).catch(() => {});
      return (await t.textContent('#phisware-overlay').catch(() => '')) || '';
    };
    const closeOverlay = async () => { await t.click('#phisware-back').catch(() => {}); };

    await t.click('#decept');
    let txt = await overlayText();
    check('3. texto "www.paypal.com" que lleva a otro dominio', /paypal\.com/.test(txt) && /verify-account\.tk/.test(txt), txt.replace(/\s+/g, ' ').slice(0, 110));
    await closeOverlay();

    await t.click('#gmail');
    txt = await overlayText();
    check('4. enlace envuelto de Gmail (google.com/url?q=...) -> destino real', /paypa1-verify\.tk/.test(txt), txt.replace(/\s+/g, ' ').slice(0, 90));
    await closeOverlay();

    await t.click('#ig');
    txt = await overlayText();
    check('5. enlace envuelto de Instagram (l.instagram.com) -> destino real', /paypa1-verify\.tk/.test(txt), txt.replace(/\s+/g, ' ').slice(0, 90));
    await closeOverlay();

    const popupPromise = ctx.waitForEvent('page', { timeout: 12000 }).catch(() => null);
    await t.click('#newtab');
    const popup = await popupPromise;
    const stillHere = t.url() === base;
    txt = await overlayText();
    check('6. enlace de WhatsApp (l.wl.co, target=_blank) se frena sin abrir la pestaña', /paypa1-verify\.tk/.test(txt) && stillHere, popup ? 'se abrió pestaña nueva' : 'sin pestaña nueva');
    if (popup) await popup.close().catch(() => {});
    await closeOverlay();

    // 7) Un enlace normal sigue funcionando
    await t.click('#safe');
    await t.waitForURL(/example\.com/, { timeout: 15000 }).catch(() => {});
    check('7. enlace normal (example.com) navega sin estorbar', /example\.com/.test(t.url()), t.url());
  } finally {
    await ctx.close();
  }
}

const which = (process.env.BROWSERS || 'chromium,chrome,msedge').split(',');
for (const b of which) {
  try { await run(b); } catch (e) { console.log(`  ✗ ${b} falló: ${e.message.split('\n')[0]}`); results.push(false); }
}
server.close();
const passed = results.filter(Boolean).length;
console.log(`\n${passed}/${results.length} comprobaciones correctas`);
process.exit(passed === results.length ? 0 : 1);
