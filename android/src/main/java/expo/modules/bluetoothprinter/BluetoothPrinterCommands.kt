package expo.modules.bluetoothprinter

class BluetoothPrinterCommands {
  companion object {
    internal val RESET = byteArrayOf(0x1B, 0x40)
    internal val NEW_LINE = byteArrayOf(0x0A)
    internal val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0)
    internal val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 1)
    internal val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 2)
    internal val FONT_A = byteArrayOf(0x1B, 0x4D, 0x00)
    internal val FONT_B = byteArrayOf(0x1B, 0x4D, 0x01)
    internal val FONT_SIZE_1 = byteArrayOf(0x1D, 0x21, 0x00)
    internal val FONT_SIZE_2 = byteArrayOf(0x1D, 0x21, 0x11)
    internal val FONT_SIZE_3 = byteArrayOf(0x1D, 0x21, 0x22)
    internal val BOLD = byteArrayOf(0x1B, 0x45, 1)
    internal val UNDERLINE = byteArrayOf(0x1B, 0x2D, 1)
    internal val CUT = byteArrayOf(0x1D, 0x56, 0x41, 0x10)

    internal fun POS_Set_PrtAndFeedPaper(feed: Int): ByteArray? {
      if (feed > 255 || feed < 0) return null
      val data = byteArrayOf(0x1B, "J".toByte(), 0x00).copyOf()
      data[2] = feed.toByte()
      return data
    }

    internal fun POS_Set_Cut(cut: Int): ByteArray? {
      if (cut > 255 || cut < 0) return null
      val data = byteArrayOf(0x1D, "V".toByte(), "B".toByte(), 0x00).copyOf()
      data[3] = cut.toByte()
      return data
    }
  }
}
