// Run once with: node generate-icons.js
// Requires: npm install canvas (or skip and use any 16x16/48x48/128x128 PNG)
// Alternative: just use the SVG below and convert manually.
//
// If you don't want to install canvas, rename any PNG files to icon16.png,
// icon48.png, icon128.png and place them in the icons/ folder.

const { createCanvas } = require('canvas');
const fs = require('fs');
const path = require('path');

function drawIcon(size) {
  const canvas = createCanvas(size, size);
  const ctx = canvas.getContext('2d');

  // Background circle
  ctx.fillStyle = '#1e1e2e';
  ctx.beginPath();
  ctx.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2);
  ctx.fill();

  // Shield shape
  ctx.fillStyle = '#89b4fa';
  const s = size * 0.6;
  const x = (size - s) / 2;
  const y = (size - s * 1.05) / 2;
  ctx.beginPath();
  ctx.moveTo(x + s / 2, y);
  ctx.lineTo(x + s, y + s * 0.3);
  ctx.lineTo(x + s, y + s * 0.6);
  ctx.quadraticCurveTo(x + s / 2, y + s * 1.05, x, y + s * 0.6);
  ctx.lineTo(x, y + s * 0.3);
  ctx.closePath();
  ctx.fill();

  // "P" letter
  ctx.fillStyle = '#1e1e2e';
  ctx.font = `bold ${size * 0.35}px Arial`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText('P', size / 2, size / 2 + size * 0.05);

  return canvas.toBuffer('image/png');
}

const iconsDir = path.join(__dirname, 'icons');
if (!fs.existsSync(iconsDir)) fs.mkdirSync(iconsDir);

[16, 48, 128].forEach(size => {
  try {
    fs.writeFileSync(path.join(iconsDir, `icon${size}.png`), drawIcon(size));
    console.log(`Generated icon${size}.png`);
  } catch (e) {
    console.error(`Error generating icon${size}.png:`, e.message);
  }
});
