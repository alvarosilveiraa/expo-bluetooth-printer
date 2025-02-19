package expo.modules.bluetoothprinter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
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

    AsyncFunction("getDevices") {
      val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
      if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        return@AsyncFunction emptyList<Map<String, String>>()
      }
      val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
      pairedDevices.map { device ->
        mapOf("id" to device.address, "name" to (device.name ?: "Unknown Device"))
      }
    }

    AsyncFunction("printText") { deviceId: String, text: String ->
      withContext(Dispatchers.IO) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = bluetoothAdapter.getRemoteDevice(deviceId)
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID para Bluetooth SPP
        var socket: BluetoothSocket? = null
        try {
          socket = device.createRfcommSocketToServiceRecord(uuid)
          socket.connect()
          val outputStream: OutputStream = socket.outputStream

          // Início do comando ESC/POS
          val resetCommand = byteArrayOf(0x1B, 0x40) // Reseta impressora
          outputStream.write(resetCommand)

          val textToPrint = "$text\n\n\n"
          outputStream.write(textToPrint.toByteArray(charset("UTF-8")))

          // Comando para corte de papel (opcional, dependendo da impressora)
          val cutCommand = byteArrayOf(0x1D, 0x56, 0x41, 0x10)
          outputStream.write(cutCommand)

          outputStream.flush()
        } catch (e: IOException) {
          e.printStackTrace()
        } finally {
          socket?.close()
        }
      }
    }
  }
}
