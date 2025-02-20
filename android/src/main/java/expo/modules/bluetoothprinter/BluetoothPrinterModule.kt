package expo.modules.bluetoothprinter

import expo.modules.kotlin.exception.Exceptions
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.io.IOException
import java.util.*

class BluetoothPrinterModule : Module() {
  private lateinit var mContext: Context
  private lateinit var mAdapter: BluetoothAdapter
  private val SOCKET_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (BluetoothDevice.ACTION_FOUND.equals(intent.action))
        sendDevices()
    }
  }

  override fun definition() = ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    OnCreate {
      mContext = appContext.reactContext ?: throw Exceptions.ReactContextLost()
      mAdapter = BluetoothAdapter.getDefaultAdapter() ?: throw NoBluetoothAdapterException()
    }

    Events("onDevices")

    AsyncFunction("listenDevices") {
      val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
      }
      mContext.registerReceiver(receiver, filter)
      mAdapter.startDiscovery()
      sendDevices()
    }

    AsyncFunction("unlistenDevices") {
      mContext.unregisterReceiver(receiver)
      mAdapter.cancelDiscovery()
    }

    AsyncFunction("printText") { deviceID: String, text: String ->
      printText(deviceID, text)
    }

    Function("isEnabled") {
      mAdapter.isEnabled()
    }
  }

  private fun printText(deviceID: String, text: String) {
    val device = mAdapter.getRemoteDevice(deviceID)
    val socket = device.createRfcommSocketToServiceRecord(SOCKET_UUID)
    try {
      socket.connect()
      val outputStream: OutputStream = socket.outputStream
      outputStream.write(byteArrayOf(0x1B, 0x40))
      outputStream.write("$text\n\n\n".toByteArray(Charsets.UTF_8))
      outputStream.write(byteArrayOf(0x1D, 0x56, 0x41, 0x10))
      outputStream.flush()
    } catch (e: IOException) {
      e.printStackTrace()
      throw IOException(e.message)
    } finally {
      socket.close()
    }
  }

  private fun sendDevices() {
    val devices = mAdapter.bondedDevices.map { device ->
      mapOf(
        "id" to device.address,
        "name" to (device.name ?: "Unknown")
      )
    }
    sendEvent("onDevices", mapOf("devices" to devices))
  }
}
