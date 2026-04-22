// Sumi — AuroraSweep: the watercolour band that traverses a completed row/col/box.
// Five-hue gradient, slow brush-out easing, multiply blend so it reads as ink on paper.

const AURORA_HUES_PAPER = [
  'rgba(168, 52, 42, 0.22)',   // red
  'rgba(138, 107, 42, 0.22)',  // gold
  'rgba(42, 90, 110, 0.22)',   // teal
  'rgba(90, 122, 58, 0.18)',   // moss
  'rgba(122, 90, 138, 0.18)',  // plum
];

const AURORA_HUES_DARK = [
  'rgba(232, 74, 62, 0.28)',
  'rgba(217, 168, 85, 0.28)',
  'rgba(111, 168, 188, 0.28)',
  'rgba(148, 186, 102, 0.24)',
  'rgba(188, 148, 212, 0.24)',
];

function AuroraSweep({ kind = 'row', index = 0, cellSize = 38, tone = 'paper' }) {
  const hues = tone === 'paper' ? AURORA_HUES_PAPER : AURORA_HUES_DARK;
  const gradient = `linear-gradient(90deg, transparent 0%, ${hues[0]} 18%, ${hues[1]} 36%, ${hues[2]} 54%, ${hues[3]} 72%, ${hues[4]} 86%, transparent 100%)`;

  // Row: horizontal band 1 cell high, translateX
  // Col: rotate 90deg, translateY
  // Box: radial bloom from center
  const uniqueId = `aurora-${kind}-${index}-${Math.random().toString(36).slice(2, 7)}`;
  const duration = kind === 'box' ? '1000ms' : '1200ms';

  if (kind === 'row') {
    return (
      <>
        <style>{`
          @keyframes ${uniqueId} {
            0%   { transform: translateX(-110%); opacity: 0; }
            15%  { opacity: 1; }
            85%  { opacity: 1; }
            100% { transform: translateX(110%); opacity: 0; }
          }
        `}</style>
        <div style={{
          position: 'absolute',
          left: 0, right: 0,
          top: index * cellSize,
          height: cellSize,
          background: gradient,
          mixBlendMode: 'multiply',
          animation: `${uniqueId} ${duration} cubic-bezier(.22, 1, .36, 1) forwards`,
          pointerEvents: 'none',
        }} />
      </>
    );
  }

  if (kind === 'col') {
    return (
      <>
        <style>{`
          @keyframes ${uniqueId} {
            0%   { transform: translateY(110%); opacity: 0; }
            15%  { opacity: 1; }
            85%  { opacity: 1; }
            100% { transform: translateY(-110%); opacity: 0; }
          }
        `}</style>
        <div style={{
          position: 'absolute',
          top: 0, bottom: 0,
          left: index * cellSize,
          width: cellSize,
          background: gradient.replace('90deg', '180deg'),
          mixBlendMode: 'multiply',
          animation: `${uniqueId} ${duration} cubic-bezier(.22, 1, .36, 1) forwards`,
          pointerEvents: 'none',
        }} />
      </>
    );
  }

  // Box: 3x3 radial bloom. index is 0..8 (boxIndex).
  const boxR = Math.floor(index / 3);
  const boxC = index % 3;
  return (
    <>
      <style>{`
        @keyframes ${uniqueId} {
          0%   { transform: scale(0.3); opacity: 0; }
          20%  { opacity: 1; }
          100% { transform: scale(1.8); opacity: 0; }
        }
      `}</style>
      <div style={{
        position: 'absolute',
        left: boxC * cellSize * 3,
        top: boxR * cellSize * 3,
        width: cellSize * 3,
        height: cellSize * 3,
        background: `radial-gradient(circle at 50% 50%, ${hues[0]}, ${hues[1]} 30%, ${hues[2]} 50%, ${hues[3]} 70%, transparent 100%)`,
        mixBlendMode: 'multiply',
        animation: `${uniqueId} ${duration} cubic-bezier(.22, 1, .36, 1) forwards`,
        pointerEvents: 'none',
      }} />
    </>
  );
}

// Sample puzzle — one medium-difficulty grid, used by the design system preview board.
const SUMI_SAMPLE_PUZZLE = {
  given: [
    [5,3,0,0,7,0,0,0,0],
    [6,0,0,1,9,5,0,0,0],
    [0,9,8,0,0,0,0,6,0],
    [8,0,0,0,6,0,0,0,3],
    [4,0,0,8,0,3,0,0,1],
    [7,0,0,0,2,0,0,0,6],
    [0,6,0,0,0,0,2,8,0],
    [0,0,0,4,1,9,0,0,5],
    [0,0,0,0,8,0,0,7,9],
  ],
  user: {
    '0,2': 4,
    '1,6': 8,
    '2,3': 3,
    '5,7': 9,
    '7,7': 6,
  },
  notes: {
    '3,1': [1, 2, 5],
    '4,1': [2, 5, 6],
    '4,7': [2, 5, 7],
  },
};

Object.assign(window, { AuroraSweep, SUMI_SAMPLE_PUZZLE, AURORA_HUES_PAPER, AURORA_HUES_DARK });
