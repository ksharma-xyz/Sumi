// Sumi — Seasonal app-start animation.
// Subtle, brief: a few falling elements on splash, season-aware.
// Spring: cherry petals (sakura). Summer: ink drops. Autumn: maple leaves. Winter: snow.

function currentSeason() {
  const m = new Date().getMonth() + 1; // 1-12
  if (m >= 3 && m <= 5) return 'spring';
  if (m >= 6 && m <= 8) return 'summer';
  if (m >= 9 && m <= 11) return 'autumn';
  return 'winter';
}

const SEASON_PALETTE = {
  spring: { color: '#E8B8C2', shape: 'petal',  count: 14, label: '春 · Spring' }, // sakura
  summer: { color: '#2A5A6E', shape: 'drop',   count: 10, label: '夏 · Summer' }, // ink
  autumn: { color: '#A8632A', shape: 'maple',  count: 12, label: '秋 · Autumn' }, // maple
  winter: { color: '#E8DCC5', shape: 'snow',   count: 18, label: '冬 · Winter' }, // snow
};

// Shape renderers — small SVGs around a 0,0 center.
function SeasonShape({ kind, size = 14, color }) {
  if (kind === 'petal') {
    return (
      <svg width={size} height={size*1.2} viewBox="-10 -12 20 24">
        <path d="M0,-10 C5,-8 6,-2 5,3 C4,7 1,10 0,10 C-1,10 -4,7 -5,3 C-6,-2 -5,-8 0,-10 Z"
          fill={color} opacity="0.85"/>
        <path d="M0,-10 C5,-8 6,-2 5,3 C4,7 1,10 0,10" stroke="#C68798" strokeWidth="0.4" fill="none" opacity="0.6"/>
      </svg>
    );
  }
  if (kind === 'drop') {
    return (
      <svg width={size} height={size*1.3} viewBox="-6 -8 12 16">
        <path d="M0,-7 C4,-2 4,4 0,7 C-4,4 -4,-2 0,-7 Z" fill={color} opacity="0.7"/>
      </svg>
    );
  }
  if (kind === 'maple') {
    return (
      <svg width={size*1.2} height={size*1.2} viewBox="-12 -12 24 24">
        <path d="M0,-11 L3,-4 L10,-5 L5,0 L10,5 L3,4 L0,11 L-3,4 L-10,5 L-5,0 L-10,-5 L-3,-4 Z"
          fill={color} opacity="0.8"/>
      </svg>
    );
  }
  // snow
  return (
    <svg width={size} height={size} viewBox="-8 -8 16 16">
      <g stroke={color} strokeWidth="0.8" strokeLinecap="round" opacity="0.8">
        <line x1="0" y1="-6" x2="0" y2="6"/>
        <line x1="-5" y1="-3" x2="5" y2="3"/>
        <line x1="-5" y1="3" x2="5" y2="-3"/>
      </g>
    </svg>
  );
}

// Single falling particle. Random column + delay + rotation + sway.
function Particle({ season, i, total, viewport }) {
  const { shape, color } = SEASON_PALETTE[season];
  const left = (i / total) * 100 + (Math.sin(i * 13.7) * 10);
  const delay = (i * 0.35) + Math.abs(Math.cos(i * 7.3)) * 1.2;
  const duration = 7 + Math.abs(Math.sin(i * 3.1)) * 5;   // 7-12s
  const size = 10 + Math.abs(Math.cos(i * 5.9)) * 8;      // 10-18
  const spin = Math.sin(i * 2.1) * 360;
  const sway = 20 + Math.abs(Math.sin(i * 4.3)) * 30;
  const animName = `sumi-fall-${i}-${season}`;
  const css = `
    @keyframes ${animName} {
      0%   { transform: translate3d(0, -10%, 0) rotate(0deg); opacity: 0; }
      10%  { opacity: 0.9; }
      50%  { transform: translate3d(${sway}px, 55%, 0) rotate(${spin/2}deg); }
      90%  { opacity: 0.6; }
      100% { transform: translate3d(${-sway*0.7}px, 110%, 0) rotate(${spin}deg); opacity: 0; }
    }
  `;
  return (
    <>
      <style>{css}</style>
      <div style={{
        position: 'absolute',
        left: `${left}%`,
        top: 0,
        pointerEvents: 'none',
        animation: `${animName} ${duration}s ${delay}s cubic-bezier(.4,0,.4,1) infinite`,
        willChange: 'transform, opacity',
      }}>
        <SeasonShape kind={shape} size={size} color={color} />
      </div>
    </>
  );
}

