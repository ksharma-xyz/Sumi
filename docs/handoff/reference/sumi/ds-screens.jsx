// Sumi DS — screens, premium, sound, handoff

// ════════════════════════════════════════════════════════════════
// 09 SCREENS — phone gallery
// ════════════════════════════════════════════════════════════════
// Minimal phone shell — a single stroked rectangle with a thin status strip.
// Intentionally paper-quiet: no glossy bezels, no notches. The canvas is what matters.
function Phone({ children, label, caption, dark = false }) {
  const W = 340, H = 640;
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 14 }}>
      <div style={{
        width: W, height: H, position: 'relative', overflow: 'hidden',
        background: dark ? SUMI.color.night.paperWarm : SUMI.color.paper,
        border: `1px solid ${dark ? 'rgba(244,236,224,0.2)' : SUMI.color.paperEdge}`,
        boxShadow: dark ? '0 1px 0 rgba(244,236,224,0.04) inset' : '0 1px 0 rgba(26,20,16,0.03) inset',
      }}>
        {/* status bar */}
        <div style={{
          position: 'absolute', top: 0, left: 0, right: 0, height: 22,
          display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 16px',
          fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 1.2, fontWeight: 600,
          color: dark ? 'rgba(244,236,224,0.65)' : SUMI.color.inkFaint, zIndex: 50,
        }}>
          <span>9:41</span>
          <span style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
            <span style={{ width: 14, height: 7, borderRadius: 1, border: `1px solid ${dark ? 'rgba(244,236,224,0.55)' : SUMI.color.inkFaint}` }} />
          </span>
        </div>
        {/* canvas */}
        <div style={{ position: 'absolute', inset: '22px 0 0 0' }}>{children}</div>
      </div>
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 3, color: SUMI.color.red, textTransform: 'uppercase', fontWeight: 700 }}>{label}</div>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 13, color: SUMI.color.inkSoft, fontStyle: 'italic', marginTop: 2, maxWidth: 280 }}>{caption}</div>
      </div>
    </div>
  );
}

function SectionScreens() {
  const [sel, setSel] = React.useState({ r: 4, c: 4 });
  return (
    <Section id="09" eyebrow="09 · Screens" title="The practice, in seven views" summary={<>The full screen set for v1. Tap any board in the game screen to see selection propagate. A complete product: splash, onboarding, home, game, win, pause, daily, stats, paywall.</>}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 48, rowGap: 64 }}>
        <Phone label="01 · Splash" caption="A single breath. Enso draws on open, wordmark fades in after.">
          <ScreenSplash />
        </Phone>
        <Phone label="02 · Onboarding" caption="Four slides. The Enso, the rule, the difficulty, the quiet.">
          <ScreenOnboarding />
        </Phone>
        <Phone label="03 · Home" caption="Daily quote · streak · continue · quick-start difficulty.">
          <ScreenHome />
        </Phone>
        <Phone label="04 · Game" caption="Board, tools, number-pad. No chrome noise. Tap cells to select.">
          <ScreenGame selected={sel} setSelected={setSel} />
        </Phone>
        <Phone label="05 · Win" caption="The grid stills. Seal stamps. Three stats, one quote, two buttons.">
          <ScreenWin />
        </Phone>
        <Phone label="06 · Pause" caption="Blurred over the paused puzzle. 休 — a character for rest.">
          <ScreenPause />
        </Phone>
        <Phone label="07 · Daily" caption="April heatmap, today's puzzle flagged in red. Previous days listed.">
          <ScreenDaily />
        </Phone>
        <Phone label="08 · Stats" caption="Practice log. Improvement curve. PBs by difficulty.">
          <ScreenStats />
        </Phone>
        <Phone label="09 · Paywall" caption="Night mode. Sumi Pro — an uninterrupted practice." dark>
          <ScreenPaywall />
        </Phone>
      </div>
    </Section>
  );
}

