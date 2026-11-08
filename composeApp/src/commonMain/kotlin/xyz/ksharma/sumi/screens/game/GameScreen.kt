@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.game

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.design.board.BoardEntrance
import xyz.ksharma.sumi.design.board.DIGIT_SIZE_RATIO
import xyz.ksharma.sumi.design.board.NOTE_SIZE_RATIO
import xyz.ksharma.sumi.design.board.SumiBoard
import xyz.ksharma.sumi.design.board.THIN_LINE_ALPHA
import xyz.ksharma.sumi.design.board.THIN_STROKE_MIN
import xyz.ksharma.sumi.design.board.THIN_STROKE_RATIO
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
import xyz.ksharma.sumi.theme.cornerSafeTopPadding
import xyz.ksharma.sumi.theme.safeHorizontalPadding
import xyz.ksharma.sumi.theme.statusBarsWithCornerClearance
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
    highLegibility: Boolean = false,
    strictConflicts: Boolean = false,
    selectedDigit: Int? = null,
    livesMode: Boolean = false,
    boardEntrance: BoardEntrance = BoardEntrance.None,
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
                highLegibility = highLegibility,
                strictConflicts = strictConflicts,
                selectedDigit = selectedDigit,
                livesMode = livesMode,
                boardEntrance = boardEntrance,
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
                    .safeHorizontalPadding()
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

/** Everything the game body draws apart from the callbacks, so the two layouts share one signature. */
private data class GameBodyUi(
    val state: BoardState,
    val elapsedMs: Long,
    val diff: Difficulty,
    val boardReady: Boolean,
    val rewardedHintAvailable: Boolean,
    val showMistakes: Boolean,
    val highLegibility: Boolean,
    val strictConflicts: Boolean,
    val selectedDigit: Int?,
    val livesMode: Boolean,
    val boardEntrance: BoardEntrance,
)

@Suppress("LongParameterList")
@Composable
private fun GameBody(
    state: BoardState,
    elapsedMs: Long,
    diff: Difficulty,
    boardReady: Boolean,
    callbacks: GameCallbacks,
    bottomReservedHeight: Dp = 0.dp,
    rewardedHintAvailable: Boolean = false,
    showMistakes: Boolean = true,
    highLegibility: Boolean = false,
    strictConflicts: Boolean = false,
    selectedDigit: Int? = null,
    livesMode: Boolean = false,
    boardEntrance: BoardEntrance = BoardEntrance.None,
) {
    val ui = GameBodyUi(
        state = state,
        elapsedMs = elapsedMs,
        diff = diff,
        boardReady = boardReady,
        rewardedHintAvailable = rewardedHintAvailable,
        showMistakes = showMistakes,
        highLegibility = highLegibility,
        strictConflicts = strictConflicts,
        selectedDigit = selectedDigit,
        livesMode = livesMode,
        boardEntrance = boardEntrance,
    )
    // The layout follows the window, never the device: wide-and-short windows (iPhone Duo's open
    // display or outer display held sideways, iPad landscape, phones on their side) put the board
    // beside the controls so nothing scrolls; every other window keeps the stacked layout.
    BoxWithConstraints(modifier = Modifier.fillMaxSize().safeHorizontalPadding()) {
        val usableHeight = maxHeight - cornerSafeTopPadding() - bottomReservedHeight
        when (val layout = gameLayoutFor(width = maxWidth, height = usableHeight)) {
            GameLayout.Stacked -> StackedGameBody(
                ui = ui,
                callbacks = callbacks,
                bottomReservedHeight = bottomReservedHeight,
                fitToHeight = stackedFitsHeight(usableHeight),
            )
            is GameLayout.SideBySide -> SideBySideGameBody(ui, callbacks, layout, bottomReservedHeight)
        }
    }
}

