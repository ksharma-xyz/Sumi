// Sumi DS — components, icons, logos

// ════════════════════════════════════════════════════════════════
// 06 COMPONENTS
// ════════════════════════════════════════════════════════════════
function CompCell({ title, children, span = 1, dark = false }) {
  return (
    <div style={{ gridColumn: `span ${span}`, border: `1px solid ${SUMI.color.paperEdge}`, background: dark ? SUMI.color.night.paper : SUMI.color.paperGlow, padding: 24 }}>
      <Label color={SUMI.color.red} style={{ marginBottom: 16 }}>{title}</Label>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16, alignItems: 'flex-start' }}>{children}</div>
    </div>
  );
}

function SectionComponents() {
  return (
    <Section id="06" eyebrow="06 · Components" title="The kit" summary={<>A small, opinionated component set. Buttons are square. Cards have hairline borders. The number pad is a horizontal rhythm of 9 numerals with ghost counts underneath. Nothing floats on a shadow that wasn't earned.</>}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 20 }}>

        {/* Buttons */}
        <CompCell title="Primary button" span={2}>
          <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'center' }}>
            <SumiButton size="lg">Begin Today's Puzzle →</SumiButton>
            <SumiButton size="md">Continue</SumiButton>
            <SumiButton size="sm">Resume</SumiButton>
            <SumiButton size="md" variant="ghost">Share Result</SumiButton>
            <SumiButton size="md" variant="outline">New Puzzle</SumiButton>
            <SumiButton size="md" disabled>Unavailable</SumiButton>
          </div>
          <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft, marginTop: 4 }}>
            <Mono>padding: 16–20px</Mono> · <Mono>border: 1px ink</Mono> · <Mono>font: UI 13 / tracking 0.2em</Mono> · <Mono>never radius</Mono>
          </div>
        </CompCell>

        {/* Number pad */}
        <CompCell title="Number pad">
          <div style={{ width: '100%', borderTop: `1px solid ${SUMI.color.paperEdge}`, borderBottom: `1px solid ${SUMI.color.paperEdge}`, padding: '10px 0' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(9, 1fr)', gap: 2 }}>
              {[1,2,3,4,5,6,7,8,9].map(n => (
                <div key={n} style={{ aspectRatio: '1', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', borderLeft: n===1?'none':`1px solid ${SUMI.color.paperEdge}` }}>
                  <span style={{ fontFamily: SUMI.type.numeral, fontSize: 26, color: SUMI.color.ink }}>{n}</span>
                  <span style={{ fontFamily: SUMI.type.ui, fontSize: 9, color: SUMI.color.inkFaint, marginTop: -2 }}>{(n * 37) % 9}</span>
                </div>
              ))}
            </div>
          </div>
          <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft }}>Ghost count beneath each numeral tracks remaining placements. Dims to faint when 0.</div>
        </CompCell>

        {/* Tool strip */}
        <CompCell title="Tool strip">
          <div style={{ display: 'flex', gap: 16 }}>
            {[[IconUndo,'Undo'],[IconBrush,'Note'],[IconErase,'Erase'],[IconLantern,'Hint']].map(([I,l]) => (
              <div key={l} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 5 }}>
                <div style={{ width: 44, height: 44, border: `1px solid ${SUMI.color.paperEdge}`, display: 'grid', placeItems: 'center', color: SUMI.color.ink }}>
                  <I size={20} />
                </div>
                <span style={{ fontFamily: SUMI.type.ui, fontSize: 9, letterSpacing: 1.5, color: SUMI.color.inkFaint, textTransform: 'uppercase', fontWeight: 600 }}>{l}</span>
              </div>
            ))}
          </div>
        </CompCell>

        {/* Chips */}
        <CompCell title="Chips">
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <SumiChip tone="red">Hard</SumiChip>
            <SumiChip tone="gold">Premium</SumiChip>
            <SumiChip tone="muted">8 min avg</SumiChip>
            <SumiChip tone="ink">Active</SumiChip>
            <SumiChip tone="red">PB</SumiChip>
          </div>
        </CompCell>

        {/* Cards */}
        <CompCell title="Cards · continue / daily / stat" span={2}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12, width: '100%' }}>
            <div style={{ border: `1px solid ${SUMI.color.ink}`, padding: 18, background: SUMI.color.paperGlow }}>
              <Label color={SUMI.color.ink}>Continue</Label>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 }}>
                <div>
                  <div style={{ fontFamily: SUMI.type.display, fontSize: 22, fontStyle: 'italic', fontWeight: 500 }}>Medium · #142</div>
                  <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft, marginTop: 2 }}>62% · 04:12</div>
                </div>
                <div style={{ color: SUMI.color.ink, fontSize: 22 }}>→</div>
              </div>
              <div style={{ marginTop: 10, height: 2, background: SUMI.color.paperEdge }}>
                <div style={{ width: '62%', height: '100%', background: SUMI.color.ink }} />
              </div>
            </div>

            <div style={{ border: `1px solid ${SUMI.color.red}`, padding: 18 }}>
              <Label color={SUMI.color.red}>Today · April 22</Label>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 22, fontStyle: 'italic', fontWeight: 500, marginTop: 6 }}>The Threshold</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft, marginTop: 4 }}>A hard puzzle for a calm hand.</div>
              <div style={{ display: 'flex', gap: 4, marginTop: 10 }}>
                <SumiChip tone="red">Hard</SumiChip>
                <SumiChip tone="gold">2× streak</SumiChip>
              </div>
            </div>

            <div style={{ border: `1px solid ${SUMI.color.paperEdge}`, padding: 18 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <Label>Medium</Label>
                <SumiChip tone="red" style={{ padding: '1px 6px', fontSize: 9 }}>PB</SumiChip>
              </div>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 28, fontStyle: 'italic', fontWeight: 500, fontVariantNumeric: 'tabular-nums', marginTop: 4 }}>03:47</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 11, color: SUMI.color.inkFaint }}>personal best</div>
            </div>
          </div>
        </CompCell>

        {/* Cell states */}
        <CompCell title="Cell · states" span={2}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: 12, width: '100%' }}>
            {[
              ['Empty','',false,false,false,false],
              ['Given','5',false,true,false,false],
              ['User mark','2',false,false,false,false],
              ['Selected','',true,false,false,false],
              ['House wash','',false,false,true,false],
              ['Same digit','5',false,true,true,false],
              ['Conflict','3',false,false,false,true],
              ['With notes','',false,false,false,false],
            ].map(([l,v,sel,given,unit,conf], i) => (
              <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6 }}>
                <div style={{
                  width: 52, height: 52, border: `1px solid ${SUMI.color.ink}`,
                  background: conf ? 'rgba(168,52,42,0.12)' : sel ? 'rgba(168,52,42,0.08)' : unit ? 'rgba(26,20,16,0.04)' : SUMI.color.paperGlow,
                  display: 'grid', placeItems: 'center', position: 'relative',
                }}>
                  {l === 'With notes' ? (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', width: '100%', height: '100%', placeItems: 'center', fontFamily: SUMI.type.hand, fontSize: 11, color: SUMI.color.inkSoft }}>
                      <span>2</span><span></span><span>4</span><span></span><span>5</span><span></span><span>7</span><span></span><span>9</span>
                    </div>
                  ) : v && (
                    <span style={{ fontFamily: given ? SUMI.type.numeral : SUMI.type.hand, fontSize: given ? 28 : 28, fontWeight: given ? 600 : 500, color: conf ? SUMI.color.red : given ? SUMI.color.ink : SUMI.color.teal }}>{v}</span>
                  )}
                </div>
                <span style={{ fontFamily: SUMI.type.ui, fontSize: 9, letterSpacing: 1.5, color: SUMI.color.inkFaint, textTransform: 'uppercase' }}>{l}</span>
              </div>
            ))}
          </div>
        </CompCell>

        {/* Tabs */}
        <CompCell title="Tab bar">
          <div style={{ display: 'flex', justifyContent: 'space-around', paddingTop: 8, width: '100%', borderTop: `1px solid ${SUMI.color.paperEdge}` }}>
            {[[IconBook,'Play',true],[IconCalendar,'Daily'],[IconTrophy,'Stats'],[IconQuote,'Zen']].map(([I,l,active],i) => (
              <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4, color: active ? SUMI.color.ink : SUMI.color.inkFaint }}>
                <I size={22} />
                <div style={{ fontFamily: SUMI.type.ui, fontSize: 9, letterSpacing: 1.5, textTransform: 'uppercase', fontWeight: active ? 700 : 500 }}>{l}</div>
              </div>
            ))}
          </div>
        </CompCell>

        {/* Eyebrow + rule */}
        <CompCell title="Eyebrow + quote rule">
          <SumiEyebrow>April 22 · Today</SumiEyebrow>
          <div style={{ fontFamily: SUMI.type.display, fontSize: 20, fontStyle: 'italic' }}>"The grid is quiet again."</div>
          <QuoteRule />
          <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 2, color: SUMI.color.gold, textTransform: 'uppercase' }}>— attributed to a master</div>
        </CompCell>

        {/* Progress bar */}
        <CompCell title="Progress · two variants">
          <div style={{ width: '100%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 1.5, textTransform: 'uppercase', color: SUMI.color.inkFaint }}>
              <span>Puzzle</span><span>62%</span>
            </div>
            <div style={{ height: 2, background: SUMI.color.paperEdge }}>
              <div style={{ width: '62%', height: '100%', background: SUMI.color.ink }} />
            </div>
          </div>
          <div style={{ width: '100%', marginTop: 8 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 1.5, textTransform: 'uppercase', color: SUMI.color.red }}>
              <span>Streak</span><span>14 / 30</span>
            </div>
            <div style={{ display: 'flex', gap: 3 }}>
              {Array.from({length: 30}).map((_,i) => <div key={i} style={{ flex: 1, height: 6, background: i < 14 ? SUMI.color.red : SUMI.color.paperEdge, opacity: i < 14 ? 0.35 + i*0.045 : 1 }} />)}
            </div>
          </div>
        </CompCell>

        {/* Seal */}
        <CompCell title="Seal / chop">
          <div style={{ display: 'flex', gap: 18, alignItems: 'center' }}>
            <Seal size={64} />
            <Seal size={80} />
            <Seal size={48} />
          </div>
          <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft }}>Appears once per solve. The kanji 完 (complete) on vermilion.</div>
        </CompCell>

        {/* Ink bleed */}
        <CompCell title="Ink bleed · ambient">
          <div style={{ position: 'relative', width: 200, height: 140 }}>
            <InkBleed size={140} color={SUMI.color.red} opacity={0.2} seed={2} />
          </div>
        </CompCell>

      </div>
    </Section>
  );
}

