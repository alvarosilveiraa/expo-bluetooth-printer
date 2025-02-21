package expo.modules.bluetoothprinter

import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.functions.Coroutine
import androidx.core.app.ActivityCompat
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.Manifest
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
  private val MODULE_NAME = "ExpoBluetoothPrinter"
  private val SOCKET_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (BluetoothDevice.ACTION_FOUND.equals(intent.action))
        sendDevices()
    }
  }

  override fun definition() = ModuleDefinition {
    Name(MODULE_NAME)

    OnCreate {
      mContext = appContext.reactContext ?: throw Exceptions.ReactContextLost()
      mAdapter = BluetoothAdapter.getDefaultAdapter() ?: throw NoBluetoothAdapterException()
    }

    Events("onDevices")

    AsyncFunction("checkPermissions") Coroutine { ->
      Log.d(MODULE_NAME, "checkPermissions")
      val permissionsManager = appContext.permissions ?: throw NoPermissionsModuleException()
      return@Coroutine BluetoothPrinterHelpers.askForPermissions(
        permissionsManager,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION
      )
    }

    AsyncFunction("listenDevices") {
      Log.d(MODULE_NAME, "listenDevices")
      val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
      }
      mContext.registerReceiver(receiver, filter)
      mAdapter.startDiscovery()
      sendDevices()
    }

    AsyncFunction("unlistenDevices") {
      Log.d(MODULE_NAME, "unlistenDevices")
      mContext.unregisterReceiver(receiver)
      mAdapter.cancelDiscovery()
    }

    AsyncFunction("connectDevice") { id: String ->
      Log.d(MODULE_NAME, "connectDevice")
      val device = mAdapter.getRemoteDevice(id)
      val socket = device.createRfcommSocketToServiceRecord(SOCKET_UUID)
      BluetoothPrinterService.connect(socket)
    }

    AsyncFunction("closeDevice") {
      Log.d(MODULE_NAME, "closeDevice")
      BluetoothPrinterService.close()
    }

    AsyncFunction("print") { byteArrayList: List<ByteArray> ->
      Log.d(MODULE_NAME, "print")
      BluetoothPrinterService.print(byteArrayList)
    }

    AsyncFunction("printPdf") { fileUri: String ->
      Log.d(MODULE_NAME, "printPdf")
      BluetoothPrinterService.printPdf(fileUri)
    }

    Function("isEnabled") {
      Log.d(MODULE_NAME, "isEnabled")
      mAdapter.isEnabled()
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
