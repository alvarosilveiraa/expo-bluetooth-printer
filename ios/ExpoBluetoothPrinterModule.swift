import ExpoModulesCore
import ExternalAccessory

public class ExpoBluetoothPrinterModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoBluetoothPrinter")

    AsyncFunction("getDevices") {
      EAAccessoryManager.shared().connectedAccessories.map { accessory in
        [
          "id": accessory.serialNumber,
          "name": accessory.name
        ]
      }
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
        
        // Envia comando de reset ESC/POS
        let resetCommand: [UInt8] = [0x1B, 0x40]
        outputStream.write(resetCommand, maxLength: resetCommand.count)
        
        // Envia o texto para impressão
        let textToPrint = "\(text)\n\n\n"
        if let data = textToPrint.data(using: .utf8) {
          _ = data.withUnsafeBytes {
            outputStream.write($0.bindMemory(to: UInt8.self).baseAddress!, maxLength: data.count)
          }
        }
        
        // Envia comando de corte de papel (opcional)
        let cutCommand: [UInt8] = [0x1D, 0x56, 0x41, 0x10]
        outputStream.write(cutCommand, maxLength: cutCommand.count)
        
        outputStream.close()
      } else {
        throw NSError(domain: "ExpoBluetoothPrinter", code: 500, userInfo: [NSLocalizedDescriptionKey: "Failed to open output stream"])
      }
    }
  }
}
