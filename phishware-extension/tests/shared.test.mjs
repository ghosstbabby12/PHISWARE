import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const { unwrapRedirect, textDomain, isDeceptive, registrable, hostOf } = require('../shared.js');

test('Gmail/Google: saca el destino real de google.com/url', () => {
  const wrapped = 'https://www.google.com/url?q=http://secure-login.paypa1-verify.tk/account&sa=D&source=gmail';
  assert.equal(unwrapRedirect(wrapped), 'http://secure-login.paypa1-verify.tk/account');
});

test('Instagram y WhatsApp: l.instagram.com y l.wl.co', () => {
  assert.equal(
    unwrapRedirect('https://l.instagram.com/?u=https%3A%2F%2Fevil.tk%2Flogin&e=AT0abc'),
    'https://evil.tk/login');
  assert.equal(unwrapRedirect('https://l.wl.co/l?u=https%3A%2F%2Fevil.tk%2F'), 'https://evil.tk/');
});

test('Facebook, Outlook SafeLinks y YouTube', () => {
  assert.equal(unwrapRedirect('https://l.facebook.com/l.php?u=https%3A%2F%2Fevil.tk%2F&h=x'), 'https://evil.tk/');
  assert.equal(
    unwrapRedirect('https://eur01.safelinks.protection.outlook.com/?url=https%3A%2F%2Fevil.tk%2F&data=x'),
    'https://evil.tk/');
  assert.equal(unwrapRedirect('https://www.youtube.com/redirect?q=https%3A%2F%2Fevil.tk%2F'), 'https://evil.tk/');
});

test('redirecciones anidadas se desenvuelven hasta el destino final', () => {
  const inner = 'https://evil.tk/x';
  const l1 = 'https://l.instagram.com/?u=' + encodeURIComponent(inner);
  const l2 = 'https://www.google.com/url?q=' + encodeURIComponent(l1);
  assert.equal(unwrapRedirect(l2), inner);
});

test('un enlace normal o con parámetro u= en un sitio cualquiera NO se toca', () => {
  assert.equal(unwrapRedirect('https://example.com/?u=https://other.com'), 'https://example.com/?u=https://other.com');
  assert.equal(unwrapRedirect('https://www.google.com/search?q=https://x.com'), 'https://www.google.com/search?q=https://x.com');
  assert.equal(unwrapRedirect('not a url'), 'not a url');
});

test('registrable distingue dominios y subdominios engañosos', () => {
  assert.equal(registrable('secure.paypal.com'), 'paypal.com');
  assert.equal(registrable('paypal.com.verify-account.tk'), 'verify-account.tk');
  assert.equal(registrable('www.bancolombia.com.co'), 'bancolombia.com.co');
  assert.equal(registrable('bbc.co.uk'), 'bbc.co.uk');
  assert.equal(hostOf('https://WWW.PayPal.com/x'), 'paypal.com');
});

test('textDomain solo reconoce texto que ES una URL', () => {
  assert.equal(textDomain('www.paypal.com'), 'paypal.com');
  assert.equal(textDomain('https://www.bbva.es/login'), 'bbva.es');
  assert.equal(textDomain('  PayPal.com  '), 'paypal.com');
  assert.equal(textDomain('Haz clic aquí'), null);
  assert.equal(textDomain('Visita paypal.com para más información'), null);
  assert.equal(textDomain(''), null);
});

test('isDeceptive: texto dice paypal.com, destino es otro', () => {
  const d = isDeceptive('www.paypal.com', 'http://paypal.com.verify-account.tk/login');
  assert.deepEqual(d, { shown: 'paypal.com', real: 'paypal.com.verify-account.tk' });
});

test('isDeceptive: no hay falso positivo con mismo dominio, subdominio o texto libre', () => {
  assert.equal(isDeceptive('www.paypal.com', 'https://www.paypal.com/signin'), null);
  assert.equal(isDeceptive('paypal.com', 'https://secure.paypal.com/x'), null);
  assert.equal(isDeceptive('Haz clic aquí', 'https://evil.tk/'), null);
});
