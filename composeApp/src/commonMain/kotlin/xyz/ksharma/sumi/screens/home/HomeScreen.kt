@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.Quote
import xyz.ksharma.sumi.design.components.LogoWordmark
import xyz.ksharma.sumi.design.components.PetalBurstConfig
import xyz.ksharma.sumi.design.components.QuoteRule
import xyz.ksharma.sumi.design.components.SumiEyebrow
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.SumiPetalBurst
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.navigation.ExpandedMinWidth
import xyz.ksharma.sumi.navigation.LocalNavRailWidth
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.statusBarsWithCornerClearance
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private val DIFFICULTY_TILES = listOf(
    Triple("Easy", "一", "3 min"),
    Triple("Medium", "二", "6 min"),
    Triple("Hard", "三", "12 min"),
    Triple("Master", "四", "25 min"),
    Triple("Edo", "五", "45 min"),
)

@Suppress("LongParameterList", "LongMethod")
@Composable
fun HomeScreen(
    streakDays: Int,
    quote: Quote,
    onStartGame: (difficulty: String) -> Unit,
    onSettings: () -> Unit,
    onInvite: (ImageBitmap) -> Unit,
    isPro: Boolean,
    modifier: Modifier = Modifier,
    lockedDifficulties: Set<String> = emptySet(),
    bottomBanner: (@Composable () -> Unit)? = null,
) {
    // Pre-prepared graphics layer — the offscreen InviteShareCard records into
    // it on every composition. The invite tap snapshots it to a bitmap and
    // hands the bitmap + text to the OS share sheet via ShareManager.
    val inviteShareLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    // Every ~2.5 min on Home, a brief wind passes — a tiny petal gust + a soft
    // glow on the invite button. Kept very subtle: 5 petals over 2.4s, glow
    // ~600ms breath. Performant: SumiPetalBurst already pools draw calls.
    var gustTrigger by remember { mutableIntStateOf(0) }
    val inviteGlow = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(GUST_INITIAL_DELAY_MS)
        while (true) {
            gustTrigger++
            inviteGlow.animateTo(1f, tween(900, easing = Sumi.Ease.paper))
            inviteGlow.animateTo(0f, tween(1_400, easing = Sumi.Ease.paper))
            delay(GUST_INTERVAL_MS)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // The window class is the window's width, which includes any navigation rail beside us.
        val windowWidth = maxWidth + LocalNavRailWidth.current
        val expanded = windowWidth >= ExpandedMinWidth
        val shareInvite: () -> Unit = {
            // Snapshot the offscreen InviteShareCard, hand the
            // bitmap up so HomeEntry can call shareImage(bitmap, text).
            coroutineScope.launch {
                onInvite(inviteShareLayer.toImageBitmap())
            }
        }
        WashiBG(modifier = Modifier.fillMaxSize(), capContentWidth = !expanded) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (expanded) {
                    ExpandedHomeContent(
                        streakDays = streakDays,
                        quote = quote,
                        onStartGame = onStartGame,
                        onSettings = onSettings,
                        onInvite = shareInvite,
                        inviteGlow = inviteGlow.value,
                        lockedDifficulties = lockedDifficulties,
                        bottomReserve = if (bottomBanner != null) 64.dp else Sumi.Space.s5,
                    )
                } else {
                    CompactHomeContent(
                        streakDays = streakDays,
                        quote = quote,
                        onStartGame = onStartGame,
                        onSettings = onSettings,
                        onInvite = shareInvite,
                        inviteGlow = inviteGlow.value,
                        lockedDifficulties = lockedDifficulties,
                        bottomReserve = if (bottomBanner != null) 64.dp else Sumi.Space.s5,
                        wide = windowWidth >= MEDIUM_MIN_WIDTH,
                    )
                }
                // Offscreen InviteShareCard — positioned far off-screen via absoluteOffset
                // so it lays out + draws (into inviteShareLayer) on every composition,
                // but is never visible. toImageBitmap() above always has a fresh capture
                // ready. Pro variant uses gold ink + "Sumi Pro"; otherwise normal Sumi.
                Box(
                    modifier = Modifier.absoluteOffset(x = (-10_000).dp, y = (-10_000).dp),
                ) {
                    InviteShareCard(layer = inviteShareLayer, isPro = isPro)
                }
                // Ambient gust — a wind passes ~every 2.5 min. Sparse, slow.
                SumiPetalBurst(
                    trigger = gustTrigger,
                    modifier = Modifier.fillMaxSize(),
                    config = HOME_GUST_CONFIG,
                )
                if (bottomBanner != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter,
                    ) { bottomBanner() }
                }
            }
        }
    }
}

