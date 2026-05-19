@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.game

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.design.board.SumiBoard
import xyz.ksharma.sumi.design.components.PetalBurstConfig
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.SumiPetalBurst
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.components.WashiVariant
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.platform.KeepScreenOn
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private val DIFFICULTY_KANJI = mapOf(
    Difficulty.Easy to "一",
    Difficulty.Medium to "二",
    Difficulty.Hard to "三",
    Difficulty.Master to "四",
    Difficulty.Edo to "五",
)

// Same graceful, drifting character as the full-grid shower below — just
// fewer petals so a row / column / box completion reads as a smaller version
// of the win moment rather than a separate, abrupt effect.
private val LINE_BURST = PetalBurstConfig(
    count = 16,
    sizeMultiplier = 2.0f,
    durationMs = 2_800,
    swayScale = 1.0f,
)

private val GRID_BURST = PetalBurstConfig(
    count = 48,
    sizeMultiplier = 2.4f,
    durationMs = 3_400,
    swayScale = 1.2f,
)

/** Cap board width on tablets so the grid scales up without dominating wide screens. */
private val MAX_BOARD_WIDTH = 560.dp

@Suppress("LongParameterList")
@Composable
fun GameScreen(
    state: BoardState,
    elapsedMs: Long,
    celebrationCount: Int,
    gridCelebrationCount: Int,
    isInitializing: Boolean,
    paused: Boolean,
    difficulty: Difficulty,
    callbacks: GameCallbacks,
    modifier: Modifier = Modifier,
    bottomBanner: (@Composable () -> Unit)? = null,
    rewardedHintAvailable: Boolean = false,
    showMistakes: Boolean = true,
) {
    // A solve can take a long time with little screen interaction — keep the
    // display awake for the whole Game screen (auto-reverts on leave).
    KeepScreenOn()
    Box(modifier = modifier.fillMaxSize()) {
        // Game content — blurred when paused so the scrim reads as frosted glass
        val blurMod = if (paused) Modifier.blur(24.dp, BlurredEdgeTreatment.Unbounded) else Modifier
        Box(modifier = Modifier.fillMaxSize().then(blurMod)) {
            WashiBG(modifier = Modifier.fillMaxSize(), variant = WashiVariant.Quiet)
            // The whole game scaffold (timer, controls, number pad) shows
            // immediately so the screen feels responsive. Only the 9×9 grid
            // is withheld until the puzzle is ready — it fades in over the
            // blank board area (see GameBody's `boardReady`). No stale grid,
            // no full-screen spinner, no flash.
            GameBody(
                state = state,
                elapsedMs = elapsedMs,
                diff = difficulty,
                boardReady = !isInitializing,
                callbacks = callbacks,
                bottomReservedHeight = if (bottomBanner != null) 64.dp else 0.dp,
                rewardedHintAvailable = rewardedHintAvailable,
                showMistakes = showMistakes,
            )
        }
        // Subtle, sparse petals for row / column / 3x3 completions — they should
        // feel like a wink, not a parade. The big shower is reserved for the
        // full grid (below) so the "you finished!" moment lands distinctly.
        SumiPetalBurst(
            trigger = celebrationCount,
            modifier = Modifier.fillMaxSize(),
            config = LINE_BURST,
        )
        // Full-grid completion shower — held for ~3s before the Win navigation
        // fires (see GameEntry's WIN_CELEBRATION_DWELL_MS). Premium pacing.
        SumiPetalBurst(
            trigger = gridCelebrationCount,
            modifier = Modifier.fillMaxSize(),
            config = GRID_BURST,
        )
        GameAnnouncer(state = state)
        if (bottomBanner != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) { bottomBanner() }
        }
        if (paused) {
            PauseOverlay(
                onResume = callbacks.onResume,
                onNewPuzzle = callbacks.onNewPuzzle,
                onHome = callbacks.onBack,
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun GameBody(
    state: BoardState,
    elapsedMs: Long,
    diff: Difficulty,
    boardReady: Boolean,
    callbacks: GameCallbacks,
    bottomReservedHeight: androidx.compose.ui.unit.Dp = 0.dp,
    rewardedHintAvailable: Boolean = false,
    showMistakes: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Lets landscape + small phones scroll the
            // board + tools + number-pad stack instead of clipping the bottom.
            .windowInsetsPadding(WindowInsets.statusBars)
            // Tight side margin so the grid (and number-pad tap targets) get
            // as much width as possible — easier to read / tap, better for
            // low vision. MAX_BOARD_WIDTH still caps it on tablets.
            .padding(horizontal = Sumi.Space.s3)
            .padding(bottom = bottomReservedHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Sumi.Space.s3))
        GameTopBar(difficulty = diff, elapsedMs = elapsedMs, onBack = callbacks.onBack, onPause = callbacks.onPause)
        Spacer(Modifier.height(Sumi.Space.s3))
        MarksHintsRow(
            mistakeCount = state.mistakeCount,
            hintsRemaining = state.hintsRemaining,
            showMistakes = showMistakes,
            rewardedHintAvailable = rewardedHintAvailable,
        )
        Spacer(Modifier.height(Sumi.Space.s3))
        // BoxWithConstraints lets the board scale to fill the available width —
        // up to MAX_BOARD_WIDTH (560dp) so it grows properly on tablets but
        // doesn't dominate a 12-inch display. SumiBoard owns its own perimeter
        // so no wrapping border / padding is needed (would have caused the
        // interior lines to overhang the perimeter).
        androidx.compose.foundation.layout.BoxWithConstraints(
            contentAlignment = Alignment.Center,
        ) {
            val resolvedCellSize = minOf(maxWidth, MAX_BOARD_WIDTH) / 9
            // Reserve the board's square footprint so the tools row + number
            // pad never jump when the grid arrives. The grid itself fades in
            // from the blank paper once the puzzle is ready.
            Box(modifier = Modifier.size(resolvedCellSize * 9), contentAlignment = Alignment.Center) {
                Crossfade(
                    targetState = boardReady,
                    // Slow, calm reveal — the grid eases in rather than
                    // snapping. Matches the zen pacing of the rest of the app.
                    animationSpec = tween(620, easing = Sumi.Ease.paper),
                    label = "boardFade",
                ) { ready ->
                    if (ready) {
                        SumiBoard(
                            state = state,
                            cellSize = resolvedCellSize,
                            onCellTap = { r, c -> callbacks.onSelect(r, c) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(Sumi.Space.s4))
        ToolsRow(
            notesActive = state.notesMode,
            onUndo = callbacks.onUndo,
            onNote = callbacks.onToggleNotes,
            onErase = callbacks.onErase,
            onHint = callbacks.onHint,
        )
        Spacer(Modifier.height(Sumi.Space.s3))
        NumberPad(state = state, onDigit = callbacks.onEnter)
    }
}

@Composable
private fun GameTopBar(difficulty: Difficulty, elapsedMs: Long, onBack: () -> Unit, onPause: () -> Unit) {
    val backSrc = remember { MutableInteractionSource() }
    val pauseSrc = remember { MutableInteractionSource() }
    val kanji = DIFFICULTY_KANJI[difficulty] ?: "一"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Sumi.Space.s3),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(Sumi.Layout.minTap)
                    .clickable(interactionSource = backSrc, indication = null, onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "←", style = SumiTheme.typography.h3, color = SumiTheme.colors.ink)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$kanji  ${difficulty.label}",
                    style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.5.sp),
                    color = SumiTheme.colors.inkFaint,
                )
                Text(
                    text = formatTime(elapsedMs),
                    style = SumiTheme.typography.quote.copy(fontSize = 22.sp, lineHeight = 22.sp),
                    color = SumiTheme.colors.ink,
                )
            }
            Box(
                modifier = Modifier
                    .size(Sumi.Layout.minTap)
                    .clickable(interactionSource = pauseSrc, indication = null, onClick = onPause),
                contentAlignment = Alignment.Center,
            ) {
                SumiIcon(
                    icon = SumiIcons.Pause,
                    contentDescription = "Pause",
                    tint = SumiTheme.colors.ink,
                    size = 22.dp,
                )
            }
        }
    }
}

