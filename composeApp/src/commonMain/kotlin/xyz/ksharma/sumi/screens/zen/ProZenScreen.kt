@file:Suppress("MagicNumber", "TooManyFunctions")

package xyz.ksharma.sumi.screens.zen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
private const val CRAFTING_EXIT_MS = 420L // matches CraftingTopMessage's fade-out (400ms) + a 20ms buffer

// Hard caps to keep generation under the ANR threshold even on low-end devices.
// Per-difficulty cap protects the Edo solver (slowest); total cap protects PDF render.
private const val MAX_PER_DIFFICULTY = 15
private const val MAX_TOTAL_PUZZLES = 50

// Share button waits for ResultCenter (enso 900ms + title 520ms + subtitle 460ms ≈ 1900ms)
// to finish blooming, then reveals — so the hero lands first and the action arrives second.
private const val SHARE_BUTTON_REVEAL_DELAY_MS = 2_000L

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
            // Simple crossfade — the previous full-width horizontal slide felt
            // jittery on Android because both layers had to draw simultaneously
            // for 360ms. A pure fade is calmer and reads as a paper change.
            fadeIn(tween(280, easing = Sumi.Ease.paper)) togetherWith
                fadeOut(tween(220, easing = Sumi.Ease.paper))
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
            ZenLanding(
                quotes = quotes,
                quoteIndex = quoteIndex,
                onPageChange = onPageChange,
                onOpenDesigner = { showDesigner = true },
            )
        }
    }
}

@Composable
private fun ZenLanding(
    quotes: List<Quote>,
    quoteIndex: Int,
    onPageChange: (Int) -> Unit,
    onOpenDesigner: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        WashiBG(modifier = Modifier.fillMaxSize())
        // Ambient gold petals — sparse + slow so the page reads as still and
        // premium, not festive. Drawn behind content so swipes/taps still land.
        SumiPetals(
            modifier = Modifier.fillMaxSize(),
            count = 7,
            colors = remember {
                listOf(
                    Color(0xFFD9A855), // goldLight
                    Color(0xFF8A6B2A), // gold
                    Color(0xFFC9B48A), // paperEdge
                )
            },
            sizeMultiplier = 0.85f,
            speedFactor = 0.35f,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s8, bottom = Sumi.Space.s9),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProBadge()
            Spacer(Modifier.height(Sumi.Space.s8))
            ZenSectionLabel(text = "The Library")
            Spacer(Modifier.height(Sumi.Space.s4))
            if (quotes.isNotEmpty()) {
                QuoteStage(quotes = quotes, quoteIndex = quoteIndex, onPageChange = onPageChange)
            }
            Spacer(Modifier.height(Sumi.Space.s8))
            ZenSectionLabel(text = "Zen Sudoku")
            Spacer(Modifier.height(Sumi.Space.s4))
            PuzzleBookCard(onClick = onOpenDesigner)
            // No restore-purchase footer here — Zen tab is only reachable when
            // the user is already Pro, so restore is unnecessary on this surface.
            // (It still lives on PaywallScreen where it actually matters.)
        }
    }
}

// ── Pro badge ──────────────────────────────────────────────────────────────────

/**
 * Minimal Pro identity. Three elements only — enso, wordmark, thank-you note —
 * because the Zen tab IS the proof of value; we don't need to list features.
 */
@Composable
private fun ProBadge() {
    val ensoProgress = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        ensoProgress.animateTo(1f, tween(900, easing = Sumi.Ease.brush))
        contentAlpha.animateTo(1f, tween(520, easing = Sumi.Ease.paper))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        LogoEnso(size = 56.dp, color = SumiTheme.colors.gold, progress = ensoProgress.value)
        Spacer(Modifier.height(Sumi.Space.s3))
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
    }
}

// ── Quote stage ────────────────────────────────────────────────────────────────

private val QUOTE_CARD_HEIGHT: Dp = 320.dp

/**
 * Fixed-height swipeable quote pager. Card height stays constant regardless of
 * quote length — quotes vertically centre inside the card so a 6-word koan
 * and a 30-word passage occupy the same visual slot. No more layout jitter on
 * page change.
 *
 * Premium touches: hairline gold corner mark, large CJK quote glyph, page
 * silhouette with subtle parallax on swipe via [HorizontalPager.pageOffset].
 */