/** Phone and medium windows: one column, unchanged. */
@Suppress("LongParameterList")
@Composable
private fun CompactHomeContent(
    streakDays: Int,
    quote: Quote,
    onStartGame: (String) -> Unit,
    onSettings: () -> Unit,
    onInvite: () -> Unit,
    inviteGlow: Float,
    lockedDifficulties: Set<String>,
    bottomReserve: Dp,
    wide: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsWithCornerClearance()
            .padding(horizontal = Sumi.Space.s6)
            // Reserve room at the bottom so banner-anchored content doesn't overlap
            // the last difficulty tile when scrolled to the end.
            .padding(top = Sumi.Space.s4, bottom = bottomReserve),
    ) {
        HomeTopChrome(onSettings = onSettings)
        Spacer(Modifier.height(Sumi.Space.s5))
        DailyQuoteBlock(quote = quote)
        Spacer(Modifier.height(Sumi.Space.s5))
        QuoteRule()
        Spacer(Modifier.height(Sumi.Space.s5))
        StreakCard(streakDays = streakDays)
        Spacer(Modifier.height(Sumi.Space.s4))
        NewPracticeGrid(onStartGame = onStartGame, lockedDifficulties = lockedDifficulties, fillLastRow = wide)
        Spacer(Modifier.height(Sumi.Space.s5))
        InviteRow(glowAlpha = inviteGlow, onInvite = onInvite, fullWidth = wide)
    }
}

/**
 * Expanded windows: the header spans the content width (max [HOME_PAGE_MAX_WIDTH]), then two
 * panes on one top line, [HOME_PANE_GAP] apart. Left: the quote, streak and invite. Right: the
 * difficulty grid. The "Today / Practice" and "New Practice" labels share a baseline because both
 * panes start at the same top edge with the same first element.
 */
@Suppress("LongParameterList")
@Composable
private fun ExpandedHomeContent(
    streakDays: Int,
    quote: Quote,
    onStartGame: (String) -> Unit,
    onSettings: () -> Unit,
    onInvite: () -> Unit,
    inviteGlow: Float,
    lockedDifficulties: Set<String>,
    bottomReserve: Dp,
) {
    // The viewport is measured outside the scroll container, whose own constraints are unbounded.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsWithCornerClearance()
                .padding(top = Sumi.Space.s4, bottom = bottomReserve),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(
                    max = HOME_PAGE_MAX_WIDTH,
                ).fillMaxWidth().padding(horizontal = Sumi.Space.s6),
            ) {
                HomeTopChrome(onSettings = onSettings)
                Spacer(Modifier.height(Sumi.Space.s5))
                // The body sits centred in the height the header leaves, so a tall window is balanced
                // above and below instead of empty at the bottom. Short windows just scroll.
                Column(
                    modifier = Modifier.heightIn(min = viewportHeight - HOME_HEADER_RESERVE),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(HOME_PANE_GAP)) {
                        Column(modifier = Modifier.weight(HOME_LEFT_WEIGHT)) {
                            DailyQuoteBlock(quote = quote, large = true)
                            Spacer(Modifier.height(Sumi.Space.s6))
                            QuoteRule()
                            Spacer(Modifier.height(Sumi.Space.s6))
                            StreakCard(streakDays = streakDays)
                        }
                        BoxWithConstraints(modifier = Modifier.weight(HOME_RIGHT_WEIGHT)) {
                            NewPracticeGrid(
                                onStartGame = onStartGame,
                                lockedDifficulties = lockedDifficulties,
                                columns = if (maxWidth >= HOME_THREE_COLUMN_MIN_WIDTH) 3 else 2,
                                fillLastRow = true,
                                large = true,
                            )
                        }
                    }
                    // Spans from the left pane's left edge to the right pane's right edge.
                    Spacer(Modifier.height(HOME_PANE_GAP))
                    InviteRow(glowAlpha = inviteGlow, onInvite = onInvite, fullWidth = true)
                }
            }
        }
    }
}

/** Height the header row, its spacing and the page insets take, kept out of the centred body's minimum. */
private val HOME_HEADER_RESERVE = 160.dp

private val MEDIUM_MIN_WIDTH = 600.dp
private val HOME_PAGE_MAX_WIDTH = 1200.dp
private val HOME_PANE_GAP = 24.dp
private val HOME_THREE_COLUMN_MIN_WIDTH = 560.dp
private const val HOME_LEFT_WEIGHT = 5f
private const val HOME_RIGHT_WEIGHT = 7f