@Composable
private fun MarksHintsRow(
    mistakeCount: Int,
    hintsRemaining: Int,
    showMistakes: Boolean,
    rewardedHintAvailable: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (showMistakes) Arrangement.SpaceBetween else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showMistakes) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SumiTheme.colors.inkSoft)) { append("MISTAKES ") }
                    // colors.error (bold red, identical light/dark) — the
                    // mistake count must read at a glance regardless of theme.
                    withStyle(SpanStyle(color = SumiTheme.colors.error, fontWeight = FontWeight.Bold)) {
                        append("$mistakeCount")
                    }
                },
                style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.5.sp),
            )
        }
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SumiTheme.colors.inkSoft)) { append("HINTS ") }
                withStyle(SpanStyle(color = SumiTheme.colors.gold, fontWeight = FontWeight.Bold)) {
                    append(if (hintsRemaining == BoardState.HINTS_UNLIMITED) "∞" else "$hintsRemaining")
                }
                // Hint indicator: tells the user the empty Hint button can earn one via rewarded ad.
                if (hintsRemaining == 0 && rewardedHintAvailable) {
                    withStyle(SpanStyle(color = SumiTheme.colors.inkSoft)) { append("  /  ") }
                    withStyle(SpanStyle(color = SumiTheme.colors.teal, fontWeight = FontWeight.Bold)) {
                        append("+1 AD")
                    }
                }
            },
            style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.5.sp),
        )
    }
}

