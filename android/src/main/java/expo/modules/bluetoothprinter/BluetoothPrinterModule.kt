package expo.modules.bluetoothprinter

import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.functions.Coroutine
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.Manifest
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.bluetoothprinter.data.BluetoothPrinterValue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class BluetoothPrinterModule : Module() {
  private lateinit var mContext: Context
  private lateinit var mAdapter: BluetoothAdapter
  private val service = BluetoothPrinterService()
  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (BluetoothDevice.ACTION_FOUND.equals(intent.action))
        sendDevices()
    }
  }

  override fun definition() = ModuleDefinition {
    Name(BluetoothPrinterConstants.MODULE_NAME)

    OnCreate {
      mContext = appContext.reactContext ?: throw Exceptions.ReactContextLost()
      mAdapter = BluetoothAdapter.getDefaultAdapter() ?: throw NoBluetoothAdapterException()
    }

    Events("onDevices")

    AsyncFunction("checkPermissions") Coroutine { ->
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "checkPermissions")
      val permissionsManager = appContext.permissions ?: throw NoPermissionsModuleException()
      return@Coroutine BluetoothPrinterHelpers.askForPermissions(
        permissionsManager,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION
      )
    }

    AsyncFunction("listenDevices") {
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "listenDevices")
      val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
      }
      mContext.registerReceiver(receiver, filter)
      mAdapter.startDiscovery()
      sendDevices()
    }

    AsyncFunction("unlistenDevices") {
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "unlistenDevices")
      mContext.unregisterReceiver(receiver)
      mAdapter.cancelDiscovery()
    }

    AsyncFunction("connectDevice") { id: String ->
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "connectDevice")
      val device = mAdapter.getRemoteDevice(id)
      val socket = device.createRfcommSocketToServiceRecord(BluetoothPrinterConstants.SOCKET_UUID)
      service.connect(socket)
    }

    AsyncFunction("closeDevice") {
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "closeDevice")
      service.close()
    }

    AsyncFunction("print") { valuesString: String, count: Int? ->
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "print")
      val values: List<BluetoothPrinterValue> = Json.decodeFromString(valuesString)
      service.print(values, count)
    }

    Function("isEnabled") {
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "isEnabled")
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
