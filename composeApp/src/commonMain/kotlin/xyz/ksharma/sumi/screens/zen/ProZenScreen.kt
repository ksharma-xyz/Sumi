@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.zen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.Quote
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.SumiButton
import xyz.ksharma.sumi.design.components.SumiButtonSize
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.SumiPetals
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.navigation.TabBackHandler
import xyz.ksharma.sumi.share.BookInk
import xyz.ksharma.sumi.share.BookPaper
import xyz.ksharma.sumi.share.BookTheme
import xyz.ksharma.sumi.share.color
import xyz.ksharma.sumi.share.swatchColor
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private val STEP_LABELS = listOf("DIFFICULTY", "THEME", "OPTIONS", "PREVIEW")
private const val DESIGNER_PAGE_COUNT = 4

private val SAMPLE_CLUES = listOf(
    5, 3, 0, 0, 7, 0, 0, 0, 0,
    6, 0, 0, 1, 9, 5, 0, 0, 0,
    0, 9, 8, 0, 0, 0, 0, 6, 0,
    8, 0, 0, 0, 6, 0, 0, 0, 3,
    4, 0, 0, 8, 0, 3, 0, 0, 1,
    7, 0, 0, 0, 2, 0, 0, 0, 6,
    0, 6, 0, 0, 0, 0, 2, 8, 0,
    0, 0, 0, 4, 1, 9, 0, 0, 5,
    0, 0, 0, 0, 8, 0, 0, 7, 9,
)

/**
 * Distribution of puzzles across difficulties for a book.
 * [total] is the sum of all counts; zero entries are skipped during generation.
 */
data class DifficultyMix(
    val easy: Int = 15,
    val medium: Int = 10,
    val hard: Int = 0,
    val master: Int = 0,
    val edo: Int = 0,
) {
    val total: Int get() = easy + medium + hard + master + edo

    fun label(): String = listOf(
        easy to "Easy",
        medium to "Medium",
        hard to "Hard",
        master to "Master",
        edo to "Edo",
    ).filter { (n, _) -> n > 0 }.joinToString(", ") { (n, d) -> "$n $d" }

    fun primaryDifficulty(): String = listOf(
        easy to "Easy", medium to "Medium", hard to "Hard", master to "Master", edo to "Edo",
    ).filter { (n, _) -> n > 0 }.maxByOrNull { (n, _) -> n }?.second ?: "Mix"
}

/** Groups all book-designer UI state so ProZenScreen stays within param limits. */
data class BookDesignerState(
    val difficultyMix: DifficultyMix,
    val theme: BookTheme,
    val genState: BookGenState,
    val includeAnswers: Boolean = false,
)

/** Groups all book-designer callbacks for the same reason. */
data class BookDesignerCallbacks(
    val onSetDifficultyMix: (DifficultyMix) -> Unit,
    val onSetPaper: (BookPaper) -> Unit,
    val onSetInk: (BookInk) -> Unit,
    val onGenerate: () -> Unit,
    val onClearError: () -> Unit,
    val onToggleAnswers: (Boolean) -> Unit,
    val onShare: () -> Unit,
)

