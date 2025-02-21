package expo.modules.bluetoothprinter

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException

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

  public fun print(byteArrayList: List<ByteArray>, count: Int?) {
    val socket = mSocket ?: return
    val iterations = count ?: 1
    if (iterations <= 0) return
    try {
      repeat(iterations) {
        socket.outputStream.write(BluetoothPrinterCommands.RESET)
        byteArrayList.forEach { socket.outputStream.write(it) }
        socket.outputStream.write(BluetoothPrinterCommands.CUT)
        socket.outputStream.flush()
      }
    } catch (e: IOException) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing!", e)
      throw e
    }
  }

  public fun printPdf(uri: String, count: Int?) {
    val file = BluetoothPrinterHelpers.convertUriToFile(uri)
    val byteArrayList = BluetoothPrinterHelpers.convertPdfToByteArrayList(file)
    print(byteArrayList, count)
  }
}
