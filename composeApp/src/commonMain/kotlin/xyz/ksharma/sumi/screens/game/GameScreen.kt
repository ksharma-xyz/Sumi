@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.design.board.SumiBoard
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private val DIFFICULTY_KANJI = mapOf(
    Difficulty.Easy to "一",
    Difficulty.Medium to "二",
    Difficulty.Hard to "三",
    Difficulty.Master to "四",
    Difficulty.Edo to "五",
)

@Composable
fun GameScreen(
    difficulty: String,
    onBack: () -> Unit,
    onWin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: GameViewModel = koinViewModel()
    val diff = Difficulty.entries.firstOrNull { it.name == difficulty } ?: Difficulty.Medium
    val currentOnWin by rememberUpdatedState(onWin)
    val currentOnBack by rememberUpdatedState(onBack)

    LaunchedEffect(difficulty) { vm.init(diff) }

    val state by vm.state.collectAsState()
    var paused by remember { mutableStateOf(false) }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) currentOnWin()
    }

    Box(modifier = modifier.fillMaxSize()) {
        WashiBG(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Sumi.Space.s8))
            GameTopBar(difficulty = diff, elapsedMs = state.elapsedMs, onBack = onBack, onPause = { paused = true })
            Spacer(Modifier.height(Sumi.Space.s3))
            MarksHintsRow(mistakeCount = state.mistakeCount, hintsRemaining = state.hintsRemaining)
            Spacer(Modifier.height(Sumi.Space.s3))
            Box(
                modifier = Modifier
                    .border(1.dp, SumiTheme.colors.ink)
                    .background(SumiTheme.colors.paperGlow)
                    .padding(2.dp),
            ) {
                SumiBoard(state = state, onCellTap = { r, c -> vm.select(r, c) })
            }
            Spacer(Modifier.height(Sumi.Space.s4))
            ToolsRow(
                notesActive = state.notesMode,
                onUndo = { vm.undo() },
                onNote = { vm.toggleNotes() },
                onErase = { vm.erase() },
                onHint = { vm.hint() },
            )
            Spacer(Modifier.height(Sumi.Space.s3))
            NumberPad(state = state, onDigit = { vm.enter(it) })
        }
        if (paused) {
            PauseOverlay(
                onResume = { paused = false },
                onNewPuzzle = {
                    paused = false
                    vm.init(diff)
                },
                onHome = { currentOnBack() },
            )
        }
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
            SumiIcon(
                icon = SumiIcons.Back,
                contentDescription = "Back",
                tint = SumiTheme.colors.ink,
                size = 22.dp,
                modifier = Modifier.clickable(interactionSource = backSrc, indication = null, onClick = onBack),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$kanji · ${difficulty.label}",
                    style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.5.sp),
                    color = SumiTheme.colors.inkFaint,
                )
                Text(
                    text = formatTime(elapsedMs),
                    style = SumiTheme.typography.quote.copy(fontSize = 22.sp, lineHeight = 22.sp),
                    color = SumiTheme.colors.ink,
                )
            }
            SumiIcon(
                icon = SumiIcons.Pause,
                contentDescription = "Pause",
                tint = SumiTheme.colors.ink,
                size = 22.dp,
                modifier = Modifier.clickable(interactionSource = pauseSrc, indication = null, onClick = onPause),
            )
        }
    }
}

@Composable
private fun MarksHintsRow(mistakeCount: Int, hintsRemaining: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SumiTheme.colors.inkSoft)) { append("MARKS ") }
                withStyle(SpanStyle(color = SumiTheme.colors.red, fontWeight = FontWeight.Bold)) {
                    append("$mistakeCount / 3")
                }
            },
            style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.5.sp),
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SumiTheme.colors.inkSoft)) { append("HINTS ") }
                withStyle(SpanStyle(color = SumiTheme.colors.gold, fontWeight = FontWeight.Bold)) {
                    append("$hintsRemaining")
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
                modifier = Modifier.clickable(interactionSource = src, indication = null, onClick = tool.onClick),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                        .height(40.dp)
                        .background(SumiTheme.colors.paperEdge),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clickable(interactionSource = src, indication = null) { onDigit(n) },
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
                    style = SumiTheme.typography.uiMeta.copy(fontSize = 9.sp),
                    color = SumiTheme.colors.inkFaint,
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}