@Composable
fun ProZenScreen(
    quotes: List<Quote>,
    quoteIndex: Int,
    designerState: BookDesignerState,
    designerCallbacks: BookDesignerCallbacks,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onDesignerVisibilityChange: (Boolean) -> Unit = {},
) {
    var showDesigner by rememberSaveable { mutableStateOf(false) }
    val currentOnDesignerVisibilityChange by rememberUpdatedState(onDesignerVisibilityChange)

    LaunchedEffect(showDesigner) { currentOnDesignerVisibilityChange(showDesigner) }

    AnimatedContent(
        targetState = showDesigner,
        transitionSpec = {
            val dir = if (targetState) 1 else -1
            slideInHorizontally(tween(360)) { dir * it } togetherWith slideOutHorizontally(tween(360)) { -dir * it }
        },
        label = "designer",
        modifier = modifier.fillMaxSize(),
    ) { showingDesigner ->
        if (showingDesigner) {
            BookDesignerScreen(
                state = designerState,
                callbacks = designerCallbacks,
                onBack = { showDesigner = false },
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                WashiBG(modifier = Modifier.fillMaxSize())
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Sumi.Space.s6)
                        .padding(top = Sumi.Space.s8, bottom = Sumi.Space.s9),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProThankYouCard()
                    Spacer(Modifier.height(Sumi.Space.s7))
                    ZenSectionLabel(text = "The Library")
                    Spacer(Modifier.height(Sumi.Space.s4))
                    if (quotes.isNotEmpty()) {
                        QuoteBrowser(quotes = quotes, quoteIndex = quoteIndex, onPageChange = onPageChange)
                    }
                    Spacer(Modifier.height(Sumi.Space.s7))
                    ZenSectionLabel(text = "Puzzle Book")
                    Spacer(Modifier.height(Sumi.Space.s4))
                    PuzzleBookCard(onClick = { showDesigner = true })
                }
            }
        }
    }
}

// ── Thank-you card ─────────────────────────────────────────────────────────────

@Composable
private fun ProThankYouCard() {
    val ensoProgress = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        ensoProgress.animateTo(1f, tween(900, easing = Sumi.Ease.brush))
        contentAlpha.animateTo(1f, tween(500, easing = Sumi.Ease.paper))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SumiTheme.colors.gold.copy(alpha = 0.35f))
            .padding(vertical = Sumi.Space.s7, horizontal = Sumi.Space.s5),
    ) {
        LogoEnso(size = 64.dp, color = SumiTheme.colors.gold, progress = ensoProgress.value)
        Spacer(Modifier.height(Sumi.Space.s4))
        Text(
            text = "Sumi Pro",
            style = SumiTheme.typography.h2,
            color = SumiTheme.colors.gold,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha.value },
        )
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "Thank you for your practice.",
            style = SumiTheme.typography.subhead.copy(fontStyle = FontStyle.Italic),
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha.value },
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        Text(
            text = "Ad-free / Unlimited hints / All difficulties\nFull quote library / PDF puzzle books / Themes",
            style = SumiTheme.typography.uiMeta,
            color = SumiTheme.colors.inkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha.value },
        )
    }
}

// ── Quote browser ──────────────────────────────────────────────────────────────

@Composable
private fun QuoteBrowser(quotes: List<Quote>, quoteIndex: Int, onPageChange: (Int) -> Unit) {
    val pagerState = rememberPagerState(initialPage = quoteIndex, pageCount = { quotes.size })
    val scope = rememberCoroutineScope()
    val currentOnPageChange by rememberUpdatedState(onPageChange)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { currentOnPageChange(it) }
    }
    LaunchedEffect(quoteIndex) {
        if (pagerState.settledPage != quoteIndex) pagerState.animateScrollToPage(quoteIndex)
    }

    Column(
        modifier = Modifier.fillMaxWidth().border(1.dp, SumiTheme.colors.paperEdge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            QuotePage(quote = quotes[page])
        }
        QuoteProgressBar(current = pagerState.currentPage, total = quotes.size)
        QuoteNavRow(
            index = pagerState.currentPage,
            total = quotes.size,
            onPrev = { scope.launch { pagerState.animateScrollToPage(maxOf(0, pagerState.currentPage - 1)) } },
            onNext = {
                scope.launch { pagerState.animateScrollToPage(minOf(quotes.size - 1, pagerState.currentPage + 1)) }
            },
        )
    }
}

@Composable
private fun QuotePage(quote: Quote) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Sumi.Space.s5, vertical = Sumi.Space.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "“${quote.text}”",
            style = SumiTheme.typography.quote.copy(fontSize = 16.sp, fontStyle = FontStyle.Italic),
            color = SumiTheme.colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        Text(
            text = "— ${quote.attribution}",
            style = SumiTheme.typography.uiMeta,
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun QuoteProgressBar(current: Int, total: Int) {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SumiTheme.colors.paperEdge)) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((current + 1f) / total)
                .background(SumiTheme.colors.inkFaint),
        )
    }
}