@Composable
private fun QuoteStage(quotes: List<Quote>, quoteIndex: Int, onPageChange: (Int) -> Unit) {
    val pagerState = rememberPagerState(initialPage = quoteIndex, pageCount = { quotes.size })
    val currentOnPageChange by rememberUpdatedState(onPageChange)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { currentOnPageChange(it) }
    }
    LaunchedEffect(quoteIndex) {
        if (pagerState.settledPage != quoteIndex) pagerState.animateScrollToPage(quoteIndex)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(QUOTE_CARD_HEIGHT),
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
            QuoteCard(quote = quotes[page], pageOffset = pageOffset)
        }
        Spacer(Modifier.height(Sumi.Space.s4))
        QuoteDots(current = pagerState.currentPage, total = quotes.size)
    }
}

@Composable
private fun QuoteCard(quote: Quote, pageOffset: Float) {
    val cornerInk = SumiTheme.colors.gold.copy(alpha = 0.55f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Sumi.Space.s2)
            .graphicsLayer {
                // Subtle parallax — adjacent pages drift slightly so the swipe reads
                // as paper sliding rather than a stiff slot machine.
                translationX = pageOffset * 24f
                alpha = 1f - (kotlin.math.abs(pageOffset) * 0.4f).coerceAtMost(0.4f)
            }
            .border(1.dp, SumiTheme.colors.paperEdge),
    ) {
        QuoteCornerOrnaments(cornerColor = cornerInk)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "「",
                style = SumiTheme.typography.cjk.copy(fontSize = 36.sp),
                color = SumiTheme.colors.gold.copy(alpha = 0.45f),
            )
            Spacer(Modifier.height(Sumi.Space.s3))
            Text(
                text = quote.text,
                // Tighten lineHeight relative to fontSize — the base quote style's
                // 32sp lineHeight + our 18sp size gives a ~1.78 ratio which reads
                // as too airy. ~1.35 sits closer to the type's natural body.
                style = SumiTheme.typography.quote.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = SumiTheme.colors.ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Sumi.Space.s5))
            Box(
                modifier = Modifier
                    .width(Sumi.Space.s7)
                    .height(1.dp)
                    .background(SumiTheme.colors.gold.copy(alpha = 0.5f)),
            )
            Spacer(Modifier.height(Sumi.Space.s3))
            Text(
                text = quote.attribution,
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkSoft,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Four hairline gold L-marks anchoring the quote card corners — paper-of-record feel. */
@Composable
private fun QuoteCornerOrnaments(cornerColor: Color) {
    val markLen = 14.dp
    val inset = 10.dp
    Box(modifier = Modifier.fillMaxSize()) {
        listOf(
            Alignment.TopStart to (true to true),
            Alignment.TopEnd to (false to true),
            Alignment.BottomStart to (true to false),
            Alignment.BottomEnd to (false to false),
        ).forEach { (align, dir) ->
            CornerMark(
                color = cornerColor,
                length = markLen,
                inset = inset,
                isLeft = dir.first,
                isTop = dir.second,
                modifier = Modifier.align(align),
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun CornerMark(
    color: Color,
    length: Dp,
    inset: Dp,
    isLeft: Boolean,
    isTop: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(inset)) {
        Box(
            modifier = Modifier
                .align(if (isTop) Alignment.TopStart else Alignment.BottomStart)
                .width(length)
                .height(1.dp)
                .background(color),
        )
        Box(
            modifier = Modifier
                .align(if (isLeft) Alignment.TopStart else Alignment.TopEnd)
                .width(1.dp)
                .height(length)
                .background(color),
        )
    }
}

@Composable
private fun QuoteDots(current: Int, total: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${current + 1} / $total",
            style = SumiTheme.typography.uiMeta,
            color = SumiTheme.colors.inkFaint,
        )
    }
}

// ── Puzzle book CTA ────────────────────────────────────────────────────────────

/**
 * Two-column card: large CJK glyph on the left grounds the action, copy on the
 * right reads naturally. A right-arrow chevron + gold corner marker make the
 * tap-target unambiguous without resorting to a button bar.
 */
@Composable
private fun PuzzleBookCard(onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SumiTheme.colors.paperEdge)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = Sumi.Space.s5, vertical = Sumi.Space.s6),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(Sumi.Space.s2)
                .background(SumiTheme.colors.gold.copy(alpha = 0.6f)),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s5),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(1.dp, SumiTheme.colors.gold.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "本",
                    style = SumiTheme.typography.cjk.copy(fontSize = 38.sp),
                    color = SumiTheme.colors.gold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Design Zen Sudoku",
                    style = SumiTheme.typography.h3,
                    color = SumiTheme.colors.ink,
                )
                Spacer(Modifier.height(Sumi.Space.s1))
                Text(
                    text = "Mix difficulties, choose a theme, export a print-ready A5 book.",
                    style = SumiTheme.typography.bodySmall,
                    color = SumiTheme.colors.inkSoft,
                )
            }
            SumiIcon(
                icon = SumiIcons.Back,
                contentDescription = null,
                tint = SumiTheme.colors.gold,
                size = 18.dp,
                modifier = Modifier.graphicsLayer { rotationZ = 180f },
            )
        }
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
                text = "Crafting your\nZen Sudoku…",
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
    val atTotalCap = mix.total >= MAX_TOTAL_PUZZLES
    val statusLabel = when {
        mix.total == 0 -> "Add at least 1 puzzle to continue"
        atTotalCap -> "Maximum $MAX_TOTAL_PUZZLES puzzles per book"
        else -> "${mix.total} puzzles total"
    }
    val statusColor = when {
        mix.total == 0 -> SumiTheme.colors.red
        atTotalCap -> SumiTheme.colors.gold
        else -> SumiTheme.colors.inkFaint
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = Sumi.Space.s4),
    ) {
        Text(text = "How many of each?", style = SumiTheme.typography.h2, color = SumiTheme.colors.ink)
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = statusLabel,
            style = SumiTheme.typography.uiLabel.copy(fontSize = 14.sp),
            color = statusColor,
        )
        Spacer(Modifier.height(Sumi.Space.s5))
        DifficultyMixRow("Easy", "~3 min", mix.easy, mix.total) {
            onSetMix(mix.copy(easy = it.coerceIn(0, MAX_PER_DIFFICULTY)))
        }
        DifficultyMixRow("Medium", "~6 min", mix.medium, mix.total) {
            onSetMix(mix.copy(medium = it.coerceIn(0, MAX_PER_DIFFICULTY)))
        }
        DifficultyMixRow("Hard", "~12 min", mix.hard, mix.total) {
            onSetMix(mix.copy(hard = it.coerceIn(0, MAX_PER_DIFFICULTY)))
        }
        DifficultyMixRow("Master", "~25 min", mix.master, mix.total) {
            onSetMix(mix.copy(master = it.coerceIn(0, MAX_PER_DIFFICULTY)))
        }
        DifficultyMixRow("Edo", "~45 min", mix.edo, mix.total) {
            onSetMix(mix.copy(edo = it.coerceIn(0, MAX_PER_DIFFICULTY)))
        }
    }
}

