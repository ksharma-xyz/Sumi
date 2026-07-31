package xyz.ksharma.sumi.ui.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "1. Phone",
    group = "Component",
    showBackground = true,
    device = Devices.PHONE,
    backgroundColor = 0xFF0D0D0D,
)
@Preview(
    // ASCII only: Roborazzi derives the screenshot file name from this, and a non-ASCII
    // character there hangs the capture on macOS and throws on Linux CI.
    name = "2. 2x Font Scale",
    group = "Component",
    fontScale = 2.0f,
    showBackground = true,
    device = Devices.PHONE,
    backgroundColor = 0xFF0D0D0D,
)
annotation class PreviewComponent

@Preview(
    name = "1. Phone",
    group = "Screen",
    showBackground = true,
    device = Devices.PHONE,
    backgroundColor = 0xFF0D0D0D,
)
@Preview(
    name = "2. Tablet",
    group = "Screen",
    showBackground = true,
    device = Devices.TABLET,
    backgroundColor = 0xFF0D0D0D,
)
annotation class PreviewScreen
