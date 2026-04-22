// Sumi Sudoku board — the final, production-quality board component.
// Higher polish than the exploration version: ink-weight numerals,
// subtle grain on selection, proper 3x3 box separation with a thin double rule.

// Build a rendering-ready grid from SUMI_SAMPLE_PUZZLE: each cell has { v, given, notes }.
function sumiBuildCells() {
  const P = SUMI_SAMPLE_PUZZLE;
  const cells = P.given.map((row, r) => row.map((v, c) => {
    const key = `${r},${c}`;
    if (v !== 0) return { v, given: true, notes: null };
    if (P.user[key] != null) return { v: P.user[key], given: false, notes: null };
    return { v: 0, given: false, notes: P.notes[key] || null };
  }));
  const counts = [0,0,0,0,0,0,0,0,0,0];
  cells.forEach(row => row.forEach(cell => { if (cell.v) counts[cell.v]++; }));
  return { cells, counts, selected: { r: 4, c: 4 } };
}

const PUZZLE = sumiBuildCells();

function boxOf(r, c) { return Math.floor(r / 3) * 3 + Math.floor(c / 3); }

function SumiBoard({ cellSize = 38, selected, setSelected, sweep, conflictCell, dark = false, showNotes = true }) {
  const rows = PUZZLE.cells;
  const sel = selected ?? PUZZLE.selected;
  const selVal = sel ? rows[sel.r][sel.c].v : 0;
  const size = cellSize * 9;
  const c = dark ? SUMI.color.night : SUMI.color;

  return (
    <div style={{ position: 'relative', width: size, height: size, background: dark ? SUMI.color.night.paperWarm : SUMI.color.paperGlow }}>
      <div style={{ display: 'grid', gridTemplateColumns: `repeat(9, ${cellSize}px)`, gridTemplateRows: `repeat(9, ${cellSize}px)`, position: 'relative' }}>
        {rows.map((row, r) => row.map((cell, col) => {
          const isSel = sel && sel.r === r && sel.c === col;
          const inUnit = sel && (sel.r === r || sel.c === col || boxOf(sel.r, sel.c) === boxOf(r, col));
          const sameDigit = cell.v !== 0 && selVal !== 0 && cell.v === selVal && !isSel;
          const isConflict = conflictCell && conflictCell.r === r && conflictCell.c === col;
          return (
            <div key={`${r}-${col}`} onClick={() => setSelected && setSelected({ r, c: col })}
              style={{ width: cellSize, height: cellSize, cursor: 'pointer', position: 'relative', display: 'grid', placeItems: 'center',
                background: isConflict ? (dark ? 'rgba(232,74,62,0.22)' : 'rgba(168,52,42,0.1)')
                  : isSel ? (dark ? 'rgba(244,236,224,0.08)' : 'rgba(168,52,42,0.08)')
                  : sameDigit ? (dark ? 'rgba(111,168,188,0.14)' : 'rgba(42,90,110,0.08)')
                  : inUnit ? (dark ? 'rgba(244,236,224,0.025)' : 'rgba(26,20,16,0.03)')
                  : 'transparent' }}>
              {cell.v !== 0 && (
                <span style={{
                  fontFamily: cell.given ? SUMI.type.numeral : SUMI.type.hand,
                  fontSize: cell.given ? 22 : 24,
                  fontWeight: cell.given ? 600 : 500,
                  color: isConflict ? c.red : cell.given ? c.ink : c.teal,
                  letterSpacing: -0.5,
                }}>{cell.v}</span>
              )}
              {showNotes && cell.v === 0 && cell.notes && (
                <div style={{ position: 'absolute', inset: 3, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gridTemplateRows: 'repeat(3, 1fr)', placeItems: 'center', pointerEvents: 'none' }}>
                  {[1,2,3,4,5,6,7,8,9].map(n => (
                    <span key={n} style={{ fontFamily: SUMI.type.hand, fontSize: cellSize * 0.28, color: dark ? 'rgba(244,236,224,0.45)' : 'rgba(26,20,16,0.45)', lineHeight: 1 }}>
                      {cell.notes.includes(n) ? n : ''}
                    </span>
                  ))}
                </div>
              )}
            </div>
          );
        }))}
      </div>
      {/* grid lines */}
      <svg width={size} height={size} style={{ position: 'absolute', inset: 0, pointerEvents: 'none' }}>
        {[1,2,3,4,5,6,7,8].map(i => (
          <React.Fragment key={i}>
            <line x1={i*cellSize} y1={0} x2={i*cellSize} y2={size} stroke={c.ink} strokeWidth={i%3===0 ? 0 : 0.5} opacity={0.25} />
            <line y1={i*cellSize} x1={0} y2={i*cellSize} x2={size} stroke={c.ink} strokeWidth={i%3===0 ? 0 : 0.5} opacity={0.25} />
          </React.Fragment>
        ))}
        {[0,3,6,9].map(i => (
          <React.Fragment key={`b${i}`}>
            <line x1={i*cellSize} y1={0} x2={i*cellSize} y2={size} stroke={c.ink} strokeWidth={1.5} opacity={0.85} />
            <line y1={i*cellSize} x1={0} y2={i*cellSize} x2={size} stroke={c.ink} strokeWidth={1.5} opacity={0.85} />
          </React.Fragment>
        ))}
      </svg>
      {sweep === 'row' && <AuroraSweep kind="row" index={4} cellSize={cellSize} tone="paper" />}
      {sweep === 'col' && <AuroraSweep kind="col" index={4} cellSize={cellSize} tone="paper" />}
      {sweep === 'box' && <AuroraSweep kind="box" index={4} cellSize={cellSize} tone="paper" />}
    </div>
  );
}

Object.assign(window, { SumiBoard, PUZZLE, sumiBuildCells, boxOf });
