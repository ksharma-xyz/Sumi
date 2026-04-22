// Sumi shared primitives: WashiBG, InkBleed, BrushStroke, Seal, QuoteRule, Chip, Button

// Washi paper texture via SVG noise — scoped so it renders per-surface.
function WashiBG({ dark = false, intensity = 1, style = {} }) {
  const color = dark ? SUMI.color.night.paper : SUMI.color.paper;
  const warm  = dark ? SUMI.color.night.paperWarm : SUMI.color.paperWarm;
  return (
    <div style={{ position: 'absolute', inset: 0, overflow: 'hidden', background: color, ...style }}>
      <svg style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', opacity: 0.55 * intensity, mixBlendMode: dark ? 'screen' : 'multiply' }}>
        <filter id="wbg-fiber">
          <feTurbulence type="fractalNoise" baseFrequency="0.85" numOctaves="2" seed="3"/>
          <feColorMatrix values={dark
            ? "0 0 0 0 0.95  0 0 0 0 0.9  0 0 0 0 0.8  0 0 0 0.05 0"
            : "0 0 0 0 0.4  0 0 0 0 0.3  0 0 0 0 0.2  0 0 0 0.08 0"} />
        </filter>
        <rect width="100%" height="100%" filter="url(#wbg-fiber)"/>
      </svg>
      <svg style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', opacity: 0.3 * intensity, mixBlendMode: dark ? 'screen' : 'multiply' }}>
        <filter id="wbg-cloud">
          <feTurbulence type="fractalNoise" baseFrequency="0.012" numOctaves="2" seed="7"/>
          <feColorMatrix values={dark
            ? "0 0 0 0 0.7  0 0 0 0 0.6  0 0 0 0 0.5  0 0 0 0.2 0"
            : "0 0 0 0 0.5  0 0 0 0 0.4  0 0 0 0 0.25  0 0 0 0.25 0"} />
        </filter>
        <rect width="100%" height="100%" filter="url(#wbg-cloud)"/>
      </svg>
      <div style={{ position: 'absolute', inset: 0, boxShadow: dark ? 'inset 0 0 60px rgba(0,0,0,0.4)' : 'inset 0 0 80px rgba(100,70,30,0.18)' }} />
    </div>
  );
}

// InkBleed — organic ink blot, used as decorative accent.
let _bleedCounter = 0;
function InkBleed({ color = SUMI.color.ink, size = 80, seed, style = {}, opacity = 0.2 }) {
  const id = React.useMemo(() => `ink-${++_bleedCounter}-${seed || 0}`, [seed]);
  return (
    <svg width={size} height={size} viewBox="0 0 60 60" style={{ pointerEvents: 'none', ...style }}>
      <filter id={id}>
        <feTurbulence type="fractalNoise" baseFrequency="0.5" numOctaves="2" seed={seed || 3}/>
        <feDisplacementMap in="SourceGraphic" scale="5"/>
      </filter>
      <g filter={`url(#${id})`}>
        <circle cx="30" cy="30" r="18" fill={color} opacity={opacity*0.6} />
        <circle cx="30" cy="30" r="12" fill={color} opacity={opacity} />
        <circle cx="30" cy="30" r="6" fill={color} opacity={opacity*1.4} />
      </g>
    </svg>
  );
}

// BrushStroke — hand-painted underline / divider
function BrushStroke({ color = SUMI.color.ink, width = 120, height = 10, style = {} }) {
  return (
    <svg width={width} height={height} viewBox="0 0 120 10" style={style}>
      <path d="M1,5 Q20,2 40,5 T80,5 T119,4" stroke={color} strokeWidth="2.5" fill="none" strokeLinecap="round" opacity="0.9" />
      <path d="M3,6 Q22,4 42,6 T82,6 T117,5" stroke={color} strokeWidth="1" fill="none" strokeLinecap="round" opacity="0.4" />
    </svg>
  );
}

