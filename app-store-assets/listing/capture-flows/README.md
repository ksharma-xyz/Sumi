# Sumi Android capture flows

Flows are Maestro YAML, one per screen, named `android-<class>-<NN>-<screen>.yaml`.
Output PNGs go to `app-store-assets/screenshots/android-phone/` (1080x2400) and
`app-store-assets/screenshots/android-tablet/` (2560x1600, landscape). The flows only drive the
UI; the PNG itself is taken with `adb exec-out screencap -p` so the size is the native display size.

Debug build package is `xyz.ksharma.sumi.debug` (the debug build type adds an applicationIdSuffix).

## Build and install

```sh
cd "$(git rev-parse --show-toplevel)"
./gradlew :androidApp:assembleDebug
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
# S = the emulator serial from `adb devices` (emulator-5554 when one emulator runs). Never the physical phone.
S=emulator-5554
adb -s $S install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

## Emulators (one at a time)

```sh
~/Library/Android/sdk/emulator/emulator -avd phone  -no-snapshot-save -no-audio &   # Pixel 8a, 1080x2400
~/Library/Android/sdk/emulator/emulator -avd tablet -no-snapshot-save -no-audio &   # 2560x1600
until [ "$(adb -s $S shell getprop sys.boot_completed | tr -d '\r')" = 1 ]; do sleep 3; done
```

Tablet: its natural orientation is landscape, so landscape is `user_rotation 0`
(`user_rotation 1` gives a 1600x2560 portrait frame on this AVD):

```sh
adb -s $S shell settings put system accelerometer_rotation 0
adb -s $S shell settings put system user_rotation 0
```

Right after boot SystemUI can show "System UI isn't responding" if Gradle is busy; tap "Wait"
and let it settle before launching the app.

## Demo mode (clean status bar, 9:41)

```sh
adb -s $S shell settings put global sysui_demo_allowed 1
adb -s $S shell am broadcast -a com.android.systemui.demo -e command enter
adb -s $S shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941
adb -s $S shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false
adb -s $S shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e mobile show -e level 4 -e datatype none
adb -s $S shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
# The demo "mobile" icon renders as a second wifi glyph (phone) or a stray "3G" (tablet). The tablet set hides it with:
adb -s $S shell am broadcast -a com.android.systemui.demo -e command network -e mobile hide
# exit
adb -s $S shell am broadcast -a com.android.systemui.demo -e command exit
adb -s $S shell settings put global sysui_demo_allowed 0
```

## Canonical state (fresh install, once)

```sh
cd app-store-assets/listing/capture-flows
maestro --device $S test android-phone-00-setup.yaml     # or android-tablet-00-setup.yaml
```

Sets Simulate Pro ON, Show Ads OFF, taps Seed (5-day streak, 20 puzzles), then restarts the app
(Home reads the streak only after a restart). Run it once per fresh install: the toggles are
flips, a second run undoes them. NOTE: the phone state was set with equivalent adb taps and the
tablet state with a mix of Maestro and adb taps while the flow was being fixed, so the final
versions of the two setup flows were not run end to end.

## Capture each screen

Start from Home (Play tab) after a fresh app start. Pattern for every screen:

```sh
O=../../screenshots/android-phone     # or android-tablet
maestro --device $S test android-phone-01-home.yaml;        adb -s $S exec-out screencap -p > $O/01-home.png
maestro --device $S test android-phone-02-game.yaml;        adb -s $S exec-out screencap -p > $O/02-game.png        # timer reads 00:04-00:05
maestro --device $S test android-phone-06-game-paused.yaml; adb -s $S exec-out screencap -p > $O/06-game-paused.png # run straight after 02-game
# Start the next group from a fresh app start:
adb -s $S shell am force-stop xyz.ksharma.sumi.debug
adb -s $S shell am start -n xyz.ksharma.sumi.debug/xyz.ksharma.sumi.MainActivity; sleep 9
maestro --device $S test android-phone-03-stats.yaml;       adb -s $S exec-out screencap -p > $O/03-stats.png
maestro --device $S test android-phone-04-daily.yaml;       adb -s $S exec-out screencap -p > $O/04-daily.png
maestro --device $S test android-phone-07-zen.yaml;         adb -s $S exec-out screencap -p > $O/07-zen.png
maestro --device $S test android-phone-05-win.yaml;         adb -s $S exec-out screencap -p > $O/05-win.png
```

Use the `android-tablet-*` flows and `android-tablet` output dir for the tablet. The tablet flows
differ only in tap points (settings icon 96%,8%; pause 92%,16%).

## Known differences from the iOS set

- Stats shows the seed only (20 puzzles, 5 streak). "This week" is 0 solved with no average time or
  level, and the Improvement chart shows "Solve a couple more to see the trend". The iOS set had
  real play history behind it.
- Home quote and the Daily puzzle number differ (quote and daily are date-driven).