private const val GUST_INITIAL_DELAY_MS = 60_000L // first gust after 1 min on Home
private const val GUST_INTERVAL_MS = 150_000L // every 2.5 min after
private val HOME_GUST_CONFIG = PetalBurstConfig(
    count = 5,
    sizeMultiplier = 0.7f,
    durationMs = 2_400,
    swayScale = 1.4f,
)

/**
 * Calm, Zen-themed "invite a friend" affordance. Glows briefly with each ambient
 * petal-gust so it draws attention without ever feeling like a CTA. Single tap
 * opens the OS share sheet.
 */
@Composable
private fun InviteRow(glowAlpha: Float, onInvite: () -> Unit, fullWidth: Boolean = false) {
    val src = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Sumi.Space.s3),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
            modifier = Modifier
                .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                .clickable(interactionSource = src, indication = null, onClick = onInvite)
                .border(
                    width = 1.dp,
                    color = SumiTheme.colors.gold.copy(alpha = 0.35f + glowAlpha * 0.5f),
                )
                .background(SumiTheme.colors.gold.copy(alpha = glowAlpha * 0.06f))
                .padding(horizontal = Sumi.Space.s5, vertical = Sumi.Space.s3)
                .graphicsLayer {
                    // A breath of scale on the glow peak — like the page noticing.
                    val s = 1f + glowAlpha * 0.015f
                    scaleX = s
                    scaleY = s
                },
        ) {
            Text(
                text = "風",
                style = SumiTheme.typography.cjk.copy(fontSize = 18.sp),
                color = SumiTheme.colors.gold,
                modifier = Modifier.semantics { hideFromAccessibility() },
            )
            Column {
                Text(
                    text = "Pass Sumi along",
                    style = SumiTheme.typography.body.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight(500),
                    ),
                    color = SumiTheme.colors.ink,
                )
                Text(
                    text = "Share this quiet with someone you'd like to share quiet with.",
                    style = SumiTheme.typography.uiMeta.copy(fontSize = 11.sp),
                    color = SumiTheme.colors.inkSoft,
                )
            }
        }
    }
}

@Composable
private fun HomeTopChrome(onSettings: () -> Unit) {
    val settingsSrc = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogoWordmark(scale = 0.56f, color = SumiTheme.colors.ink, accent = SumiTheme.colors.red)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(Sumi.Layout.minTap)
                .clickable(interactionSource = settingsSrc, indication = null, onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(
                icon = SumiIcons.Settings,
                contentDescription = "Settings",
                tint = SumiTheme.colors.inkSoft,
                size = 22.dp,
            )
        }
    }
}

@Composable
private fun DailyQuoteBlock(quote: Quote, large: Boolean = false) {
    Column {
        SumiEyebrow(text = "Today / Practice", color = SumiTheme.colors.red)
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "\u201C${quote.text}\u201D",
            style = if (large) {
                SumiTheme.typography.quote.copy(fontSize = 40.sp, lineHeight = 52.sp)
            } else {
                SumiTheme.typography.quote.copy(lineHeight = 36.sp)
            },
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = "— ${quote.attribution}".uppercase(),
            style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.sp),
            color = SumiTheme.colors.inkSoft,
        )
    }
}