function SumiSeasonalRain({ season, count, style = {} }) {
  const s = season || currentSeason();
  const n = count ?? SEASON_PALETTE[s].count;
  return (
    <div style={{ position: 'absolute', inset: 0, overflow: 'hidden', pointerEvents: 'none', ...style }}>
      {Array.from({ length: n }).map((_, i) => (
        <Particle key={`${s}-${i}`} season={s} i={i} total={n} />
      ))}
    </div>
  );
}

// Enso draw-on animation — stroke-dasharray trick
function AnimatedEnso({ size = 120, delay = 0 }) {
  const id = `enso-${delay}`;
  return (
    <svg width={size} height={size} viewBox="0 0 200 200" style={{ filter: 'blur(0.3px)' }}>
      <style>{`
        @keyframes ${id}-draw {
          0%   { stroke-dashoffset: 520; opacity: 0; }
          15%  { opacity: 1; }
          100% { stroke-dashoffset: 0; opacity: 1; }
        }
        @keyframes ${id}-fade {
          0%, 85% { opacity: 1; }
          100%    { opacity: 1; }
        }
        .${id}-stroke {
          stroke-dasharray: 520;
          stroke-dashoffset: 520;
          animation: ${id}-draw 2s ${delay}s cubic-bezier(.4,.1,.3,1) forwards;
        }
      `}</style>
      <path
        className={`${id}-stroke`}
        d="M 100,20
           C 145,20 180,55 180,100
           C 180,145 145,180 100,180
           C 55,180 20,145 20,100
           C 20,60 55,25 95,20"
        fill="none"
        stroke="#1A1410"
        strokeWidth="11"
        strokeLinecap="round"
      />
    </svg>
  );
}

// Splash that uses both: enso draws, wordmark fades, petals drift.
function AnimatedSplash({ season, dark = false }) {
  const s = season || currentSeason();
  const bg = dark ? '#1A1410' : '#F4ECE0';
  const ink = dark ? '#F4ECE0' : '#1A1410';
  const soft = dark ? '#B3A088' : '#8A7560';
  return (
    <div style={{ position: 'absolute', inset: 0, background: bg, overflow: 'hidden' }}>
      <WashiBG dark={dark} />
      <SumiSeasonalRain season={s} />
      <div style={{ position: 'absolute', inset: 0, display: 'grid', placeItems: 'center' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ filter: dark ? 'invert(1)' : 'none' }}>
            <AnimatedEnso size={120} />
          </div>
          <div style={{
            fontFamily: 'Cormorant Garamond, serif',
            fontSize: 56, fontStyle: 'italic', fontWeight: 500, letterSpacing: -1.5,
            color: ink, marginTop: 8,
            opacity: 0, animation: 'sumi-fadein 1s 1.6s forwards',
          }}>Sumi</div>
          <div style={{
            fontFamily: 'Inter, sans-serif',
            fontSize: 10, letterSpacing: 4, color: soft, marginTop: 6, textTransform: 'uppercase',
            opacity: 0, animation: 'sumi-fadein 1s 2.2s forwards',
          }}>{SEASON_PALETTE[s].label} · A Quiet Practice</div>
        </div>
      </div>
      <style>{`
        @keyframes sumi-fadein { to { opacity: 1; } }
      `}</style>
    </div>
  );
}

Object.assign(window, { currentSeason, SEASON_PALETTE, SeasonShape, SumiSeasonalRain, AnimatedEnso, AnimatedSplash });
