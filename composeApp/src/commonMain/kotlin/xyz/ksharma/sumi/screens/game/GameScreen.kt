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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.design.board.SumiBoard
import xyz.ksharma.sumi.design.components.SumiChip
import xyz.ksharma.sumi.design.components.SumiChipTone
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

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

    LaunchedEffect(difficulty) { vm.init(diff) }

    val state by vm.state.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) currentOnWin()
    }

    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GameTopBar(
                title = "Daily puzzle",
                difficulty = diff,
                elapsedMs = state.elapsedMs,
                onBack = onBack,
            )

            Spacer(Modifier.height(Sumi.Space.s3))

            SumiBoard(
                state = state,
                onCellTap = { r, c -> vm.select(r, c) },
            )

            Spacer(Modifier.height(Sumi.Space.s4))

            DigitLedger(state = state, onDigitTap = { vm.enter(it) })

            Spacer(Modifier.height(Sumi.Space.s4))

            ToolsRow(
                notesActive = state.notesMode,
                hintsRemaining = state.hintsRemaining,
                onUndo = { vm.undo() },
                onErase = { vm.erase() },
                onNotes = { vm.toggleNotes() },
                onHint = { vm.hint() },
            )

            Spacer(Modifier.height(Sumi.Space.s4))

            NumberPad(onDigit = { vm.enter(it) })
        }
    }
}

@Composable
private fun GameTopBar(
    title: String,
    difficulty: Difficulty,
    elapsedMs: Long,
    onBack: () -> Unit,
) {
    val backSource = remember { MutableInteractionSource() }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Sumi.Space.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SumiIcon(
                icon = SumiIcons.Back,
                contentDescription = "Back",
                tint = Sumi.Color.ink,
                size = 24.dp,
                modifier = Modifier.clickable(interactionSource = backSource, indication = null, onClick = onBack),
            )
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = SumiTheme.typography.uiMeta, color = Sumi.Color.inkFaint)
                SumiChip(text = difficulty.label, tone = SumiChipTone.Red)
            }
            Spacer(Modifier.weight(1f))
            SumiIcon(icon = SumiIcons.Pause, contentDescription = "Pause", tint = Sumi.Color.inkFaint, size = 24.dp)
        }
        Text(
            text = formatTime(elapsedMs),
            style = SumiTheme.typography.h3.copy(fontSize = 15.sp),
            color = Sumi.Color.inkFaint,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun DigitLedger(state: BoardState, onDigitTap: (Int) -> Unit) {
    val remaining = state.remainingCounts
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (d in 1..9) {
            val count = remaining[d - 1]
            val src = remember { MutableInteractionSource() }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(interactionSource = src, indication = null) { onDigitTap(d) },
            ) {
                Text(
                    text = d.toString(),
                    style = SumiTheme.typography.numeral,
                    color = if (count > 0) Sumi.Color.ink else Sumi.Color.inkGhost,
                )
                Text(
                    text = if (count > 0) count.toString() else "",
                    style = SumiTheme.typography.uiMeta,
                    color = Sumi.Color.inkFaint,
                )
            }
        }
    }
}

@Composable
private fun ToolsRow(
    notesActive: Boolean,
    hintsRemaining: Int,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onNotes: () -> Unit,
    onHint: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ToolButton(SumiIcons.Undo, "Undo", onClick = onUndo)
        ToolButton(SumiIcons.Erase, "Erase", onClick = onErase)
        ToolButton(
            icon = SumiIcons.Lantern,
            label = "Notes",
            onClick = onNotes,
            tint = if (notesActive) Sumi.Color.teal else Sumi.Color.inkFaint,
        )
        Box {
            ToolButton(SumiIcons.Sparkle, "Hint", onClick = onHint)
            if (hintsRemaining > 0) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Sumi.Color.red)
                        .align(Alignment.TopEnd),
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Sumi.Color.inkFaint,
) {
    val src = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(Sumi.Space.s2),
    ) {
        SumiIcon(icon = icon, contentDescription = label, tint = tint, size = 24.dp)
    }
}

@Composable
private fun NumberPad(onDigit: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (d in 1..9) {
            val src = remember { MutableInteractionSource() }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Sumi.Layout.minTap)
                    .border(1.dp, Sumi.Color.paperEdge)
                    .clickable(interactionSource = src, indication = null) { onDigit(d) },
            ) {
                Text(
                    text = d.toString(),
                    style = SumiTheme.typography.numeral,
                    color = Sumi.Color.ink,
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
