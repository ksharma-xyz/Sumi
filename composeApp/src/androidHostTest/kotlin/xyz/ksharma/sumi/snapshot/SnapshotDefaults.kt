package xyz.ksharma.sumi.snapshot

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziOptions

/**
 * Shared configuration for Sumi's snapshot tests.
 *
 * Ported from KRAIL's `core:snapshot-testing`. Kept separate from [BaseSnapshotTest] so both can
 * move into a shared library later without reshaping the call sites.
 */
object SnapshotDefaults {

    /** Font scales captured in light mode. 2x is the accessibility case that breaks layouts. */
    val lightModeFontScales = listOf(1.0f, 2.0f)

    /** Font scales captured in dark mode. */
    val darkModeFontScales = listOf(1.0f)

    const val DEFAULT_DEVICE = RobolectricDeviceQualifiers.Pixel6

    /** 0.0 means an exact pixel match. */
    const val DEFAULT_THRESHOLD = 0.0

    const val DEFAULT_SDK = 34

    @OptIn(ExperimentalRoborazziApi::class)
    fun roborazziOptions(
        threshold: Double = DEFAULT_THRESHOLD,
        resizeScale: Double = 1.0,
    ) = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(
            changeThreshold = threshold.toFloat(),
        ),
        recordOptions = RoborazziOptions.RecordOptions(
            resizeScale = resizeScale,
        ),
    )
}
