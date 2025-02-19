package expo.modules.bluetoothprinter

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.net.URL

class ExpoBluetoothPrinterModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    Constants(
      "PI" to Math.PI
    )

    Function("hello") {
      "Hello world! 👋"
    }

    AsyncFunction("setValueAsync")
  }
}
