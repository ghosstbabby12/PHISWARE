// Genera los iconos de la extensión a partir del logo oficial (el mismo de la web).
// Uso: node generate-icons.js            (usa phishware-frontend/public/phishware-logo.svg)
//      node generate-icons.js otro.svg   (usa tu propio SVG)
// Requiere rsvg-convert (macOS: brew install librsvg).
const { execFileSync } = require('node:child_process');
const path = require('node:path');

const src = path.resolve(process.argv[2] || path.join(__dirname, '..', 'phishware-frontend', 'public', 'phishware-logo.svg'));
for (const size of [16, 32, 48, 128]) {
  const out = path.join(__dirname, 'icons', `icon${size}.png`);
  execFileSync('rsvg-convert', ['-w', String(size), '-h', String(size), src, '-o', out]);
  console.log(`  ✓ icons/icon${size}.png`);
}
