package xyz.ksharma.sumi.ui.preview

/**
 * Opts a `@Preview` composable into snapshot testing.
 *
 * The scanner finds previews by their `@Preview` annotation in the bytecode and then filters by
 * this one, so it must sit on a function that already carries `@Preview` (directly, or via a
 * multi-preview annotation like [PreviewComponent] / [PreviewScreen] whose own meta-annotations
 * expand to `@Preview`). A function annotated only with `@ScreenshotTest` is never found.
 *
 * ```
 * @ScreenshotTest
 * @PreviewComponent
 * @Composable
 * internal fun SumiButtonPreview() {
 *     AppPreviewTheme { SumiButton(text = "Continue", onClick = {}) }
 * }
 * ```
 *
 * Do not annotate previews that drive infinite animations — they never settle under Robolectric's
 * frame clock and the capture hangs. Add those to `excludedPreviewNames` in the snapshot test
 * instead.
 *
 * @param threshold Comparison tolerance, 0.0 (exact match, the default) to 1.0.
 * @param description Free text for documentation and debugging.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScreenshotTest(
    val threshold: Double = 0.0,
    val description: String = "",
)
