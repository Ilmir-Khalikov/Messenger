package ru.khalikov.messenger.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

object AvatarEncoder {
    fun encode(contentResolver: ContentResolver, uri: Uri): String {
        val source = ImageDecoder.createSource(contentResolver, uri)
        val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
            decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
            val longest = maxOf(info.size.width, info.size.height)
            if (longest > MAX_SIDE_PX) {
                val scale = MAX_SIDE_PX.toFloat() / longest
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt().coerceAtLeast(1),
                    (info.size.height * scale).roundToInt().coerceAtLeast(1)
                )
            }
        }
        val software = decoded.toSoftwareArgb()
        val encoded = compress(software)
        if (encoded.isEmpty()) {
            throw IllegalStateException("Не удалось сохранить аватар")
        }
        return "data:$MIME_TYPE;base64,${Base64.encodeToString(encoded, Base64.NO_WRAP)}"
    }

    fun decodeToBitmap(photo: String): Bitmap? {
        if (photo.isBlank() || photo.startsWith("http://") || photo.startsWith("https://")) {
            return null
        }
        return try {
            val payload = photo.substringAfter("base64,", photo)
            val bytes = Base64.decode(payload, Base64.DEFAULT)
            if (bytes.isEmpty()) null else BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    private fun Bitmap.toSoftwareArgb(): Bitmap {
        if (config == Bitmap.Config.ARGB_8888 && !isHardware()) return this
        return copy(Bitmap.Config.ARGB_8888, false) ?: this
    }

    private fun Bitmap.isHardware(): Boolean = config == Bitmap.Config.HARDWARE

    private fun compress(bitmap: Bitmap): ByteArray {
        var quality = START_QUALITY
        var bytes = ByteArray(0)
        do {
            val stream = ByteArrayOutputStream()
            val compressed = bitmap.compress(compressFormat, quality, stream)
            if (!compressed) return ByteArray(0)
            bytes = stream.toByteArray()
            quality -= 5
        } while (bytes.size > MAX_BYTES && quality >= MIN_QUALITY)
        return bytes
    }

    private val compressFormat: Bitmap.CompressFormat
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }

    private const val MIME_TYPE = "image/webp"
    private const val MAX_SIDE_PX = 512
    private const val MAX_BYTES = 220_000
    private const val START_QUALITY = 92
    private const val MIN_QUALITY = 75
}
