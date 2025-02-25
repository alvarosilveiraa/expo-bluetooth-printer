import ExpoModulesCore

public class BluetoothPrinterModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    Events("onDevices")

    AsyncFunction("checkPermissions")

    AsyncFunction("listenDevices")

    AsyncFunction("unlistenDevices")

    AsyncFunction("connect")

    AsyncFunction("print")

    Function("isEnabled")

    Function("isConnected")
  }
}
