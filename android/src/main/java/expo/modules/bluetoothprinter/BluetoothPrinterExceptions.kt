package expo.modules.bluetoothprinter

import expo.modules.kotlin.exception.CodedException

internal class NoBluetoothAdapterException :
  CodedException("BluetoothAdapter is null")

internal class NoPermissionsModuleException :
  CodedException("Permissions module is null")

internal class ConversionException(fromClass: Class<*>, toClass: Class<*>, message: String? = "") :
  CodedException("Couldn't cast from ${fromClass::class.simpleName} to ${toClass::class.java.simpleName}: $message")
