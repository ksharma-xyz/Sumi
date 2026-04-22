// Sumi DS — tokens sections: color, type, space, motion

// ════════════════════════════════════════════════════════════════
// 02 COLOR
// ════════════════════════════════════════════════════════════════
function Swatch({ name, hex, role, dark }) {
  const lum = parseInt(hex.slice(1,3),16)*0.299 + parseInt(hex.slice(3,5),16)*0.587 + parseInt(hex.slice(5,7),16)*0.114;
  const onLight = lum < 160;
  return (
    <div style={{ background: hex, padding: 20, minHeight: 140, display: 'flex', flexDirection: 'column', justifyContent: 'space-between', color: onLight ? '#F4ECE0' : '#1A1410', border: `1px solid ${SUMI.color.paperEdge}` }}>
      <div>
        <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, textTransform: 'uppercase', opacity: 0.7 }}>{role}</div>
        <div style={{ fontFamily: SUMI.type.display, fontSize: 22, fontStyle: 'italic', fontWeight: 500, marginTop: 2 }}>{name}</div>
      </div>
      <div style={{ fontFamily: 'ui-monospace, monospace', fontSize: 11, opacity: 0.85 }}>{hex.toUpperCase()}</div>
    </div>
  );
}

function SectionColor() {
  return (
    <Section id="02" eyebrow="02 · Color" title="Ink on paper" summary={<>A palette of two families: warm neutrals (paper) and warm darks (ink). Three accents — red, teal, gold — each with a single sanctioned job. Night mode inverts the paper/ink relationship but keeps the accents recognizable.</>}>
      <Label color={SUMI.color.ink}>Paper · surfaces</Label>
      <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 0 }}>
        <Swatch name="Paper" hex={SUMI.color.paper} role="primary surface" />
        <Swatch name="Paper Warm" hex={SUMI.color.paperWarm} role="secondary" />
        <Swatch name="Paper Deep" hex={SUMI.color.paperDeep} role="tertiary" />
        <Swatch name="Paper Edge" hex={SUMI.color.paperEdge} role="divider" />
        <Swatch name="Paper Glow" hex={SUMI.color.paperGlow} role="lifted" />
      </div>

      <Label color={SUMI.color.ink} style={{ marginTop: 32 }}>Ink · foreground</Label>
      <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 0 }}>
        <Swatch name="Ink" hex={SUMI.color.ink} role="primary text" />
        <Swatch name="Ink Soft" hex={SUMI.color.inkSoft} role="secondary text" />
        <Swatch name="Ink Faint" hex={SUMI.color.inkFaint} role="tertiary text" />
        <Swatch name="Ink Ghost" hex={SUMI.color.inkGhost} role="disabled" />
      </div>

      <Label color={SUMI.color.ink} style={{ marginTop: 32 }}>Accents · earned appearances</Label>
      <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 0 }}>
        <Swatch name="Red" hex={SUMI.color.red} role="completion / chop" />
        <Swatch name="Red Deep" hex={SUMI.color.redDeep} role="seal shadow" />
        <Swatch name="Teal" hex={SUMI.color.teal} role="user marks" />
        <Swatch name="Gold" hex={SUMI.color.gold} role="premium / hints" />
      </div>

      <Label color={SUMI.color.ink} style={{ marginTop: 32 }}>Night mode · Sumi on black</Label>
      <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 0 }}>
        <Swatch name="Night Paper" hex={SUMI.color.night.paper} role="primary surface" />
        <Swatch name="Night Warm" hex={SUMI.color.night.paperWarm} role="secondary" />
        <Swatch name="Night Ink" hex={SUMI.color.night.ink} role="text" />
        <Swatch name="Night Red" hex={SUMI.color.night.red} role="chop" />
        <Swatch name="Night Gold" hex={SUMI.color.night.gold} role="hint" />
      </div>

      {/* Usage pairings */}
      <div style={{ marginTop: 40, borderTop: `1px solid ${SUMI.color.paperEdge}`, paddingTop: 32 }}>
        <Label color={SUMI.color.ink}>Sanctioned pairings · where each color may speak</Label>
        <div style={{ marginTop: 16, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14 }}>
          {[
            ['Given numeral','Cormorant italic 22 on Paper','#1A1410','#F4ECE0'],
            ['User mark','Cormorant italic 24 on Paper','#2A5A6E','#F4ECE0'],
            ['Mistake','Cormorant 22 on red-wash','#A8342A','rgba(168,52,42,0.10)'],
            ['Chop · solved','Red wax on paper','#F4ECE0','#A8342A'],
            ['Hint glyph','Gold on paper','#8A6B2A','#F4ECE0'],
            ['Hand note','Caveat on paper-warm','#5A4838','#EBE0CC'],
          ].map(([l,s,fg,bg]) => (
            <div key={l} style={{ background: bg, color: fg, padding: '20px 18px', border: `1px solid ${SUMI.color.paperEdge}` }}>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 40, fontStyle: 'italic', fontWeight: 500, lineHeight: 1 }}>7</div>
              <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, textTransform: 'uppercase', fontWeight: 600, marginTop: 10, opacity: 0.9 }}>{l}</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 12, marginTop: 4, opacity: 0.75 }}>{s}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Accessibility */}
      <div style={{ marginTop: 32, padding: 24, border: `1px solid ${SUMI.color.paperEdge}`, background: SUMI.color.paperGlow }}>
        <Label color={SUMI.color.red}>Accessibility · contrast</Label>
        <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 20, fontFamily: SUMI.type.body, fontSize: 13 }}>
          {[['Ink on Paper','13.8 : 1','AAA'],['Ink Soft on Paper','7.2 : 1','AAA'],['Red on Paper','5.8 : 1','AA+'],['Teal on Paper','7.1 : 1','AAA']].map(([l,r,g]) => (
            <div key={l}>
              <div style={{ color: SUMI.color.inkFaint }}>{l}</div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 24, fontStyle: 'italic', fontWeight: 500, marginTop: 2 }}>{r}</div>
              <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color: SUMI.color.success, marginTop: 2 }}>WCAG {g}</div>
            </div>
          ))}
        </div>
      </div>
    </Section>
  );
}

