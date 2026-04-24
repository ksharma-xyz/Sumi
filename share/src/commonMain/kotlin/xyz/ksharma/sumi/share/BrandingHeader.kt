package xyz.ksharma.sumi.share

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Returns a new [ImageBitmap] with a Sumi branding header painted above [this] image.
 *
 * The header contains:
 *  - [titleText]    — centred, bold (~28sp). Default: "Sumi".
 *  - [subtitleText] — centred, smaller (~12sp). Default: "Daily Zen Sudoku".
 *  - [BRANDING_HEADER_PADDING_DP] dp padding above the title and below the subtitle.
 *
 * @param backgroundColor Background colour of the header strip — should match the card background.
 * @param textColor        Colour for both title and subtitle text.
 * @param density          Screen density from [androidx.compose.ui.platform.LocalDensity], used to
 *                         convert dp/sp to pixels.
 */
expect fun ImageBitmap.withBrandingHeader(
    titleText: String = "Sumi",
    subtitleText: String = "Daily Zen Sudoku",
    backgroundColor: Color,
    textColor: Color,
    density: Float,
): ImageBitmap