@Composable
private fun DifficultyMixRow(name: String, hint: String, count: Int, currentTotal: Int, onCountChange: (Int) -> Unit) {
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
        // The "+" is gated by both per-difficulty cap and the total-puzzles cap.
        val canIncrement = count < MAX_PER_DIFFICULTY && currentTotal < MAX_TOTAL_PUZZLES
        CountStepper(count = count, canIncrement = canIncrement, onCountChange = onCountChange)
    }
}

@Composable
private fun CountStepper(count: Int, canIncrement: Boolean, onCountChange: (Int) -> Unit) {
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
                .border(1.dp, if (canIncrement) SumiTheme.colors.ink else SumiTheme.colors.paperEdge)
                .clickable(interactionSource = plusSrc, indication = null, enabled = canIncrement) {
                    onCountChange(count + 1)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                style = SumiTheme.typography.h3.copy(fontSize = 20.sp),
                color = if (canIncrement) SumiTheme.colors.ink else SumiTheme.colors.inkFaint,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ThemeDesignerPage(
    theme: BookTheme,
    onSetPaper: (BookPaper) -> Unit,
    onSetInk: (BookInk) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = Sumi.Space.s4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hero — the live PDF preview leads. The paper / ink controls below
        // are about adjusting THIS artifact, so it gets prime visual real-estate.
        PdfPagePreview(
            theme = theme,
            difficulty = "Easy",
            modifier = Modifier.fillMaxWidth(0.65f),
        )
        Spacer(Modifier.height(Sumi.Space.s7))

        // PAPER — two big side-by-side cards. Each tile is wrapped in an outer
        // frame using the screen's paperGlow so the swatch stays visible even
        // when (a) the user picks Dark paper and (b) the app theme is also dark
        // — the frame contrast carries the legibility regardless.
        SectionRule(label = "PAPER")
        Spacer(Modifier.height(Sumi.Space.s3))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
        ) {
            BookPaper.entries.forEach { paper ->
                PaperTile(
                    paper = paper,
                    inkOnPaper = theme.ink.color(paper),
                    selected = paper == theme.paper,
                    onClick = { onSetPaper(paper) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(Sumi.Space.s7))

        // INK — wells wrap onto a new row when there are too many to fit
        // horizontally (FlowRow, not Row, so they don't get squished).
        SectionRule(label = "INK")
        Spacer(Modifier.height(Sumi.Space.s3))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
        ) {
            BookInk.entries.forEach { ink ->
                InkWell(
                    label = ink.name,
                    inkColor = ink.color(theme.paper),
                    selected = ink == theme.ink,
                    onClick = { onSetInk(ink) },
                )
            }
        }
    }
}

/** Thin horizontal rule with an inline label — replaces the boxy section header. */
@Composable
private fun SectionRule(label: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(SumiTheme.colors.paperEdge),
        )
        Text(
            text = label,
            style = SumiTheme.typography.uiLabel,
            color = SumiTheme.colors.inkFaint,
            modifier = Modifier.padding(horizontal = Sumi.Space.s3),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(SumiTheme.colors.paperEdge),
        )
    }
}

