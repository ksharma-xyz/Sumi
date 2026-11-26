/* Sumi panel artwork, built from the app's theme: season (or night) paper, one
   dry-brush enso (the brand's marketing mark), one InkBleed, washi tooth and the
   WashiBG edge vignette. Seeded from the panel slug, so a rebuild is identical. */
function drawArt(ctx, cfg) {
  const { W, H, dsf, colors: c, index } = cfg;
  const landscape = cfg.mode === 'landscape';
  const S = Math.min(W, H);
  const night = c.mode === 'night';

  let seed = 2166136261;
  for (const ch of cfg.slug) seed = Math.imul(seed ^ ch.charCodeAt(0), 16777619) >>> 0;
  const rng = () => {
    seed = (seed + 0x6D2B79F5) >>> 0;
    let t = Math.imul(seed ^ (seed >>> 15), 1 | seed);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
  const between = (a, b) => a + (b - a) * rng();
  const gauss = () => Math.sqrt(-2 * Math.log(rng() + 1e-9)) * Math.cos(2 * Math.PI * rng());
  const rgba = (hex, a) => {
    const n = parseInt(hex.slice(1), 16);
    return `rgba(${n >> 16},${(n >> 8) & 255},${n & 255},${a})`;
  };
  const radial = (x, y, r0, r1, colour, a0, a1) => {
    const g = ctx.createRadialGradient(x, y, r0, x, y, r1);
    g.addColorStop(0, rgba(colour, a0)); g.addColorStop(1, rgba(colour, a1));
    ctx.fillStyle = g; ctx.fillRect(0, 0, W, H);
  };

  // Paper, lit from the upper left.
  ctx.fillStyle = c.paper; ctx.fillRect(0, 0, W, H);
  radial(W * 0.22, H * 0.06, 0, Math.max(W, H) * 0.62, c.glow, night ? 0.85 : 0.95, 0);
  radial(W * 0.9, H * 1.02, 0, Math.max(W, H) * 0.60, c.deep, night ? 0.35 : 0.75, 0);

  // Where the artwork sits: alternating sides, so the set reads as one scroll.
  const right = index % 2 === 0;
  const wide = !landscape && W > H;  // Duo inner: stacked copy on an almost 4:3 sheet
  const R = landscape ? H * 0.92 : wide ? H : W;
  const ex = landscape ? W * (right ? 0.30 : 0.06) : wide ? W * (right ? 0.82 : 0.18) : W * (right ? 0.93 : 0.07);
  const ey = landscape ? H * (right ? 0.94 : 0.08) : wide ? H * 0.54 : H * (right ? 0.37 : 0.40);
  const er = R * 0.585;

  // The app's InkBleed: three concentric circles in the accent, softened.
  const bleed = (x, y, size, colour, alphas) => {
    ctx.save();
    ctx.filter = `blur(${size * 0.055 * dsf}px)`;
    [[0.30, alphas[0]], [0.20, alphas[1]], [0.10, alphas[2]]].forEach(([k, a]) => {
      ctx.fillStyle = rgba(colour, a);
      ctx.beginPath(); ctx.arc(x, y, size * k, 0, Math.PI * 2); ctx.fill();
    });
    ctx.restore();
  };
  bleed(landscape ? W * 0.97 : W * (right ? 0.05 : 0.95), landscape ? H * 0.9 : H * 0.88,
    R * 0.95, c.accent, night ? [0.14, 0.18, 0.22] : [0.10, 0.14, 0.18]);

  // Dry-brush enso: clumped bristles that run out of ink at their own pace.
  const enso = (cx, cy, r, thick, startDeg, sweepDeg, colour, alpha) => {
    const cv = document.createElement('canvas');
    cv.width = Math.round(W * dsf); cv.height = Math.round(H * dsf);
    const x = cv.getContext('2d'); x.scale(dsf, dsf);
    const a0 = (startDeg * Math.PI) / 180, sweep = (sweepDeg * Math.PI) / 180;
    const phase = between(0, 6.28);
    const widthAt = t => thick * (0.62 + 0.48 * Math.pow(Math.sin(Math.PI * Math.min(1, t * 1.25 + 0.06)), 0.5)) * (1 - 0.46 * t * t);
    x.lineCap = 'round'; x.lineJoin = 'round';
    const clumps = Array.from({ length: 15 }, () => between(0.22, 1));
    for (let b = 0; b < 460; b++) {
      const off = between(-0.5, 0.5);
      const edge = Math.abs(off) * 2;
      const load = clumps[Math.min(14, Math.floor((off + 0.5) * 15))];
      const from = 0.030 * edge * edge + Math.pow(rng(), 3) * 0.008;  // rounded, loaded brush head
      const to = 1 - Math.pow(rng(), 1.4) * (0.07 + 0.40 * edge);
      const f1 = between(11, 40), p1 = between(0, 6.28);
      const f2 = between(9, 31), p2 = between(0, 6.28), wander = thick * between(0.006, 0.034);
      x.strokeStyle = rgba(colour, Math.min(1, load * between(0.55, 1.2)));
      x.lineWidth = between(thick * 0.005, thick * 0.026) * (0.5 + load * 0.7);
      x.beginPath();
      let pen = false;
      for (let t = from; t <= to; t += 0.0025) {
        const a = a0 + sweep * t;
        const rr = r + off * widthAt(t) + r * 0.014 * Math.sin(t * 7.1 + phase) + wander * Math.sin(t * f2 + p2);
        const px = cx + Math.cos(a) * rr, py = cy + Math.sin(a) * rr;
        const dry = t > 0.42 && Math.sin(t * f1 + p1) > 1.45 - 1.35 * (t - 0.42) * 1.7 - 0.45 * edge - (1 - load) * 0.35;
        if (dry) { pen = false; continue; }
        if (pen) x.lineTo(px, py); else { x.moveTo(px, py); pen = true; }
      }
      x.stroke();
    }
    x.globalCompositeOperation = 'destination-out';
    const specks = 26000;
    for (let i = 0; i < specks; i++) {
      const a = a0 + sweep * rng(), rr = r + gauss() * thick * 0.34;
      const t = ((a - a0) / sweep);
      x.fillStyle = `rgba(0,0,0,${between(0.12, 0.30) + 0.45 * t * rng()})`;
      x.beginPath();
      x.ellipse(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr, between(0.5, 3.4), between(0.35, 1.1), a + Math.PI / 2, 0, Math.PI * 2);
      x.fill();
    }
    ctx.save(); ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.filter = `blur(${thick * 0.13 * dsf}px)`; ctx.globalAlpha = alpha * 0.55; ctx.drawImage(cv, 0, 0);
    ctx.filter = 'none'; ctx.globalAlpha = alpha; ctx.drawImage(cv, 0, 0);
    ctx.restore();
  };
  // One continuous sweep over the headline; the brush runs dry as it leaves the sheet.
  enso(ex, ey, er, er * 0.19, right ? 168 : 196, 322, c.stroke, parseFloat(c.strokeAlpha));

  // A breath of bare paper behind the headline keeps the type crisp over the ink.
  const k = cfg.copy;
  ctx.save();
  ctx.translate(k.x + k.w / 2, k.y + k.h * 0.64); ctx.scale(1, (k.h * 0.66) / (k.w * 0.60));
  const calm = ctx.createRadialGradient(0, 0, 0, 0, 0, k.w * 0.60);
  calm.addColorStop(0, rgba(c.paper, 0.55)); calm.addColorStop(0.60, rgba(c.paper, 0.34)); calm.addColorStop(1, rgba(c.paper, 0));
  ctx.fillStyle = calm; ctx.fillRect(-W, -H, W * 2, H * 2);
  ctx.restore();

  // Washi tooth: sparse mulberry fibres and a fine grain.
  ctx.lineCap = 'round';
  const fibres = Math.round((W * H) / 900);
  for (let i = 0; i < fibres; i++) {
    const x = rng() * W, y = rng() * H, a = rng() * Math.PI * 2;
    const len = S * (0.006 + Math.pow(rng(), 3) * 0.055);
    ctx.strokeStyle = rgba(c.fibre, between(0.08, night ? 0.22 : 0.34));
    ctx.lineWidth = between(0.3, 0.7);
    ctx.beginPath(); ctx.moveTo(x, y);
    ctx.quadraticCurveTo(x + Math.cos(a) * len * 0.5 + gauss() * len * 0.2, y + Math.sin(a) * len * 0.5 + gauss() * len * 0.2,
      x + Math.cos(a) * len, y + Math.sin(a) * len);
    ctx.stroke();
  }
  const grain = document.createElement('canvas'); grain.width = grain.height = 384;
  const gx = grain.getContext('2d'), gi = gx.createImageData(384, 384);
  for (let i = 0; i < gi.data.length; i += 4) {
    const v = rng() < 0.5 ? 0 : 255;
    gi.data[i] = gi.data[i + 1] = gi.data[i + 2] = v; gi.data[i + 3] = Math.floor(rng() * (night ? 11 : 13));
  }
  gx.putImageData(gi, 0, 0);
  // 2 px grain: per-pixel noise makes the iPad PNGs exceed the 8 MB store limit.
  ctx.save(); ctx.setTransform(1, 0, 0, 1, 0, 0); ctx.imageSmoothingEnabled = false; ctx.scale(2, 2);
  ctx.fillStyle = ctx.createPattern(grain, 'repeat'); ctx.fillRect(0, 0, W * dsf / 2, H * dsf / 2); ctx.restore();

  // WashiBG layer 3: the warm edge vignette (#645820 on paper, #0A0704 at night).
  radial(W / 2, H / 2, Math.max(W, H) * 0.34, Math.max(W, H) * 0.80, night ? '#0A0704' : '#645820', 0, night ? 0.55 : 0.17);
}
