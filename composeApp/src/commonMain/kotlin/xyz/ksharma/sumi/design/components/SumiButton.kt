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
import xyz.ksharma.sumi.theme.SumiTokens as Sumi
import xyz.ksharma.sumi.theme.SumiTheme

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

    val bg = variantBg(variant)
    val fg = variantFg(variant)
    val borderColor = variantBorder(variant)

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
                if (pressAlpha > 0f) drawRect(color = Sumi.Color.ink.copy(alpha = pressAlpha))
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
    val fontSize = when (size) {
        SumiButtonSize.Sm -> 11
        SumiButtonSize.Md -> 13
        SumiButtonSize.Lg -> 14
    }
    SumiButton(onClick = onClick, modifier = modifier, variant = variant, size = size, enabled = enabled) {
        Text(
            text = text.uppercase(),
            style = SumiTheme.typography.uiButton,
            textAlign = TextAlign.Center,
        )
    }
}

private fun variantBg(variant: SumiButtonVariant): Color = when (variant) {
    SumiButtonVariant.Primary -> Sumi.Color.ink
    SumiButtonVariant.Ghost   -> Color.Transparent
    SumiButtonVariant.Subtle  -> Sumi.Color.paperWarm
    SumiButtonVariant.Red     -> Sumi.Color.red
}

private fun variantFg(variant: SumiButtonVariant): Color = when (variant) {
    SumiButtonVariant.Primary -> Sumi.Color.paper
    SumiButtonVariant.Ghost   -> Sumi.Color.ink
    SumiButtonVariant.Subtle  -> Sumi.Color.ink
    SumiButtonVariant.Red     -> Sumi.Color.paper
}

private fun variantBorder(variant: SumiButtonVariant): Color = when (variant) {
    SumiButtonVariant.Primary -> Sumi.Color.ink
    SumiButtonVariant.Ghost   -> Sumi.Color.ink
    SumiButtonVariant.Subtle  -> Sumi.Color.paperEdge
    SumiButtonVariant.Red     -> Sumi.Color.red
}
