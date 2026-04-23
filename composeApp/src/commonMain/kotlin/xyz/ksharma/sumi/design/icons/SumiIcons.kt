@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

object SumiIcons {
    // Navigation
    val Back: ImageVector by lazy { strokeIcon("Back", "M15 5l-7 7 7 7") }
    val Close: ImageVector by lazy { strokeIcon("Close", "M6 6l12 12M18 6L6 18") }
    val Menu: ImageVector by lazy { strokeIcon("Menu", "M4 7h16M4 12h16M4 17h16") }
    val More: ImageVector by lazy {
        strokeIcon(
            "More",
            "M4 12 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M11 12 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M18 12 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
        )
    }

    // Game
    val Pause: ImageVector by lazy { strokeIcon("Pause", "M8 5v14M16 5v14") }
    val Play: ImageVector by lazy { strokeIcon("Play", "M7 5l12 7-12 7z") }
    val Undo: ImageVector by lazy { strokeIcon("Undo", "M9 14l-5-5 5-5", "M4 9h10a6 6 0 016 6") }
    val Erase: ImageVector by lazy { strokeIcon("Erase", "M18 13l-6 6H7l-3-3 9-9 5 5z", "M9 9l5 5") }
    val Brush: ImageVector by lazy {
        strokeIcon("Brush", "M7 21c-2 0-3-1-3-3s2-3 4-3", "M8 15l8-10 3 3-10 8", "M14 7l3 3")
    }
    val Lantern: ImageVector by lazy {
        strokeIcon("Lantern", "M12 3v2", "M7 7h10v6a5 5 0 01-10 0z", "M7 13h10", "M12 18v3")
    }
    val Note: ImageVector by lazy {
        strokeIcon(
            "Note",
            "M6 7 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M11 7 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M16 7 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M6 12 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M16 12 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M6 17 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M11 17 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
            "M16 17 a1 1 0 1 0 2 0 a1 1 0 1 0 -2 0",
        )
    }

    // Stats & Progress
    val Flame: ImageVector by lazy {
        strokeIcon("Flame", "M12 3c1 3 4 5 4 9a4 4 0 01-8 0c0-2 1-3 2-4-2-1-2-3 2-5z")
    }
    val Calendar: ImageVector by lazy {
        strokeIcon(
            "Calendar",
            "M5 5H19Q20 5 20 6V19Q20 20 19 20H5Q4 20 4 19V6Q4 5 5 5Z",
            "M4 9h16M9 3v4M15 3v4",
        )
    }
    val Chart: ImageVector by lazy { strokeIcon("Chart", "M4 19V5M4 19h16", "M7 15l3-4 3 2 5-7") }
    val Trophy: ImageVector by lazy {
        strokeIcon(
            "Trophy",
            "M7 4h10v5a5 5 0 01-10 0z",
            "M7 6H4v2a3 3 0 003 3M17 6h3v2a3 3 0 01-3 3",
            "M10 14v3H8v2h8v-2h-2v-3",
        )
    }
    val Check: ImageVector by lazy { strokeIcon("Check", "M5 12l5 5 9-11") }

    // Shelf
    val Book: ImageVector by lazy {
        strokeIcon("Book", "M5 4h10a3 3 0 013 3v13H8a3 3 0 01-3-3z", "M5 17a3 3 0 013-3h10")
    }
    val Quote: ImageVector by lazy {
        strokeIcon(
            "Quote",
            "M6 11V8c0-2 1-3 3-3",
            "M14 11V8c0-2 1-3 3-3",
            "M6 11H9V16H6Z",
            "M14 11H17V16H14Z",
        )
    }
    val Settings: ImageVector by lazy {
        strokeIcon(
            "Settings",
            "M9 12 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0",
            "M12 2v3M12 19v3M4 12H2M22 12h-3M5 5l2 2M17 17l2 2M5 19l2-2M17 7l2-2",
        )
    }
    val Share: ImageVector by lazy {
        strokeIcon(
            "Share",
            "M12 4v13M12 4l-4 4M12 4l4 4",
            "M5 15v3a2 2 0 002 2h10a2 2 0 002-2v-3",
        )
    }
    val Sparkle: ImageVector by lazy {
        strokeIcon("Sparkle", "M12 4v4M12 16v4M4 12h4M16 12h4M6 6l3 3M15 15l3 3M6 18l3-3M15 9l3-3")
    }
    val Heart: ImageVector by lazy {
        strokeIcon("Heart", "M12 19s-7-4-7-10a4 4 0 017-2 4 4 0 017 2c0 6-7 10-7 10z")
    }
    val Lock: ImageVector by lazy {
        strokeIcon(
            "Lock",
            "M6 11H18Q19 11 19 12V19Q19 20 18 20H6Q5 20 5 19V12Q5 11 6 11Z",
            "M8 11V8a4 4 0 018 0v3",
        )
    }
    val Sound: ImageVector by lazy {
        strokeIcon("Sound", "M5 10v4h3l4 4V6l-4 4z", "M16 8a5 5 0 010 8")
    }
}

private fun strokeIcon(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { svgPath ->
            addPath(
                pathData = PathParser().parsePathString(svgPath).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()
