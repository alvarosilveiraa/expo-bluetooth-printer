package expo.modules.bluetoothprinter

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.Bundle
import expo.modules.interfaces.permissions.Permissions
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import java.io.File
import java.io.FileOutputStream

class BluetoothPrinterHelpers {
  companion object {
    internal fun convertPdfToBitmaps(file: File): List<Bitmap> {
      val bitmaps = mutableListOf<Bitmap>()
      val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
      val pdfRenderer = PdfRenderer(fileDescriptor)
      for (i in 0 until pdfRenderer.pageCount) {
        val page = pdfRenderer.openPage(i)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        bitmaps.add(bitmap)
        page.close()
      }
      pdfRenderer.close()
      fileDescriptor.close()
      return bitmaps
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
