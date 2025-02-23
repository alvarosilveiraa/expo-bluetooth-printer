package expo.modules.bluetoothprinter.data

import kotlinx.serialization.Serializable

@Serializable
data class BluetoothPrinterTextOptions(
  val align: String? = "left",
  val fontSize: Int? = 1,
  val isBold: Boolean? = false,
  val isUnderline: Boolean? = false,
)