@Composable
private fun QuoteNavRow(index: Int, total: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val prevSrc = remember { MutableInteractionSource() }
    val nextSrc = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
        modifier = Modifier.padding(vertical = Sumi.Space.s4),
    ) {
        SumiIcon(
            icon = SumiIcons.Back,
            contentDescription = "Previous quote",
            tint = SumiTheme.colors.inkFaint,
            size = 20.dp,
            modifier = Modifier.clickable(interactionSource = prevSrc, indication = null, onClick = onPrev),
        )
        Text(text = "${index + 1} / $total", style = SumiTheme.typography.uiMeta, color = SumiTheme.colors.inkFaint)
        SumiIcon(
            icon = SumiIcons.Back,
            contentDescription = "Next quote",
            tint = SumiTheme.colors.inkFaint,
            size = 20.dp,
            modifier = Modifier
                .graphicsLayer { rotationZ = 180f }
                .clickable(interactionSource = nextSrc, indication = null, onClick = onNext),
        )
    }
}

// ── Puzzle book CTA ────────────────────────────────────────────────────────────

@Composable
private fun PuzzleBookCard(onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SumiTheme.colors.paperEdge)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = Sumi.Space.s5, vertical = Sumi.Space.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "活", style = SumiTheme.typography.cjk.copy(fontSize = 36.sp), color = SumiTheme.colors.inkFaint)
        Spacer(Modifier.height(Sumi.Space.s3))
        Text(text = "Design a Puzzle Book", style = SumiTheme.typography.h2, color = SumiTheme.colors.ink)
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "Mix difficulties, choose a theme, export a print-ready A5 book.",
            style = SumiTheme.typography.bodySmall,
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ZenSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = SumiTheme.typography.uiLabel,
        color = SumiTheme.colors.inkFaint,
        modifier = Modifier.fillMaxWidth(),
    )
}

// ── Book designer screen ───────────────────────────────────────────────────────

@Composable
private fun BookDesignerScreen(state: BookDesignerState, callbacks: BookDesignerCallbacks, onBack: () -> Unit) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val currentCallbacks by rememberUpdatedState(callbacks)
    val isProcessing = state.genState is BookGenState.Generating || state.genState is BookGenState.Ready
    val onNext: () -> Unit = { if (currentStep < DESIGNER_PAGE_COUNT - 1) currentStep++ }
    val onDone: () -> Unit = { currentCallbacks.onClearError().also { onBack() } }
    val goBack: () -> Unit = {
        when {
            state.genState is BookGenState.Ready -> onDone()
            currentStep > 0 -> currentStep--
            else -> onBack()
        }
    }
    val (topReservedHeight, bottomReservedHeight) = reservedSlotHeights(isProcessing)
    TabBackHandler(enabled = true, onBack = goBack)
    Box(modifier = Modifier.fillMaxSize().background(SumiTheme.colors.paper)) {
        WashiBG(modifier = Modifier.fillMaxSize())
        if (isProcessing) DesignerPetals(theme = state.theme)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s5, bottom = Sumi.Space.s6),
        ) {
            DesignerHeader(
                step = currentStep,
                isProcessing = isProcessing,
                isReady = state.genState is BookGenState.Ready,
                onBack = goBack,
                onDismiss = onBack,
            )
            Spacer(Modifier.height(Sumi.Space.s6))
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = topReservedHeight)) {
                CraftingTopMessage(visible = state.genState is BookGenState.Generating)
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                DesignerStepHost(currentStep, isProcessing, state, currentCallbacks)
            }
            Spacer(Modifier.height(Sumi.Space.s4))
            BookDesignerBottomBar(
                currentStep = currentStep,
                state = state,
                callbacks = currentCallbacks,
                minHeight = bottomReservedHeight,
                onNext = onNext,
                onDone = onDone,
            )
        }
    }
}

