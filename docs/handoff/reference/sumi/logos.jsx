// Sumi logos — 5 mark variations + wordmark + app icon.
// All drawn purely in SVG so you can copy the <svg> into Figma,
// or download individual files from the /logos page.

// 1. Enso mark — the zen brush circle with a '9' hidden in it
function LogoEnso({ size = 120, color = SUMI.color.ink, style = {} }) {
  const id = 'enso-' + size;
  return (
    <svg width={size} height={size} viewBox="0 0 120 120" style={style}>
      <defs>
        <filter id={id}>
          <feTurbulence type="fractalNoise" baseFrequency="0.8" numOctaves="2" seed="2"/>
          <feDisplacementMap in="SourceGraphic" scale="2"/>
        </filter>
      </defs>
      {/* Enso - incomplete brush circle */}
      <path d="M 95,30 A 42,42 0 1,0 82,92 Q 72,94 60,92"
        fill="none" stroke={color} strokeWidth="11" strokeLinecap="round" filter={`url(#${id})`} />
      {/* tail drip */}
      <circle cx="60" cy="94" r="3" fill={color} opacity="0.7" />
      <circle cx="66" cy="99" r="1.5" fill={color} opacity="0.5" />
    </svg>
  );
}

// 2. Nine-dot grid mark — 3x3 of ink dots, one missing (the puzzle)
function LogoGrid({ size = 120, color = SUMI.color.ink, accent = SUMI.color.red, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 120 120" style={style}>
      {[0,1,2].map(r => [0,1,2].map(c => {
        const isAccent = r === 1 && c === 1;
        const isEmpty = r === 2 && c === 0;
        return (
          <circle key={`${r}-${c}`}
            cx={30 + c*30} cy={30 + r*30} r={isEmpty ? 5 : 9}
            fill={isEmpty ? 'none' : (isAccent ? accent : color)}
            stroke={isEmpty ? color : 'none'}
            strokeWidth={isEmpty ? 2 : 0}
            strokeDasharray={isEmpty ? '2 3' : 'none'}
          />
        );
      }))}
    </svg>
  );
}

// 3. Kanji chop — 墨 (sumi/ink) in a red square seal
function LogoChop({ size = 120, bg = SUMI.color.red, fg = SUMI.color.paper, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 120 120" style={style}>
      <rect x="8" y="8" width="104" height="104" rx="6" fill={bg} />
      <rect x="14" y="14" width="92" height="92" rx="3" fill="none" stroke={fg} strokeWidth="2" />
      <text x="60" y="84" textAnchor="middle"
        fontFamily='"Shippori Mincho", "Noto Serif JP", serif'
        fontSize="70" fontWeight="600" fill={fg}>墨</text>
    </svg>
  );
}

// 4. Brush-9 — a hand-drawn numeral 9 as the whole mark
function LogoNine({ size = 120, color = SUMI.color.ink, style = {} }) {
  const id = 'nine-' + size;
  return (
    <svg width={size} height={size} viewBox="0 0 120 120" style={style}>
      <defs>
        <filter id={id}>
          <feTurbulence type="fractalNoise" baseFrequency="0.7" numOctaves="2" seed="5"/>
          <feDisplacementMap in="SourceGraphic" scale="2.5"/>
        </filter>
      </defs>
      {/* Brushed 9 */}
      <path d="M 75,30 A 25,25 0 1,0 75,62 L 75,92 Q 75,100 68,102"
        fill="none" stroke={color} strokeWidth="14" strokeLinecap="round" strokeLinejoin="round"
        filter={`url(#${id})`} />
      <circle cx="68" cy="102" r="3" fill={color} opacity="0.8" />
    </svg>
  );
}

// 5. Minimal — two brush strokes forming an elegant 'S'
function LogoStrokes({ size = 120, color = SUMI.color.ink, accent = SUMI.color.red, style = {} }) {
  const id = 'strokes-' + size;
  return (
    <svg width={size} height={size} viewBox="0 0 120 120" style={style}>
      <defs>
        <filter id={id}>
          <feTurbulence type="fractalNoise" baseFrequency="0.6" numOctaves="2" seed="9"/>
          <feDisplacementMap in="SourceGraphic" scale="2"/>
        </filter>
      </defs>
      <path d="M 22,34 Q 60,18 98,42" stroke={color} strokeWidth="14" fill="none" strokeLinecap="round" filter={`url(#${id})`} />
      <path d="M 22,86 Q 60,66 98,94" stroke={color} strokeWidth="14" fill="none" strokeLinecap="round" filter={`url(#${id})`} />
      <circle cx="60" cy="60" r="5" fill={accent} />
    </svg>
  );
}

// Wordmark — "Sumi" with brushy italic serif + kanji subscript
function LogoWordmark({ size = 1, color = SUMI.color.ink, accent = SUMI.color.red, style = {} }) {
  return (
    <div style={{ display: 'inline-flex', alignItems: 'baseline', gap: 12 * size, fontFamily: SUMI.type.display, ...style }}>
      <div style={{ fontSize: 56 * size, fontWeight: 500, fontStyle: 'italic', letterSpacing: -1 * size, color, lineHeight: 1 }}>Sumi</div>
      <div style={{ fontSize: 22 * size, fontFamily: SUMI.type.cjk, color: accent, fontWeight: 500 }}>墨</div>
    </div>
  );
}

// App icon — square composition at 1024x1024 spec
function LogoAppIcon({ size = 180, variant = 'paper', style = {} }) {
  const bg = variant === 'paper' ? SUMI.color.paper : variant === 'ink' ? SUMI.color.ink : SUMI.color.red;
  const fg = variant === 'paper' ? SUMI.color.ink : SUMI.color.paper;
  const accent = variant === 'red' ? SUMI.color.paper : SUMI.color.red;
  return (
    <div style={{
      width: size, height: size, borderRadius: size * 0.22,
      background: bg, position: 'relative', overflow: 'hidden',
      boxShadow: '0 8px 24px rgba(26,20,16,0.2)',
      ...style,
    }}>
      {/* paper texture */}
      {variant === 'paper' && (
        <svg style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', opacity: 0.4, mixBlendMode: 'multiply' }}>
          <filter id={`appicon-${size}`}><feTurbulence baseFrequency="0.85" numOctaves="2"/><feColorMatrix values="0 0 0 0 0.4 0 0 0 0 0.3 0 0 0 0 0.2 0 0 0 0.1 0"/></filter>
          <rect width="100%" height="100%" filter={`url(#appicon-${size})`} />
        </svg>
      )}
      {/* mark, centered */}
      <div style={{ position: 'absolute', inset: 0, display: 'grid', placeItems: 'center' }}>
        <LogoEnso size={size * 0.6} color={fg} />
      </div>
      {/* tiny red seal in corner */}
      <div style={{ position: 'absolute', bottom: size*0.08, right: size*0.08, width: size*0.14, height: size*0.14, borderRadius: 2, background: accent, display: 'grid', placeItems: 'center', fontFamily: SUMI.type.cjk, color: bg, fontSize: size*0.09, fontWeight: 600 }}>墨</div>
    </div>
  );
}

Object.assign(window, { LogoEnso, LogoGrid, LogoChop, LogoNine, LogoStrokes, LogoWordmark, LogoAppIcon });
