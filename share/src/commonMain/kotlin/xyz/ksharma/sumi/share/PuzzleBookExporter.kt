package xyz.ksharma.sumi.share

interface PuzzleBookExporter {
    /** Renders the PDF bytes from [spec]. Call [share] afterwards to present the OS share sheet. */
    suspend fun generate(spec: PuzzleBookSpec): Result<ByteArray>

    /** Presents the OS share sheet for a previously [generate]d PDF [bytes]. */
    suspend fun share(bytes: ByteArray, title: String): Result<Unit>
}
