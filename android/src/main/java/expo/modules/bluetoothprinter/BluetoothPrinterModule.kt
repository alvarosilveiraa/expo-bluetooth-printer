package expo.modules.bluetoothprinter

import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.functions.Coroutine
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.Manifest
import kotlinx.coroutines.delay
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.bluetoothprinter.data.BluetoothPrinterValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BluetoothPrinterModule : Module() {
  private lateinit var mContext: Context
  private lateinit var mAdapter: BluetoothAdapter
  private val service = BluetoothPrinterService()
  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent.action)
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
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
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

    AsyncFunction("connect") Coroutine { id: String ->
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "connect")
      mAdapter.cancelDiscovery()
      val device = mAdapter.getRemoteDevice(id)
      val socket = device.createRfcommSocketToServiceRecord(BluetoothPrinterConstants.SOCKET_UUID)
      delay(1000)
      return@Coroutine service.connect(socket)
    }

    AsyncFunction("print") { valuesString: String, count: Int? ->
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "print")
      val listType = object : TypeToken<List<BluetoothPrinterValue>>() {}.type
      val values: List<BluetoothPrinterValue> = Gson().fromJson(valuesString, listType)
      service.print(values, count)
    }

    Function("isEnabled") {
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "isEnabled")
      mAdapter.isEnabled()
    }

    Function("isConnected") {
      Log.d(BluetoothPrinterConstants.MODULE_NAME, "isConnected")
      service.isConnected()
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