@Composable
private fun DesignerPetals(theme: BookTheme) {
    val petalColors = remember(theme) {
        val accent = theme.ink.color(theme.paper)
        val base = if (theme.paper == BookPaper.Dark) {
            listOf(Color(0xFF8A7A6A), Color(0xFF6A5A4A), Color(0xFFAA9A8A))
        } else {
            listOf(Color(0xFFD4B896), Color(0xFFB8A0A0), Color(0xFFD0C0B0))
        }
        base + accent
    }
    SumiPetals(modifier = Modifier.fillMaxSize(), count = 18, colors = petalColors, speedFactor = 0.65f)
}

@Composable
private fun CraftingTopMessage(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 2 },
        exit = fadeOut(tween(400)) + slideOutVertically(tween(400)) { -it / 2 },
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Crafting your\npuzzle book…",
                style = SumiTheme.typography.h2,
                color = SumiTheme.colors.ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Sumi.Space.s5))
        }
    }
}

@Composable
private fun DesignerStepHost(
    step: Int,
    isProcessing: Boolean,
    state: BookDesignerState,
    callbacks: BookDesignerCallbacks,
) {
    val target = if (isProcessing) -1 else step
    AnimatedContent(
        targetState = target,
        transitionSpec = {
            val forward = targetState > initialState || initialState == -1
            val dir = if (forward) 1 else -1
            (slideInHorizontally(tween(360)) { dir * it / 4 } + fadeIn(tween(360))) togetherWith
                (slideOutHorizontally(tween(360)) { -dir * it / 4 } + fadeOut(tween(360)))
        },
        label = "step",
        modifier = Modifier.fillMaxSize(),
    ) { activeStep ->
        when (activeStep) {
            -1 -> ResultCenter(genState = state.genState)
            0 -> DifficultyMixPage(mix = state.difficultyMix, onSetMix = callbacks.onSetDifficultyMix)
            1 -> ThemeDesignerPage(
                theme = state.theme,
                onSetPaper = callbacks.onSetPaper,
                onSetInk = callbacks.onSetInk,
            )
            2 -> OptionsDesignerPage(
                includeAnswers = state.includeAnswers,
                onToggleAnswers = callbacks.onToggleAnswers,
            )
            else -> IdlePreviewContent(state = state)
        }
    }
}

@Composable
private fun DesignerHeader(
    step: Int,
    isProcessing: Boolean,
    isReady: Boolean,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
) {
    val backSrc = remember { MutableInteractionSource() }
    val closeSrc = remember { MutableInteractionSource() }
    val label = when {
        isReady -> "READY"
        isProcessing -> "CRAFTING"
        else -> STEP_LABELS.getOrElse(step) { "PREVIEW" }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            if (!isProcessing && step > 0 || isReady) {
                SumiIcon(
                    icon = SumiIcons.Back,
                    contentDescription = "Back",
                    tint = SumiTheme.colors.inkSoft,
                    size = 22.dp,
                    modifier = Modifier.clickable(interactionSource = backSrc, indication = null, onClick = onBack),
                )
            }
        }
        Text(text = label, style = SumiTheme.typography.uiLabel, color = SumiTheme.colors.inkFaint)
        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            if (!isProcessing) {
                SumiIcon(
                    icon = SumiIcons.Close,
                    contentDescription = "Close",
                    tint = SumiTheme.colors.inkSoft,
                    size = 22.dp,
                    modifier = Modifier.clickable(
                        interactionSource = closeSrc,
                        indication = null,
                        onClick = onDismiss,
                    ),
                )
            }
        }
    }
}

// ── Designer pages ─────────────────────────────────────────────────────────────

