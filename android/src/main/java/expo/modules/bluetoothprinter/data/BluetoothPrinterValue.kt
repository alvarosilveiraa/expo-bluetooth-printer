package expo.modules.bluetoothprinter.data

data class BluetoothPrinterValue(
  val image: BluetoothPrinterImage?,
  val text: BluetoothPrinterText?,
  val qrCode: BluetoothPrinterQRCode?,
  val newLines: Int?,
)
