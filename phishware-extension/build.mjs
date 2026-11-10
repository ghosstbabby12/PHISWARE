// Empaqueta la extensión para cada familia de navegadores:
//   dist/chromium  -> Chrome, Edge, Brave, Opera, Vivaldi, Arc (Manifest V3 + service worker)
//   dist/firefox   -> Firefox (Manifest V3 con background.scripts y id de Gecko)
//   Safari         -> se convierte desde dist/chromium en un Mac:
//                     xcrun safari-web-extension-converter dist/chromium --project-location dist/safari
// Uso: npm run build
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFileSync } from 'node:child_process';

const root = path.dirname(fileURLToPath(import.meta.url));
const dist = path.join(root, 'dist');
const FILES = [
  'manifest.json', 'background.js', 'heuristics.js', 'shared.js', 'content.js',
  'popup.html', 'popup.js', 'options.html', 'options.js',
  'warning.html', 'warning.css', 'warning.js', '_locales', 'icons',
];
const base = JSON.parse(fs.readFileSync(path.join(root, 'manifest.json'), 'utf8'));

function copyTo(dir) {
  fs.rmSync(dir, { recursive: true, force: true });
  fs.mkdirSync(dir, { recursive: true });
  for (const f of FILES) fs.cpSync(path.join(root, f), path.join(dir, f), { recursive: true });
}

// ── Chromium ─────────────────────────────────────────────────────────────────
const chromiumDir = path.join(dist, 'chromium');
copyTo(chromiumDir);

// ── Firefox ──────────────────────────────────────────────────────────────────
const firefoxDir = path.join(dist, 'firefox');
copyTo(firefoxDir);
const ff = structuredClone(base);
// Firefox no admite service_worker en MV3: usa páginas de fondo (scripts) con módulos ES
ff.background = { scripts: ['background.js'], type: 'module' };
ff.browser_specific_settings = {
  gecko: {
    id: 'link-guard@phishware.app',
    strict_min_version: '140.0',
    // Se envía la URL visitada al backend configurado por el usuario (análisis de phishing)
    data_collection_permissions: { required: ['browsingActivity'] },
  },
  gecko_android: { strict_min_version: '142.0' },
};
fs.writeFileSync(path.join(firefoxDir, 'manifest.json'), JSON.stringify(ff, null, 2) + '\n');

// ── ZIPs (mejor esfuerzo: PowerShell en Windows, zip en macOS/Linux) ───
for (const name of ['chromium', 'firefox']) {
  const out = path.join(dist, `phishware-link-guard-${name}-${base.version}.zip`);
  try {
    fs.rmSync(out, { force: true });
    if (process.platform === 'win32') {
      execFileSync('powershell', ['-NoProfile', '-Command',
        `Compress-Archive -Path '${path.join(dist, name)}\*' -DestinationPath '${out}' -Force`], { stdio: 'ignore' });
    } else {
      execFileSync('zip', ['-qr', out, '.'], { cwd: path.join(dist, name), stdio: 'ignore' });
    }
    console.log(`  ✓ ${path.relative(root, out)}`);
  } catch {
    console.log(`  - no se pudo crear el zip de ${name} (comprime la carpeta dist/${name} a mano)`);
  }
}
console.log(`\nVersión ${base.version} lista en dist/:\n  chromium/  firefox/`);
