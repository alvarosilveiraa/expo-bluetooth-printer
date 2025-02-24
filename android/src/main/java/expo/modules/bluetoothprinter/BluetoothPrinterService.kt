package expo.modules.bluetoothprinter

import android.bluetooth.BluetoothSocket
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.IOException
import expo.modules.bluetoothprinter.data.*

class BluetoothPrinterService {
  private var mSocket: BluetoothSocket? = null

  public fun connect(socket: BluetoothSocket) {
    try {
      socket.connect()
      mSocket = socket
    } catch (e: IOException) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while connecting!", e)
      throw e
    }
  }

  public fun close() {
    val socket = mSocket ?: return
    socket.close()
    mSocket = null
  }

  public fun isConnected(): Boolean {
    val socket = mSocket ?: return false
    return socket.isConnected()
  }

  public fun print(values: List<BluetoothPrinterValue>, count: Int?) {
    repeat(count ?: 1) {
      values.forEach { value ->
        if (value.image != null) printImage(value.image)
        else if (value.text != null) printText(value.text)
      }
      printByteArrayList(listOf(BluetoothPrinterCommands.CUT))
    }
  }

  private fun printImage(base64: String) {
    val byteArrayList = mutableListOf<ByteArray>()
    try {
      val decoded = Base64.decode(base64, Base64.DEFAULT)
      val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size) ?: return
      val resized = BluetoothPrinterHelpers.resizeBitmap(bitmap, 576)
      val bitmapByteArray = BluetoothPrinterHelpers.convertBitmapToByteArray(resized)
      byteArrayList.add(bitmapByteArray)
    } catch (e: Exception) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing image!", e)
      throw e
    }
    printByteArrayList(byteArrayList)
  }

  private fun printText(text: BluetoothPrinterText) {
    val byteArrayList = mutableListOf<ByteArray>()
    try {
      val align: String = options?.align ?: "left"
      val fontSize: Int = options?.fontSize ?: 1
      val isBold: Boolean = options?.isBold ?: false
      val isUnderline: Boolean = options?.isUnderline ?: false
      val hasNewLine: Boolean = options?.hasNewLine ?: false
      when (align) {
        "center" -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_CENTER)
        "right" -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_RIGHT)
        else -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_LEFT)
      }
      when (fontSize) {
        2 -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_2)
        3 -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_3)
        else -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_1)
      }
      if (isBold) byteArrayList.add(BluetoothPrinterCommands.BOLD)
      if (isUnderline) byteArrayList.add(BluetoothPrinterCommands.UNDERLINE)
      byteArrayList.add(text.value.toByteArray(Charsets.UTF_8))
      if (hasNewLine) byteArrayList.add(BluetoothPrinterCommands.NEW_LINE)
      byteArrayList.add(BluetoothPrinterCommands.RESET)
    } catch (e: Exception) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing text!", e)
      throw e
    }
    printByteArrayList(byteArrayList)
  }

  private fun printByteArrayList(byteArrayList: List<ByteArray>) {
    val socket = mSocket ?: return
    try {
      if (!socket.isConnected()) return
      byteArrayList.forEach {
        socket.outputStream.write(it)
        socket.outputStream.flush()
        Thread.sleep(50)
      }
    } catch (e: IOException) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing!", e)
      throw e
    }
  }
}
