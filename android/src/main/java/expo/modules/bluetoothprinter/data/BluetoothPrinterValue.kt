package expo.modules.bluetoothprinter.data

import kotlinx.serialization.Serializable

@Serializable
data class BluetoothPrinterValue(
  val image: String?,
  val text: BluetoothPrinterText?,
)
