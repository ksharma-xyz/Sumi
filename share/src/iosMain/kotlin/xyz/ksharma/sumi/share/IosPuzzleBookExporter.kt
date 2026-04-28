package xyz.ksharma.sumi.share

class IosPuzzleBookExporter : PuzzleBookExporter {
    override suspend fun generate(spec: PuzzleBookSpec): Result<ByteArray> =
        Result.failure(UnsupportedOperationException("PDF export coming soon on iOS"))

    override suspend fun share(bytes: ByteArray, title: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("PDF export coming soon on iOS"))
}
