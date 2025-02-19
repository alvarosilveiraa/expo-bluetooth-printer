package expo.modules.bluetoothprinter

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
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

class ExpoBluetoothPrinterModule : Module() {
  private val NAME = "ExpoBluetoothPrinter"
  private val SOCKET_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
  private val adapter = BluetoothAdapter.getDefaultAdapter()
  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.getAction()
      Log.d(NAME, "on receive: $action")
      sendDevices()
    }
  }

  override fun definition() = ModuleDefinition {
    Name(NAME)

    Events("onDevices")

    Function("isEnabled") {
      isEnabled()
    }

    AsyncFunction("listenDevices") {
      checkPermissions()
      val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
      }
      appContext.reactContext.registerReceiver(receiver, filter)
      if (isEnabled()) adapter.startDiscovery()
      sendDevices()
    }

    AsyncFunction("unlistenDevices") {
      appContext.reactContext.unregisterReceiver(receiver)
      if (isEnabled() && adapter.isDiscovering()) adapter.cancelDiscovery()
    }

    AsyncFunction("printText") { deviceID: String, text: String ->
      if (!isEnabled()) return
      val device = adapter.getRemoteDevice(deviceID)
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
  }

  private fun checkPermissions() {
    val permissionChecked = ContextCompat.checkSelfPermission(
      appContext.reactContext,
      android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (permissionChecked == PackageManager.PERMISSION_DENIED) {
      ActivityCompat.requestPermissions(
        appContext.reactContext.getCurrentActivity(),
        listOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
        1
      )
    }
  }

  private fun sendDevices() {
    if (!isEnabled()) return
    val bondedDevices = adapter.getBondedDevices()
    val devices = bondedDevices.map { device ->
      mapOf(
        "id" to device.address,
        "name" to (device.name ?: "Unknown")
      )
    }
    sendEvent("onDevices", mapOf("devices" to devices))
  }

  private fun isEnabled() = adapter != null && adapter.isEnabled()
}
