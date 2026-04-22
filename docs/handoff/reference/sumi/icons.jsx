// Sumi icon library — brush-line icon system at 24px.
// All icons use the same 1.6px stroke weight, round caps, subtle organic jitter.

const ICON_STROKE = 1.8;

function SumiIcon({ children, size = 24, color = 'currentColor', style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
      stroke={color} strokeWidth={ICON_STROKE} strokeLinecap="round" strokeLinejoin="round"
      style={style}>
      {children}
    </svg>
  );
}

// — Navigation —
const IconBack     = (p) => <SumiIcon {...p}><path d="M15 5l-7 7 7 7"/></SumiIcon>;
const IconClose    = (p) => <SumiIcon {...p}><path d="M6 6l12 12M18 6L6 18"/></SumiIcon>;
const IconMenu     = (p) => <SumiIcon {...p}><path d="M4 7h16M4 12h16M4 17h16"/></SumiIcon>;
const IconMore     = (p) => <SumiIcon {...p}><circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/></SumiIcon>;

// — Game —
const IconPause    = (p) => <SumiIcon {...p}><path d="M8 5v14M16 5v14"/></SumiIcon>;
const IconPlay     = (p) => <SumiIcon {...p}><path d="M7 5l12 7-12 7z"/></SumiIcon>;
const IconUndo     = (p) => <SumiIcon {...p}><path d="M9 14l-5-5 5-5"/><path d="M4 9h10a6 6 0 016 6"/></SumiIcon>;
const IconErase    = (p) => <SumiIcon {...p}><path d="M18 13l-6 6H7l-3-3 9-9 5 5z"/><path d="M9 9l5 5"/></SumiIcon>;
const IconBrush    = (p) => <SumiIcon {...p}><path d="M7 21c-2 0-3-1-3-3s2-3 4-3"/><path d="M8 15l8-10 3 3-10 8"/><path d="M14 7l3 3"/></SumiIcon>;
const IconLantern  = (p) => <SumiIcon {...p}><path d="M12 3v2"/><path d="M7 7h10v6a5 5 0 01-10 0z"/><path d="M7 13h10"/><path d="M12 18v3"/></SumiIcon>;
const IconNote     = (p) => <SumiIcon {...p}><circle cx="7" cy="7" r="1"/><circle cx="12" cy="7" r="1"/><circle cx="17" cy="7" r="1"/><circle cx="7" cy="12" r="1"/><circle cx="17" cy="12" r="1"/><circle cx="7" cy="17" r="1"/><circle cx="12" cy="17" r="1"/><circle cx="17" cy="17" r="1"/></SumiIcon>;

// — Stats & progress —
const IconFlame    = (p) => <SumiIcon {...p}><path d="M12 3c1 3 4 5 4 9a4 4 0 01-8 0c0-2 1-3 2-4-2-1-2-3 2-5z"/></SumiIcon>;
const IconCalendar = (p) => <SumiIcon {...p}><rect x="4" y="5" width="16" height="15" rx="1"/><path d="M4 9h16M9 3v4M15 3v4"/></SumiIcon>;
const IconChart    = (p) => <SumiIcon {...p}><path d="M4 19V5M4 19h16"/><path d="M7 15l3-4 3 2 5-7"/></SumiIcon>;
const IconTrophy   = (p) => <SumiIcon {...p}><path d="M7 4h10v5a5 5 0 01-10 0z"/><path d="M7 6H4v2a3 3 0 003 3M17 6h3v2a3 3 0 01-3 3"/><path d="M10 14v3H8v2h8v-2h-2v-3"/></SumiIcon>;
const IconCheck    = (p) => <SumiIcon {...p}><path d="M5 12l5 5 9-11"/></SumiIcon>;

// — Shelf —
const IconBook     = (p) => <SumiIcon {...p}><path d="M5 4h10a3 3 0 013 3v13H8a3 3 0 01-3-3z"/><path d="M5 17a3 3 0 013-3h10"/></SumiIcon>;
const IconQuote    = (p) => <SumiIcon {...p}><path d="M6 11V8c0-2 1-3 3-3"/><path d="M14 11V8c0-2 1-3 3-3"/><path d="M6 11h3v5H6zM14 11h3v5h-3z"/></SumiIcon>;
const IconSettings = (p) => <SumiIcon {...p}><circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M4 12H2M22 12h-3M5 5l2 2M17 17l2 2M5 19l2-2M17 7l2-2"/></SumiIcon>;
const IconShare    = (p) => <SumiIcon {...p}><path d="M12 4v13M12 4l-4 4M12 4l4 4"/><path d="M5 15v3a2 2 0 002 2h10a2 2 0 002-2v-3"/></SumiIcon>;
const IconSparkle  = (p) => <SumiIcon {...p}><path d="M12 4v4M12 16v4M4 12h4M16 12h4M6 6l3 3M15 15l3 3M6 18l3-3M15 9l3-3"/></SumiIcon>;
const IconHeart    = (p) => <SumiIcon {...p}><path d="M12 19s-7-4-7-10a4 4 0 017-2 4 4 0 017 2c0 6-7 10-7 10z"/></SumiIcon>;
const IconLock     = (p) => <SumiIcon {...p}><rect x="5" y="11" width="14" height="9" rx="1"/><path d="M8 11V8a4 4 0 018 0v3"/></SumiIcon>;
const IconSound    = (p) => <SumiIcon {...p}><path d="M5 10v4h3l4 4V6l-4 4z"/><path d="M16 8a5 5 0 010 8"/></SumiIcon>;

Object.assign(window, {
  SumiIcon, IconBack, IconClose, IconMenu, IconMore, IconPause, IconPlay,
  IconUndo, IconErase, IconBrush, IconLantern, IconNote, IconFlame, IconCalendar,
  IconChart, IconTrophy, IconCheck, IconBook, IconQuote, IconSettings, IconShare,
  IconSparkle, IconHeart, IconLock, IconSound,
});
