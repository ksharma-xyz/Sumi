// Sumi — App screens. Each screen is a self-contained composition
// rendering inside the iOS frame. All use SUMI tokens + primitives.

const Px = { sx: SUMI.layout.screenPadX };

// ─────────────────────── SPLASH ───────────────────────
function ScreenSplash() {
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG />
      <SumiPetals count={14} />
      <div style={{ position: 'absolute', inset: 0, display: 'grid', placeItems: 'center' }}>
        <div style={{ textAlign: 'center' }}>
          <LogoEnsoDrawing size={120} />
          <div style={{ fontFamily: SUMI.type.display, fontSize: 56, fontStyle: 'italic', fontWeight: 500, letterSpacing: -1.5, marginTop: 8 }}>Sumi</div>
          <div style={{ fontFamily: SUMI.type.ui, fontSize: 11, letterSpacing: 4, color: SUMI.color.inkSoft, marginTop: 4, textTransform: 'uppercase' }}>A Quiet Practice</div>
        </div>
      </div>
      <div style={{ position: 'absolute', bottom: 70, left: 0, right: 0, textAlign: 'center', fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 3, color: SUMI.color.inkFaint }}>v 1.0</div>
    </div>
  );
}

// ─────────────────────── HOME ───────────────────────
function ScreenHome() {
  const quote = SUMI_QUOTES[2];
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG />
      <div style={{ position: 'absolute', top: 60, right: 20, opacity: 0.6 }}>
        <InkBleed size={140} color={SUMI.color.red} opacity={0.15} seed={3} />
      </div>
      <div style={{ position: 'relative', padding: `58px ${Px.sx}px 40px`, height: '100%', display: 'flex', flexDirection: 'column' }}>
        {/* Top chrome */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 22 }}>
          <LogoWordmark size={0.48} />
          <div style={{ display: 'flex', gap: 14, color: SUMI.color.inkSoft }}>
            <IconChart size={22} />
            <IconSettings size={22} />
          </div>
        </div>

        {/* Daily quote */}
        <div style={{ marginBottom: 24 }}>
          <SumiEyebrow>April 22 · Today</SumiEyebrow>
          <div style={{ fontFamily: SUMI.type.display, fontSize: 22, fontStyle: 'italic', lineHeight: 1.35, color: SUMI.color.ink, margin: '8px 0 6px', textWrap: 'pretty' }}>"{quote.q}"</div>
          <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color: SUMI.color.gold, textTransform: 'uppercase' }}>— {quote.a}</div>
        </div>

        <QuoteRule style={{ marginBottom: 22 }} />

        {/* Streak card */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 18, marginBottom: 18 }}>
          <div style={{ position: 'relative' }}>
            <div style={{ width: 64, height: 64, borderRadius: 32, border: `1.5px solid ${SUMI.color.red}`, display: 'grid', placeItems: 'center', background: 'rgba(168,52,42,0.04)' }}>
              <IconFlame size={28} color={SUMI.color.red} />
            </div>
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color: SUMI.color.inkFaint, textTransform: 'uppercase' }}>Streak</div>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 32, fontStyle: 'italic', fontWeight: 500, lineHeight: 1 }}>14 <span style={{ fontSize: 16, color: SUMI.color.inkSoft, fontStyle: 'normal' }}>days</span></div>
            <div style={{ display: 'flex', gap: 3, marginTop: 6 }}>
              {Array.from({length: 14}).map((_,i) => (<div key={i} style={{ width: 8, height: 4, background: SUMI.color.red, opacity: 0.5 + i*0.035 }} />))}
            </div>
          </div>
        </div>

        {/* Continue card */}
        <div style={{ border: `1px solid ${SUMI.color.ink}`, padding: 18, marginBottom: 14, background: SUMI.color.paperGlow, position: 'relative' }}>
          <SumiEyebrow color={SUMI.color.ink}>Continue</SumiEyebrow>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 }}>
            <div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 26, fontStyle: 'italic', fontWeight: 500, lineHeight: 1 }}>Medium · #142</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 13, color: SUMI.color.inkSoft, marginTop: 4 }}>62% · 04:12 elapsed</div>
            </div>
            <div style={{ color: SUMI.color.ink, fontSize: 22 }}>→</div>
          </div>
          {/* progress bar */}
          <div style={{ marginTop: 12, height: 2, background: SUMI.color.paperEdge, position: 'relative' }}>
            <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: '62%', background: SUMI.color.ink }} />
          </div>
        </div>

        {/* Quick new game */}
        <SumiEyebrow color={SUMI.color.inkFaint} style={{ marginBottom: 8 }}>New Practice</SumiEyebrow>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 14 }}>
          {[['Easy','一','3m'],['Medium','二','6m'],['Hard','三','12m'],['Master','四','25m']].map(([n,k,t]) => (
            <div key={n} style={{ border: `1px solid ${SUMI.color.paperEdge}`, padding: '12px 14px', display: 'flex', alignItems: 'center', gap: 12 }}>
              <div style={{ fontFamily: SUMI.type.cjk, fontSize: 26, color: SUMI.color.red, lineHeight: 1 }}>{k}</div>
              <div>
                <div style={{ fontFamily: SUMI.type.display, fontSize: 17, fontStyle: 'italic', fontWeight: 500, lineHeight: 1 }}>{n}</div>
                <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, color: SUMI.color.inkFaint, letterSpacing: 1 }}>{t}</div>
              </div>
            </div>
          ))}
        </div>

        <div style={{ flex: 1 }} />
        {/* Bottom nav */}
        <div style={{ display: 'flex', justifyContent: 'space-around', paddingTop: 14, borderTop: `1px solid ${SUMI.color.paperEdge}` }}>
          {[[IconBook,'Play',true],[IconCalendar,'Daily'],[IconTrophy,'Stats'],[IconQuote,'Zen']].map(([I,l,active],i) => (
            <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4, color: active ? SUMI.color.ink : SUMI.color.inkFaint }}>
              <I size={22} />
              <div style={{ fontFamily: SUMI.type.ui, fontSize: 9, letterSpacing: 1.5, textTransform: 'uppercase', fontWeight: active ? 700 : 500 }}>{l}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ─────────────────────── GAME ───────────────────────
function ScreenGame({ selected, setSelected, sweep }) {
  const cellSize = 38;
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG />
      <div style={{ position: 'relative', padding: `58px ${Px.sx}px 20px`, height: '100%', display: 'flex', flexDirection: 'column' }}>
        {/* Top bar */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingBottom: 14, borderBottom: `1px solid ${SUMI.color.paperEdge}`, marginBottom: 14 }}>
          <IconBack size={22} color={SUMI.color.ink} />
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2.5, color: SUMI.color.inkFaint, textTransform: 'uppercase' }}>二 · Medium · #142</div>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 22, fontStyle: 'italic', fontWeight: 500, fontVariantNumeric: 'tabular-nums', lineHeight: 1, marginTop: 2 }}>04:32</div>
          </div>
          <IconPause size={22} color={SUMI.color.ink} />
        </div>

        {/* Mistakes / hints row */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 1.5, textTransform: 'uppercase', color: SUMI.color.inkSoft }}>
          <span>Marks <span style={{ color: SUMI.color.red, fontWeight: 700 }}>0 / 3</span></span>
          <span>Hints <span style={{ color: SUMI.color.gold, fontWeight: 700 }}>2</span></span>
        </div>

        {/* Board */}
        <div style={{ alignSelf: 'center', padding: 2, border: `1px solid ${SUMI.color.ink}`, background: SUMI.color.paperGlow, marginBottom: 18 }}>
          <SumiBoard cellSize={cellSize} selected={selected} setSelected={setSelected} sweep={sweep} />
        </div>

        {/* Tools */}
        <div style={{ display: 'flex', justifyContent: 'space-around', marginBottom: 16 }}>
          {[[IconUndo,'Undo'],[IconBrush,'Note'],[IconErase,'Erase'],[IconLantern,'Hint']].map(([I,l]) => (
            <div key={l} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 5 }}>
              <div style={{ width: 44, height: 44, border: `1px solid ${SUMI.color.paperEdge}`, display: 'grid', placeItems: 'center', color: SUMI.color.ink }}>
                <I size={20} />
              </div>
              <span style={{ fontFamily: SUMI.type.ui, fontSize: 9, letterSpacing: 1.5, color: SUMI.color.inkFaint, textTransform: 'uppercase', fontWeight: 600 }}>{l}</span>
            </div>
          ))}
        </div>

        {/* Number pad */}
        <div style={{ borderTop: `1px solid ${SUMI.color.paperEdge}`, borderBottom: `1px solid ${SUMI.color.paperEdge}`, padding: '10px 0' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(9, 1fr)', gap: 2 }}>
            {[1,2,3,4,5,6,7,8,9].map(n => (
              <div key={n} style={{ aspectRatio: '1', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', borderLeft: n===1?'none':`1px solid ${SUMI.color.paperEdge}` }}>
                <span style={{ fontFamily: SUMI.type.numeral, fontSize: 26, color: SUMI.color.ink }}>{n}</span>
                <span style={{ fontFamily: SUMI.type.ui, fontSize: 9, color: SUMI.color.inkFaint, marginTop: -2 }}>{9 - PUZZLE.counts[n]}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// ─────────────────────── WIN ───────────────────────
function ScreenWin() {
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG />
      <div style={{ position: 'absolute', top: 80, left: 30 }}><InkBleed size={180} color={SUMI.color.red} opacity={0.12} seed={1} /></div>
      <div style={{ position: 'absolute', top: 320, right: -20 }}><InkBleed size={160} color={SUMI.color.teal} opacity={0.1} seed={2} /></div>

      <div style={{ position: 'relative', padding: `80px ${Px.sx}px 32px`, height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
        <SumiEyebrow>完 · Complete</SumiEyebrow>
        <div style={{ fontFamily: SUMI.type.display, fontSize: 68, fontStyle: 'italic', fontWeight: 500, letterSpacing: -2, lineHeight: 1, marginTop: 10 }}>Sumi</div>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 14, color: SUMI.color.inkSoft, fontStyle: 'italic', marginTop: 6 }}>The grid is quiet again.</div>

        <div style={{ marginTop: 26 }}><Seal size={80} /></div>

        <div style={{ marginTop: 32, width: '100%', borderTop: `1px solid ${SUMI.color.paperEdge}`, borderBottom: `1px solid ${SUMI.color.paperEdge}`, padding: '18px 0', display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)' }}>
          {[['Time','05:47'],['Marks','0'],['Streak','15']].map(([l,v],i) => (
            <div key={l} style={{ borderLeft: i===0?'none':`1px solid ${SUMI.color.paperEdge}`, padding: '0 4px' }}>
              <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color: SUMI.color.inkFaint, textTransform: 'uppercase' }}>{l}</div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 24, fontStyle: 'italic', fontWeight: 500, fontVariantNumeric: 'tabular-nums', marginTop: 2 }}>{v}</div>
            </div>
          ))}
        </div>

        <div style={{ marginTop: 22, padding: '14px 20px', fontFamily: SUMI.type.display, fontStyle: 'italic', fontSize: 14, color: SUMI.color.inkSoft, lineHeight: 1.6 }}>
          "The master's brush lifted only nine times.<br/>The student's, ninety."
        </div>

        <div style={{ flex: 1 }} />
        <SumiButton size="lg" style={{ width: '100%', marginBottom: 8 }}>Next Practice →</SumiButton>
        <SumiButton size="md" variant="ghost" style={{ width: '100%' }}>Share Result</SumiButton>
      </div>
    </div>
  );
}

// ─────────────────────── PAUSE ───────────────────────
function ScreenPause() {
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG intensity={0.5} />
      <div style={{ position: 'absolute', inset: 0, background: 'rgba(10,7,5,0.74)', backdropFilter: 'blur(10px)' }} />
      <div style={{ position: 'relative', padding: `${Px.sx}px`, height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center' }}>
        <div style={{ fontFamily: SUMI.type.cjk, fontSize: 52, color: SUMI.color.paper }}>休</div>
        <div style={{ fontFamily: SUMI.type.display, fontSize: 34, fontStyle: 'italic', fontWeight: 500, color: SUMI.color.paper, marginTop: 6 }}>A moment of rest</div>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 15, color: '#E8DCC5', marginTop: 10, maxWidth: 260, lineHeight: 1.5 }}>Your time is paused. Return when you are ready.</div>

        <div style={{ marginTop: 40, width: '100%', maxWidth: 280, display: 'flex', flexDirection: 'column', gap: 10 }}>
          <button style={{ padding: 16, background: SUMI.color.paper, color: SUMI.color.ink, border: 'none', fontFamily: SUMI.type.ui, fontSize: 13, letterSpacing: 3, textTransform: 'uppercase', fontWeight: 600 }}>Resume</button>
          <button style={{ padding: 16, background: 'transparent', color: SUMI.color.paper, border: `1px solid ${SUMI.color.paper}`, fontFamily: SUMI.type.ui, fontSize: 13, letterSpacing: 3, textTransform: 'uppercase', fontWeight: 600 }}>New Puzzle</button>
          <button style={{ padding: 16, background: 'transparent', color: '#E8DCC5', border: 'none', fontFamily: SUMI.type.ui, fontSize: 11, letterSpacing: 2.5, textTransform: 'uppercase' }}>Return Home</button>
        </div>
      </div>
    </div>
  );
}

// ─────────────────────── DAILY ───────────────────────
function ScreenDaily() {
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG />
      <div style={{ position: 'relative', padding: `58px ${Px.sx}px 40px`, height: '100%', display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <IconBack size={22} />
          <SumiEyebrow color={SUMI.color.ink}>Daily</SumiEyebrow>
          <div style={{ width: 22 }} />
        </div>
        <div style={{ fontFamily: SUMI.type.display, fontSize: 42, fontStyle: 'italic', fontWeight: 500, letterSpacing: -1, lineHeight: 1 }}>April</div>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 13, color: SUMI.color.inkSoft, fontStyle: 'italic', marginTop: 4 }}>14 of 22 days solved</div>

        {/* Heatmap */}
        <div style={{ marginTop: 24, display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 5 }}>
          {['M','T','W','T','F','S','S'].map((d,i) => <div key={i} style={{ fontFamily: SUMI.type.ui, fontSize: 9, letterSpacing: 1, color: SUMI.color.inkFaint, textAlign: 'center', textTransform: 'uppercase', marginBottom: 4 }}>{d}</div>)}
          {Array.from({length: 30}).map((_, i) => {
            const solved = i < 22 && Math.random() > 0.2;
            const today = i === 21;
            return (
              <div key={i} style={{
                aspectRatio: '1', position: 'relative',
                background: solved ? SUMI.color.ink : 'transparent',
                border: today ? `1.5px solid ${SUMI.color.red}` : `1px solid ${SUMI.color.paperEdge}`,
                display: 'grid', placeItems: 'center',
                fontFamily: SUMI.type.ui, fontSize: 10,
                color: solved ? SUMI.color.paper : SUMI.color.inkFaint,
              }}>{i+1}</div>
            );
          })}
        </div>

        <div style={{ marginTop: 26, border: `1px solid ${SUMI.color.red}`, padding: 18 }}>
          <SumiEyebrow>Today · April 22</SumiEyebrow>
          <div style={{ fontFamily: SUMI.type.display, fontSize: 26, fontStyle: 'italic', fontWeight: 500, marginTop: 6 }}>The Threshold</div>
          <div style={{ fontFamily: SUMI.type.body, fontSize: 13, color: SUMI.color.inkSoft, marginTop: 6, lineHeight: 1.5 }}>A hard puzzle crafted for the calm hand. Medium difficulty with one single-cell deduction moment.</div>
          <div style={{ display: 'flex', gap: 6, marginTop: 12 }}>
            <SumiChip tone="red">Hard</SumiChip>
            <SumiChip tone="muted">8 min avg</SumiChip>
            <SumiChip tone="gold">2× streak</SumiChip>
          </div>
          <SumiButton style={{ width: '100%', marginTop: 16 }}>Begin Today's Puzzle →</SumiButton>
        </div>

        <div style={{ marginTop: 18 }}>
          <SumiEyebrow color={SUMI.color.inkFaint}>Previous 7 days</SumiEyebrow>
          <div style={{ marginTop: 8, borderTop: `1px solid ${SUMI.color.paperEdge}` }}>
            {[['April 21','Medium','04:52',true],['April 20','Easy','02:12',true],['April 19','—','—',false]].map(([d,g,t,s],i) => (
              <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderBottom: `1px solid ${SUMI.color.paperEdge}`, fontFamily: SUMI.type.body, fontSize: 13 }}>
                <span>{d}</span><span style={{ color: SUMI.color.inkFaint }}>{g}</span>
                <span style={{ fontFamily: SUMI.type.display, fontStyle: 'italic', color: s ? SUMI.color.ink : SUMI.color.inkGhost }}>{t}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// ─────────────────────── STATS ───────────────────────
function ScreenStats() {
  const data = [52, 48, 45, 42, 38, 40, 35, 32, 30, 33, 28, 26, 24, 22];
  const max = 60;
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG />
      <div style={{ position: 'relative', padding: `58px ${Px.sx}px 40px`, height: '100%', display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }}>
          <IconBack size={22} />
          <SumiEyebrow color={SUMI.color.ink}>Practice Log</SumiEyebrow>
          <IconShare size={20} />
        </div>
        <div style={{ fontFamily: SUMI.type.display, fontSize: 42, fontStyle: 'italic', fontWeight: 500, letterSpacing: -1, lineHeight: 1 }}>486</div>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 13, color: SUMI.color.inkSoft, fontStyle: 'italic', marginTop: 2 }}>puzzles solved, all time</div>

        {/* Graph */}
        <div style={{ marginTop: 28 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
            <SumiEyebrow color={SUMI.color.inkFaint}>Medium · avg time</SumiEyebrow>
            <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, color: SUMI.color.red, letterSpacing: 1.5 }}>↓ 58% in 14 days</div>
          </div>
          <svg width="100%" height="100" viewBox="0 0 300 100" style={{ display: 'block' }}>
            <path d={`M 0,${100 - data[0]/max*100} ` + data.map((d,i) => `L ${(i/(data.length-1))*300},${100 - d/max*100}`).join(' ')} stroke={SUMI.color.ink} strokeWidth="1.5" fill="none" />
            <path d={`M 0,${100 - data[0]/max*100} ` + data.map((d,i) => `L ${(i/(data.length-1))*300},${100 - d/max*100}`).join(' ') + ` L 300,100 L 0,100 Z`} fill={SUMI.color.ink} opacity="0.08" />
            {data.map((d,i) => <circle key={i} cx={(i/(data.length-1))*300} cy={100 - d/max*100} r="1.5" fill={SUMI.color.ink} />)}
          </svg>
        </div>

        <div style={{ marginTop: 26, display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 12 }}>
          {[['Easy','01:24','PR'],['Medium','03:47','PR'],['Hard','08:12','—'],['Master','19:04','—']].map(([d,t,b]) => (
            <div key={d} style={{ border: `1px solid ${SUMI.color.paperEdge}`, padding: 14 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color: SUMI.color.inkFaint, textTransform: 'uppercase' }}>{d}</span>
                {b === 'PR' && <SumiChip tone="red" style={{ padding: '1px 6px', fontSize: 9 }}>PB</SumiChip>}
              </div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 28, fontStyle: 'italic', fontWeight: 500, fontVariantNumeric: 'tabular-nums', marginTop: 4 }}>{t}</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 11, color: SUMI.color.inkFaint, marginTop: 2 }}>personal best</div>
            </div>
          ))}
        </div>

        <div style={{ marginTop: 18, borderTop: `1px solid ${SUMI.color.paperEdge}`, paddingTop: 14 }}>
          {[['Win rate','94%'],['Longest streak','32 days'],['Total time','64h 12m'],['Flawless solves','218']].map(([l,v]) => (
            <div key={l} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: `1px solid ${SUMI.color.paperEdge}`, fontFamily: SUMI.type.body, fontSize: 14 }}>
              <span style={{ color: SUMI.color.inkSoft }}>{l}</span>
              <span style={{ fontFamily: SUMI.type.display, fontStyle: 'italic', fontWeight: 500 }}>{v}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ─────────────────────── PAYWALL ───────────────────────
function ScreenPaywall() {
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG dark />
      <div style={{ position: 'absolute', top: 40, right: -40 }}><InkBleed size={220} color={SUMI.color.red} opacity={0.2} seed={4} /></div>
      <div style={{ position: 'relative', padding: `58px ${Px.sx}px 32px`, height: '100%', display: 'flex', flexDirection: 'column', color: SUMI.color.night.ink }}>
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}><IconClose size={22} color={SUMI.color.night.inkSoft} /></div>

        <div style={{ marginTop: 30 }}>
          <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 4, color: SUMI.color.red, textTransform: 'uppercase', fontWeight: 700 }}>Sumi Pro</div>
          <div style={{ fontFamily: SUMI.type.display, fontSize: 52, fontStyle: 'italic', fontWeight: 500, letterSpacing: -1.5, lineHeight: 1.05, marginTop: 6 }}>An uninterrupted<br/>practice.</div>
          <div style={{ fontFamily: SUMI.type.body, fontSize: 15, color: SUMI.color.night.inkSoft, marginTop: 10, lineHeight: 1.5, maxWidth: 300 }}>No ads, no limits, no bells — just the grid, the quote, and your time.</div>
        </div>

        <div style={{ marginTop: 28, display: 'flex', flexDirection: 'column', gap: 12 }}>
          {[['Remove all ads','Forever quiet'],['Unlimited hints','Use what you need'],['Custom daily quote','Your own library'],['Theme variants','Sumi Night, Sumi Gold, Sumi Edo'],['Export PDF puzzle books','Solve on paper'],['iCloud sync','Practice anywhere']].map(([t,s]) => (
            <div key={t} style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
              <IconCheck size={18} color={SUMI.color.red} style={{ marginTop: 2, flexShrink: 0 }} />
              <div>
                <div style={{ fontFamily: SUMI.type.body, fontSize: 14, color: SUMI.color.night.ink, fontWeight: 500 }}>{t}</div>
                <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.night.inkSoft, fontStyle: 'italic' }}>{s}</div>
              </div>
            </div>
          ))}
        </div>

        <div style={{ flex: 1 }} />

        <div style={{ border: `1px solid ${SUMI.color.red}`, padding: 16, marginBottom: 10, position: 'relative' }}>
          <SumiChip tone="red" style={{ position: 'absolute', top: -9, left: 14, background: SUMI.color.night.paper }}>Best value</SumiChip>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 22, fontStyle: 'italic', fontWeight: 500 }}>Lifetime</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.night.inkSoft }}>One time · no renewal</div>
            </div>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 28, fontStyle: 'italic', fontWeight: 500 }}>$29.99</div>
          </div>
        </div>
        <div style={{ border: `1px solid ${SUMI.color.night.paperEdge}`, padding: 16, marginBottom: 14 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 20, fontStyle: 'italic', fontWeight: 500 }}>Yearly</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.night.inkSoft }}>$0.99 / month · billed annually</div>
            </div>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 24, fontStyle: 'italic', fontWeight: 500 }}>$11.99</div>
          </div>
        </div>

        <button style={{ padding: 18, background: SUMI.color.red, color: SUMI.color.paper, border: 'none', fontFamily: SUMI.type.ui, fontSize: 13, fontWeight: 600, letterSpacing: 3, textTransform: 'uppercase', cursor: 'pointer' }}>Start 7 days free</button>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 10, color: '#B3A088', textAlign: 'center', marginTop: 8 }}>Cancel anytime · Restore purchase</div>
      </div>
    </div>
  );
}

