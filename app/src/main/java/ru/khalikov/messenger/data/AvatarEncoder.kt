package ru.khalikov.messenger.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

object AvatarEncoder {
    fun encode(contentResolver: ContentResolver, uri: Uri): String {
        val source = ImageDecoder.createSource(contentResolver, uri)
        val original = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        val scaled = scaleToMax(original, MAX_SIDE_PX)
        val jpeg = compress(scaled)
        return "data:image/jpeg;base64,${Base64.encodeToString(jpeg, Base64.NO_WRAP)}"
    }

    private fun scaleToMax(bitmap: Bitmap, maxSide: Int): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= maxSide) return bitmap
        val scale = maxSide.toFloat() / longest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).roundToInt().coerceAtLeast(1),
            (bitmap.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
    }

    private fun compress(bitmap: Bitmap): ByteArray {
        var quality = 80
        var bytes: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            bytes = stream.toByteArray()
            quality -= 10
        } while (bytes.size > MAX_BYTES && quality >= 30)
        return bytes
    }

    private const val MAX_SIDE_PX = 256
    private const val MAX_BYTES = 80_000
}
