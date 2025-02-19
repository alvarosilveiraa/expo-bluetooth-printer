import ExpoModulesCore

public class ExpoBluetoothPrinterModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    Events("onDevices")

    AsyncFunction("listenDevices")

    AsyncFunction("unlistenDevices")

    AsyncFunction("printText")
  }
}