@Composable
private fun ToolsRow(
    notesActive: Boolean,
    onUndo: () -> Unit,
    onNote: () -> Unit,
    onErase: () -> Unit,
    onHint: () -> Unit,
) {
    data class Tool(val icon: ImageVector, val label: String, val onClick: () -> Unit, val active: Boolean = false)
    val tools = listOf(
        Tool(SumiIcons.Undo, "Undo", onUndo),
        Tool(SumiIcons.Lantern, "Note", onNote, notesActive),
        Tool(SumiIcons.Erase, "Erase", onErase),
        Tool(SumiIcons.Sparkle, "Hint", onHint),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        tools.forEach { tool ->
            val src = remember { MutableInteractionSource() }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(interactionSource = src, indication = null, onClick = tool.onClick),
            ) {
                Box(
                    modifier = Modifier
                        .size(Sumi.Layout.minTap)
                        .border(1.dp, SumiTheme.colors.paperEdge),
                    contentAlignment = Alignment.Center,
                ) {
                    SumiIcon(
                        icon = tool.icon,
                        contentDescription = tool.label,
                        tint = if (tool.active) SumiTheme.colors.teal else SumiTheme.colors.ink,
                        size = 20.dp,
                    )
                }
                Text(
                    text = tool.label.uppercase(),
                    style = SumiTheme.typography.uiMeta.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight(600),
                    ),
                    color = SumiTheme.colors.inkFaint,
                )
            }
        }
    }
}

@Composable
private fun NumberPad(state: BoardState, onDigit: (Int) -> Unit) {
    // Row uses IntrinsicSize.Min so the dividers stretch to whatever the
    // tallest number cell needs. Cells use intrinsic height (no aspectRatio
    // square constraint) so the remaining-count badge below the digit can
    // grow at large system font scales without getting clipped — that was
    // the bug where the count got cut off.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
            .border(width = 1.dp, color = SumiTheme.colors.paperEdge)
            .padding(vertical = Sumi.Space.s2),
    ) {
        for (n in 1..9) {
            val src = remember { MutableInteractionSource() }
            val remaining = state.remainingCounts.getOrElse(n - 1) { 0 }
            if (n > 1) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(SumiTheme.colors.paperEdge),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .padding(vertical = Sumi.Space.s2)
                    .clickable(interactionSource = src, indication = null, enabled = remaining > 0) { onDigit(n) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = n.toString(),
                    style = SumiTheme.typography.numeral.copy(fontSize = 26.sp),
                    color = if (remaining > 0) SumiTheme.colors.ink else SumiTheme.colors.inkGhost,
                )
                Text(
                    text = if (remaining > 0) remaining.toString() else "",
                    style = SumiTheme.typography.uiMeta.copy(fontSize = 11.sp),
                    color = SumiTheme.colors.inkFaint,
                )
            }
        }
    }
}

@Composable
private fun GameAnnouncer(state: BoardState) {
    var announcement by remember { mutableStateOf("") }
    var prevRows by remember { mutableStateOf(emptySet<Int>()) }
    var prevCols by remember { mutableStateOf(emptySet<Int>()) }
    var prevBoxes by remember { mutableStateOf(emptySet<Int>()) }
    var prevErrors by remember { mutableStateOf(emptySet<Pair<Int, Int>>()) }

    LaunchedEffect(state.completedRows, state.completedCols, state.completedBoxes, state.isComplete, state.errorCells) {
        val newRows = state.completedRows - prevRows
        val newCols = state.completedCols - prevCols
        val newBoxes = state.completedBoxes - prevBoxes
        val newErrors = state.errorCells - prevErrors
        announcement = when {
            state.isComplete -> "Puzzle complete"
            newRows.isNotEmpty() -> "Row ${newRows.min() + 1} complete"
            newCols.isNotEmpty() -> "Column ${newCols.min() + 1} complete"
            newBoxes.isNotEmpty() -> "Box complete"
            newErrors.isNotEmpty() -> "Conflict"
            else -> announcement
        }
        prevRows = state.completedRows
        prevCols = state.completedCols
        prevBoxes = state.completedBoxes
        prevErrors = state.errorCells
    }

    Box(
        modifier = Modifier
            .size(1.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = announcement
            },
    )
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}