/**
 * Large paper swatch tile — the actual book paper colour fills the inner block,
 * with a sample of ink-on-paper text rendered in the matching ink colour so the
 * combo reads at a glance. An always-visible outer frame solves the
 * dark-mode-on-dark visibility issue.
 */
@Composable
private fun PaperTile(
    paper: BookPaper,
    inkOnPaper: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val src = remember { MutableInteractionSource() }
    val frameColor = if (selected) SumiTheme.colors.gold else SumiTheme.colors.paperEdge
    val frameWidth = if (selected) 1.5.dp else 1.dp
    Column(
        modifier = modifier
            .border(frameWidth, frameColor)
            .background(SumiTheme.colors.paperGlow)
            .clickable(interactionSource = src, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The inner swatch — actual book paper colour. Tall enough to read as a
        // page sample, not a button. Two lines of ink-on-paper text preview the
        // final combination.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Sumi.Space.s2)
                .background(paper.swatchColor())
                .padding(vertical = Sumi.Space.s3, horizontal = Sumi.Space.s3),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "墨",
                    style = SumiTheme.typography.cjk.copy(fontSize = 22.sp),
                    color = inkOnPaper,
                )
                Spacer(Modifier.height(Sumi.Space.s1))
                Text(
                    text = "Aa 123",
                    style = SumiTheme.typography.numeral.copy(fontSize = 11.sp),
                    color = inkOnPaper.copy(alpha = 0.85f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Sumi.Space.s3, vertical = Sumi.Space.s2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = paper.name.uppercase(),
                style = SumiTheme.typography.uiLabel.copy(fontSize = 11.sp),
                color = SumiTheme.colors.ink,
            )
            if (selected) {
                // Tiny gold dot — confirms selection without a heavy checkmark
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SumiTheme.colors.gold),
                )
            }
        }
    }
}

