// SUMI — Design system tokens.
// Single source of truth. Import this in every component file.

const SUMI = {
  // ── Color tokens ───────────────────────────────────────────
  color: {
    // Paper (light mode — day)
    paper:        '#F4ECE0',  // primary surface
    paperWarm:    '#EBE0CC',  // secondary surface / panels
    paperDeep:    '#DFD1B6',  // tertiary (washi-darkened)
    paperEdge:    '#C9B48A',  // seam / divider / stitch
    paperGlow:    '#FBF6ED',  // lifted surfaces

    // Ink (primary foreground)
    ink:          '#1A1410',  // primary text, strong borders
    inkSoft:      '#5A4838',  // secondary text, captions
    inkFaint:     '#8A7560',  // tertiary text, placeholders
    inkGhost:     '#B3A088',  // disabled text

    // Accents (used sparingly)
    red:          '#A8342A',  // chop-stamp, completion, today
    redDeep:      '#7E2820',  // red seal shadow
    teal:         '#2A5A6E',  // user-entered numbers (like fountain-pen ink)
    tealSoft:     '#4A7A8E',
    gold:         '#8A6B2A',  // premium / accent moments
    goldLight:    '#C49A4A',

    // States
    success:      '#5A7A3A',  // soft moss
    warning:      '#A8632A',  // burnt sienna
    error:        '#A8342A',  // same as red
    hint:         '#8A6B2A',  // gold

    // Night mode — Sumi on black paper
    night: {
      paper:      '#1A1410',  // still 'paper' semantically
      paperWarm:  '#241C14',
      paperDeep:  '#2D241A',
      paperEdge:  '#3D3022',
      ink:        '#F4ECE0',  // inverted
      inkSoft:    '#B3A088',
      inkFaint:   '#8A7560',
      inkGhost:   '#5A4838',
      red:        '#E84A3E',
      teal:       '#6FA8BC',
      gold:       '#D9A855',
    },
  },

  // ── Type scale ─────────────────────────────────────────────
  // Three families, mapped roles. Load from Google Fonts.
  type: {
    // Display serif — headlines, large numerics, quotes
    display: '"Cormorant Garamond", "Source Serif Pro", Georgia, serif',
    // Body serif — readable long-form, editorial
    body:    '"Source Serif 4", "Source Serif Pro", Georgia, serif',
    // UI sans — chrome, buttons, tabs, stats (purposeful neutral)
    ui:      '"Söhne", "Inter", -apple-system, sans-serif',
    // Sudoku numerals — distinctive, readable at small sizes
    numeral: '"Cormorant Garamond", Georgia, serif',
    // User-entered marks (warm, handwritten)
    hand:    '"Caveat", "Kalam", cursive',
    // CJK / decorative (kanji chops, section markers)
    cjk:     '"Shippori Mincho", "Noto Serif JP", serif',
  },
  // Size scale — mobile-first, rem-equivalent in px
  size: {
    caption: 11,
    small:   12,
    body:    15,
    bodyLg:  17,
    subhead: 20,
    h3:      26,
    h2:      34,
    h1:      46,
    display: 62,
    hero:    88,
  },
  weight: {
    light: 300, regular: 400, medium: 500, semi: 600, bold: 700,
  },
  track: { // letter-spacing
    tight: -1.5, snug: -0.5, normal: 0, wide: 1.5, wider: 3, widest: 4,
  },

  // ── Space / radius / elevation ─────────────────────────────
  space: { 0:0, 1:4, 2:8, 3:12, 4:16, 5:20, 6:24, 7:32, 8:40, 9:48, 10:64, 11:80 },
  radius: {
    none: 0,  // sharp, editorial default
    xs: 2,    // most surfaces — matches washi paper edges
    sm: 4,    // cells, chips
    md: 8,    // buttons
    lg: 12,   // sheets
    pill: 9999,
  },
  // Shadows are SUBTLE. This is paper, not Material.
  elevation: {
    e0: 'none',
    e1: '0 1px 0 rgba(26,20,16,0.08)',                          // paper seam
    e2: '0 2px 8px rgba(26,20,16,0.08)',                        // lifted card
    e3: '0 6px 20px rgba(26,20,16,0.10), 0 2px 4px rgba(26,20,16,0.06)',  // floating sheet
    e4: '0 14px 40px rgba(26,20,16,0.14), 0 4px 10px rgba(26,20,16,0.08)', // modal
    seal: '0 4px 0 rgba(120,40,30,0.35), 0 10px 24px rgba(168,52,42,0.25)', // chop-stamp
  },

  // ── Motion tokens ──────────────────────────────────────────
  // "Breath" — everything moves like wet ink settling on paper.
  // Use ease-out curves that start quick and settle.
  motion: {
    duration: {
      brief: 160,      // small taps, highlights
      short: 260,      // button presses, chip swaps
      base:  380,      // screen chrome, sheet rise
      breath:560,      // ink bleed settle, quote fade
      slow:  900,      // celebrations in / out
      ceremony:1600,   // aurora sweep, win bloom
    },
    ease: {
      paper:  'cubic-bezier(0.22, 0.75, 0.28, 1)',   // default — settles softly
      brush:  'cubic-bezier(0.65, 0, 0.35, 1)',      // sharper stroke
      bleed:  'cubic-bezier(0.16, 0.84, 0.24, 1)',   // ink spreading
      snap:   'cubic-bezier(0.5, -0.2, 0.3, 1.4)',   // confirmation ping
    },
  },

  // ── Layout tokens ──────────────────────────────────────────
  layout: {
    screenPadX: 24,     // generous side margin — editorial whitespace
    screenPadY: 28,
    cellSize: 38,       // sudoku cell on iPhone
    minTap: 44,
  },
};

// Quote library — swap daily. Tone: quiet, contemplative, craft-focused.
// Mix of real attributed and original "house" quotes; the app should cycle
// deterministically by day-of-year.
const SUMI_QUOTES = [
  { q: 'A line drawn once, with certainty, is worth a thousand redrawn.', a: 'Unsui, 1812' },
  { q: 'The quieter you become, the more you can hear.', a: 'Ram Dass' },
  { q: 'Nothing in excess.', a: 'Delphic Maxim' },
  { q: 'The master begins where the student finishes.', a: '—' },
  { q: 'What is essential is invisible to the eye.', a: 'Saint-Exupéry' },
  { q: 'Nine lines. Nine columns. One patience.', a: 'House' },
  { q: 'Empty space is not empty. It is waiting.', a: 'House' },
  { q: 'Make haste slowly.', a: 'Augustus' },
  { q: 'The brush lifts; the mind descends.', a: 'House' },
  { q: 'Do the difficult things while they are easy.', a: 'Lao Tzu' },
  { q: 'Simplicity is the ultimate sophistication.', a: 'da Vinci' },
  { q: 'A puzzle is a conversation with its maker.', a: 'House' },
  { q: 'Mistakes are the ink the solution is written in.', a: 'House' },
];

Object.assign(window, { SUMI, SUMI_QUOTES });