// ════════════════════════════════════════════════════════════════
// 10 PREMIUM
// ════════════════════════════════════════════════════════════════
function SectionPremium() {
  return (
    <Section id="10" eyebrow="10 · Membership" title="Free to begin, Pro to linger" summary={<>A two-tier model: a gracious free app for the casual solver, and <strong style={{color: SUMI.color.gold}}>Sumi Pro</strong> — The Circle (墨会) — for the devoted. <strong>Gold is the membership colour.</strong> It appears nowhere else: no free-app CTAs, no errors, no decoration. When you see gold, you are seeing privilege.</>}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.1fr', gap: 24 }}>
        <Card>
          <Label color={SUMI.color.inkFaint}>Free · Sumi 墨</Label>
          <div style={{ fontFamily: SUMI.type.display, fontSize: 34, fontStyle: 'italic', fontWeight: 500, marginTop: 4 }}>Sumi</div>
          <div style={{ fontFamily: SUMI.type.display, fontStyle: 'italic', fontSize: 15, color: SUMI.color.inkFaint, marginTop: 2 }}>A daily puzzle and three, on your own terms.</div>
          <ul style={{ fontFamily: SUMI.type.body, fontSize: 14, lineHeight: 1.85, color: SUMI.color.inkSoft, marginTop: 14, paddingLeft: 20 }}>
            <li>The Daily puzzle (one per morning)</li>
            <li>Three additional puzzles per day</li>
            <li><strong>Easy and Medium only</strong> · Hard, Master, Edo require Pro</li>
            <li>3 hints per puzzle</li>
            <li>Basic streak tracking</li>
            <li>Paper theme only</li>
            <li>One full-bleed interstitial between puzzles (never mid-solve)</li>
            <li>One literary quote per day, from the short anthology (30 quotes)</li>
          </ul>
        </Card>
        <Card style={{ background: '#1A1410', color: SUMI.color.paper, border: `1px solid ${SUMI.color.gold}`, borderWidth: 2 }}>
          <Label color={SUMI.color.gold}>Pro · Sumi 墨会 · The Circle</Label>
          <div style={{ fontFamily: SUMI.type.display, fontSize: 34, fontStyle: 'italic', fontWeight: 500, marginTop: 4, color: SUMI.color.paper }}>Sumi Pro</div>
          <div style={{ fontFamily: SUMI.type.display, fontStyle: 'italic', fontSize: 15, color: '#D9A855', marginTop: 2 }}>$3.99/month · $29.99 lifetime · 7-day guest pass</div>
          <ul style={{ fontFamily: SUMI.type.body, fontSize: 14, lineHeight: 1.85, color: '#D6C8B3', marginTop: 14, paddingLeft: 20 }}>
            <li><strong style={{color: '#D9A855'}}>The Salon</strong> — weekly global leaderboard, presented as a drawing-room register</li>
            <li>Unlimited puzzles · <strong style={{color: '#D9A855'}}>Hard, Master, Edo</strong> difficulties unlocked</li>
            <li>Unlimited hints</li>
            <li>The full anthology — 600 literary quotations across seven libraries</li>
            <li>Full statistics — streaks, heatmaps, PBs by difficulty, deduction log</li>
            <li><strong style={{color: '#D9A855'}}>Gold</strong>, <strong style={{color: '#6A85A8'}}>Indigo</strong>, and <strong>Edo Pattern</strong> themes</li>
            <li>iCloud sync across devices</li>
            <li>PDF puzzle book export — for trains, tea rooms, travel</li>
            <li>No advertisements, anywhere</li>
          </ul>
        </Card>
      </div>

      <div style={{ marginTop: 32 }}>
        <Label color={SUMI.color.ink}>Why members stay · the real value</Label>
        <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14 }}>
          {[
            ['The Salon 墨会','A weekly global register. The top 50 times each week, entered in vermilion by Monday morning. Names, cities, times — presented like an old guestbook. This is the primary reason members stay.'],
            ['Difficulty as depth','Easy and Medium are the welcome. Hard, Master, and Edo are the practice. A serious solver pays to reach the puzzles worth solving.'],
            ['Gold as privilege','The colour appears only on Pro surfaces: the membership card, the Salon chop, the Pro themes. Free users never see gold. Seeing it is the reward.'],
          ].map(([t,d]) => (
            <Card key={t}>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 19, fontStyle: 'italic', fontWeight: 500 }}>{t}</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 13, lineHeight: 1.6, color: SUMI.color.inkSoft, marginTop: 10 }}>{d}</div>
            </Card>
          ))}
        </div>
      </div>

      <div style={{ marginTop: 32 }}>
        <Label color={SUMI.color.ink}>Ad placement · the only rule</Label>
        <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14 }}>
          {[
            ['Never during a solve','The puzzle is sacred. No mid-game interstitials, no banners over the board.'],
            ['Only at natural pauses','Between puzzles, on return-to-home. Maximum one ad per 3 puzzle completions.'],
            ['Full-bleed, never boxed','If an ad appears, it takes the full screen for a few seconds and exits cleanly. No 320×50 banners, ever.'],
          ].map(([t,d]) => (
            <Card key={t}>
              <div style={{ fontFamily: SUMI.type.display, fontSize: 18, fontStyle: 'italic', fontWeight: 500 }}>{t}</div>
              <div style={{ fontFamily: SUMI.type.body, fontSize: 13, lineHeight: 1.55, color: SUMI.color.inkSoft, marginTop: 8 }}>{d}</div>
            </Card>
          ))}
        </div>
      </div>

      <div style={{ marginTop: 32 }}>
        <Label color={SUMI.color.ink}>Upsell surfaces · where Pro is offered</Label>
        <div style={{ marginTop: 12, fontFamily: SUMI.type.body, fontSize: 14 }}>
          {[
            ['Difficulty picker','Hard, Master, Edo rows show a small gold 会 mark instead of being hidden. Tap reveals the membership card.'],
            ['After a Medium solve','If the user\'s time is in the top 20%: "Your time would place you in the Salon this week."'],
            ['The Salon tab','Visible to all users. Free users see the register with a gentle veil: "A membership opens the register."'],
            ['Out-of-hints prompt','When a 4th hint is requested.'],
            ['Streak milestone · 7, 30, 100','"You\'ve earned a gift — 30% off Pro for a week."'],
          ].map(([w,h],i) => (
            <div key={i} style={{ display: 'grid', gridTemplateColumns: '240px 1fr', gap: 20, padding: '14px 0', borderBottom: `1px solid ${SUMI.color.paperEdge}` }}>
              <span style={{ fontFamily: SUMI.type.display, fontStyle: 'italic', fontWeight: 500, fontSize: 17 }}>{w}</span>
              <span style={{ color: SUMI.color.inkSoft }}>{h}</span>
            </div>
          ))}
        </div>
      </div>
    </Section>
  );
}

