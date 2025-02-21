package expo.modules.bluetoothprinter

class BluetoothPrinterCommands {
  companion object {
    internal val RESET = byteArrayOf(0x1B, 0x40)
    internal val CUT = byteArrayOf(0x1D, 0x56, 0x41, 0x10)
  }
}
