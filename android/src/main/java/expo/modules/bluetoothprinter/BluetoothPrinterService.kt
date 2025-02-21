package expo.modules.bluetoothprinter

import android.graphics.Bitmap
import android.bluetooth.BluetoothSocket
import java.io.File
import java.io.ByteArrayOutputStream

class BluetoothPrinterService {
  private var mSocket: BluetoothSocket? = null

  public fun connect(socket: BluetoothSocket) {
    socket.connect()
    mSocket = socket
  }

  public fun close() {
    val socket = mSocket ?: return
    socket.close()
    mSocket = null
  }

  public fun print(byteArrayList: List<ByteArray>) {
    val socket = mSocket ?: return
    socket.outputStream.write(BluetoothPrinterCommands.RESET)
    byteArrayList.forEach { socket.outputStream.write(it) }
    socket.outputStream.write(BluetoothPrinterCommands.CUT)
    socket.outputStream.flush()
  }

  public fun printPdf(fileUri: String) {
    val file = File(fileUri)
    val bitmaps = BluetoothPrinterHelpers.convertPdfToBitmaps(file)
    val byteArrayList = mutableListOf<ByteArray>()
    bitmaps.forEach {
      val byteArrayOutputStream = ByteArrayOutputStream()
      it.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
      val byteArray = byteArrayOutputStream.toByteArray()
      byteArrayList.add(byteArray)
      it.recycle()
    }
    print(byteArrayList)
  }
}
