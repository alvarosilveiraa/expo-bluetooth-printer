package expo.modules.bluetoothprinter

import expo.modules.kotlin.exception.CodedException

internal class NoBluetoothAdapterException :
  CodedException("BluetoothAdapter is null")