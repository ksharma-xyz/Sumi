package xyz.ksharma.sumi.snapshot

import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Captures every `@ScreenshotTest`-annotated preview in the app.
 *
 * | Action | Command |
 * |---|---|
 * | Record baselines | `./gradlew :composeApp:recordRoborazziAndroidHostTest` |
 * | Verify (CI) | `./gradlew :composeApp:verifyRoborazziAndroidHostTest` |
 * | Emit diff PNGs | `./gradlew :composeApp:compareRoborazziAndroidHostTest` |
 *
 * The `Debug`-suffixed task names do not exist on a KMP `androidLibrary` module.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [SnapshotDefaults.DEFAULT_SDK],
    qualifiers = RobolectricDeviceQualifiers.Pixel6,
    manifest = Config.NONE,
)
class SumiSnapshotTest : BaseSnapshotTest() {

    override val packageToScan = "xyz.ksharma.sumi"

    /**
     * Off for now: `AppPreviewTheme` passes `dark = false` to `AppTheme` unconditionally, so it
     * ignores the uiMode this harness sets and every dark capture would be byte-identical to its
     * light counterpart. To enable, give `AppPreviewTheme`'s `dark` parameter a default of
     * `isSystemInDarkTheme()` and flip this to true.
     */
    override val testDarkMode = false

    @Test
    fun generateScreenshots() = generateSnapshots()
}
