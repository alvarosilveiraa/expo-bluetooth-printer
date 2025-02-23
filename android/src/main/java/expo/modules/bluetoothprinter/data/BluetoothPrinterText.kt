package expo.modules.bluetoothprinter.data

import kotlinx.serialization.Serializable

@Serializable
data class BluetoothPrinterText(
  val value: String,
  val options: BluetoothPrinterTextOptions?,
)
