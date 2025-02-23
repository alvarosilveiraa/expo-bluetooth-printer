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

  public fun print(values: List<BluetoothPrinterValue>, count: Int?) {
    repeat(count ?: 1) {
      values.forEach { value ->
        if (value.image != null) printImage(value.image)
        else if (value.text != null) printText(value.text.value, value.text.options)
      }
      printByteArrayList(listOf(BluetoothPrinterCommands.CUT))
    }
  }

  private fun printImage(base64: String) {
    val decoded = Base64.decode(base64, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size) ?: return
    val resized = BluetoothPrinterHelpers.resizeBitmap(bitmap, 576)
    val byteArrayList = mutableListOf<ByteArray>()
    byteArrayList.add(BluetoothPrinterHelpers.convertBitmapToByteArray(resized))
    byteArrayList.add(BluetoothPrinterCommands.NEW_LINE)
    printByteArrayList(byteArrayList)
  }

  private fun printText(text: String, options: BluetoothPrinterTextOptions?) {
    val byteArrayList = mutableListOf<ByteArray>()
    val validOptions = BluetoothPrinterTextOptions(
      align = options?.align,
      fontSize = options?.fontSize,
      isBold = options?.isBold,
      isUnderline = options?.isUnderline,
    )
    when (validOptions.align) {
      "center" -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_CENTER)
      "right" -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_RIGHT)
      else -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_LEFT)
    }
    when (validOptions.fontSize) {
      2 -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_2)
      3 -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_3)
      else -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_1)
    }
    if (validOptions.isBold) byteArrayList.add(BluetoothPrinterCommands.BOLD)
    if (validOptions.isUnderline) byteArrayList.add(BluetoothPrinterCommands.UNDERLINE)
    byteArrayList.add(text.toByteArray(Charsets.UTF_8))
    byteArrayList.add(BluetoothPrinterCommands.NEW_LINE)
    byteArrayList.add(BluetoothPrinterCommands.RESET)
    printByteArrayList(byteArrayList)
  }

  private fun printByteArrayList(byteArrayList: List<ByteArray>) {
    val socket = mSocket ?: return
    try {
      byteArrayList.forEach { socket.outputStream.write(it) }
      socket.outputStream.flush()
    } catch (e: IOException) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing!", e)
      throw e
    }
  }
}