// ════════════════════════════════════════════════════════════════
// 07 ICONS
// ════════════════════════════════════════════════════════════════
function IconTile({ Icon, name }) {
  return (
    <div style={{ border: `1px solid ${SUMI.color.paperEdge}`, padding: 18, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10, background: SUMI.color.paperGlow }}>
      <div style={{ color: SUMI.color.ink }}>
        <Icon size={26} />
      </div>
      <div style={{ fontFamily: SUMI.type.ui, fontSize: 9, letterSpacing: 1.5, color: SUMI.color.inkFaint, textTransform: 'uppercase', fontWeight: 600 }}>{name}</div>
    </div>
  );
}

function SectionIcons() {
  const icons = [
    ['Back',IconBack],['Close',IconClose],['Settings',IconSettings],['Chart',IconChart],
    ['Share',IconShare],['Pause',IconPause],['Flame',IconFlame],['Book',IconBook],
    ['Calendar',IconCalendar],['Trophy',IconTrophy],['Quote',IconQuote],['Check',IconCheck],
    ['Undo',IconUndo],['Brush',IconBrush],['Erase',IconErase],['Lantern',IconLantern],
  ];
  return (
    <Section id="07" eyebrow="07 · Icons" title="The hand-drawn system" summary={<>16 icons at a 24px base. Stroke-only, 1.5px weight, no fills. Rounded caps with a slight hand-drawn imperfection — a brush, not a pen. Paired always with an UI-typeset label at 9/10pt.</>}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(8, 1fr)', gap: 0 }}>
        {icons.map(([n, I]) => <IconTile key={n} name={n} Icon={I} />)}
      </div>

      <div style={{ marginTop: 32, padding: 24, background: SUMI.color.paperGlow, border: `1px solid ${SUMI.color.paperEdge}` }}>
        <Label color={SUMI.color.ink}>Rules</Label>
        <ul style={{ fontFamily: SUMI.type.body, fontSize: 14, lineHeight: 1.8, color: SUMI.color.inkSoft, marginTop: 8, paddingLeft: 20 }}>
          <li>Stroke-only. No fills, no gradients, no multi-color. Ink on paper or paper on ink.</li>
          <li>1.5px stroke at 24px; 1.25px at 20px; 1px at 16px. Scale the stroke, not just the SVG.</li>
          <li>Rounded line joins and caps — the brush lifted cleanly, but not sharply.</li>
          <li>Never alone. Every icon in navigation pairs with a text label.</li>
          <li>Never as brand. The logo never appears as an icon in navigation.</li>
        </ul>
      </div>
    </Section>
  );
}