// ════════════════════════════════════════════════════════════════
// 03 TYPE
// ════════════════════════════════════════════════════════════════
function TypeSpec({ family, role, size, weight, tracking, sample, lineHeight = 1.2, italic = false }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: '280px 1fr', gap: 32, alignItems: 'baseline', padding: '24px 0', borderTop: `1px solid ${SUMI.color.paperEdge}` }}>
      <div>
        <Label color={SUMI.color.ink}>{role}</Label>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 14, color: SUMI.color.inkSoft, marginTop: 6, lineHeight: 1.5 }}>
          {family}<br/>
          <Mono>{size}px · {weight} · {tracking}</Mono>
        </div>
      </div>
      <div style={{ fontFamily: family, fontSize: size, fontWeight: weight, letterSpacing: tracking === 'normal' ? 0 : tracking, lineHeight, fontStyle: italic ? 'italic' : 'normal', color: SUMI.color.ink }}>{sample}</div>
    </div>
  );
}

function SectionType() {
  return (
    <Section id="03" eyebrow="03 · Type" title="A three-voice score" summary={<>Cormorant Garamond italic for voice — headlines, numerals, quotes. Source Serif 4 for the body — dense, readable, editorial. Söhne / Inter for the UI — chrome, buttons, tabs. A handful of supporting roles: Caveat for hand-notes, Shippori Mincho for CJK chops.</>}>
      {/* Specimen */}
      <div style={{ padding: 40, background: SUMI.color.paperGlow, border: `1px solid ${SUMI.color.paperEdge}`, marginBottom: 32, textAlign: 'center' }}>
        <div style={{ fontFamily: SUMI.type.ui, fontSize: 11, letterSpacing: 4, color: SUMI.color.red, textTransform: 'uppercase', fontWeight: 700 }}>Specimen</div>
        <div style={{ fontFamily: SUMI.type.display, fontSize: 140, fontStyle: 'italic', fontWeight: 400, letterSpacing: -4, lineHeight: 1, color: SUMI.color.ink, marginTop: 8 }}>Sumi.</div>
        <div style={{ fontFamily: SUMI.type.display, fontSize: 34, fontWeight: 500, letterSpacing: -0.5, color: SUMI.color.inkSoft, marginTop: 4 }}>1234567890</div>
        <div style={{ fontFamily: SUMI.type.hand, fontSize: 38, color: SUMI.color.teal, marginTop: 8 }}>abcdefghij</div>
        <div style={{ fontFamily: SUMI.type.cjk, fontSize: 44, color: SUMI.color.ink, marginTop: 4 }}>墨 · 数 · 独 · 一二三四五六七八九</div>
      </div>

      <Label color={SUMI.color.ink}>Scale</Label>

      <TypeSpec role="Hero display" family={SUMI.type.display} size={88} weight={400} tracking="-0.03em" italic sample="A quiet practice." />
      <TypeSpec role="H1 · screen title" family={SUMI.type.display} size={46} weight={500} tracking="-0.02em" italic sample="The grid is quiet again." />
      <TypeSpec role="H2 · section" family={SUMI.type.display} size={34} weight={500} tracking="-0.015em" italic sample="Today's practice" />
      <TypeSpec role="H3 · card title" family={SUMI.type.display} size={26} weight={500} tracking="-0.01em" italic sample="Medium · Puzzle 142" />
      <TypeSpec role="Subhead" family={SUMI.type.display} size={20} weight={500} tracking="normal" italic sample="A hard puzzle, calmly drawn." />
      <TypeSpec role="Body · reading" family={SUMI.type.body} size={15} weight={400} tracking="normal" lineHeight={1.6} sample="The numbers one through nine, each placed once, no more, no less — the rest is patience." />
      <TypeSpec role="Body · small" family={SUMI.type.body} size={13} weight={400} tracking="normal" sample="62% complete · 04:12 elapsed" />
      <TypeSpec role="Caption" family={SUMI.type.body} size={11} weight={400} tracking="normal" sample="14 of 22 days solved in April" />
      <TypeSpec role="UI · button" family={SUMI.type.ui} size={13} weight={600} tracking="0.2em" sample="BEGIN TODAY'S PUZZLE" />
      <TypeSpec role="UI · label" family={SUMI.type.ui} size={11} weight={600} tracking="0.25em" sample="DIFFICULTY" />
      <TypeSpec role="UI · meta" family={SUMI.type.ui} size={10} weight={500} tracking="0.2em" sample="APRIL 22 · TODAY" />
      <TypeSpec role="Numeral · given" family={SUMI.type.numeral} size={48} weight={600} tracking="-0.04em" sample="5 7 3 6 9 2 8 4 1" />
      <TypeSpec role="Numeral · mark" family={SUMI.type.numeral} size={48} weight={500} tracking="-0.04em" italic sample="2 8 4 6 1 9 5 3 7" />
      <TypeSpec role="Hand · note" family={SUMI.type.hand} size={34} weight={500} tracking="normal" sample="maybe 4? or 9..." />
      <TypeSpec role="CJK · chop" family={SUMI.type.cjk} size={48} weight={500} tracking="normal" sample="完 · 休 · 始" />

      {/* Pairings */}
      <div style={{ marginTop: 48, borderTop: `1px solid ${SUMI.color.paperEdge}`, paddingTop: 32 }}>
        <Label color={SUMI.color.ink}>Pairings · by screen role</Label>
        <div style={{ marginTop: 16, display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 20 }}>
          {[
            { eyebrow: 'APRIL 22 · TODAY', head: '"The grid is quiet again."', sub: 'A soft puzzle, on a quiet day.', ctx: 'Home hero · daily quote' },
            { eyebrow: 'DIFFICULTY', head: 'Medium · Puzzle 142', sub: '04:12 elapsed', ctx: 'Card · continue' },
            { eyebrow: '完  COMPLETE', head: 'Sumi.', sub: 'The grid is quiet again.', ctx: 'Win screen' },
            { eyebrow: 'PRACTICE LOG', head: '486', sub: 'puzzles solved, all time', ctx: 'Stats header' },
          ].map((p, i) => (
            <div key={i} style={{ border: `1px solid ${SUMI.color.paperEdge}`, padding: 24 }}>
              <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color: SUMI.color.red, textTransform: 'uppercase', fontWeight: 700 }}>{p.eyebrow}</div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 26, fontStyle: 'italic', fontWeight: 500, marginTop: 4 }}>{p.head}</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 13, color: SUMI.color.inkSoft, fontStyle: 'italic', marginTop: 4 }}>{p.sub}</div>
              <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color: SUMI.color.inkFaint, textTransform: 'uppercase', marginTop: 20, paddingTop: 14, borderTop: `1px solid ${SUMI.color.paperEdge}` }}>↳ {p.ctx}</div>
            </div>
          ))}
        </div>
      </div>
    </Section>
  );
}

