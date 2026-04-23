package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

enum class SumiButtonVariant { Primary, Ghost, Subtle, Red }
enum class SumiButtonSize { Sm, Md, Lg }

@Composable
fun SumiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SumiButtonVariant = SumiButtonVariant.Primary,
    size: SumiButtonSize = SumiButtonSize.Md,
    enabled: Boolean = true,
) {
    SumiButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        content = {},
    )
}

@Composable
fun SumiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SumiButtonVariant = SumiButtonVariant.Primary,
    size: SumiButtonSize = SumiButtonSize.Md,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val colors = SumiTheme.colors

    val bg = variantBg(variant, colors)
    val fg = variantFg(variant, colors)
    val borderColor = variantBorder(variant, colors)

    // 1-frame ink-darken press state — no ripple
    val pressAlpha = if (pressed && enabled) 0.12f else 0f
    val disabledAlpha = if (!enabled) 0.4f else 1f

    val padding = when (size) {
        SumiButtonSize.Sm -> PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        SumiButtonSize.Md -> PaddingValues(horizontal = 20.dp, vertical = 14.dp)
        SumiButtonSize.Lg -> PaddingValues(horizontal = 24.dp, vertical = 18.dp)
    }
    val typography = SumiTheme.typography

    val shape = RoundedCornerShape(Sumi.Radius.xs)

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                drawRect(color = bg.copy(alpha = bg.alpha * disabledAlpha))
                if (pressAlpha > 0f) drawRect(color = colors.ink.copy(alpha = pressAlpha))
            }
            .border(1.dp, borderColor.copy(alpha = borderColor.alpha * disabledAlpha), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides fg.copy(alpha = fg.alpha * disabledAlpha)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon?.invoke()
                content()
            }
        }
    }
}

@Composable
fun SumiTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SumiButtonVariant = SumiButtonVariant.Primary,
    size: SumiButtonSize = SumiButtonSize.Md,
    enabled: Boolean = true,
) {
    SumiButton(onClick = onClick, modifier = modifier, variant = variant, size = size, enabled = enabled) {
        Text(
            text = text.uppercase(),
            style = SumiTheme.typography.uiButton,
            textAlign = TextAlign.Center,
        )
    }
}

private fun variantBg(variant: SumiButtonVariant, c: xyz.ksharma.sumi.theme.SumiColors): Color = when (variant) {
    SumiButtonVariant.Primary -> c.ink
    SumiButtonVariant.Ghost -> Color.Transparent
    SumiButtonVariant.Subtle -> c.paperWarm
    SumiButtonVariant.Red -> c.red
}

private fun variantFg(variant: SumiButtonVariant, c: xyz.ksharma.sumi.theme.SumiColors): Color = when (variant) {
    SumiButtonVariant.Primary -> c.paper
    SumiButtonVariant.Ghost -> c.ink
    SumiButtonVariant.Subtle -> c.ink
    SumiButtonVariant.Red -> c.paper
}

private fun variantBorder(variant: SumiButtonVariant, c: xyz.ksharma.sumi.theme.SumiColors): Color = when (variant) {
    SumiButtonVariant.Primary -> c.ink
    SumiButtonVariant.Ghost -> c.ink
    SumiButtonVariant.Subtle -> c.paperEdge
    SumiButtonVariant.Red -> c.red
}