@Composable
private fun DifficultyMixPage(mix: DifficultyMix, onSetMix: (DifficultyMix) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = Sumi.Space.s4),
    ) {
        Text(text = "How many of each?", style = SumiTheme.typography.h2, color = SumiTheme.colors.ink)
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = if (mix.total == 0) "Add at least 1 puzzle to continue" else "${mix.total} puzzles total",
            style = SumiTheme.typography.uiLabel.copy(fontSize = 14.sp),
            color = if (mix.total == 0) SumiTheme.colors.red else SumiTheme.colors.inkFaint,
        )
        Spacer(Modifier.height(Sumi.Space.s5))
        DifficultyMixRow("Easy", "~3 min", mix.easy) { onSetMix(mix.copy(easy = it.coerceAtLeast(0))) }
        DifficultyMixRow("Medium", "~6 min", mix.medium) { onSetMix(mix.copy(medium = it.coerceAtLeast(0))) }
        DifficultyMixRow("Hard", "~12 min", mix.hard) { onSetMix(mix.copy(hard = it.coerceAtLeast(0))) }
        DifficultyMixRow("Master", "~25 min", mix.master) { onSetMix(mix.copy(master = it.coerceAtLeast(0))) }
        DifficultyMixRow("Edo", "~45 min", mix.edo) { onSetMix(mix.copy(edo = it.coerceAtLeast(0))) }
    }
}

@Composable
private fun DifficultyMixRow(name: String, hint: String, count: Int, onCountChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Sumi.Space.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = SumiTheme.typography.body.copy(fontSize = 18.sp),
                color = if (count > 0) SumiTheme.colors.ink else SumiTheme.colors.inkSoft,
            )
            Text(
                text = hint,
                style = SumiTheme.typography.uiMeta.copy(fontSize = 13.sp),
                color = SumiTheme.colors.inkFaint,
            )
        }
        CountStepper(count = count, onCountChange = onCountChange)
    }
}

@Composable
private fun CountStepper(count: Int, onCountChange: (Int) -> Unit) {
    val minusSrc = remember { MutableInteractionSource() }
    val plusSrc = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.dp, if (count > 0) SumiTheme.colors.ink else SumiTheme.colors.paperEdge)
                .clickable(interactionSource = minusSrc, indication = null) {
                    if (count > 0) onCountChange(count - 1)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "−",
                style = SumiTheme.typography.h3.copy(fontSize = 20.sp),
                color = if (count > 0) SumiTheme.colors.ink else SumiTheme.colors.inkFaint,
            )
        }
        Text(
            text = count.toString().padStart(2),
            style = SumiTheme.typography.h3.copy(fontSize = 20.sp),
            color = if (count > 0) SumiTheme.colors.ink else SumiTheme.colors.inkFaint,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.dp, SumiTheme.colors.ink)
                .clickable(interactionSource = plusSrc, indication = null) { onCountChange(count + 1) },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "+", style = SumiTheme.typography.h3.copy(fontSize = 20.sp), color = SumiTheme.colors.ink)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeDesignerPage(
    theme: BookTheme,
    onSetPaper: (BookPaper) -> Unit,
    onSetInk: (BookInk) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = Sumi.Space.s4),
    ) {
        Text(text = "Paper & ink", style = SumiTheme.typography.h2, color = SumiTheme.colors.ink)
        Spacer(Modifier.height(Sumi.Space.s5))
        SwatchSection(label = "PAPER") {
            BookPaper.entries.forEach { paper ->
                ThemeSwatch(
                    label = paper.name,
                    bgColor = paper.swatchColor(),
                    textColor = BookInk.Sumi.color(paper),
                    selected = paper == theme.paper,
                    onClick = { onSetPaper(paper) },
                )
            }
        }
        Spacer(Modifier.height(Sumi.Space.s5))
        SwatchSection(label = "INK") {
            BookInk.entries.forEach { ink ->
                ThemeSwatch(
                    label = ink.name,
                    bgColor = theme.paper.swatchColor(),
                    textColor = ink.color(theme.paper),
                    selected = ink == theme.ink,
                    onClick = { onSetInk(ink) },
                )
            }
        }
        Spacer(Modifier.height(Sumi.Space.s5))
        PdfPagePreview(
            theme = theme,
            difficulty = "Easy",
            modifier = Modifier.fillMaxWidth(0.55f).align(Alignment.CenterHorizontally),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SwatchSection(label: String, content: @Composable FlowRowScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = SumiTheme.typography.uiLabel,
            color = SumiTheme.colors.inkFaint,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Sumi.Space.s2))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
private fun OptionsDesignerPage(includeAnswers: Boolean, onToggleAnswers: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = Sumi.Space.s4),
    ) {
        Text(text = "Final touches", style = SumiTheme.typography.h2, color = SumiTheme.colors.ink)
        Spacer(Modifier.height(Sumi.Space.s5))
        AnswerBookToggle(checked = includeAnswers, onToggle = onToggleAnswers)
    }
}

