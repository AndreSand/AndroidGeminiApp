package com.androidai.gemini.api

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream

/**
 * Data classes for multimodal requests to the Gemini API.
 * These classes support sending images along with text.
 */

@Serializable
data class MultimodalContent(
    val parts: List<MultimodalPart>,
    val role: String = "user"
)

@Serializable
data class MultimodalPart(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class MultimodalRequest(
    val contents: List<MultimodalContent>
)

/**
 * Utility function to convert a Bitmap to a Base64 encoded string.
 *
 * @param bitmap The bitmap to convert.
 * @param format The format to compress the bitmap to (default: JPEG).
 * @param quality The compression quality (0-100, default: 90).
 * @return A Base64 encoded string representation of the bitmap.
 */
fun Bitmap.toBase64String(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 90
): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(format, quality, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

/**
 * Converts a bitmap to an InlineData object for use in API requests.
 *
 * @return An InlineData object containing the Base64-encoded bitmap.
 */
fun Bitmap.toInlineData(): InlineData {
    return InlineData(
        mimeType = "image/jpeg",
        data = toBase64String()
    )
}