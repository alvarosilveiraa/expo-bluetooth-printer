package expo.modules.bluetoothprinter

import android.os.Bundle
import android.graphics.Bitmap
import expo.modules.interfaces.permissions.Permissions
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import java.io.ByteArrayOutputStream

class BluetoothPrinterHelpers {
  companion object {
    internal fun resizeBitmap(bitmap: Bitmap, width: Int): Bitmap {
      val aspectRatio = bitmap.height.toDouble() / bitmap.width.toDouble()
      val height = (width * aspectRatio).toInt()
      return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    internal fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
      val baos = ByteArrayOutputStream()
      val width = bitmap.width
      val height = bitmap.height
      val data = ByteArray((width * height / 8) + 8)
      data[0] = 0x1B
      data[1] = 0x2A
      data[2] = 33
      data[3] = (width % 256).toByte()
      data[4] = (width / 256).toByte()
      var index = 5
      for (y in 0 until height step 24) {
        for (x in 0 until width) {
          var byte = 0
          for (bit in 0 until 24) {
            if (y + bit < height) {
              val pixel = bitmap.getPixel(x, y + bit)
              val gray = (pixel shr 16 and 0xFF) * 0.299 + (pixel shr 8 and 0xFF) * 0.587 + (pixel and 0xFF) * 0.114
              if (gray < 128) byte = byte or (1 shl (7 - bit % 8))
            }
            if (bit % 8 == 7) {
              data[index++] = byte.toByte()
              byte = 0
            }
          }
        }
        data[index++] = 0x0A
      }
      return baos.toByteArray()
    }

    internal suspend fun askForPermissions(manager: Permissions, vararg args: String): Bundle {
      return suspendCoroutine {
        Permissions.askForPermissionsWithPermissionsManager(
          manager,
          object : Promise {
            override fun resolve(value: Any?) {
              it.resume(
                value as? Bundle
                  ?: throw ConversionException(Any::class.java, Bundle::class.java, "value returned by the permission promise is not a Bundle")
              )
            }

            override fun reject(code: String, message: String?, cause: Throwable?) {
              it.resumeWithException(CodedException(code, message, cause))
            }
          },
          *args
        )
      }
    }
  }
}