@Composable
private fun AnswerBookToggle(checked: Boolean, onToggle: (Boolean) -> Unit) {
    val src = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SumiTheme.colors.paperEdge)
            .clickable(interactionSource = src, indication = null) { onToggle(!checked) }
            .padding(horizontal = Sumi.Space.s4, vertical = Sumi.Space.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = Sumi.Space.s4)) {
            Text(text = "Include answer book", style = SumiTheme.typography.body, color = SumiTheme.colors.ink)
            Text(
                text = "Answers appended after puzzle pages",
                style = SumiTheme.typography.uiMeta.copy(fontSize = 13.sp),
                color = SumiTheme.colors.inkFaint,
            )
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(1.dp, if (checked) SumiTheme.colors.ink else SumiTheme.colors.paperEdge),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                SumiIcon(icon = SumiIcons.Check, contentDescription = null, tint = SumiTheme.colors.ink, size = 14.dp)
            }
        }
    }
}

@Composable
private fun IdlePreviewContent(state: BookDesignerState) {
    val a1 = remember { Animatable(0f) }
    val a2 = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        a1.animateTo(1f, tween(380, easing = Sumi.Ease.paper))
        delay(60L)
        a2.animateTo(1f, tween(340, easing = Sumi.Ease.paper))
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PdfPagePreview(
            theme = state.theme,
            difficulty = state.difficultyMix.primaryDifficulty(),
            modifier = Modifier.fillMaxWidth(0.72f).graphicsLayer { alpha = a1.value },
        )
        Spacer(Modifier.height(Sumi.Space.s5))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = a2.value },
        ) {
            Text(
                text = state.difficultyMix.total.toString(),
                style = SumiTheme.typography.numeral.copy(fontSize = 64.sp, fontStyle = FontStyle.Italic),
                color = SumiTheme.colors.ink,
            )
            Text(text = "puzzles", style = SumiTheme.typography.subhead, color = SumiTheme.colors.inkSoft)
            Spacer(Modifier.height(Sumi.Space.s2))
            Text(
                text = "${state.theme.paper.name} / ${state.theme.ink.name} / ${state.difficultyMix.label()}",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Sumi.Space.s4),
            )
        }
        Spacer(Modifier.height(Sumi.Space.s4))
    }
}

// ── Result center (Generating + Ready stage) ──────────────────────────────────

@Composable
private fun ResultCenter(genState: BookGenState) {
    // Always-mounted column keeps the layout slot reserved. The reveal is sequential:
    // the enso draws first, then the wordmark inks in (alpha + a tiny "ink-settling"
    // scale-down from 1.04 → 1.0), then the subtitle fades in. No Y-translation —
    // anchoring nothing to "below the line" prevents the elements reading as if they
    // slid up from somewhere; they bloom into place where they belong.
    val ensoProgress = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(1.04f) }
    val subtitleAlpha = remember { Animatable(0f) }
    LaunchedEffect(genState) {
        if (genState is BookGenState.Ready) {
            ensoProgress.animateTo(1f, tween(900, easing = Sumi.Ease.brush))
            launch { titleScale.animateTo(1f, tween(700, easing = Sumi.Ease.brush)) }
            titleAlpha.animateTo(1f, tween(520, easing = Sumi.Ease.paper))
            delay(160L)
            subtitleAlpha.animateTo(1f, tween(460, easing = Sumi.Ease.paper))
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LogoEnso(size = 88.dp, color = SumiTheme.colors.ink, progress = ensoProgress.value)
            Spacer(Modifier.height(Sumi.Space.s5))
            Text(
                text = "Sumi",
                style = SumiTheme.typography.h1,
                color = SumiTheme.colors.ink,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    scaleX = titleScale.value
                    scaleY = titleScale.value
                },
            )
            Spacer(Modifier.height(Sumi.Space.s2))
            Text(
                text = "Your book is ready.",
                style = SumiTheme.typography.subhead.copy(fontStyle = FontStyle.Italic),
                color = SumiTheme.colors.inkSoft,
                modifier = Modifier.graphicsLayer { alpha = subtitleAlpha.value },
            )
        }
    }
}

