package expo.modules.bluetoothprinter

class BluetoothPrinterCommands {
  public val RESET = byteArrayOf(0x1B, 0x40)
  public val CUT = byteArrayOf(0x1D, 0x56, 0x41, 0x10)
}