// ─────────────────────── ONBOARDING ───────────────────────
function ScreenOnboarding() {
  return (
    <div style={{ position: 'absolute', inset: 0 }}>
      <WashiBG />
      <div style={{ position: 'relative', padding: `80px ${Px.sx}px 40px`, height: '100%', display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', gap: 6, marginBottom: 40 }}>
          {[0,1,2,3].map(i => (
            <div key={i} style={{ flex: 1, height: 2, background: i<=1 ? SUMI.color.ink : SUMI.color.paperEdge }} />
          ))}
        </div>

        <div style={{ alignSelf: 'center', marginBottom: 30 }}>
          <LogoGrid size={100} />
        </div>

        <div style={{ fontFamily: SUMI.type.display, fontSize: 40, fontStyle: 'italic', fontWeight: 500, letterSpacing: -1.5, lineHeight: 1.05, textAlign: 'center' }}>
          Every row,<br/>every column,<br/>every house.
        </div>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 15, color: SUMI.color.inkSoft, textAlign: 'center', marginTop: 18, lineHeight: 1.6, maxWidth: 300, alignSelf: 'center' }}>
          The numbers 一 through 九, each placed once — no more, no less. The rest is patience.
        </div>

        <div style={{ flex: 1 }} />
        <SumiButton size="lg" style={{ width: '100%' }}>Continue</SumiButton>
        <div style={{ textAlign: 'center', marginTop: 14, fontFamily: SUMI.type.ui, fontSize: 11, letterSpacing: 2, color: SUMI.color.inkFaint, textTransform: 'uppercase' }}>Skip</div>
      </div>
    </div>
  );
}

Object.assign(window, { ScreenSplash, ScreenHome, ScreenGame, ScreenWin, ScreenPause, ScreenDaily, ScreenStats, ScreenPaywall, ScreenOnboarding });