// ── Bottom bar — anchors the primary action across all states ─────────────────

@Composable
private fun BookDesignerBottomBar(
    currentStep: Int,
    state: BookDesignerState,
    callbacks: BookDesignerCallbacks,
    minHeight: Dp,
    onNext: () -> Unit,
    onDone: () -> Unit,
) {
    val target = when {
        state.genState is BookGenState.Error -> "error"
        state.genState is BookGenState.Ready -> "ready"
        state.genState is BookGenState.Generating -> "generating"
        currentStep < DESIGNER_PAGE_COUNT - 1 -> "next"
        else -> "generate"
    }
    AnimatedContent(
        targetState = target,
        transitionSpec = {
            (fadeIn(tween(320)) togetherWith fadeOut(tween(220)))
                .using(SizeTransform(clip = false) { _, _ -> tween(0) })
        },
        label = "bottombar",
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
        contentAlignment = Alignment.BottomCenter,
    ) { phase ->
        when (phase) {
            "ready" -> ReadyBottom(onShare = callbacks.onShare, onDone = onDone)
            "generating" -> Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = Sumi.Space.s4),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = SumiTheme.colors.inkSoft,
                    strokeWidth = 2.dp,
                )
            }
            "error" -> ErrorRow(
                message = (state.genState as? BookGenState.Error)?.message ?: "Export failed",
                onDismiss = callbacks.onClearError,
            )
            "next" -> SumiButton(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                size = SumiButtonSize.Lg,
                enabled = currentStep != 0 || state.difficultyMix.total > 0,
            ) { Text(text = "Next", style = SumiTheme.typography.uiButton) }
            else -> SumiButton(
                onClick = callbacks.onGenerate,
                modifier = Modifier.fillMaxWidth(),
                size = SumiButtonSize.Lg,
            ) {
                SumiIcon(icon = SumiIcons.Share, contentDescription = null, size = 16.dp)
                Spacer(Modifier.width(Sumi.Space.s2))
                Text(text = "Generate", style = SumiTheme.typography.uiButton)
            }
        }
    }
}

/** Animated reserved slot heights for the top message + bottom action area. */
@Composable
private fun reservedSlotHeights(isProcessing: Boolean): Pair<Dp, Dp> {
    val top by animateDpAsState(
        targetValue = if (isProcessing) 72.dp else 0.dp,
        animationSpec = tween(500),
        label = "topReserved",
    )
    val bottom by animateDpAsState(
        targetValue = if (isProcessing) 196.dp else 56.dp,
        animationSpec = tween(500),
        label = "bottomReserved",
    )
    return top to bottom
}

@Composable
private fun ReadyBottom(onShare: () -> Unit, onDone: () -> Unit) {
    val shareSrc = remember { MutableInteractionSource() }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(1.5.dp, SumiTheme.colors.ink, CircleShape)
                .clickable(interactionSource = shareSrc, indication = null, onClick = onShare),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(
                icon = SumiIcons.Share,
                contentDescription = "Share puzzle book",
                tint = SumiTheme.colors.ink,
                size = 26.dp,
            )
        }
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(text = "tap to share", style = SumiTheme.typography.uiMeta, color = SumiTheme.colors.inkFaint)
        Spacer(Modifier.height(Sumi.Space.s4))
        SumiButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            variant = SumiButtonVariant.Ghost,
            size = SumiButtonSize.Md,
        ) {
            Text(text = "Done", style = SumiTheme.typography.uiButton)
        }
    }
}

