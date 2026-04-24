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
├── navigation/
│   └── entries/    NavEntry composables (own the ViewModels)
├── di/             Koin modules
└── App.kt          Root composable
```

### ViewModel pattern (mandatory)

ViewModels are created **only** inside `Entry` composables (the `entry<Route> { ... }` blocks in `navigation/entries/`). Screen composables are pure — they receive only plain state values and callback lambdas.

```kotlin
// CORRECT — VM created at Entry level
entry<GameRoute> { key ->
    val vm: GameViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    GameScreen(state = state, onSelect = { r, c -> vm.select(r, c) })
}

// WRONG — never create VMs inside screen composables
@Composable
fun GameScreen(...) {
    val vm: GameViewModel = koinViewModel()  // ❌
}
```

### Business logic in ViewModel

Screens and components are **display-only**. All calculations, formatting, mapping, and business logic live in the ViewModel and are resolved before data reaches the UI.

- Time formatting → ViewModel (or helper called from VM), not inside `Text { ... }`
- Derived values (e.g. remaining counts, streak labels) → computed in VM, passed as plain fields
- Condition checks beyond simple `if (visible)` → ViewModel

### Immutable collections

Use standard immutable `List<T>` / `Set<T>` with `remember { mutableStateOf(emptyList()) }`. Never use `mutableStateListOf()` or `mutableStateMapOf()` for state that is replaced atomically.

```kotlin
// CORRECT
var sweeps by remember { mutableStateOf(emptyList<Sweep>()) }
sweeps = sweeps + newItem       // creates a new list
sweeps = sweeps - oldItem

// WRONG
val sweeps = remember { mutableStateListOf<Sweep>() }  // ❌
```

For complex stable data passed to composables, use `ImmutableList`/`ImmutableSet` from `kotlinx-collections-immutable` (tells Compose the type is stable so it can skip recomposition).

### rememberSaveable vs remember

- **`rememberSaveable`** — use for primitive/String UI state that must survive screen rotation and process death (e.g. `paused`, `gameOver`, selected tab). It's the right default for simple flags and strings.
- **`remember`** — use for transient animation state, interaction sources, or objects that can't be serialised (e.g. `Animatable`, `MutableInteractionSource`, short-lived sweep lists).
- Complex cross-screen state lives in the ViewModel's `StateFlow`, not in composable memory.

## Detekt suppression policy

**Do not suppress detekt violations without checking first.** The default approach is to fix properly.

`autoCorrect: true` is set in `config/detekt.yml` — import ordering and trailing commas are fixed in-place automatically.

Suppression guidance by rule:
- `MaximumLineLength` / `MaxLineLength` — break the line, never suppress
- `MagicNumber` — extract a named constant; suppress only if the value has no semantic meaning beyond layout maths
- `MatchingDeclarationName`, `ComposableParamOrder` — **fix properly, do not suppress**
- `LongMethod` — extract private helpers first; suppress only when refactoring is genuinely impossible (rare)
- `CyclomaticComplexMethod` — same: simplify first, suppress only as last resort
- If you think a suppression is the right call, **ask the user** before adding `@file:Suppress` or `@Suppress`

## Test commands

KMP modules with `androidLibrary { withHostTestBuilder {} }` expose `testAndroidHostTest` — **not** `testDebugUnitTest`, `jvmTest`, or `allTests`.

| Scope | Command |
|---|---|
| Single module | `./gradlew :game:testAndroidHostTest` |
| All modules | `./gradlew testAndroidHostTest --continue` |

To enable host tests in a new module, add `withHostTestBuilder {}` inside `androidLibrary {}` in its `build.gradle.kts`.

## Gradle dependencies

Always use **type-safe project accessors** — never the string form.

```kotlin
// ✅ correct
implementation(projects.share)
implementation(projects.game)
implementation(libs.di.koinComposeViewmodel)

// ❌ wrong — do not use
implementation(project(":share"))
implementation(project(":game"))
```

`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` is active in `settings.gradle.kts`.
Accessor name mirrors the directory path with dots (`game/` → `projects.game`).

## LazyColumn / LazyRow item keys

**Always provide an explicit `key` for every `item {}` call** — critical for correct recomposition, scroll-state preservation, and animation.

```kotlin
// ✅ correct — stable, unique key per item
item(key = "header-streak") { ... }
items(puzzles, key = { it.id }) { ... }

// ❌ wrong — positional identity breaks on reorder/insert
item { ... }
```

Key rules:
- Static items: descriptive string literal (`"header-streak"`, `"spacer-bottom"`)
- Dynamic items: stable domain identifier (e.g. `puzzle.id`, `stat.label`)
- If the same data appears twice in one list, prefix keys to keep them unique

## Build structure

- Convention plugins in `gradle/build-logic/`
- `composeApp` uses `com.android.kotlin.multiplatform.library` (AGP 9)
- `androidLibrary {}` block (NOT `androidTarget {}`) inside `kotlin {}`
- Always call `applyDefaultHierarchyTemplate()` in `kotlin {}` block

### Compose Multiplatform resources (AGP 9)

Any module that ships `composeResources/` (fonts, images, XML drawables) **must** have both of these or resources are silently dropped from Android builds:

```kotlin
// inside androidLibrary {}
androidResources {
    enable = true   // MANDATORY for AGP 9 — packages composeResources/ as Android assets
}
```

```kotlin
// top-level, outside kotlin {}
compose.resources {
    publicResClass = true
    packageOfResClass = "xyz.ksharma.sumi.resources"
    generateResClass = auto
}
```

Without `androidResources { enable = true }`, AGP 9 skips the asset merging step and fonts/icons will be missing at runtime on Android.
