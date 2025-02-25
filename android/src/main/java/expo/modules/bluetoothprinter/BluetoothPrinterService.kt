package expo.modules.bluetoothprinter

import android.os.Bundle
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import expo.modules.bluetoothprinter.data.BluetoothPrinterImage
import expo.modules.bluetoothprinter.data.BluetoothPrinterText
import expo.modules.bluetoothprinter.data.BluetoothPrinterQRCode
import expo.modules.bluetoothprinter.data.BluetoothPrinterValue
import expo.modules.bluetoothprinter.helpers.BluetoothPrinterImageHelper
import expo.modules.bluetoothprinter.helpers.BluetoothPrinterQRCodeHelper

class BluetoothPrinterService {
  private var mSocket: BluetoothSocket? = null

  public suspend fun connect(socket: BluetoothSocket): Bundle {
    return suspendCancellableCoroutine {
      try {
        close()
        socket.connect()
        mSocket = socket
        it.resume(Bundle())
      } catch (e: IOException) {
        Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while connecting!", e)
        it.resumeWithException(e)
      }
    }
  }

  public fun print(values: List<BluetoothPrinterValue>, count: Int?) {
    if (!isConnected()) return
    repeat(count ?: 1) {
      values.forEach { value ->
        if (value.image != null) printImage(value.image)
        else if (value.text != null) printText(value.text)
        else if (value.qrCode != null) printQRCode(value.qrCode)
        else if (value.newLines != null) printNewLines(value.newLines)
      }
      printByteArrayList(listOf(BluetoothPrinterCommands.CUT))
    }
  }

  public fun isConnected(): Boolean {
    val socket = mSocket ?: return false
    return socket.isConnected()
  }

  private fun printImage(image: BluetoothPrinterImage) {
    val byteArrayList = mutableListOf<ByteArray>()
    try {
      val width = image.options?.width ?: BluetoothPrinterConstants.WIDTH_80
      val left = image.options?.left ?: 0
      val newLines = image.options?.newLines ?: 1
      val imageByteArray = BluetoothPrinterImageHelper.generateBase64ByteArray(
        image.value,
        width,
        left
      ) ?: return
      byteArrayList.add(imageByteArray)
      if (newLines > 0) repeat(newLines) { byteArrayList.add(BluetoothPrinterCommands.NEW_LINE) }
    } catch (e: Exception) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing image!", e)
      throw e
    }
    printByteArrayList(byteArrayList)
  }

  private fun printText(text: BluetoothPrinterText) {
    val byteArrayList = mutableListOf<ByteArray>()
    try {
      val align = text.options?.align ?: "left"
      val font = text.options?.font ?: "A"
      val fontSize = text.options?.fontSize ?: 1
      val newLines = text.options?.newLines ?: 1
      val isBold = text.options?.isBold ?: false
      val isUnderline = text.options?.isUnderline ?: false
      when (align) {
        "center" -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_CENTER)
        "right" -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_RIGHT)
        else -> byteArrayList.add(BluetoothPrinterCommands.ALIGN_LEFT)
      }
      when (font) {
        "A" -> byteArrayList.add(BluetoothPrinterCommands.FONT_A)
        "B" -> byteArrayList.add(BluetoothPrinterCommands.FONT_B)
      }
      when (fontSize) {
        2 -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_2)
        3 -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_3)
        else -> byteArrayList.add(BluetoothPrinterCommands.FONT_SIZE_1)
      }
      if (isBold) byteArrayList.add(BluetoothPrinterCommands.BOLD)
      if (isUnderline) byteArrayList.add(BluetoothPrinterCommands.UNDERLINE)
      byteArrayList.add(text.value.toByteArray(Charsets.UTF_8))
      if (newLines > 0) {
        repeat(newLines) { byteArrayList.add(BluetoothPrinterCommands.NEW_LINE) }
        byteArrayList.add(BluetoothPrinterCommands.RESET)
      }
    } catch (e: Exception) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing text!", e)
      throw e
    }
    printByteArrayList(byteArrayList)
  }

  private fun printQRCode(qrCode: BluetoothPrinterQRCode) {
    val byteArrayList = mutableListOf<ByteArray>()
    try {
      val size = qrCode.options?.size ?: 200
      val newLines = qrCode.options?.newLines ?: 1
      val qrCodeByteArray = BluetoothPrinterQRCodeHelper.generateQRCodeByteArray(
        qrCode.value,
        size
      ) ?: return
      byteArrayList.add(qrCodeByteArray)
      if (newLines > 0) repeat(newLines) { byteArrayList.add(BluetoothPrinterCommands.NEW_LINE) }
    } catch (e: Exception) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing qr code!", e)
      throw e
    }
    printByteArrayList(byteArrayList)
  }

  private fun printNewLines(newLines: Int) {
    val byteArrayList = mutableListOf<ByteArray>()
    repeat(newLines) { byteArrayList.add(BluetoothPrinterCommands.NEW_LINE) }
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

  private fun close() {
    val socket = mSocket ?: return
    socket.close()
    mSocket = null
  }
}
