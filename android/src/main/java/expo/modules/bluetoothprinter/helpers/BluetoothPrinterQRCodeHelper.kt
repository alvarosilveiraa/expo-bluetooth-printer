package expo.modules.bluetoothprinter.helpers

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream

class BluetoothPrinterQRCodeHelper {
  companion object {
    internal fun generateQRCodeByteArray(data: String, size: Int): ByteArray? {
      val bitmap = qrCodeBitmap(data, size) ?: return null
      return bitmapToByteArray(bitmap)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
      val width = bitmap.width
      val height = bitmap.height
      val bytesPerRow = (width + 7) / 8
      val imageData = ByteArrayOutputStream()
      val escPosHeader = byteArrayOf(0x1D, 0x76, 0x30, 0x00, (width / 8).toByte(), 0, height.toByte(), 0)
      imageData.write(escPosHeader)
      for (y in 0 until height) {
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

    private fun qrCodeBitmap(data: String, size: Int): Bitmap? {
      return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
          for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
          }
        }
        bitmap
      } catch (e: Exception) {
        e.printStackTrace()
        null
      }
    }
  }
}