// Seal — red chop-stamp with kanji, for completion moments
function Seal({ kanji = '墨', size = 72, rotation = -5, style = {} }) {
  return (
    <div style={{
      width: size, height: size, borderRadius: 6,
      background: SUMI.color.red,
      display: 'grid', placeItems: 'center',
      transform: `rotate(${rotation}deg)`,
      boxShadow: SUMI.elevation.seal,
      position: 'relative',
      fontFamily: SUMI.type.cjk, color: SUMI.color.paper,
      fontSize: size * 0.52,
      ...style,
    }}>
      <div style={{ lineHeight: 1 }}>{kanji}</div>
      <div style={{ position: 'absolute', inset: 4, border: `1.5px solid ${SUMI.color.paper}`, borderRadius: 4, pointerEvents: 'none' }} />
      {/* scratchy noise overlay */}
      <svg style={{ position: 'absolute', inset: 0, borderRadius: 6, mixBlendMode: 'overlay', opacity: 0.4 }}>
        <filter id={`seal-n-${size}`}>
          <feTurbulence baseFrequency="0.9" numOctaves="2" />
        </filter>
        <rect width="100%" height="100%" filter={`url(#seal-n-${size})`} />
      </svg>
    </div>
  );
}

// QuoteRule — section divider with centered ornament
function QuoteRule({ ornament = '墨', color = SUMI.color.paperEdge, style = {} }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14, ...style }}>
      <div style={{ flex: 1, height: 1, background: color }} />
      <div style={{ fontFamily: SUMI.type.cjk, fontSize: 16, color: SUMI.color.inkFaint }}>{ornament}</div>
      <div style={{ flex: 1, height: 1, background: color }} />
    </div>
  );
}

// Button — 3 variants
function SumiButton({ children, variant = 'primary', size = 'md', onClick, style = {}, icon }) {
  const base = {
    fontFamily: SUMI.type.ui, fontWeight: 500, letterSpacing: 3, textTransform: 'uppercase',
    cursor: 'pointer', border: 'none', borderRadius: SUMI.radius.xs,
    display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8,
    transition: `background ${SUMI.motion.duration.short}ms ${SUMI.motion.ease.paper}`,
  };
  const sizes = {
    sm: { padding: '8px 14px', fontSize: 11 },
    md: { padding: '14px 20px', fontSize: 13 },
    lg: { padding: '18px 24px', fontSize: 14 },
  };
  const variants = {
    primary: { background: SUMI.color.ink, color: SUMI.color.paper, border: `1px solid ${SUMI.color.ink}` },
    ghost:   { background: 'transparent', color: SUMI.color.ink, border: `1px solid ${SUMI.color.ink}` },
    subtle:  { background: SUMI.color.paperWarm, color: SUMI.color.ink, border: `1px solid ${SUMI.color.paperEdge}` },
    red:     { background: SUMI.color.red, color: SUMI.color.paper, border: `1px solid ${SUMI.color.red}` },
  };
  return (
    <button onClick={onClick} style={{ ...base, ...sizes[size], ...variants[variant], ...style }}>
      {icon}{children}
    </button>
  );
}

// Chip
function SumiChip({ children, tone = 'ink', style = {} }) {
  const tones = {
    ink:  { color: SUMI.color.ink,  border: SUMI.color.ink },
    red:  { color: SUMI.color.red,  border: SUMI.color.red },
    teal: { color: SUMI.color.teal, border: SUMI.color.teal },
    gold: { color: SUMI.color.gold, border: SUMI.color.gold },
    muted:{ color: SUMI.color.inkFaint, border: SUMI.color.paperEdge },
  }[tone];
  return (
    <div style={{
      padding: '3px 10px', border: `1px solid ${tones.border}`, color: tones.color,
      fontFamily: SUMI.type.ui, fontSize: 10, fontWeight: 600, letterSpacing: 2,
      textTransform: 'uppercase', borderRadius: SUMI.radius.xs, display: 'inline-block',
      background: 'transparent',
      ...style,
    }}>{children}</div>
  );
}

// Section header with brush stroke underline
function SumiEyebrow({ children, color = SUMI.color.red, style = {} }) {
  return (
    <div style={{ ...style }}>
      <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, fontWeight: 700, letterSpacing: 3, color, textTransform: 'uppercase' }}>{children}</div>
    </div>
  );
}

Object.assign(window, { WashiBG, InkBleed, BrushStroke, Seal, QuoteRule, SumiButton, SumiChip, SumiEyebrow });