// ════════════════════════════════════════════════════════════════
// 11 SOUND
// ════════════════════════════════════════════════════════════════
function SectionSound() {
  return (
    <Section id="11" eyebrow="11 · Sound" title="Felt, not heard" summary={<>Sumi is silent by default. When sound is on, it is organic — a brush, paper, water, wood. No synths, no chimes, no "woosh." Every sound has a haptic counterpart that fires even when muted.</>}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 16 }}>
        {[
          ['Cell tap','brush-tip.wav','24ms','UIImpactFeedbackGenerator.light'],
          ['Number place','paper-press.wav','80ms','UIImpactFeedbackGenerator.soft'],
          ['Erase','brush-lift.wav','60ms','UISelectionFeedbackGenerator'],
          ['Correct solve (row)','water-drop.wav','300ms','UINotificationFeedbackGenerator.success'],
          ['Mistake','ink-splash.wav','140ms','UIImpactFeedbackGenerator.rigid'],
          ['Puzzle solved','woodblock.wav','600ms','UINotificationFeedbackGenerator.success · double'],
          ['Chop stamp','seal-press.wav','400ms','UIImpactFeedbackGenerator.heavy'],
          ['Menu open','scroll-unroll.wav','240ms','UIImpactFeedbackGenerator.soft'],
        ].map(([n,f,d,h]) => (
          <div key={n} style={{ padding: 20, border: `1px solid ${SUMI.color.paperEdge}`, background: SUMI.color.paperGlow }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
              <span style={{ fontFamily: SUMI.type.display, fontSize: 20, fontStyle: 'italic', fontWeight: 500 }}>{n}</span>
              <Mono>{d}</Mono>
            </div>
            <div style={{ marginTop: 6, fontFamily: SUMI.type.body, fontSize: 13, color: SUMI.color.inkSoft }}>
              <Mono>sfx/{f}</Mono>
              <div style={{ marginTop: 4, fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 1.5, textTransform: 'uppercase', color: SUMI.color.red, fontWeight: 600 }}>⤷ Haptic</div>
              <Mono size={11}>{h}</Mono>
            </div>
          </div>
        ))}
      </div>
    </Section>
  );
}

