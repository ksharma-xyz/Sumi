package xyz.ksharma.sumi.snapshot

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import xyz.ksharma.darpan.BaseSnapshotTest
import xyz.ksharma.darpan.SnapshotDefaults

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
    qualifiers = SnapshotDefaults.DEFAULT_DEVICE,
    manifest = Config.NONE,
)
class SumiSnapshotTest : BaseSnapshotTest() {

    override val packageToScan = "xyz.ksharma.sumi"

    @Test
    fun generateScreenshots() = generateSnapshots()
}