// ════════════════════════════════════════════════════════════════
// 08 LOGOS
// ════════════════════════════════════════════════════════════════
function SectionLogos() {
  return (
    <Section id="08" eyebrow="08 · Logos" title="Three marks" summary={<>The Enso — a single brush ring drawn in one breath. The Wordmark — Sumi set in Cormorant italic with a red-ink terminal dot. The Grid mark — a 3×3 of single strokes, used at favicon / app-icon scale.</>}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 20 }}>
        <Card style={{ aspectRatio: '1', display: 'grid', placeItems: 'center', position: 'relative' }}>
          <LogoEnso size={180} />
          <div style={{ position: 'absolute', bottom: 20, left: 20 }}>
            <Label color={SUMI.color.red}>Mark · Enso</Label>
            <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft, marginTop: 2 }}>App icon · splash · premium</div>
          </div>
        </Card>
        <Card style={{ aspectRatio: '1', display: 'grid', placeItems: 'center', position: 'relative' }}>
          <LogoWordmark size={1.4} />
          <div style={{ position: 'absolute', bottom: 20, left: 20 }}>
            <Label color={SUMI.color.red}>Wordmark</Label>
            <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft, marginTop: 2 }}>Nav · headers · footers</div>
          </div>
        </Card>
        <Card style={{ aspectRatio: '1', display: 'grid', placeItems: 'center', position: 'relative' }}>
          <LogoGrid size={160} />
          <div style={{ position: 'absolute', bottom: 20, left: 20 }}>
            <Label color={SUMI.color.red}>Grid</Label>
            <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft, marginTop: 2 }}>Favicon · notifications · share</div>
          </div>
        </Card>
      </div>

      {/* App icon grid */}
      <div style={{ marginTop: 32 }}>
        <Label color={SUMI.color.ink}>App icon · contexts</Label>
        <div style={{ marginTop: 16, display: 'flex', gap: 20, alignItems: 'flex-end' }}>
          {[[180,'1024 · store'],[120,'180 · home'],[80,'120 · spotlight'],[60,'60 · settings'],[40,'29 · notifs']].map(([s, l]) => (
            <div key={l} style={{ textAlign: 'center' }}>
              <div style={{ width: s, height: s, borderRadius: s*0.22, background: SUMI.color.paperGlow, border: `1px solid ${SUMI.color.paperEdge}`, display: 'grid', placeItems: 'center', position: 'relative', overflow: 'hidden' }}>
                <WashiBG intensity={0.6} />
                <LogoEnso size={s*0.7} />
              </div>
              <div style={{ fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.inkFaint, marginTop: 8 }}>{l}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Clear space */}
      <div style={{ marginTop: 32, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        <Card>
          <Label color={SUMI.color.ink}>Clear space · enso</Label>
          <div style={{ marginTop: 16, position: 'relative', display: 'grid', placeItems: 'center', padding: 40, border: `1px dashed ${SUMI.color.red}`, background: SUMI.color.paper }}>
            <LogoEnso size={120} />
            <div style={{ position: 'absolute', inset: 16, border: `1px dashed ${SUMI.color.inkFaint}` }} />
          </div>
          <div style={{ fontFamily: SUMI.type.body, fontSize: 12, color: SUMI.color.inkSoft, marginTop: 8 }}>Clear space = 1× the ink diameter on all sides.</div>
        </Card>
        <Card>
          <Label color={SUMI.color.ink}>Minimum sizes</Label>
          <div style={{ marginTop: 16, display: 'flex', gap: 20, alignItems: 'flex-end' }}>
            <div style={{ textAlign: 'center' }}>
              <LogoEnso size={24} />
              <div style={{ fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.inkFaint, marginTop: 8 }}>24px min</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <LogoWordmark size={0.4} />
              <div style={{ fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.inkFaint, marginTop: 8 }}>72px wide min</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <LogoGrid size={20} />
              <div style={{ fontFamily: 'ui-monospace, monospace', fontSize: 10, color: SUMI.color.inkFaint, marginTop: 8 }}>16px min</div>
            </div>
          </div>
        </Card>
      </div>
    </Section>
  );
}

Object.assign(window, { SectionComponents, SectionIcons, SectionLogos });
