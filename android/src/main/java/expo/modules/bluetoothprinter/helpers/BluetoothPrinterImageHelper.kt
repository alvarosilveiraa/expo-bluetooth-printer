package expo.modules.bluetoothprinter.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Canvas
import android.util.Base64
import java.io.ByteArrayOutputStream

class BluetoothPrinterImageHelper {
  companion object {
    internal fun generateBase64ByteArray(base64: String, width: Int, maxWidth: Int, align: String): ByteArray? {
      val decoded = Base64.decode(base64, Base64.DEFAULT)
      val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size) ?: return null
      val resizedBitmap = resizeBitmap(bitmap, width)
      val coloredBitmap = colorizeBitmap(resizedBitmap)
      val left = calculateLeft(width, maxWidth, align)
      return bitmapToByteArray(coloredBitmap, left)
    }

    private fun bitmapToByteArray(bitmap: Bitmap, left: Int): ByteArray {
      val width = bitmap.width
      val height = bitmap.height
      val bytesPerRow = (width + 7) / 8
      val imageData = ByteArrayOutputStream()
      val escPosHeader = byteArrayOf(
        0x1D,
        0x76,
        0x30,
        0x00,
        ((width + left) / 8).toByte(),
        0,
        height.toByte(),
        0
      )
      imageData.write(escPosHeader)
      val leftBytes = ByteArray(left / 8) { 0x00 }
      for (y in 0 until height) {
        imageData.write(leftBytes)
        for (x in 0 until bytesPerRow) {
          var byte = 0
          for (bit in 0..7) {
            val pixelX = x * 8 + bit
            if (pixelX < width) {
              val pixelColor = bitmap.getPixel(pixelX, y)
              if (pixelColor == Color.BLACK) {
                byte = byte or (1 shl (7 - bit))
              }
            }
          }
          imageData.write(byte)
        }
      }
      return imageData.toByteArray()
    }

    private fun colorizeBitmap(bitmap: Bitmap): Bitmap {
      val width = bitmap.width
      val height = bitmap.height
      val bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
      val threshold = otsuThreshold(bitmap)
      for (y in 0 until height) {
        for (x in 0 until width) {
          val pixel = bitmap.getPixel(x, y)
          val alpha = Color.alpha(pixel)
          if (alpha < 128) {
            bwBitmap.setPixel(x, y, Color.WHITE)
            continue
          }
          val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
          val bwColor = if (gray < threshold) Color.WHITE else Color.BLACK
          bwBitmap.setPixel(x, y, bwColor)
        }
      }
      return bwBitmap
    }


    private fun resizeBitmap(bitmap: Bitmap, width: Int): Bitmap {
      val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
      val height = (width * aspectRatio).toInt()
      return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun otsuThreshold(bitmap: Bitmap): Int {
      val hist = IntArray(256)
      for (y in 0 until bitmap.height) {
        for (x in 0 until bitmap.width) {
          val pixel = bitmap.getPixel(x, y)
          val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
          hist[gray]++
        }
      }
      var sum = 0
      for (i in hist.indices) sum += i * hist[i]
      var sumB = 0
      var wB = 0
      var wF = 0
      val total = bitmap.width * bitmap.height
      var varMax = 0.0
      var threshold = 0
      for (i in hist.indices) {
        wB += hist[i]
        if (wB == 0) continue
        wF = total - wB
        if (wF == 0) break
        sumB += i * hist[i]
        val mB = sumB / wB
        val mF = (sum - sumB) / wF
        val variance = (wB * wF * (mB - mF) * (mB - mF)).toDouble()
        if (variance > varMax) {
          varMax = variance
          threshold = i
        }
      }
      return threshold
    }

    private fun calculateLeft(width: Int, maxWidth: Int, align: String): Int {
      val rawLeft = when (align) {
        "center" -> (maxWidth - width) / 2
        "right" -> maxWidth - width
        else -> 0
      }.coerceAtLeast(0)
      return rawLeft - (rawLeft % 8)
    }
  }
}
