import ExpoModulesCore
import ExternalAccessory

public class ExpoBluetoothPrinterModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    Events("onDevices")

    AsyncFunction("loadDevices") {
      let devices = EAAccessoryManager.shared().connectedAccessories.map { accessory in
        [
          "id": accessory.serialNumber,
          "name": accessory.name
        ]
      }
      self.sendEvent("onDevices", , [
        "devices": devices
      ])
    }

    AsyncFunction("printText") { deviceId: String, text: String in
      guard let accessory = EAAccessoryManager.shared().connectedAccessories.first(where: { $0.serialNumber == deviceId }),
          let protocolString = accessory.protocolStrings.first else {
        throw NSError(domain: "ExpoBluetoothPrinter", code: 404, userInfo: [NSLocalizedDescriptionKey: "Device not found"])
      }
      let session = EASession(accessory: accessory, forProtocol: protocolString)
      if let outputStream = self.session?.outputStream {
        outputStream.schedule(in: .current, forMode: .default)
        outputStream.open()
        outputStream.write([0x1B, 0x40], maxLength: 2)
        if let data = "\(text)\n\n\n".data(using: .utf8) {
          _ = data.withUnsafeBytes {
            outputStream.write($0.bindMemory(to: UInt8.self).baseAddress!, maxLength: data.count)
          }
        }
        outputStream.write([0x1D, 0x56, 0x41, 0x10], maxLength: 4)
        outputStream.close()
      } else {
        throw NSError(domain: "ExpoBluetoothPrinter", code: 500, userInfo: [NSLocalizedDescriptionKey: "Failed to open output stream"])
      }
    }
  }
}