@Composable
private fun StackedGameBody(
    ui: GameBodyUi,
    callbacks: GameCallbacks,
    bottomReservedHeight: Dp,
    fitToHeight: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Tall enough: nothing scrolls and the board takes the height the controls leave
            // (iPhone Duo's outer display, where a full-width board pushed the number pad under
            // the banner). Too short (phones on their side): scroll instead of clipping.
            .then(if (fitToHeight) Modifier else Modifier.verticalScroll(rememberScrollState()))
            .statusBarsWithCornerClearance()
            // Tight side margin so the grid (and number-pad tap targets) get
            // as much width as possible — easier to read / tap, better for
            // low vision. MAX_BOARD_WIDTH still caps it on tablets.
            .padding(horizontal = Sumi.Space.s2)
            .padding(bottom = bottomReservedHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Sumi.Space.s3))
        GameTopBar(
            difficulty = ui.diff,
            elapsedMs = ui.elapsedMs,
            onBack = callbacks.onBack,
            onPause = callbacks.onPause,
        )
        Spacer(Modifier.height(Sumi.Space.s3))
        GameMarksRow(ui)
        Spacer(Modifier.height(Sumi.Space.s3))
        // BoxWithConstraints lets the board scale to fill the available width —
        // up to MAX_BOARD_WIDTH (560dp) so it grows properly on tablets but
        // doesn't dominate a 12-inch display. SumiBoard owns its own perimeter
        // so no wrapping border / padding is needed (would have caused the
        // interior lines to overhang the perimeter).
        BoxWithConstraints(
            modifier = if (fitToHeight) Modifier.weight(1f, fill = false) else Modifier,
            contentAlignment = Alignment.Center,
        ) {
            val side = if (fitToHeight) minOf(maxWidth, maxHeight) else maxWidth
            BoardArea(ui = ui, callbacks = callbacks, cellSize = minOf(side, MAX_BOARD_WIDTH) / 9)
        }
        Spacer(Modifier.height(Sumi.Space.s4))
        ToolsRow(tools = gameTools(ui.state, callbacks))
        Spacer(Modifier.height(Sumi.Space.s3))
        NumberPad(
            state = ui.state,
            selectedDigit = ui.selectedDigit,
            highLegibility = ui.highLegibility,
            onDigit = callbacks.onEnter,
        )
    }
}

/**
 * One centred unit on a grid of board cells (u = one cell, board = 9u x 9u). The top bar spans board
 * and panel. Beside the board a panel 6u wide and 9u tall, every horizontal line on a board line:
 * row 0 (1u) marks and hints, rows 1-2 (2u) the tools, rows 3-8 (6u) a 3x3 pad of square 2u cells
 * whose top edge is the board's first thick box line and whose bottom edge is the board bottom.
 */
@Composable
private fun SideBySideGameBody(
    ui: GameBodyUi,
    callbacks: GameCallbacks,
    layout: GameLayout.SideBySide,
    bottomReservedHeight: Dp,
) {
    val u = layout.cell
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsWithCornerClearance()
            .padding(bottom = bottomReservedHeight),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.width(layout.unitWidth)) {
            GameTopBar(
                difficulty = ui.diff,
                elapsedMs = ui.elapsedMs,
                onBack = callbacks.onBack,
                onPause = callbacks.onPause,
                edgeAligned = true,
            )
            Spacer(Modifier.height(SIDE_TOP_BAR_GAP))
            Row(
                modifier = Modifier.fillMaxWidth().height(layout.boardSize),
                horizontalArrangement = Arrangement.spacedBy(layout.gap),
            ) {
                BoardArea(ui = ui, callbacks = callbacks, cellSize = u)
                Column(modifier = Modifier.width(layout.panelWidth).fillMaxHeight()) {
                    Box(modifier = Modifier.fillMaxWidth().height(u), contentAlignment = Alignment.Center) {
                        GameMarksRow(ui)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(u * 2), contentAlignment = Alignment.Center) {
                        ToolsRow(tools = gameTools(ui.state, callbacks), slotWidth = u * PANEL_CELLS / 5)
                    }
                    NumberPadGrid(
                        state = ui.state,
                        selectedDigit = ui.selectedDigit,
                        highLegibility = ui.highLegibility,
                        onDigit = callbacks.onEnter,
                        cell = u,
                    )
                }
            }
        }
    }
}

