// Sumi design system sections — color, type, space, components, icons, screens.

// ════════════════════════════════════════════════════════════════
// SECTION: SHELL
// ════════════════════════════════════════════════════════════════
function Section({ id, eyebrow, title, summary, children, dark = false }) {
  const c = dark ? SUMI.color.night : SUMI.color;
  return (
    <section id={id} style={{
      position: 'relative',
      padding: '80px 64px',
      background: dark ? c.paper : '#F4ECE0',
      color: c.ink,
      borderTop: `1px solid ${c.paperEdge}`,
    }}>
      <div style={{ maxWidth: 1440, margin: '0 auto' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '280px 1fr', gap: 48, marginBottom: 48, alignItems: 'start' }}>
          <div style={{ position: 'sticky', top: 40 }}>
            <div style={{ fontFamily: SUMI.type.ui, fontSize: 11, letterSpacing: 4, color: c.red, textTransform: 'uppercase', fontWeight: 700 }}>{eyebrow}</div>
            <h2 style={{ fontFamily: SUMI.type.display, fontSize: 56, fontStyle: 'italic', fontWeight: 500, letterSpacing: -1.5, margin: '4px 0 14px', lineHeight: 1 }}>{title}</h2>
            <div style={{ fontFamily: SUMI.type.body, fontSize: 15, lineHeight: 1.6, color: c.inkSoft }}>{summary}</div>
          </div>
          <div>{children}</div>
        </div>
      </div>
    </section>
  );
}

function Card({ children, style, dark = false }) {
  const c = dark ? SUMI.color.night : SUMI.color;
  return <div style={{ background: c.paperGlow, border: `1px solid ${c.paperEdge}`, padding: 24, ...style }}>{children}</div>;
}

function Label({ children, color = SUMI.color.inkFaint, style }) {
  return <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color, textTransform: 'uppercase', fontWeight: 600, ...style }}>{children}</div>;
}

function Mono({ children, size = 12, color = SUMI.color.inkSoft }) {
  return <code style={{ fontFamily: 'ui-monospace, "SF Mono", Menlo, monospace', fontSize: size, color, background: 'rgba(168,52,42,0.06)', padding: '1px 6px', borderRadius: 2 }}>{children}</code>;
}

// ════════════════════════════════════════════════════════════════
// COVER / HERO
// ════════════════════════════════════════════════════════════════
function HeroCover() {
  return (
    <div style={{ position: 'relative', minHeight: '90vh', padding: '80px 64px 60px', overflow: 'hidden' }}>
      <WashiBG />
      <div style={{ position: 'absolute', top: 80, right: 60 }}><InkBleed size={520} color={SUMI.color.red} opacity={0.14} seed={1} /></div>
      <div style={{ position: 'absolute', bottom: 80, left: 80 }}><InkBleed size={360} color={SUMI.color.teal} opacity={0.1} seed={4} /></div>

      <div style={{ position: 'relative', maxWidth: 1440, margin: '0 auto', height: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <Label color={SUMI.color.red}>墨 · Sumi Design System</Label>
            <div style={{ fontFamily: SUMI.type.ui, fontSize: 11, letterSpacing: 2, color: SUMI.color.inkFaint, marginTop: 6 }}>v 1.0 · April 2026 · iOS 17+</div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <Label>Prepared for</Label>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 20, fontStyle: 'italic', fontWeight: 500, marginTop: 2 }}>Sudoku — Mobile</div>
          </div>
        </div>

        <div style={{ marginTop: 80, display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: 64, alignItems: 'center' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 18, marginBottom: 24 }}>
              <LogoEnso size={110} />
              <div>
                <div style={{ fontFamily: SUMI.type.cjk, fontSize: 42, color: SUMI.color.ink, lineHeight: 1 }}>墨</div>
                <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 4, color: SUMI.color.inkFaint, marginTop: 4 }}>SUMI · INK</div>
              </div>
            </div>
            <h1 style={{ fontFamily: SUMI.type.display, fontSize: 130, fontStyle: 'italic', fontWeight: 400, letterSpacing: -4, lineHeight: 0.9, margin: 0 }}>
              A quiet<br/>practice.
            </h1>
            <div style={{ fontFamily: SUMI.type.body, fontSize: 19, lineHeight: 1.6, color: SUMI.color.inkSoft, marginTop: 28, maxWidth: 520, textWrap: 'pretty' }}>
              Sumi is a Sudoku for the calm hand. A washi paper surface, a brush of sumi ink, and a single red chop stamp at the end of every solve. Editorial typography, generous negative space, and <em>aurora</em> — a multi-hue celebration that travels the grid when a row, column, or house completes.
            </div>

            <div style={{ display: 'flex', gap: 32, marginTop: 40 }}>
              {[['Primary surface','Washi'],['Mark','Sumi ink'],['Seal','Vermilion'],['Motion','Aurora']].map(([l,v]) => (
                <div key={l}>
                  <Label>{l}</Label>
                  <div style={{ fontFamily: SUMI.type.display, fontSize: 22, fontStyle: 'italic', fontWeight: 500, marginTop: 2 }}>{v}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Right: a "masterpiece" composition */}
          <div style={{ position: 'relative', aspectRatio: '1', background: SUMI.color.paperGlow, border: `1px solid ${SUMI.color.paperEdge}`, padding: 36, display: 'grid', placeItems: 'center' }}>
            <WashiBG intensity={0.7} />
            <div style={{ position: 'absolute', top: -20, right: -20 }}><Seal size={100} /></div>
            <div style={{ position: 'relative', textAlign: 'center' }}>
              <div style={{ fontFamily: SUMI.type.cjk, fontSize: 160, color: SUMI.color.ink, lineHeight: 1 }}>数</div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 38, fontStyle: 'italic', fontWeight: 500, marginTop: 4 }}>Sudoku</div>
              <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 5, color: SUMI.color.inkFaint, marginTop: 6 }}>SU · DOKU · NUMBER · SOLITARY</div>
              <div style={{ marginTop: 30, fontFamily: SUMI.type.display, fontStyle: 'italic', fontSize: 16, color: SUMI.color.inkSoft, lineHeight: 1.6 }}>
                "The masters of old<br/>had nothing to prove —<br/>only a grid to fill."
              </div>
            </div>
          </div>
        </div>

        {/* Nav */}
        <div style={{ marginTop: 80, display: 'flex', gap: 2, flexWrap: 'wrap', borderTop: `1px solid ${SUMI.color.paperEdge}`, paddingTop: 20 }}>
          {['01 Brand','02 Color','03 Type','04 Space · Grid','05 Motion · Aurora','06 Components','07 Icons','08 Logos','09 Screens','10 Premium','11 Sound','12 Handoff'].map(l => (
            <a key={l} href={`#${l.slice(0,2)}`} style={{ fontFamily: SUMI.type.ui, fontSize: 11, letterSpacing: 2, padding: '10px 18px', border: `1px solid ${SUMI.color.paperEdge}`, textDecoration: 'none', color: SUMI.color.ink, textTransform: 'uppercase', fontWeight: 600 }}>{l}</a>
          ))}
        </div>
      </div>
    </div>
  );
}

