import ExpoModulesCore

public class ExpoBluetoothPrinterModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    Constants([
      "PI": Double.pi
    ])

    Function("hello") {
      return "Hello world! 👋"
    }

    AsyncFunction("setValueAsync")
  }
}