/** Single ink well — a circular swatch with the ink colour, label below. */
@Composable
private fun InkWell(
    label: String,
    inkColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    // Inner dot pulses when selection becomes true — a small "ink touched the
    // paper" beat. animateFloatAsState gives us the spring for free.
    val dotScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (selected) 1.05f else 0.92f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
        ),
        label = "inkDotScale",
    )
    val ringWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (selected) 1.5.dp else 1.dp,
        animationSpec = tween(220, easing = Sumi.Ease.paper),
        label = "inkRingWidth",
    )
    val ringColor = if (selected) SumiTheme.colors.gold else SumiTheme.colors.paperEdge
    Column(
        modifier = Modifier
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(Sumi.Space.s2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(width = ringWidth, color = ringColor, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer {
                        scaleX = dotScale
                        scaleY = dotScale
                    }
                    .background(inkColor, CircleShape),
            )
        }
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = label.uppercase(),
            style = SumiTheme.typography.uiMeta.copy(fontSize = 10.sp),
            color = if (selected) SumiTheme.colors.ink else SumiTheme.colors.inkFaint,
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
        // Checkbox tile — bumped contrast in both states so the box reads as
        // a checkbox even in dark mode (paperEdge alone disappeared against
        // the night-paper bg). Filled when checked, hairline-bordered with a
        // subtle inner tint when unchecked so it's unambiguously interactive.
        val borderColor = if (checked) SumiTheme.colors.ink else SumiTheme.colors.inkSoft
        val fillColor = if (checked) SumiTheme.colors.ink else SumiTheme.colors.paperGlow
        val checkColor = if (checked) SumiTheme.colors.paper else Color.Transparent
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(1.5.dp, borderColor)
                .background(fillColor),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(icon = SumiIcons.Check, contentDescription = null, tint = checkColor, size = 16.dp)
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
            // Wait for the "Crafting…" message at the top to finish its 400ms fade-out
            // (CraftingTopMessage's AnimatedVisibility exit) before starting the reveal,
            // so the two motions don't overlap.
            delay(CRAFTING_EXIT_MS)
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
                // Custom enso-style loader sized to match ReadyBottom's 72dp
                // share circle. AnimatedContent's crossfade between this and
                // the share button reads as one element morphing — far more
                // premium than a Material spinner-to-button cut.
                EnsoLoader(size = 72.dp)
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
    // Hold the share button until after the result-center (enso → title → subtitle)
    // has finished its bloom — premium pacing: hero first, action last.
    val shareAlpha = remember { Animatable(0f) }
    val doneAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(SHARE_BUTTON_REVEAL_DELAY_MS)
        shareAlpha.animateTo(1f, tween(420, easing = Sumi.Ease.paper))
        delay(120L)
        doneAlpha.animateTo(1f, tween(360, easing = Sumi.Ease.paper))
    }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(1.5.dp, SumiTheme.colors.ink, CircleShape)
                .graphicsLayer { alpha = shareAlpha.value }
                .clickable(interactionSource = shareSrc, indication = null, onClick = onShare),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(
                icon = SumiIcons.Share,
                contentDescription = "Share Zen Sudoku",
                tint = SumiTheme.colors.ink,
                size = 26.dp,
            )
        }
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "tap to share",
            style = SumiTheme.typography.uiMeta,
            color = SumiTheme.colors.inkFaint,
            modifier = Modifier.graphicsLayer { alpha = shareAlpha.value },
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        SumiButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = doneAlpha.value },
            variant = SumiButtonVariant.Ghost,
            size = SumiButtonSize.Md,
        ) {
            Text(text = "Done", style = SumiTheme.typography.uiButton)
        }
    }
}

/**
 * Enso-styled loader: a hairline ring at the share-button diameter with a
 * sweeping arc that rotates infinitely. Sized to match ReadyBottom's share
 * circle so the loading→ready crossfade reads as a single element morphing.
 */
@Composable
private fun EnsoLoader(size: Dp = 72.dp) {
    val transition = rememberInfiniteTransition(label = "ensoLoader")
    val rotation: Float by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200, easing = LinearEasing),
        ),
        label = "ensoLoaderRot",
    )
    val ink = SumiTheme.colors.ink
    val faint = SumiTheme.colors.paperEdge
    Box(
        modifier = Modifier
            .size(size)
            .border(1.5.dp, faint, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size).graphicsLayer { rotationZ = rotation }) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
            // 90° arc that rotates around the ring — the calligraphic "drawing
            // an enso" feel without the cost of an actual brush spline.
            drawArc(
                color = ink,
                startAngle = -90f,
                sweepAngle = 90f,
                useCenter = false,
                style = stroke,
                topLeft = Offset(stroke.width / 2f, stroke.width / 2f),
                size = androidx.compose.ui.geometry.Size(
                    this.size.width - stroke.width,
                    this.size.height - stroke.width,
                ),
            )
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
    val ruleColor = textColor.copy(alpha = 0.3f)
    // Bolder grid alphas so the ink colour reads cleanly inside the preview
    // tile (was 0.35 / 0.6 — too washed out for previewing colour choices).
    val gridColor = accentColor.copy(alpha = 0.55f)
    val boxColor = accentColor

    // Outer cream frame so a Dark-paper preview sits distinctly inside the
    // app's dark mode bg (without it the preview tile blends into the screen
    // and the user can't tell where the page edge is).
    Box(
        modifier = modifier
            .aspectRatio(0.707f)
            .background(SumiTheme.colors.paperGlow)
            .padding(2.dp)
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
            // Bolder strokes so the ink colour actually reads in the preview.
            // Box separators get 2.5px (was 1.5), thin lines stay at 0.7px.
            val sw = if (isBox) 2.5f else 0.7f
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