// ════════════════════════════════════════════════════════════════
// 12 HANDOFF
// ════════════════════════════════════════════════════════════════
function SectionHandoff() {
  return (
    <Section id="12" eyebrow="12 · Handoff" title="For engineering" summary={<>A quick map of the system in code. Tokens live in one file; components reference tokens only. SwiftUI naming. The board is a single reusable view driven by a model. Aurora is a Metal overlay, not CoreAnimation.</>}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 24 }}>
        <Card>
          <Label color={SUMI.color.red}>Tokens · SumiTokens.swift</Label>
          <pre style={{ margin: 0, marginTop: 10, fontFamily: 'ui-monospace, monospace', fontSize: 11, lineHeight: 1.6, color: SUMI.color.ink, whiteSpace: 'pre-wrap' }}>{
`extension Color {
  static let paper        = Color(hex: 0xF4ECE0)
  static let paperWarm    = Color(hex: 0xEBE0CC)
  static let paperEdge    = Color(hex: 0xC9B48A)
  static let ink          = Color(hex: 0x1A1410)
  static let inkSoft      = Color(hex: 0x5A4838)
  static let sumiRed      = Color(hex: 0xA8342A)
  static let sumiTeal     = Color(hex: 0x2A5A6E)
  static let sumiGold     = Color(hex: 0x8A6B2A)
}

extension Font {
  static let sumiDisplay = Font.custom(
    "CormorantGaramond-MediumItalic", size: 46)
  static let sumiBody    = Font.custom(
    "SourceSerif4-Regular", size: 15)
  static let sumiUI      = Font.custom(
    "Inter-SemiBold", size: 13)
    .tracking(2.6)
  static let sumiNumeral = Font.custom(
    "CormorantGaramond-SemiBold", size: 22)
}`
          }</pre>
        </Card>
        <Card>
          <Label color={SUMI.color.red}>Structure · view hierarchy</Label>
          <pre style={{ margin: 0, marginTop: 10, fontFamily: 'ui-monospace, monospace', fontSize: 11, lineHeight: 1.6, color: SUMI.color.ink, whiteSpace: 'pre-wrap' }}>{
`App
├─ RootView
│  ├─ SplashView
│  └─ RootTabs
│     ├─ HomeView
│     │  ├─ WashiBackground
│     │  ├─ DailyQuoteCard
│     │  ├─ StreakCard
│     │  ├─ ContinueCard
│     │  └─ DifficultyGrid
│     ├─ DailyView
│     ├─ StatsView
│     └─ ZenView
│
└─ Modals
   ├─ GameView (board + tools + pad)
   │  ├─ SudokuBoardView
   │  ├─ AuroraOverlay (Metal)
   │  └─ NumberPadView
   ├─ PauseView
   ├─ WinView
   └─ PaywallView`
          }</pre>
        </Card>
      </div>

      <div style={{ marginTop: 32, display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
        {[
          ['Framework','SwiftUI + Metal'],['Min OS','iOS 17'],['Layout','Stack-based · no AutoLayout'],
          ['Theming','@Environment(\\.sumiTheme)'],['Motion','Animation.timingCurve'],['Aurora','MTKView overlay'],
          ['Persistence','SwiftData + CloudKit'],['Analytics','Opt-in, local-first'],
        ].map(([l,v]) => (
          <div key={l} style={{ padding: 16, border: `1px solid ${SUMI.color.paperEdge}`, background: SUMI.color.paperGlow }}>
            <Label>{l}</Label>
            <div style={{ fontFamily: SUMI.type.display, fontSize: 18, fontStyle: 'italic', fontWeight: 500, marginTop: 4 }}>{v}</div>
          </div>
        ))}
      </div>

      <div style={{ marginTop: 32, padding: 24, background: SUMI.color.ink, color: SUMI.color.paper }}>
        <div style={{ fontFamily: SUMI.type.display, fontSize: 34, fontStyle: 'italic', fontWeight: 500 }}>One last thing.</div>
        <div style={{ fontFamily: SUMI.type.body, fontSize: 15, lineHeight: 1.6, color: SUMI.color.paperEdge, marginTop: 12, maxWidth: 640 }}>
          When in doubt, remove. A feature that doesn't serve the grid doesn't ship. A line of copy that doesn't sound calm doesn't survive review. A color that isn't paper, ink, red, teal, or gold isn't allowed. That is the whole brief.
        </div>
        <div style={{ marginTop: 18, display: 'flex', alignItems: 'center', gap: 14 }}>
          <div style={{ fontFamily: SUMI.type.cjk, fontSize: 32, color: SUMI.color.night.red }}>墨</div>
          <div style={{ fontFamily: SUMI.type.ui, fontSize: 10, letterSpacing: 4, color: SUMI.color.paperEdge, textTransform: 'uppercase' }}>Sumi · end of document</div>
        </div>
      </div>
    </Section>
  );
}

Object.assign(window, { SectionScreens, SectionPremium, SectionSound, SectionHandoff });