@Composable
private fun GameMarksRow(ui: GameBodyUi) {
    MarksHintsRow(
        mistakeCount = ui.state.mistakeCount,
        hintsRemaining = ui.state.hintsRemaining,
        showMistakes = ui.showMistakes,
        livesMode = ui.livesMode,
        rewardedHintAvailable = ui.rewardedHintAvailable,
    )
}

/**
 * The board's square footprint, reserved even before the puzzle is ready so the tools row and
 * number pad never jump when the grid arrives. The grid fades in from the blank paper.
 */
@Composable
private fun BoardArea(ui: GameBodyUi, callbacks: GameCallbacks, cellSize: Dp) {
    Box(modifier = Modifier.size(cellSize * 9), contentAlignment = Alignment.Center) {
        // With the ink-bloom entrance, the per-cell bloom IS the reveal — fade the
        // container in fast so the diagonal pop reads instead of being washed out by
        // a slow uniform crossfade. Otherwise keep the slow, calm grid fade.
        val boardFadeMs = if (ui.boardEntrance == BoardEntrance.InkBloom) 140 else 620
        Crossfade(
            targetState = ui.boardReady,
            animationSpec = tween(boardFadeMs, easing = Sumi.Ease.paper),
            label = "boardFade",
        ) { ready ->
            if (ready) {
                SumiBoard(
                    state = ui.state,
                    cellSize = cellSize,
                    highLegibility = ui.highLegibility,
                    strictConflicts = ui.strictConflicts,
                    entrance = ui.boardEntrance,
                    onCellTap = { r, c -> callbacks.onSelect(r, c) },
                )
            }
        }
    }
}