// ════════════════════════════════════════════════════════════════
// 04 SPACE + GRID
// ════════════════════════════════════════════════════════════════
function SectionSpace() {
  const scale = [2, 4, 6, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80, 120];
  return (
    <Section id="04" eyebrow="04 · Space" title="A measured hand" summary={<>A 4-pixel base. Screens use an outer margin of 20px. Cards breathe with 18–24px padding. The board is a single sacred block: 38×38 cells on iPhone 15 Pro, stretched or shrunk to fit within a fixed 342px frame.</>}>
      <Label color={SUMI.color.ink}>Scale · 4px base</Label>
      <div style={{ marginTop: 12, padding: 24, background: SUMI.color.paperGlow, border: `1px solid ${SUMI.color.paperEdge}` }}>
        {scale.map((n, i) => (
          <div key={n} style={{ display: 'grid', gridTemplateColumns: '80px 80px 1fr', alignItems: 'center', gap: 24, padding: '6px 0' }}>
            <div style={{ fontFamily: 'ui-monospace, monospace', fontSize: 12 }}>space-{i}</div>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 18, fontStyle: 'italic', fontWeight: 500 }}>{n}<span style={{ fontSize: 11, color: SUMI.color.inkFaint, fontStyle: 'normal' }}> px</span></div>
            <div style={{ height: 14, width: n * 3, background: SUMI.color.ink }} />
          </div>
        ))}
      </div>

      {/* Board anatomy */}
      <div style={{ marginTop: 48 }}>
        <Label color={SUMI.color.ink}>Board anatomy</Label>
        <div style={{ marginTop: 16, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 32, alignItems: 'center' }}>
          <div style={{ padding: 40, background: SUMI.color.paperGlow, border: `1px solid ${SUMI.color.paperEdge}`, display: 'grid', placeItems: 'center' }}>
            <div style={{ padding: 4, border: `1px solid ${SUMI.color.red}`, position: 'relative' }}>
              <div style={{ padding: 2, border: `1px solid ${SUMI.color.ink}` }}>
                <SumiBoard cellSize={36} selected={{ r: 4, c: 4 }} setSelected={() => {}} />
              </div>
              {/* measures */}
              <div style={{ position: 'absolute', left: -44, top: 0, bottom: 0, width: 40, display: 'flex', alignItems: 'center', justifyContent: 'flex-end', fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.red }}>342</div>
              <div style={{ position: 'absolute', right: -40, top: 0, bottom: 0, width: 36, display: 'flex', alignItems: 'center', fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.ink }}>9×38</div>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 4, fontFamily: SUMI.type.body, fontSize: 14 }}>
            {[
              ['Frame','342 × 342','outer'],
              ['Frame stroke','1px ink','solid'],
              ['Cell size','38 × 38','base'],
              ['Cell min','34 · max 42','responsive'],
              ['3×3 rule','1.5px ink','85% opacity'],
              ['Cell rule','0.5px ink','25% opacity'],
              ['Selected bg','red @ 8% alpha','no border'],
              ['House wash','ink @ 3% alpha','row+col+box'],
              ['Same digit','teal @ 8% alpha','everywhere'],
              ['Conflict','red @ 10% alpha','numeral → red'],
            ].map(([l,v,n]) => (
              <div key={l} style={{ display: 'grid', gridTemplateColumns: '140px 1fr 90px', gap: 16, padding: '8px 0', borderBottom: `1px solid ${SUMI.color.paperEdge}` }}>
                <span style={{ color: SUMI.color.inkSoft }}>{l}</span>
                <Mono>{v}</Mono>
                <span style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 1.5, color: SUMI.color.inkFaint, textTransform: 'uppercase' }}>{n}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Screen grid */}
      <div style={{ marginTop: 48 }}>
        <Label color={SUMI.color.ink}>Screen grid · iPhone 15 Pro · 393 × 852</Label>
        <div style={{ marginTop: 16, padding: 20, background: SUMI.color.paperGlow, border: `1px solid ${SUMI.color.paperEdge}`, display: 'flex', justifyContent: 'center' }}>
          <div style={{ position: 'relative', width: 393, height: 480, background: SUMI.color.paper, border: `1px solid ${SUMI.color.paperEdge}`, overflow: 'hidden' }}>
            {/* safe zones */}
            <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 58, background: 'rgba(168,52,42,0.06)', borderBottom: `1px dashed ${SUMI.color.red}`, display: 'grid', placeItems: 'center', fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.red }}>status · 58px</div>
            <div style={{ position: 'absolute', top: 0, bottom: 0, left: 0, width: 20, background: 'rgba(168,52,42,0.05)' }} />
            <div style={{ position: 'absolute', top: 0, bottom: 0, right: 0, width: 20, background: 'rgba(168,52,42,0.05)' }} />
            <div style={{ position: 'absolute', top: 58, left: 20, right: 20, bottom: 40, border: `1px dashed ${SUMI.color.red}`, display: 'grid', placeItems: 'center', fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.red }}>content · 353 wide</div>
            <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: 40, background: 'rgba(168,52,42,0.06)', borderTop: `1px dashed ${SUMI.color.red}`, display: 'grid', placeItems: 'center', fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.red }}>home indicator · 40px</div>
          </div>
        </div>
      </div>
    </Section>
  );
}