@Composable
private fun ErrorRow(message: String, onDismiss: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            style = SumiTheme.typography.bodySmall,
            color = SumiTheme.colors.red,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Sumi.Space.s3))
        SumiButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            variant = SumiButtonVariant.Ghost,
            size = SumiButtonSize.Md,
        ) { Text(text = "Dismiss", style = SumiTheme.typography.uiButton) }
    }
}

// ── PDF page preview ───────────────────────────────────────────────────────────

@Composable
private fun PdfPagePreview(theme: BookTheme, difficulty: String, modifier: Modifier = Modifier) {
    val bgColor = theme.paper.swatchColor()
    val textColor = BookInk.Sumi.color(theme.paper)
    val accentColor = theme.ink.color(theme.paper)
    val ruleColor = textColor.copy(alpha = 0.2f)
    val gridColor = accentColor.copy(alpha = 0.35f)
    val boxColor = accentColor.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .aspectRatio(0.707f)
            .border(1.dp, SumiTheme.colors.paperEdge)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Sumi",
                    style = SumiTheme.typography.quote.copy(fontSize = 7.sp, fontStyle = FontStyle.Italic),
                    color = textColor,
                )
                Text(
                    text = "#001",
                    style = SumiTheme.typography.uiMeta.copy(fontSize = 5.sp),
                    color = textColor.copy(alpha = 0.5f),
                )
            }
            Spacer(Modifier.height(2.dp))
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ruleColor))
            Spacer(Modifier.height(4.dp))
            SudokuMiniGrid(
                gridColor = gridColor,
                boxColor = boxColor,
                digitColor = textColor,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ruleColor))
            Spacer(Modifier.height(2.dp))
            Text(
                text = difficulty.uppercase(),
                style = SumiTheme.typography.uiLabel.copy(fontSize = 5.sp),
                color = textColor.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SudokuMiniGrid(gridColor: Color, boxColor: Color, digitColor: Color, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        val cellSize = size.width / 9f
        val gridH = cellSize * 9f
        val yOff = ((size.height - gridH) / 2f).coerceAtLeast(0f)
        val fontSizeSp = (cellSize * 0.42f / density).sp

        for (i in 0..9) {
            val isBox = i % 3 == 0
            val color = if (isBox) boxColor else gridColor
            val sw = if (isBox) 1.5f else 0.5f
            drawLine(color, Offset(i * cellSize, yOff), Offset(i * cellSize, yOff + gridH), sw)
            drawLine(color, Offset(0f, i * cellSize + yOff), Offset(size.width, i * cellSize + yOff), sw)
        }

        SAMPLE_CLUES.forEachIndexed { idx, value ->
            if (value != 0) {
                val row = idx / 9
                val col = idx % 9
                val result = measurer.measure(value.toString(), TextStyle(fontSize = fontSizeSp, color = digitColor))
                val x = col * cellSize + (cellSize - result.size.width) / 2f
                val y = yOff + row * cellSize + (cellSize - result.size.height) / 2f
                drawText(result, topLeft = Offset(x, y))
            }
        }
    }
}

// ── Shared small components ────────────────────────────────────────────────────

@Composable
private fun ThemeSwatch(
    label: String,
    bgColor: Color,
    textColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .widthIn(min = 84.dp)
            .border(1.dp, if (selected) SumiTheme.colors.ink else SumiTheme.colors.paperEdge)
            .background(bgColor)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = Sumi.Space.s4, vertical = Sumi.Space.s4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SumiIcon(
            icon = SumiIcons.Check,
            contentDescription = null,
            tint = if (selected) textColor else Color.Transparent,
            size = 14.dp,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = label,
            style = SumiTheme.typography.uiMeta.copy(fontSize = 13.sp),
            color = textColor,
        )
    }
}
