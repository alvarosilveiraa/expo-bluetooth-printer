package expo.modules.bluetoothprinter

import android.bluetooth.BluetoothSocket
import java.io.File

class BluetoothPrinterService {
  private var mSocket: BluetoothSocket? = null

  public fun connect(socket: BluetoothSocket) {
    mSocket = socket
    mSocket.connect()
  }

  public fun close() {
    if (mSocket == null) return
    mSocket.close()
    mSocket = null
  }

  public fun print(byteArrayList: List<ByteArray>) {
    if (mSocket == null) return
    mSocket.outputStream.write(BluetoothPrinterCommands.RESET)
    byteArrayList.forEach { mSocket.outputStream.write(it) }
    mSocket.outputStream.write(BluetoothPrinterCommands.CUT)
    mSocket.outputStream.flush()
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