// ════════════════════════════════════════════════════════════════
// 05 MOTION
// ════════════════════════════════════════════════════════════════
function MotionSpec({ name, duration, easing, use }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: '220px 120px 260px 1fr', gap: 20, padding: '16px 0', borderBottom: `1px solid ${SUMI.color.paperEdge}`, alignItems: 'center' }}>
      <div style={{ fontFamily: SUMI.type.display, fontSize: 18, fontStyle: 'italic', fontWeight: 500 }}>{name}</div>
      <Mono>{duration}</Mono>
      <Mono>{easing}</Mono>
      <div style={{ fontFamily: SUMI.type.body, fontSize: 13, color: SUMI.color.inkSoft }}>{use}</div>
    </div>
  );
}

function SectionMotion() {
  return (
    <Section id="05" eyebrow="05 · Motion" title="Aurora" summary={<>Sumi's signature motion system — a traveling band of shifting hue that sweeps a row, column, or 3×3 house when it completes. Aurora is our "fire" — but made of watercolor, not flame. Calm at rest, pronounced when earned.</>}>
      {/* Aurora demo */}
      <div style={{ padding: 40, background: SUMI.color.paperGlow, border: `1px solid ${SUMI.color.paperEdge}`, textAlign: 'center' }}>
        <div style={{ display: 'inline-block', position: 'relative' }}>
          <SumiBoard cellSize={38} selected={{ r: 4, c: 4 }} setSelected={() => {}} sweep="row" />
        </div>
        <div style={{ marginTop: 20, fontFamily: SUMI.type.body, fontStyle: 'italic', fontSize: 14, color: SUMI.color.inkSoft }}>A row completes. Aurora travels left to right in 1.2 seconds.</div>
      </div>

      <div style={{ marginTop: 40 }}>
        <Label color={SUMI.color.ink}>Aurora · specifications</Label>
        <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
          {[
            ['Row complete','1.2s','left → right','5 hue band'],
            ['Column complete','1.2s','bottom → top','5 hue band'],
            ['House complete','1.0s','radial from center','4 hue bloom'],
            ['Digit 1–9 done','0.8s','all cells of digit','2 hue pulse'],
            ['Puzzle solved','3.0s','grid-wide aurora, seal stamps','full spectrum'],
            ['Correct placement','0.4s','single cell','ink-bleed, no hue'],
          ].map(([n,d,dir,b]) => (
            <Card key={n} style={{ padding: 20 }}>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 20, fontStyle: 'italic', fontWeight: 500 }}>{n}</div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 10, fontFamily: 'ui-monospace, monospace', fontSize: 11 }}>
                <span style={{ color: SUMI.color.red }}>{d}</span>
                <span style={{ color: SUMI.color.inkFaint }}>{dir}</span>
              </div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft, marginTop: 8 }}>{b}</div>
            </Card>
          ))}
        </div>
      </div>

      {/* Easing curves */}
      <div style={{ marginTop: 40 }}>
        <Label color={SUMI.color.ink}>Easing · the calm-at-rest rule</Label>
        <MotionSpec name="brush-out" duration="1200ms" easing="cubic-bezier(.22,1,.36,1)" use="Aurora traveling, ink bleed, celebration crescendo — slow start, soft landing." />
        <MotionSpec name="brush-in" duration="320ms" easing="cubic-bezier(.4,0,.2,1)" use="Press-in for buttons, cell selection, immediate feedback." />
        <MotionSpec name="lift" duration="240ms" easing="cubic-bezier(.2,0,.1,1)" use="Cards elevating on tap, modal appearance." />
        <MotionSpec name="settle" duration="480ms" easing="cubic-bezier(.33,1,.68,1)" use="Digit placement, number-pad fade, count update." />
        <MotionSpec name="chop" duration="280ms" easing="cubic-bezier(.9,.2,.6,1)" use="Red seal stamping at solve — fast-down, long-hold pause." />
        <MotionSpec name="breathe" duration="4000ms" easing="ease-in-out" use="Hero washi background drift, splash/paywall ambient motion. Infinite." />
      </div>

      {/* State timing */}
      <div style={{ marginTop: 40, padding: 24, border: `1px solid ${SUMI.color.paperEdge}`, background: SUMI.color.paperGlow }}>
        <Label color={SUMI.color.red}>The ban · what we don't animate</Label>
        <ul style={{ fontFamily: SUMI.type.body, fontSize: 14, lineHeight: 1.8, color: SUMI.color.inkSoft, marginTop: 8, paddingLeft: 20 }}>
          <li>No confetti, no fireworks, no particle blasts. Ever.</li>
          <li>No screen-shakes. The grid is stationary — the celebration moves within it.</li>
          <li>No sound-reactive animation. Sound is optional; motion must stand alone.</li>
          <li>No spring bounces on UI — a spring is a toy. Sumi eases.</li>
          <li>No per-keystroke vibrations of the layout. Digits appear; they do not bounce in.</li>
        </ul>
      </div>
    </Section>
  );
}

Object.assign(window, { SectionColor, SectionType, SectionSpace, SectionMotion });