@Composable
private fun GameTopBar(
    difficulty: Difficulty,
    elapsedMs: Long,
    onBack: () -> Unit,
    onPause: () -> Unit,
    edgeAligned: Boolean = false,
) {
    val backSrc = remember { MutableInteractionSource() }
    val pauseSrc = remember { MutableInteractionSource() }
    val kanji = DIFFICULTY_KANJI[difficulty] ?: "一"
    // Edge-aligned (side by side): a fixed-height bar with no bottom padding, and the back arrow and
    // pause icon pushed to the outer edges so they sit on the board's left / panel's right edge.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (edgeAligned) Modifier else Modifier.padding(bottom = Sumi.Space.s3)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (edgeAligned) Modifier.height(SIDE_TOP_BAR_HEIGHT) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(Sumi.Layout.minTap)
                    .clickable(interactionSource = backSrc, indication = null, onClick = onBack),
                contentAlignment = if (edgeAligned) Alignment.CenterStart else Alignment.Center,
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
                contentAlignment = if (edgeAligned) Alignment.CenterEnd else Alignment.Center,
            ) {
                SumiIcon(
                    icon = SumiIcons.Pause,
                    contentDescription = "Pause",
                    tint = SumiTheme.colors.ink,
                    size = 22.dp,
                    // The pause vector has blank space at its right; push the visible bars to the edge.
                    modifier = if (edgeAligned) Modifier.offset(x = PAUSE_GLYPH_INSET) else Modifier,
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
    livesMode: Boolean = false,
    rewardedHintAvailable: Boolean = false,
) {
    // In lives mode the remaining lives always show (they decide the game); otherwise the
    // mistake count shows only when the player has opted into it.
    val showLeft = livesMode || showMistakes
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (showLeft) Arrangement.SpaceBetween else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (livesMode) {
            val livesLeft = (MAX_LIVES - mistakeCount).coerceAtLeast(0)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SumiTheme.colors.inkSoft)) { append("LIVES ") }
                    withStyle(SpanStyle(color = SumiTheme.colors.error, fontWeight = FontWeight.Bold)) {
                        append("$livesLeft")
                    }
                },
                style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.5.sp),
            )
        } else if (showMistakes) {
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

private data class Tool(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val active: Boolean = false,
    val enabled: Boolean = true,
    val onLongClick: (() -> Unit)? = null,
)

// Tool row for the game screen. Note long-press auto-fills pencil marks (one-shot).
private fun gameTools(state: BoardState, callbacks: GameCallbacks): List<Tool> = listOf(
    Tool(SumiIcons.Undo, "Undo", callbacks.onUndo, enabled = state.canUndo),
    Tool(SumiIcons.Redo, "Redo", callbacks.onRedo, enabled = state.canRedo),
    Tool(
        icon = SumiIcons.Lantern,
        label = "Note",
        onClick = callbacks.onToggleNotes,
        active = state.notesMode,
        onLongClick = callbacks.onFillNotes,
    ),
    Tool(SumiIcons.Erase, "Erase", callbacks.onErase),
    Tool(SumiIcons.Sparkle, "Hint", callbacks.onHint),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolsRow(tools: List<Tool>, slotWidth: Dp? = null) {
    // With a slot width (side by side) each tool owns an equal slice of the panel and the button
    // shrinks to fit it; otherwise the row spaces tools around at the standard tap size.
    val box = if (slotWidth == null) Sumi.Layout.minTap else minOf(Sumi.Layout.minTap, slotWidth - 4.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (slotWidth == null) Arrangement.SpaceAround else Arrangement.SpaceBetween,
    ) {
        tools.forEach { tool ->
            val src = remember { MutableInteractionSource() }
            val slotModifier = if (slotWidth == null) {
                Modifier.minimumInteractiveComponentSize()
            } else {
                Modifier.width(slotWidth)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = slotModifier
                    .combinedClickable(
                        interactionSource = src,
                        indication = null,
                        enabled = tool.enabled,
                        onClick = tool.onClick,
                        onLongClick = tool.onLongClick,
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .size(box)
                        .border(1.dp, SumiTheme.colors.paperEdge),
                    contentAlignment = Alignment.Center,
                ) {
                    SumiIcon(
                        icon = tool.icon,
                        contentDescription = tool.label,
                        tint = when {
                            !tool.enabled -> SumiTheme.colors.inkFaint
                            tool.active -> SumiTheme.colors.teal
                            else -> SumiTheme.colors.ink
                        },
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
private fun NumberPad(
    state: BoardState,
    selectedDigit: Int?,
    highLegibility: Boolean,
    onDigit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Match the board: high-legibility mode uses the UI sans family for the pad digits too.
    val digitStyle = SumiTheme.typography.numeral.copy(fontSize = 26.sp).let {
        if (highLegibility) it.copy(fontFamily = SumiTheme.typography.uiLabel.fontFamily) else it
    }
    // Row uses IntrinsicSize.Min so the dividers stretch to whatever the
    // tallest number cell needs. Cells use intrinsic height (no aspectRatio
    // square constraint) so the remaining-count badge below the digit can
    // grow at large system font scales without getting clipped — that was
    // the bug where the count got cut off.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(width = 1.dp, color = SumiTheme.colors.paperEdge)
            .padding(vertical = Sumi.Space.s2),
    ) {
        for (n in 1..9) {
            if (n > 1) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(SumiTheme.colors.paperEdge),
                )
            }
            NumberCell(
                n = n,
                state = state,
                armed = n == selectedDigit,
                digitStyle = digitStyle,
                onDigit = onDigit,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Blank space between the pause icon's box and its visible bars. */
private val PAUSE_GLYPH_INSET = 5.5.dp

/** The sans count badge matches the board's Caveat pencil marks at this fraction of their size (Caveat reads smaller). */
private const val BADGE_TO_NOTE = 0.7f

/**
 * 3x3 keypad for the side-by-side layout: nine square cells of 2 x 2 board cells, 6 x 6 cells in
 * all. Lines use the board's thin-line weight and colour, centred on the board's grid lines (the
 * top edge on the first thick box line, inner lines on rows 5 and 7, bottom edge on the board
 * bottom). Digits use the board's given-digit size and the count badge the pencil-mark size.
 */
@Composable
private fun NumberPadGrid(
    state: BoardState,
    selectedDigit: Int?,
    highLegibility: Boolean,
    onDigit: (Int) -> Unit,
    cell: Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val digitSp = with(density) { (cell * DIGIT_SIZE_RATIO).toSp() }
    val badgeSp = with(density) { (cell * NOTE_SIZE_RATIO * BADGE_TO_NOTE).toSp() }
    val baseStyle = SumiTheme.typography.numeral.let {
        if (highLegibility) it.copy(fontFamily = SumiTheme.typography.uiLabel.fontFamily) else it
    }
    val digitStyle = baseStyle.copy(fontSize = digitSp, lineHeight = digitSp)
    val lineColor = SumiTheme.colors.ink.copy(alpha = THIN_LINE_ALPHA)
    Box(
        modifier = modifier
            .size(cell * PANEL_CELLS)
            .drawBehind {
                val px = cell.toPx()
                val stroke = (cell.value * THIN_STROKE_RATIO).dp.toPx().coerceAtLeast(THIN_STROKE_MIN.toPx())
                val side = px * PANEL_CELLS
                for (i in 0..3) {
                    // Top edge is centred on the board's thick box line; the far edges sit inside.
                    val pos = when (i) {
                        0 -> 0f
                        3 -> side - stroke / 2f
                        else -> i * 2 * px
                    }
                    val xPos = if (i == 0) stroke / 2f else pos
                    drawLine(lineColor, Offset(0f, pos), Offset(side, pos), stroke)
                    drawLine(lineColor, Offset(xPos, 0f), Offset(xPos, side), stroke)
                }
            },
    ) {
        for (rowIndex in 0 until 3) {
            for (colIndex in 0 until 3) {
                val n = rowIndex * 3 + colIndex + 1
                NumberCell(
                    n = n,
                    state = state,
                    armed = n == selectedDigit,
                    digitStyle = digitStyle,
                    onDigit = onDigit,
                    modifier = Modifier
                        .offset(x = cell * (colIndex * 2), y = cell * (rowIndex * 2))
                        .size(cell * 2),
                    badgeSp = badgeSp,
                    fillCell = true,
                )
            }
        }
    }
}

@Composable
private fun NumberCell(
    n: Int,
    state: BoardState,
    armed: Boolean,
    digitStyle: TextStyle,
    onDigit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badgeSp: TextUnit = 11.sp,
    fillCell: Boolean = false,
) {
    val src = remember { MutableInteractionSource() }
    val remaining = state.remainingCounts.getOrElse(n - 1) { 0 }
    Column(
        modifier = modifier
            .then(if (fillCell) Modifier else Modifier.minimumInteractiveComponentSize())
            // Armed digit (digit-first mode) gets a soft teal wash so it's clear
            // which number the next cell tap will place.
            .background(if (armed) SumiTheme.colors.teal.copy(alpha = 0.14f) else Color.Transparent)
            .then(if (fillCell) Modifier else Modifier.padding(vertical = Sumi.Space.s2))
            .clickable(interactionSource = src, indication = null, enabled = remaining > 0) { onDigit(n) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = n.toString(),
            style = digitStyle,
            color = when {
                armed -> SumiTheme.colors.teal
                remaining > 0 -> SumiTheme.colors.ink
                else -> SumiTheme.colors.inkGhost
            },
        )
        Text(
            text = if (remaining > 0) remaining.toString() else "",
            style = SumiTheme.typography.uiMeta.copy(fontSize = badgeSp),
            color = SumiTheme.colors.inkFaint,
        )
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
