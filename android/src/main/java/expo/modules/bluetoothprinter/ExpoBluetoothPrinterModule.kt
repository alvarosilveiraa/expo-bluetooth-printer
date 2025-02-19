package expo.modules.bluetoothprinter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.io.IOException
import java.util.*

class ExpoBluetoothPrinterModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    Events("onDevices")

    AsyncFunction("loadDevices") {
      val adapter = BluetoothAdapter.getDefaultAdapter()
      val bondedDevices = adapter.getBondedDevices()
      val devices = bondedDevices.map { device ->
        mapOf(
          "id" to device.address,
          "name" to (device.name ?: "Unknown")
        )
      }
      sendEvent("onDevices", mapOf(
        "devices" to devices
      ))
    }

    AsyncFunction("printText") { deviceID: String, text: String ->
      val adapter = BluetoothAdapter.getDefaultAdapter()
      val device = adapter.getRemoteDevice(deviceID)
      val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
      val socket = device.createRfcommSocketToServiceRecord(uuid)
      try {
        socket.connect()
        val outputStream: OutputStream = socket.outputStream
        outputStream.write(byteArrayOf(0x1B, 0x40))
        outputStream.write("$text\n\n\n".toByteArray(Charsets.UTF_8))
        outputStream.write(byteArrayOf(0x1D, 0x56, 0x41, 0x10))
        outputStream.flush()
      } catch (e: IOException) {
        e.printStackTrace()
        throw IOException("Erro ao tentar imprimir via Bluetooth: ${e.message}")
      } finally {
        socket?.close()
      }
    }
  }
}
