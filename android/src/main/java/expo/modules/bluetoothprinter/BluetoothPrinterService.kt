package expo.modules.bluetoothprinter

import android.os.Bundle
import android.bluetooth.BluetoothSocket
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import expo.modules.bluetoothprinter.data.*

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

  public fun isConnected(): Boolean {
    val socket = mSocket ?: return false
    return socket.isConnected()
  }

  public fun print(values: List<BluetoothPrinterValue>, count: Int?) {
    repeat(count ?: 1) {
      values.forEach { value ->
        if (value.image != null) printImage(value.image)
        else if (value.text != null) printText(value.text)
        else if (value.newLines != null) printNewLines(value.newLines)
      }
      printByteArrayList(listOf(BluetoothPrinterCommands.CUT))
    }
  }

  private fun printImage(image: BluetoothPrinterImage) {
    val byteArrayList = mutableListOf<ByteArray>()
    try {
      val decoded = Base64.decode(image.value, Base64.DEFAULT)
      val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size) ?: return
      
      // public static final int WIDTH_58 = 384;
      // public static final int WIDTH_80 = 576;
      val width: Int = image.options?.width ?: 576
      val left: Int = image.options?.left ?: 0
      byteArrayList.add(byteArrayOf(0x1B, "@".toByte()))
      byteArrayList.add(byteArrayOf(0x0A))
      byteArrayList.add(PrintPicture.POS_PrintBMP(bitmap, width, 0, left))
      byteArrayList.add(BluetoothPrinterCommands.POS_Set_PrtAndFeedPaper(30))
      byteArrayList.add(BluetoothPrinterCommands.POS_Set_Cut(1))
      byteArrayList.add(byteArrayOf(0x1B, "@".toByte()))
    } catch (e: Exception) {
      Log.e(BluetoothPrinterConstants.MODULE_NAME, "An error occurred while printing image!", e)
      throw e
    }
    printByteArrayList(byteArrayList)
  }

  private fun printText(text: BluetoothPrinterText) {
    val byteArrayList = mutableListOf<ByteArray>()
    try {
      val align: String = text.options?.align ?: "left"
      val font: String = text.options?.font ?: "A"
      val fontSize: Int = text.options?.fontSize ?: 1
      val newLines: Int = text.options?.newLines ?: 1
      val isBold: Boolean = text.options?.isBold ?: false
      val isUnderline: Boolean = text.options?.isUnderline ?: false
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

  private fun printNewLines(newLines: Int) {
    val byteArrayList = mutableListOf<ByteArray>()
    repeat(newLines) { byteArrayList.add(BluetoothPrinterCommands.NEW_LINE) }
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

  private fun close() {
    val socket = mSocket ?: return
    socket.close()
    mSocket = null
  }
}
