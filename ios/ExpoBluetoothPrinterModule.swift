import ExpoModulesCore

public class BluetoothPrinterModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    Events("onDevices")

    AsyncFunction("listenDevices")

    AsyncFunction("unlistenDevices")

    AsyncFunction("printText")

    Function("isEnabled")
  }
}
