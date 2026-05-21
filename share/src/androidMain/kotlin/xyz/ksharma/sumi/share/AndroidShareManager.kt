package xyz.ksharma.sumi.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val PNG_QUALITY = 100

class AndroidShareManager(private val context: Context) : ShareManager {

    override suspend fun shareImage(bitmap: ImageBitmap, title: String, text: String?): Result<Unit> =
        runCatching {
            val uri = withContext(Dispatchers.IO) { saveBitmapToCache(bitmap.asAndroidBitmap()) }
            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    if (text != null) putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, title).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
            }
        }

    override suspend fun shareText(text: String, title: String): Result<Unit> = runCatching {
        withContext(Dispatchers.Main) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(
                Intent.createChooser(intent, title).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val cacheDir = File(context.cacheDir, "share").also { it.mkdirs() }
        val file = File(cacheDir, "sumi_${System.currentTimeMillis()}.png")
        file.outputStream().use { out ->
            // compress() returns false on failure (recycled or hardware-backed bitmap).
            val success = bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, out)
            check(success) { "Bitmap.compress() failed" }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.share.fileprovider",
            file,
        )
    }
}
