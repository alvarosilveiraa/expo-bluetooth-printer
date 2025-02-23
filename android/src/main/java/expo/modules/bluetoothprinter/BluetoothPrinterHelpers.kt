package expo.modules.bluetoothprinter

import android.os.Bundle
import android.graphics.Bitmap
import android.util.Log
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
      val bytesPerRow = (width + 7) / 8
      val totalRows = (height + 23) / 24
      val dataSize = 5 + (totalRows * (bytesPerRow * 24 + 1))

      val data = ByteArray(dataSize)
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
              if (index < data.size) {
                data[index++] = byte.toByte()
              } else {
                Log.e(BluetoothPrinterConstants.MODULE_NAME, "Index out of bounds: $index / ${data.size}")
                return data.copyOf(index)
              }
              byte = 0
            }
          }
        }
        if (index < data.size) {
          data[index++] = 0x0A
        } else {
          Log.e(BluetoothPrinterConstants.MODULE_NAME, "Index out of bounds at line break: $index / ${data.size}")
          return data.copyOf(index)
        }
      }

      baos.write(data, 0, index)
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