@Composable
private fun StreakCard(streakDays: Int) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = if (streakDays == 1) "1 day streak" else "$streakDays day streak"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(width = 1.5.dp, color = SumiTheme.colors.red, shape = CircleShape)
                .background(SumiTheme.colors.red.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(icon = SumiIcons.Flame, contentDescription = null, tint = SumiTheme.colors.red, size = 28.dp)
        }
        Column {
            Text(
                text = "STREAK",
                style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.sp),
                color = SumiTheme.colors.inkFaint,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$streakDays",
                    style = SumiTheme.typography.h2.copy(
                        fontSize = 32.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight(500),
                    ),
                    color = SumiTheme.colors.ink,
                )
                Text(
                    text = "days",
                    style = SumiTheme.typography.h3.copy(
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Normal,
                    ),
                    color = SumiTheme.colors.inkSoft,
                )
            }
            Spacer(Modifier.height(Sumi.Space.s1))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(minOf(streakDays, 14)) { i ->
                    Box(
                        modifier = Modifier
                            .size(width = 8.dp, height = 4.dp)
                            .background(SumiTheme.colors.red.copy(alpha = 0.5f + i * 0.035f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewPracticeGrid(
    onStartGame: (String) -> Unit,
    lockedDifficulties: Set<String>,
    columns: Int = 2,
    fillLastRow: Boolean = false,
    large: Boolean = false,
) {
    Column {
        SumiEyebrow(text = "New Practice", color = SumiTheme.colors.inkFaint)
        Spacer(Modifier.height(Sumi.Space.s3))
        val rows = DIFFICULTY_TILES.chunked(columns)
        Column(verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
                    row.forEach { (name, kanji, time) ->
                        DifficultyTile(
                            name = name,
                            kanji = kanji,
                            avgTime = time,
                            isLocked = name in lockedDifficulties,
                            onClick = { onStartGame(name) },
                            modifier = Modifier.weight(1f),
                            large = large,
                        )
                    }
                    // Wide windows let the last row's cards share the full row instead of leaving a gap.
                    if (!fillLastRow) repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun DifficultyTile(
    name: String,
    kanji: String,
    avgTime: String,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    // Expanded windows have room to spare, so the cards grow instead of leaving the page empty.
    val scale = if (large) 1.4f else 1f
    val src = remember { MutableInteractionSource() }
    val borderColor = if (isLocked) SumiTheme.colors.gold else SumiTheme.colors.paperEdge
    val kanjiColor = if (isLocked) SumiTheme.colors.gold else SumiTheme.colors.red
    val tileDescription = if (isLocked) "Start $name game, requires Sumi Pro" else "Start $name game, about $avgTime"
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor)
                .semantics(mergeDescendants = true) { contentDescription = tileDescription }
                .clickable(interactionSource = src, indication = null, onClick = onClick)
                .padding(horizontal = Sumi.Space.s3 * scale, vertical = Sumi.Space.s3 * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3 * scale),
        ) {
            Text(
                text = kanji,
                style = SumiTheme.typography.cjk.copy(fontSize = (30 * scale).sp),
                color = kanjiColor,
                modifier = Modifier.semantics { hideFromAccessibility() },
            )
            Column {
                Text(
                    text = name,
                    style = SumiTheme.typography.h3.copy(
                        fontSize = (20 * scale).sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight(500),
                    ),
                    color = SumiTheme.colors.ink,
                )
                Text(
                    text = if (isLocked) "Sumi Pro" else avgTime,
                    style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.sp, fontSize = (12 * scale).sp),
                    color = if (isLocked) SumiTheme.colors.gold else SumiTheme.colors.inkFaint,
                )
            }
        }
        if (isLocked) {
            Text(
                text = "PRO",
                style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.sp, fontSize = 9.sp),
                color = SumiTheme.colors.gold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 6.dp),
            )
        }
    }
}

// ── Invite share card (offscreen, captured to bitmap) ─────────────────────────

// Hardcoded share-card palette — captured PNG looks identical regardless of
// the user's app theme. Cream paper + dark ink = portable artifact.
private val InviteShareInk = Color(0xFF2A2218)
private val InviteShareInkSoft = Color(0xFF5A4838)
private val InvitePaper = Color(0xFFFBF7EC)
private val InviteGold = Color(0xFF8A6B2A)

/**
 * Square-ish share card rendered offscreen and captured to a bitmap when the
 * user taps "Pass Sumi along". Pro variant uses the gold ink + "Sumi Pro"
 * wordmark; otherwise the standard Sumi mark.
 *
 * Compose primitives only — no painterResource (vector capture into
 * graphicsLayer is unreliable across platforms; the Box-with-character
 * approach guarantees the captured PNG always matches the on-screen render).
 */
@Composable
private fun InviteShareCard(layer: GraphicsLayer, isPro: Boolean) {
    val accent = if (isPro) InviteGold else InviteShareInk
    val brand = if (isPro) "Sumi Pro" else "Sumi"
    Column(
        modifier = Modifier
            .size(width = 360.dp, height = 360.dp)
            .drawWithContent {
                layer.record {
                    drawRect(color = InvitePaper)
                    this@drawWithContent.drawContent()
                }
                drawLayer(layer)
            }
            .padding(Sumi.Space.s7),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top — small wordmark
        Text(
            text = "墨",
            style = SumiTheme.typography.cjk.copy(fontSize = 36.sp),
            color = accent,
        )

        // Centre — brand line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = brand,
                style = SumiTheme.typography.h1.copy(
                    fontSize = 56.sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = accent,
            )
            Spacer(Modifier.height(Sumi.Space.s3))
            Text(
                text = "A quiet sudoku.",
                style = SumiTheme.typography.subhead.copy(fontStyle = FontStyle.Italic),
                color = InviteShareInkSoft,
            )
        }

        // Bottom — domain hint (URL goes in the share text body, not the image)
        Text(
            text = "iOS / Android".uppercase(),
            style = SumiTheme.typography.uiMeta.copy(fontSize = 11.sp, letterSpacing = 2.sp),
            color = InviteShareInkSoft,
        )
    }
}
