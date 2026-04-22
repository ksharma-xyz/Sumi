# Sumi — Claude Code guidance

## Project

Zen Sudoku app built with Kotlin Multiplatform + Compose Multiplatform (Android + iOS).

Package: `xyz.ksharma.sumi`
Design system: `docs/handoff/` — read `START_HERE.md` before touching UI code.

## Code quality checks

Run these before marking any task complete. All three must pass.

```bash
# Compile iOS (catches commonMain + iosMain errors)
./gradlew :composeApp:compileKotlinIosSimulatorArm64

# Compile Android (catches androidMain errors)
./gradlew :composeApp:compileAndroidMain

# Static analysis — must be zero violations
./gradlew :composeApp:detekt
```

One-liner for a full quality gate:
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64 :composeApp:compileAndroidMain :composeApp:detekt
```

## Key conventions

- Token access: always `SumiTokens.Color.ink`, never via typealias instance (`Sumi.Color.ink` fails cross-package)
- Import alias pattern in component files: `import xyz.ksharma.sumi.theme.SumiTokens as Sumi`
- No ripple: `Modifier.clickable(indication = null, interactionSource = ...)`
- No rounded corners except buttons (`Radius.xs = 2dp`) and sheets (`Radius.lg = 12dp`)
- No emoji, no gradients (except aurora sweep), no Material defaults leaking into UI
- `SumiTheme.typography` for all text styles — never `MaterialTheme.typography`
- Fonts load via `rememberSumiFonts()` which is `@Composable` (suspends on iOS)

## Architecture

```
composeApp/src/commonMain/kotlin/xyz/ksharma/sumi/
├── theme/          SumiTokens, SumiTheme, SumiFonts
├── design/
│   └── components/ WashiBG, SumiButton, SumiChip, …
├── screens/        One subpackage per screen
├── di/             Koin modules
└── App.kt          Root composable
```

## Detekt suppression policy

**Do not suppress detekt violations without checking first.** The default approach is to fix properly.

- `MagicNumber`: okay to suppress in some cases (UI constants, pixel values), but fix first if practical
- Everything else (`MatchingDeclarationName`, `LongMethod`, `ComposableParamOrder`, etc.): **fix properly, do not suppress**
- If you think a suppression is the right call, **ask the user** before adding `@file:Suppress` or `@Suppress`

## Build structure

- Convention plugins in `gradle/build-logic/`
- `composeApp` uses `com.android.kotlin.multiplatform.library` (AGP 9)
- `androidLibrary {}` block (NOT `androidTarget {}`) inside `kotlin {}`
- Always call `applyDefaultHierarchyTemplate()` in `kotlin {}` block
