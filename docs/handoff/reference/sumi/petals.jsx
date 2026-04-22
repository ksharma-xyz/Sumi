// Sumi — seasonal petal animation. Used on splash, win, paywall hero.
// Calm drift, 12-20 petals, seasonal palette auto-picked from current month.

function seasonOf(month) {
  // month: 0-11
  if (month >= 2 && month <= 4) return 'spring'; // Mar-May · sakura
  if (month >= 5 && month <= 7) return 'summer'; // Jun-Aug · leaf
  if (month >= 8 && month <= 10) return 'autumn'; // Sep-Nov · maple
  return 'winter'; // Dec-Feb · snow
}

const SEASON_PALETTE = {
  spring: { shape: 'sakura', colors: ['#F5CED6', '#E8A3B3', '#C97A8E', '#FBF6ED'], tint: '#A8342A' },
  summer: { shape: 'leaf',   colors: ['#7BA76C', '#5A8A4A', '#3F6E2F', '#B3C9A5'], tint: '#5A7A3A' },
  autumn: { shape: 'maple',  colors: ['#C9613A', '#A8342A', '#E8943A', '#7E2820'], tint: '#A8342A' },
  winter: { shape: 'snow',   colors: ['#F4ECE0', '#DDD4C4', '#C9B48A', '#EBE0CC'], tint: '#8A7560' },
};

// Petal SVG shapes — all 24×24 viewBox, stroke/fill driven by parent
function PetalShape({ shape, color, opacity = 1 }) {
  switch (shape) {
    case 'sakura':
      return (
        <svg viewBox="0 0 24 24" width="100%" height="100%" style={{ opacity }}>
          <path d="M12 4 C14 8, 18 8, 18 12 C18 16, 14 16, 12 20 C10 16, 6 16, 6 12 C6 8, 10 8, 12 4 Z"
                fill={color} />
          <path d="M12 9 C12.8 10, 13.6 10.5, 14 12" stroke="#A8342A" strokeWidth="0.3" fill="none" opacity="0.5"/>
        </svg>
      );
    case 'maple':
      return (
        <svg viewBox="0 0 24 24" width="100%" height="100%" style={{ opacity }}>
          <path d="M12 3 L13.5 8 L18 6 L15.5 10.5 L21 11 L16 13 L19 18 L13.5 15 L12 21 L10.5 15 L5 18 L8 13 L3 11 L8.5 10.5 L6 6 L10.5 8 Z"
                fill={color} />
        </svg>
      );
    case 'leaf':
      return (
        <svg viewBox="0 0 24 24" width="100%" height="100%" style={{ opacity }}>
          <path d="M12 3 C18 6, 20 14, 12 21 C4 14, 6 6, 12 3 Z" fill={color} />
          <path d="M12 4 L12 20" stroke="#3F6E2F" strokeWidth="0.5" opacity="0.4" />
        </svg>
      );
    case 'snow':
      return (
        <svg viewBox="0 0 24 24" width="100%" height="100%" style={{ opacity: opacity * 0.85 }}>
          <circle cx="12" cy="12" r="1.5" fill={color} />
          <circle cx="12" cy="12" r="4" fill={color} opacity="0.3" />
        </svg>
      );
  }
}

function SumiPetals({ count = 16, season, bounds = { w: 400, h: 800 } }) {
  const s = season || seasonOf(new Date().getMonth());
  const palette = SEASON_PALETTE[s];
  // Deterministic placement via seeded hash — consistent across renders per instance.
  const petals = React.useMemo(() => {
    return Array.from({ length: count }).map((_, i) => ({
      id: i,
      x: (i * 37 + 17) % 100,          // 0-100 %
      delay: (i * 2.3) % 12,
      duration: 14 + (i * 1.7) % 10,   // 14-24s
      sway: 20 + (i * 7) % 30,         // px
      size: 14 + (i * 3) % 18,         // 14-32px
      rotate: (i * 43) % 360,
      spin: ((i % 2 === 0) ? 1 : -1) * (60 + (i * 11) % 120),
      color: palette.colors[i % palette.colors.length],
      opacity: 0.5 + ((i * 13) % 40) / 100,
    }));
  }, [count, s]);

  return (
    <div aria-hidden style={{ position: 'absolute', inset: 0, overflow: 'hidden', pointerEvents: 'none' }}>
      <style>{`
        @keyframes sumi-drift-${s} {
          0%   { transform: translate3d(0, -20%, 0) rotate(0deg); opacity: 0; }
          10%  { opacity: var(--sumi-petal-o, 0.7); }
          50%  { transform: translate3d(var(--sumi-sway, 30px), 60vh, 0) rotate(var(--sumi-spin, 180deg)); }
          90%  { opacity: var(--sumi-petal-o, 0.7); }
          100% { transform: translate3d(calc(var(--sumi-sway, 30px) * -0.5), 120%, 0) rotate(calc(var(--sumi-spin, 180deg) * 2)); opacity: 0; }
        }
      `}</style>
      {petals.map(p => (
        <div key={p.id} style={{
          position: 'absolute',
          top: 0,
          left: `${p.x}%`,
          width: p.size,
          height: p.size,
          '--sumi-sway': `${p.sway}px`,
          '--sumi-spin': `${p.spin}deg`,
          '--sumi-petal-o': p.opacity,
          animation: `sumi-drift-${s} ${p.duration}s linear ${p.delay}s infinite`,
          willChange: 'transform, opacity',
        }}>
          <PetalShape shape={palette.shape} color={p.color} opacity={p.opacity} />
        </div>
      ))}
    </div>
  );
}

// Enso draw-on animation — a single brush ring that draws in one breath on mount.
function LogoEnsoDrawing({ size = 120, duration = 1600 }) {
  return (
    <div style={{ width: size, height: size }}>
      <style>{`
        @keyframes sumi-enso-draw {
          from { stroke-dashoffset: 360; }
          to   { stroke-dashoffset: 0; }
        }
        @keyframes sumi-enso-fade-in {
          from { opacity: 0; transform: scale(0.96); }
          to   { opacity: 1; transform: scale(1); }
        }
      `}</style>
      <svg viewBox="0 0 120 120" width={size} height={size} style={{ animation: `sumi-enso-fade-in 600ms ease-out both` }}>
        <defs>
          <filter id="enso-ink">
            <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="2" seed="3"/>
            <feDisplacementMap in="SourceGraphic" scale="1.2"/>
          </filter>
        </defs>
        <path d="M 60 15 A 45 45 0 1 1 59 15"
          fill="none" stroke="#1A1410" strokeWidth="6" strokeLinecap="round"
          filter="url(#enso-ink)"
          strokeDasharray="360"
          style={{ animation: `sumi-enso-draw ${duration}ms cubic-bezier(.22,1,.36,1) forwards` }} />
      </svg>
    </div>
  );
}

Object.assign(window, { SumiPetals, LogoEnsoDrawing, seasonOf, SEASON_PALETTE });