// ════════════════════════════════════════════════════════════════
// 01 BRAND
// ════════════════════════════════════════════════════════════════
function SectionBrand() {
  return (
    <Section id="01" eyebrow="01 · Brand" title="The principles" summary={<>Six promises the product keeps. Every design decision must answer to one of these; when two conflict, the earlier one wins.</>}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 24 }}>
        {[
          ['一','Calm is the product','Sudoku is a meditation app disguised as a game. No timers that shame, no ads that interrupt, no leaderboards unless asked for. The UI sits still until the player does something worth celebrating.'],
          ['二','Ink is the only color','Black on cream is the default. Red is a stamp of completion. Teal is a pen. Gold is a privilege. Everything else earns its place by exception.'],
          ['三','The grid is the hero','The board never shares the screen with a banner. Chrome compresses, the grid expands. In doubt: make the 9×9 bigger.'],
          ['四','Numerals are typographic','We use Cormorant Garamond italic, not a mono number font. Numerals have voice — given numerals are upright; the player\'s marks lean into a script italic. The grid reads like a page, not a spreadsheet.'],
          ['五','Celebrate in whispers, then in aurora','Small correct placements warrant a soft glow. Completing a row, column, or house unleashes aurora — a traveling band of shifting hue. A full solve earns the single red chop.'],
          ['六','Paper remembers','Washi bears the weight of the hand. Selected cells carry a faint ink bleed. Undone marks leave ghosts. Nothing is chromed or plastic — every surface is touched.'],
        ].map(([n,t,d]) => (
          <Card key={n}>
            <div style={{ display: 'flex', gap: 16 }}>
              <div style={{ fontFamily: SUMI.type.cjk, fontSize: 46, color: SUMI.color.red, lineHeight: 1, flexShrink: 0 }}>{n}</div>
              <div>
                <div style={{ fontFamily: SUMI.type.display, fontSize: 22, fontStyle: 'italic', fontWeight: 500, lineHeight: 1.2 }}>{t}</div>
                <div style={{ fontFamily: SUMI.type.body, fontSize: 14, lineHeight: 1.55, color: SUMI.color.inkSoft, marginTop: 8, textWrap: 'pretty' }}>{d}</div>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Voice & tone */}
      <div style={{ marginTop: 48, borderTop: `1px solid ${SUMI.color.paperEdge}`, paddingTop: 32 }}>
        <Label color={SUMI.color.ink}>Voice & tone</Label>
        <div style={{ marginTop: 20, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 32 }}>
          <div>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 28, fontStyle: 'italic', fontWeight: 500 }}>We say...</div>
            <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
              {['"The grid is quiet again."','"A moment of rest."','"Begin today\'s puzzle."','"You have solved 486 puzzles."','"The brush lifted."'].map((q,i) => (
                <div key={i} style={{ fontFamily: SUMI.type.display, fontSize: 17, fontStyle: 'italic', color: SUMI.color.ink, padding: '10px 14px', borderLeft: `2px solid ${SUMI.color.red}` }}>{q}</div>
              ))}
            </div>
          </div>
          <div>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 28, fontStyle: 'italic', fontWeight: 500, color: SUMI.color.inkFaint }}>We never say...</div>
            <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
              {['"You win!! 🎉🎉🎉"','"Time\'s up!"','"Watch an ad to continue"','"Congratulations Champion!"','"Level Up! +200 XP"'].map((q,i) => (
                <div key={i} style={{ fontFamily: SUMI.type.body, fontSize: 15, color: SUMI.color.inkFaint, textDecoration: 'line-through', padding: '10px 14px' }}>{q}</div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </Section>
  );
}

Object.assign(window, { Section, Card, Label, Mono, HeroCover, SectionBrand });
