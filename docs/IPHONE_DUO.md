# iPhone Duo and resizable windows

Sumi sizes its layout to the **window**, never to the device. iPhone Duo (closed and open),
iPad, landscape phones and resizable windows all go through the same code.

## Displays

| Display | Pixels | Points (3 px/pt) | Notes |
|---|---|---|---|
| Duo outer (closed) | 1398 x 2034 | 466 x 678 | Status bar and camera sit in a column on the **right** edge (left in landscape) |
| Duo inner (open), portrait | 2007 x 2853 | 669 x 951 | |
| Duo inner (open), landscape | 2853 x 2007 | 951 x 669 | |
| Resize mode | arbitrary | about 375 to 1165 wide | |

Fold or hinge aware layout is out of scope. The app treats each display as a plain window.

## Toolchain

The app must be built with Xcode 27.1 or later (iOS 27.1 SDK), otherwise iOS letterboxes it on
Duo. Built with Xcode 26 the outer display shows a black strip where the status column is;
built with 27.x the app draws under it, which is why the inset rules below matter.
CI uses the `xcode-27` runner with an exact `27.1` pin (`build-ios.yml`,
`distribute-testflight.yml`). Do not set `UIRequiresFullScreen`; there is none today.

Apple requires Duo screenshots from April 2027 for apps built with the iOS 27.1 SDK or later.
Submitting them adds the optimized for Duo badge.

## Inset rules

All helpers live in `theme/CornerSafeInsets.kt`.

- **Top**: `Modifier.statusBarsWithCornerClearance()` is `windowInsetsPadding(statusBars)` followed
  by `padding(top = cornerClearanceTop())`. The clearance is zero on phones and 16dp minus the
  status bar on the Duo outer display, where the top inset is about 0 and nothing else moves the
  title out of the rounded corner. Do not replace the status bar padding with `safeDrawing` on
  top: where a parent already consumed the status bars the gap doubles.
- **Sides**: `Modifier.safeHorizontalPadding()` is `safeDrawing.only(Horizontal)`. Compose on iOS
  reports `displayCutout` on the top edge only, so `displayCutout` padding is zero on the Duo
  outer display. Use `safeDrawing` for the sides.
- **Bottom**: `navigationBarsPadding()` as before (home indicator).
- **Backgrounds stay full-bleed**: `WashiBG` draws the paper, texture and vignette edge to edge,
  and applies the side insets and the width cap to its *content* only. A screen built on
  `WashiBG { ... }` needs nothing extra for the sides. Screens that draw `WashiBG` as a sibling
  (Onboarding, Win, the Zen tab) use `Modifier.fillMaxSize().safeContentColumn()` on their column.
  Splash passes `capContentWidth = false` so its petals stay full width.
- The bottom nav row and the game's pinned banner use `safeHorizontalPadding()` too.
- The pause overlay keeps its scrim full-bleed and its card clears the insets.

## Window size rules

- `contentMaxWidth()` caps a screen's main column at `MaxContentWidth` (640dp) and fills
  narrower windows; `WashiBG` centres it.
- **Game screen** (`screens/game/GameLayout.kt`): `gameLayoutFor(width, height)` picks the layout
  from the window alone. Wider than tall, at least 360dp of usable height, and room for a 280dp
  panel beside a board of at least 260dp: the board sits on the left sized to the height (capped
  at 560dp) and the top bar, marks row, tools and a 3x3 number pad are in a column on the right,
  nothing scrolls. Anything else keeps the stacked, scrolling layout, unchanged. "Usable height"
  already subtracts the status bar, corner clearance and the reserved banner height. The board's
  square footprint is reserved before the puzzle is ready in both layouts.
- Paywall scrolls as one page below 560dp of height so the pricing block does not starve the
  feature list; every other screen scrolls vertically.

## Snapshot tests

The darpan harness captures each preview once, at one fixed device size, and ignores a preview's
own `widthDp` / `heightDp`. `ui/preview/PreviewWindow.kt` works around that: it lays the content
out at the real dp size and shrinks the density so the window fits the frame. The game screen has
previews for phone, Duo outer (portrait, landscape) and Duo inner (portrait, landscape)
(`GameScreenPreview.kt`). Insets are zero under Robolectric, so these show layout, not the status
column. The layout decision itself is unit-tested in `GameLayoutTest`.

## Testing on the Duo simulator

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -destination 'id=<duo-udid>' -derivedDataPath <scratch>/dd CODE_SIGNING_ALLOWED=NO build
xcrun simctl install <duo-udid> <scratch>/dd/Build/Products/Debug-iphonesimulator/Sumi.app
xcrun simctl launch <duo-udid> xyz.ksharma.sumi
xcrun simctl io <duo-udid> screenshot --display=1 outer.png   # outer, 1398x2034
xcrun simctl io <duo-udid> screenshot --display=3 inner.png   # inner, 2007x2853; black unless open
```

- Maestro drives the outer display in portrait (`maestro --device <udid> test flow.yaml`). The
  Skip button and the difficulty tiles are not exposed as text, tap them by percent point.
- **Maestro taps and swipes do nothing in landscape**, in either coordinate orientation. To see a
  screen in landscape, make a throwaway build (never commit it) that sets
  `INFOPLIST_KEY_UISupportedInterfaceOrientations_iPhone=UIInterfaceOrientationLandscapeRight`
  (and `_iPad`) and temporarily changes `SplashEntry`'s `navigator.resetRoot(it)` to the route you
  want, for example `GameRoute("Easy")`, `StatsRoute`, `PaywallRoute`.
- The simulator cannot be rotated from the command line (no Simulator.app to script).
- Settings has debug toggles (Show Ads, Simulate Pro, Seed: 5-day streak, Open Win screen) that give
  clean screenshots and reach Pro screens without a purchase. Debug builds only.
- A leftover ATT dialog from another app can sit over the screen: tap "Ask App Not to Track".
- `darpan/local.properties` is needed by every Gradle call; without it set `ANDROID_HOME`.

## App Store screenshots

Duo slots in App Store Connect: outer 1398 x 2034, inner 2007 x 2853 (portrait) and 2853 x 2007
(landscape). Capture the outer set with `screenshot --display=1`. The inner display shows black
unless the phone is open in the simulator, which cannot be done from the command line. Store
assets live in the git-ignored `app-store-assets/` folder (see its `README.txt` for the Duo slots).

## Not covered

- Fold or hinge aware layout, and splitting content across the two inner halves.
- The inner display could not be lit from the command line; it is covered by wide-window
  layouts (iPad, landscape, the Duo-sized snapshot previews) rather than on the Duo itself.
